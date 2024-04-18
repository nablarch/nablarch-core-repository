package nablarch.core.repository.di;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.IgnoreProperty;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.di.config.externalize.CompositeExternalizedLoader;
import nablarch.core.repository.di.config.externalize.ExternalizedComponentDefinitionLoader;
import nablarch.core.repository.di.config.externalize.SystemPropertyExternalizedLoader;
import nablarch.core.repository.initialization.ApplicationInitializer;
import nablarch.core.util.Builder;
import nablarch.core.util.ObjectUtil;
import nablarch.core.util.StringUtil;
import nablarch.core.util.annotation.Published;


/**
 * DIコンテナの機能を実現するクラス。
 *<p>
 * staticプロパティへのインジェクションは行われない。
 * インジェクションの対象となるプロパティがstaticである場合、例外が発生する。
 *<p>
 * 後方互換性を維持するするため、システムプロパティ{@literal "nablarch.diContainer.allowStaticInjection"}に
 * {@code true}を設定することで、staticプロパティへのインジェクションを許可できる。
 * 後方互換性維持以外の目的での使用は推奨しない。
 *
 * @author Koichi Asano
 *
 */
@Published(tag = "architect")
public class DiContainer implements ObjectLoader {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager.get(DiContainer.class);

    /** staticプロパティへのインジェクションを許容する場合のシステムプロパティ名 */
    static final String ALLOW_STATIC_INJECTION_SYSTEM_PROP_NAME = "nablarch.diContainer.allowStaticInjection";

    /**
     * idをキーにコンポーネントホルダを取得するMap。
     */
    private Map<Integer, ComponentHolder> holders;
    /**
     * 名前をキーにコンポーネントホルダを取得するMap。
     */
    private Map<String, ComponentHolder> nameIndex;
    /**
     * 型をキーにコンポーネントホルダを取得するMap。
     */
    private Map<Class<?>, ComponentHolder> typeIndex;

    /**
     * 複数登録された型のSet。
     */
    private Set<Class<?>> multiRegisteredType;

    /**
     * 循環参照の情報を保持するための参照スタック。
     */
    private final ReferenceStack refStack = new ReferenceStack();

    /** staticプロパティへのインジェクションを許容するかどうか。 */
    private final boolean allowStaticInjection;

    /**
     * 外部化されたコンポーネント定義を読み込むローダー。
     */
    private final ExternalizedComponentDefinitionLoader externalizedComponentDefinitionLoader;

    /**
     * コンストラクタ。
     * @param loader コンポーネント定義のローダ
     */
    public DiContainer(ComponentDefinitionLoader loader) {
        this(loader, Boolean.getBoolean(ALLOW_STATIC_INJECTION_SYSTEM_PROP_NAME));
    }

    /**
     * コンストラクタ。
     * @param loader コンポーネント定義のローダ
     * @param allowStaticInjection staticプロパティへのインジェクションを許容するかどうか
     */
    public DiContainer(ComponentDefinitionLoader loader, boolean allowStaticInjection) {
        super();
        this.loader = loader;
        this.allowStaticInjection = allowStaticInjection;
        this.externalizedComponentDefinitionLoader = loadExternalizedComponentDefinitionLoader();
        reload();
    }

    /**
     * {@link ExternalizedComponentDefinitionLoader}を{@link ServiceLoader}を使って読み込む。
     * <p/>
     * {@link ExternalizedComponentDefinitionLoader}のサービスプロバイダが設定されていない場合は、
     * 後方互換を維持するために{@link SystemPropertyExternalizedLoader}が使用されます。
     *
     * @return ロードされた {@link ExternalizedComponentDefinitionLoader}
     */
    protected ExternalizedComponentDefinitionLoader loadExternalizedComponentDefinitionLoader() {
        ServiceLoader<ExternalizedComponentDefinitionLoader> serviceLoader
                = ServiceLoader.load(ExternalizedComponentDefinitionLoader.class);

        List<ExternalizedComponentDefinitionLoader> loaders = new ArrayList<ExternalizedComponentDefinitionLoader>();
        for (ExternalizedComponentDefinitionLoader loader : serviceLoader) {
            loaders.add(loader);
        }

        if (loaders.isEmpty()) {
            return new SystemPropertyExternalizedLoader();
        } else {
            return new CompositeExternalizedLoader(loaders);
        }
    }

    /**
     * コンポーネント定義のローダ
     */
    private final ComponentDefinitionLoader loader;

    /**
     * コンポーネントIDの最大値。
     */
    private int maxId = 0;

    /**
     * DIしたオブジェクトを取得するロードメソッド。
     *
     * @return 名前をキーにしてロードしたオブジェクトを保持するMap。
     * @see nablarch.core.repository.ObjectLoader#load()
     */
    public Map<String, Object> load() {
        Map<String, Object> loadedValues = new HashMap<String, Object>();
        for (Map.Entry<String, ComponentHolder> entry : nameIndex.entrySet()) {
            loadedValues.put(entry.getKey(), entry.getValue().getInitializedComponent());
        }
        return Collections.unmodifiableMap(loadedValues);
    }

    /**
     * コンポーネントIDの最大値を取得する。
     * @return コンポーネントIDの最大値
     */
    public int generateId() {
        return maxId++;
    }

    /**
     * コンテナの保持するオブジェクトの再生成を行う。<br/>
     * オブジェクトの再生成は下記順序で行う。
     * <ol>
     * <li>設定の読み込み</li>
     * <li>コンポーネント定義の登録</li>
     * <li>コンポーネント定義にあるObjectLoaderの生成とObjectLoader内のコンポーネントのロード</li>
     * <li>システムプロパティによるコンポーネント定義の上書き</li>
     * <li>コンポーネントの生成</li>
     * <li>コンポーネントに対するインジェクションの実行</li>
     * <li>初期化対象クラスの初期化実行</li>
     * </ol>
     */
    public void reload() {
        maxId = 0;
        List<ComponentDefinition> defs = loader.load(this);
        if (LOGGER.isTraceEnabled()) {
            dump(defs);
        }

        holders = new TreeMap<Integer, ComponentHolder>();
        nameIndex = new HashMap<String, ComponentHolder>();
        typeIndex = new HashMap<Class<?>, ComponentHolder>();
        multiRegisteredType = new HashSet<Class<?>>();
        for (ComponentDefinition def : defs) {
            register(def);
        }

        // holders内のオブジェクトにObjectLoaderがあった際の処理に使用するループ用List
        List<Map.Entry<Integer, ComponentHolder>> prevEntries = new ArrayList<Map.Entry<Integer, ComponentHolder>>(
                holders.entrySet());

        // ObjectLoaderを優先的にロード
        for (Map.Entry<Integer, ComponentHolder> entry : prevEntries) {
            ComponentHolder holder = entry.getValue();
            ComponentDefinition def = holder.getDefinition();

            if (ObjectLoader.class.isAssignableFrom(def.getType())) {
                createComponent(holder);
                completeInject(holder);
                // コンポーネントにObjectLoaderが入っていたら、
                // ObjectLoaderからロードされるものを全てコンポーネントとして扱う
                Object component = holder.getComponent();
                if (component instanceof ObjectLoader) {
                    registerAll((ObjectLoader) component);
                } else {
                    // def.getType() が ObjectLoader だったらここには到達しない。
                    throw new ContainerProcessException("ObjectLoader instantiation failed.");
                }
            }
        }

        // 外部化されたコンポーネント定義で上書き
        List<ComponentDefinition> externalized = externalizedComponentDefinitionLoader.load(this, nameIndex);
        for (ComponentDefinition definition : externalized) {
            register(definition);
        }

        // コンポーネント生成ループ
        for (Map.Entry<Integer, ComponentHolder> entry : holders.entrySet()) {
            ComponentHolder holder = entry.getValue();
            if (holder.getState() == ComponentState.NOT_INSTANTIATE) {
                createComponent(holder);
            }
        }

        // インジェクション解決ループ
        for (Map.Entry<Integer, ComponentHolder> entry : holders.entrySet()) {
            ComponentHolder holder = entry.getValue();
            if (holder.getState() == ComponentState.INSTANTIATED) {
                completeInject(holder);
            }
        }

        // 初期化対象クラスを初期化する。
        ApplicationInitializer initializer = this.getComponentByName("initializer");
        if (initializer != null) {
            initializer.initialize();
        }
    }

    /**
     * 読み出した定義をすべて出力する。
     *
     * @param defs 読み出した定義
     */
    private void dump(List<ComponentDefinition> defs) {
        for (ComponentDefinition def : defs) {
            StringBuilder sb = new StringBuilder();
            sb.append("definition loaded id = ");
            sb.append(def.getId());
            sb.append("\n");

            sb.append("\t type = ");
            sb.append(def.getType());
            sb.append("\n");

            sb.append("\t name = ");
            sb.append(def.getName());
            sb.append("\n");
            
            sb.append("\t component information = [");
            sb.append(def.getCreator().toString());
            sb.append("]\n");

            sb.append("\t------------------- component ref ------------------\n");
            for (ComponentReference ref : def.getReferences()) {
                sb.append("\t property name = ");
                sb.append(ref.getPropertyName());
                sb.append("\n");

                sb.append("\t target id = ");
                sb.append(ref.getTargetId());
                sb.append("\n");

                sb.append("\t component name = ");
                sb.append(ref.getReferenceName());
                sb.append("\n");

                sb.append("\t required type = ");
                sb.append(ref.getRequiredType());
                sb.append("\n");
                sb.append("\n");
            }
            sb.append("\t------------------- component ref ------------------\n");

            LOGGER.logTrace(sb.toString());
        }
    }

    /**
     * ObjectLoaderからロードできるオブジェクトを全て登録する。
     *
     * @param loader ObjectLoader
     */
    private void registerAll(ObjectLoader loader) {
        Map<String, Object> loaded = loader.load();
        for (Map.Entry<String, Object> entry : loaded.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            Class<?> type = value.getClass();

            ComponentCreator creator = new StoredValueComponentCreator(value);
            ComponentDefinition def = new ComponentDefinition(generateId(), key, creator, type);
            register(def);
        }
    }

    /**
     * コンポーネント定義を登録する。
     * @param def コンポーネント定義
     */
    private void register(ComponentDefinition def) {
        ComponentHolder holder = new ComponentHolder(def);
        holders.put(def.getId(), holder);
        if (def.getName() != null && !def.isUseIdOnly()) {
            nameIndex.put(def.getName(), holder);
        }

        if (!def.isUseIdOnly()) {
            registerTypes(def, holder);
        }
    }

    /**
     * 型を登録する。
     *
     * @param def コンポーネント定義
     * @param holder コンポーネントホルダ
     */
    private void registerTypes(ComponentDefinition def, ComponentHolder holder) {
        Class<?> baseType = def.getType();

        if (ComponentFactory.class.isAssignableFrom(def.getType())) {
            // ComponentFactoryは特別扱い
            Method m;
            try {
                m = def.getType().getMethod("createObject");
            } catch (Exception e) {
                // ComponentFactoryには必ずcreateObjectメソッドがあるはずなので、到達しない。
                throw new ContainerProcessException("method [createObject] execution failed.", e);
            }

            baseType = m.getReturnType();
        }

        registerTypeRecursive(baseType, holder);
    }

    /**
     * 再帰的に型インデックスにコンポーネントホルダを登録する。
     *
     * @param type 登録する型
     * @param holder コンポーネントホルダ
     */
    private void registerTypeRecursive(Class<?> type, ComponentHolder holder) {
        putTypeIndex(type, holder);

        for (Class<?> interfaces : type.getInterfaces()) {
            putTypeIndex(interfaces, holder);
        }

        if (!type.isInterface()) {
            for (Class<?> ancestor : ObjectUtil.getAncestorClasses(type)) {
                registerTypeRecursive(ancestor, holder);
            }
        }
    }

    /**
     * 型のインデックスにコンポーネントホルダを登録する。
     * @param key 型
     * @param holder ホルダ
     */
    private void putTypeIndex(Class<?> key, ComponentHolder holder) {
        if (typeIndex.containsKey(key)) {
            // 重複登録された型はからはずす
            typeIndex.remove(key);
            multiRegisteredType.add(key);
        } else if (!multiRegisteredType.contains(key)) {
            typeIndex.put(key, holder);
        }
    }

    /**
     * コンポーネントを作成する。
     *
     * @param holder コンポーネントホルダ
     */
    private void createComponent(ComponentHolder holder) {

        holder.setState(ComponentState.INSTANTIATING);

        ComponentDefinition def = holder.getDefinition();
        Object component;
        component = def.getCreator().createComponent(this, def);
        holder.setComponent(component);

        holder.setState(ComponentState.INSTANTIATED);

        if (component instanceof ComponentFactory<?>) {

            holder.setState(ComponentState.INJECTING);
            // ComponentFactoryの場合は、コンポーネントを初期化して
            initializeComponent(holder);
            ComponentFactory<?> factory = (ComponentFactory<?>) component;
            Object createdComponent = factory.createObject();
            holder.setInitializedComponent(createdComponent);
            holder.setState(ComponentState.INJECTED);

        }
    }

    /**
     * オブジェクトに対してインジェクションを実行する。
     *<p>
     * この際、オブジェクトが作成されていない場合、コンポーネントの作成も行う。
     *
     * @param holder コンポーネントホルダ
     */
    private void completeInject(ComponentHolder holder) {

        if (holder.getState() == ComponentState.INJECTED) {
            // 初期化中に再度completeInjectが呼ばれた場合(循環参照の場合)、無限ループになるため、
            // とりあえずできているものとして返す。
            return;
        }
        holder.setState(ComponentState.INJECTING);

        initializeComponent(holder);
        holder.setInitializedComponent(holder.getComponent());
        holder.setState(ComponentState.INJECTED);
    }

    /**
     * コンポーネントを初期化する。
     * @param holder 初期化するコンポーネントホルダ
     */
    private void initializeComponent(ComponentHolder holder) {

        if (holder.getDefinition().getInjector() == null) {
            // Initializerがnullの場合、普通に初期化
            for (ComponentReference ref : holder.getDefinition().getReferences()) {
                injectObject(holder, ref);
            }
        } else {
            // Initializerがnullではない場合、インジェクト処理を委譲
            holder.getDefinition().getInjector().completeInject(
                    this, holder.getDefinition(), holder.getComponent());
        }
    }

    /**
     * 1つのプロパティのインジェクションを実行する。
     * @param holder 初期化するコンポーネントホルダ
     * @param ref 参照の定義
     */
    private void injectObject(ComponentHolder holder, ComponentReference ref) {
        Object value;
        if (ref.getInjectionType() == InjectionType.ID) {
            value = getComponentById(ref.getTargetId());
            if (value == null) {
                // 設定ファイルでチェックしているためここには到達しない。
                throw new ContainerProcessException("component id was not found."
                        + " id = [" + ref.getTargetId() + "]");
            }
        } else if (ref.getInjectionType() == InjectionType.REF) {
            value = getComponentByName(ref.getReferenceName());
            if (value == null) {
                // 設定ファイルでチェックしているためここには到達しない。
                throw new ContainerProcessException("component name was not found."
                        + " name = [" + ref.getReferenceName() + "]");
            }
        } else if (ref.getInjectionType() == InjectionType.BY_TYPE) {
            // Autowireは見つからなくてもOK
            value = getComponentByType(ref.getRequiredType());
        } else {
            // Autowireは見つからなくてもOK
            value = getComponentByName(ref.getReferenceName());
        }
        if (value != null) {
            setProperty(holder, ref, value);
        }
    }

    /**
     * コンポーネントのプロパティに値を設定する。
     *
     * @param holder コンポーネントホルダ
     * @param ref 参照の定義
     * @param value 値
     */
    private void setProperty(ComponentHolder holder, ComponentReference ref, Object value) {
        Object component = holder.getComponent();
        String propertyName = ref.getPropertyName();
        writeIgnorePropertyLog(component.getClass(), propertyName);
        try {
            ObjectUtil.setProperty(component, propertyName, value, allowStaticInjection);
        } catch (IllegalConfigurationException e) {
            throw new ContainerProcessException(
                    "static property injection not allowed. " +
                            "component=[" + holder.getDefinition().getName() + "] " +
                            "property=[" + propertyName + "]", e);
        }
    }

    /**
     * 廃止されたプロパティの場合、ワーニングログを出力する。
     *
     * @param component 対象のクラス
     * @param propertyName プロパティ
     */
    private static void writeIgnorePropertyLog(
            final Class<?> component, final String propertyName) {

        final Method method = ObjectUtil.getSetterMethod(component, propertyName);
        final IgnoreProperty ignoreProperty = method.getAnnotation(IgnoreProperty.class);
        if (ignoreProperty == null) {
            return;
        }

        String invalidReason = "";
        if (StringUtil.hasValue(ignoreProperty.value())) {
            invalidReason = "(invalid reason:" + ignoreProperty.value() + ')';
        }
        LOGGER.logWarn("Setting to this property is invalid" + invalidReason + '.'
                + " It is recommended to delete the setting."
                + " class:" + component.getName()
                + " propertyName:" + propertyName);
    }

    /**
     * コンポーネントIDをキーにコンポーネントを取得する。
     * @param id コンポーネントID
     * @return コンポーネント
     */
    public Object getComponentById(int id) {
        if (!holders.containsKey(id)) {
            throw new ContainerProcessException("component id was not found."
                    + " component id = [" + id + "]");
        }

        ComponentHolder holder = holders.get(id);
        refStack.push(holder.getDefinition());
        Object component = checkStateAndCreateComponent(holder);
        completeInject(holder);
        refStack.pop();
        return component;
    }

    /**
     * コンポーネント名をキーにコンポーネントを取得する。
     * @param <T> コンポーネントの型
     * @param name コンポーネント名
     * @return コンポーネント
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponentByName(String name) {
        if (!nameIndex.containsKey(name)) {
            return null;
        }

        ComponentHolder holder = nameIndex.get(name);
        refStack.push(holder.getDefinition());
        Object component = checkStateAndCreateComponent(holder);
        if (component == null) {
            if (holder.getState() == ComponentState.INJECTING) {
                throw new ContainerProcessException(
                        "recursive referenced was found."
                        + " component name = [" + name + "] " + refStack.getReferenceStack());
            } else {
                throw new ContainerProcessException(
                        "component state was invalid."
                        + " component name = [" + name + "]"
                        + " , component state = [" + holder.getState() + "]");
            }
        }
        completeInject(holder);
        refStack.pop();
        return (T) component;
    }

    /**
     * コンポーネントの型をキーにコンポーネントを取得する。
     *
     * @param <T> コンポーネントの型
     * @param type コンポーネントの型
     * @return コンポーネント
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponentByType(Class<T> type) {
        if (!typeIndex.containsKey(type)) {
            return null;
        }
        ComponentHolder holder = typeIndex.get(type);
        refStack.push(holder.getDefinition(), type);
        Object component = checkStateAndCreateComponent(holder);
        if (component == null) {
            throw new ContainerProcessException(
                    "recursive referenced was found."
                    + " component name = [" + holder.getDefinition().getName() + "] "
                    + " , component type = [" + holder.getDefinition().getType() + "]"
                    + refStack.getReferenceStack());
        }
        completeInject(holder);
        refStack.pop();
        return (T) component;
    }

    /**
     * ステータスをチェックし、可能であればコンポーネントを取得する。
     *
     * @param holder コンポーネントホルダ
     * @return コンポーネント
     */
    private Object checkStateAndCreateComponent(ComponentHolder holder) {
        switch (holder.getState()) {
        case NOT_INSTANTIATE:
            createComponent(holder);
            return holder.getComponent();
        case INSTANTIATED:
            return holder.getComponent();
        case INJECTING:
        case INJECTED:
            return holder.getInitializedComponent();
        case INJECTION_FAILED:
        case INSTANTIATING:
        default:
            // この状態のオブジェクトは取得できない。
            return null;
        }
    }

    /**
     * コンポーネントの参照階層を保持するスタッククラス。
     */
    private static class ReferenceStack {

        /**
         * スタックの実体。
         * {@link DiContainer}インスタンスが、マルチスレッドで共用される場合を考慮して
         * {@link ThreadLocal}を使用する。
         */
        private final LinkedList<String> stack = new LinkedList<String>();

        /**
         * コンポーネント定義をスタックに格納する。
         * @param definition コンポーネント定義
         */
        void push(ComponentDefinition definition) {
            push(definition, "");
        }


        /**
         * コンポーネント定義をスタックに格納する。
         * @param definition コンポーネント定義
         * @param lookUpType ルックアップする型
         */
        void push(ComponentDefinition definition, Class<?> lookUpType) {
            String type = Builder.concat("lookup type=[", lookUpType.getName(), "]");
            push(definition, type);
        }

        /**
         * コンポーネント定義をスタックに格納する。
         * @param definition コンポーネント定義
         * @param lookUpType ルックアップする型
         */
        private void push(ComponentDefinition definition, String lookUpType) {
            stack.add(createStackElement(definition, lookUpType));
        }

        /**
         * スタックから要素を取り出す。
         * @return メッセージ
         */
        String pop() {
            return stack.removeLast();
        }

        /**
         * 循環参照が発生した場合のメッセージを作成する。
         * @return 参照スタック
         */
        String getReferenceStack() {
            StringBuilder sb = new StringBuilder("\nReference stack is below.\n");
            for (String e : stack) {
                sb.append(e);
            }
            return sb.toString();
        }

        /**
         * スタックの要素を作成する。
         * @param def コンポーネント定義
         * @param lookUpType ルックアップする型
         * @return スタックの要素
         */
        private String createStackElement(ComponentDefinition def, String lookUpType) {
            return Builder.concat(
                    "\t",
                    "id=[", def.getId(), "] ",
                    "name=[", def.getName(), "] ",
                    "component type=[", def.getType().getName(), "] ",
                    lookUpType,
                    "\n");
        }
    }

    /**
     * 外部化コンポーネント定義のローダーを取得する。
     * @return 外部化コンポーネント定義のローダー
     */
    ExternalizedComponentDefinitionLoader getExternalizedComponentDefinitionLoader() {
        return externalizedComponentDefinitionLoader;
    }
}
