package nablarch.core.repository.di.config.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.ConfigFileLoader;
import nablarch.core.repository.ObjectLoader;
import nablarch.core.repository.PropertiesFileLoader;
import nablarch.core.repository.di.ComponentCreator;
import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentDefinitionLoader;
import nablarch.core.repository.di.ComponentReference;
import nablarch.core.repository.di.ConfigurationLoadException;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.InjectionType;
import nablarch.core.repository.di.StoredValueComponentCreator;
import nablarch.core.repository.di.config.BeanComponentCreator;
import nablarch.core.repository.di.config.DuplicateDefinitionPolicy;
import nablarch.core.repository.di.config.ListComponentCreator;
import nablarch.core.repository.di.config.ListElementDefinition;
import nablarch.core.repository.di.config.LiteralComponentCreator;
import nablarch.core.repository.di.config.MapComponentCreator;
import nablarch.core.repository.di.config.MapEntryDefinition;
import nablarch.core.repository.di.config.MapEntryDefinition.DataType;
import nablarch.core.repository.di.config.xml.schema.AutowireType;
import nablarch.core.repository.di.config.xml.schema.Component;
import nablarch.core.repository.di.config.xml.schema.ComponentConfiguration;
import nablarch.core.repository.di.config.xml.schema.ComponentRef;
import nablarch.core.repository.di.config.xml.schema.ConfigFile;
import nablarch.core.repository.di.config.xml.schema.Entry;
import nablarch.core.repository.di.config.xml.schema.Import;
import nablarch.core.repository.di.config.xml.schema.Property;
import nablarch.core.util.Builder;
import nablarch.core.util.FileUtil;
import nablarch.core.util.ObjectUtil;
import nablarch.core.util.annotation.Published;

import org.xml.sax.SAXException;



/**
 * XMLファイルからコンポーネントの定義を読み込むクラス。
 * 
 * @author Koichi Asano 
 *
 */
@Published(tag = "architect")
public class XmlComponentDefinitionLoader implements ComponentDefinitionLoader {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager
            .get(XmlComponentDefinitionLoader.class);

    /**
     * オートワイヤ対象外クラスのセット。
     */
    private static final Set<Class<?>> IGNORE_AUTOWIRE_CLASSES;

    /**
     * 重複した設定値を検出した際の動作ポリシー。
     */
    private DuplicateDefinitionPolicy duplicateDefinitionPolicy;

    /**
     * 入力ファイルのURL。
     */
    private String inputFileUrl;

    /**
     * import したファイルのスタック。(参照ループが発生しないようにするため。)
     */
    private final Deque<URL> importFileNames = new ArrayDeque<URL>();

    static {
        Set<Class<?>> tmpIgnoreAutowiredClasses = new HashSet<Class<?>>();
        
        tmpIgnoreAutowiredClasses.add(boolean.class);
        tmpIgnoreAutowiredClasses.add(int.class);
        tmpIgnoreAutowiredClasses.add(long.class);

        tmpIgnoreAutowiredClasses.add(Boolean.class);
        tmpIgnoreAutowiredClasses.add(Integer.class);
        tmpIgnoreAutowiredClasses.add(Long.class);
        
        tmpIgnoreAutowiredClasses.add(String.class);
        tmpIgnoreAutowiredClasses.add(String[].class);
        tmpIgnoreAutowiredClasses.add(List.class);
        tmpIgnoreAutowiredClasses.add(Map.class);
        IGNORE_AUTOWIRE_CLASSES = Collections.unmodifiableSet(tmpIgnoreAutowiredClasses);
    }

    /**
     * コンストラクタ。
     * 
     * @param inputFileUrl 読み込むファイルのURL表現
     */
    public XmlComponentDefinitionLoader(String inputFileUrl) {
        this(inputFileUrl, DuplicateDefinitionPolicy.OVERRIDE);
    }

    /**
     * コンストラクタ。
     * 重複した設定時の動作を指定する場合、こちらを使用する。
     * 
     * @param inputFileUrl 読み込むファイルのURL表現
     * @param policy 重複した設定値を検出した際の動作ポリシー
     */
    public XmlComponentDefinitionLoader(String inputFileUrl, DuplicateDefinitionPolicy policy) {
        this.inputFileUrl = inputFileUrl;
        this.duplicateDefinitionPolicy = policy;
    }
    /**
     * コンポーネント定義のロードを行う。
     * 
     * @param container ロードするコンテナ
     * @return コンポーネント定義のリスト
     * @see nablarch.core.repository.di.ComponentDefinitionLoader#load(DiContainer)
     */
    public List<ComponentDefinition> load(DiContainer container) {
        InputStream in = null;

        // load毎にimportのスタックをクリア
        importFileNames.clear();

        try {
            if (!inputFileUrl.contains(":")) {
                // スキーマ定義なしの場合、クラスパスから取得
                inputFileUrl = "classpath:" + inputFileUrl;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.logDebug("load component config file."
                        + " file = " + inputFileUrl);
            }
            in = FileUtil.getResource(inputFileUrl);

            return loadInner(container, in, inputFileUrl);
        } catch (ConfigurationLoadException e) {
            throw new ConfigurationLoadException("file processing failed."
                    + " file = " + inputFileUrl
                    , e);
        } finally {
            FileUtil.closeQuietly(in);
            importFileNames.clear();
        }

    }

    /**
     * XMLファイルからコンポーネント定義をロードする。
     * 
     * @param container DIコンテナ
     * @param in 入力ストリーム
     * @param inputFileUrl 入力ファイルのURL
     * @return コンポーネント定義のリスト
     */
    private List<ComponentDefinition> loadInner(DiContainer container,
            InputStream in, String inputFileUrl) {
        try {
            List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();
            
            ComponentDefinitionFileParser unmarshaller = new ComponentDefinitionFileParser();
            
            ComponentConfiguration configuration = unmarshaller.parse(in);
            
            List<Object> l = configuration.getImportOrConfigFileOrComponent();

            for (Object o : l) {
                if (o instanceof Component) {
                    ComponentDefinition def = createComponentDefinition(
                            definitions, container, "", (Component) o);
                    definitions.add(def);
                } else if (o instanceof Import) {
                    definitions.addAll(prepareImport(definitions, container,
                            (Import) o, inputFileUrl));
                } else if (o instanceof nablarch.core.repository.di.config.xml.schema.List) {
                    ComponentDefinition def = createListDefinition(definitions,
                            container,
                            "", (nablarch.core.repository.di.config.xml.schema.List) o);
                    definitions.add(def);
                } else if (o instanceof nablarch.core.repository.di.config.xml.schema.Map) {
                    ComponentDefinition def = createMapDefinition(definitions,
                            container,
                            "", (nablarch.core.repository.di.config.xml.schema.Map) o);
                    definitions.add(def);
                } else {
                    definitions.addAll(prepareConfigFile(container,
                            (ConfigFile) o));
                }
            }

            if (duplicateDefinitionPolicy == DuplicateDefinitionPolicy.OVERRIDE) {
                definitions = mergeComponentDefinitions(definitions);
            } else {
                checkDuplicateName(definitions);
            }

            return definitions;
        } catch (SAXException e) {
            throw new ConfigurationLoadException("component definition load failed.", e);
        } catch (ParserConfigurationException e) {
            // SAXパーサの作成には通常失敗しないため、この例外は発生しません。
            throw new ConfigurationLoadException("component definition load failed.", e);
        } catch (IOException e) {
            // 入力ストリームが異常な状態にはなりえないため、この例外は発生しません。
            throw new ConfigurationLoadException("component definition load failed.", e);
        }
    }


    /**
     * 重複した設定のマージを行う。
     * @param definitions 定義
     * 
     * @return マージ後のコンポーネントのリスト
     */
    private List<ComponentDefinition> mergeComponentDefinitions(List<ComponentDefinition> definitions) {

        Map<String, ComponentDefinition> defMap = new HashMap<String, ComponentDefinition>();
        Map<Integer, ComponentDefinition> removedRef = new HashMap<Integer, ComponentDefinition>();
        List<ComponentDefinition> list = new ArrayList<ComponentDefinition>();

        for (ComponentDefinition def : definitions) {
            if (def.getName() != null) {
                if (defMap.containsKey(def.getName())) {
                    ComponentDefinition oldDef = defMap.get(def.getName());
                    ComponentDefinition newDef = def;
                    list.remove(oldDef);
                    def = marge(definitions, oldDef, def);

                    // 削除したものは、nameは削除するが、いったん復活候補として置いておく。
                    removedRef.put(newDef.getId(), newDef);
                }
                defMap.put(def.getName(), def);
            } 

            list.add(def);
        }

        // 必要に応じて復活させる
        for (ComponentDefinition def : definitions) {
            for (ComponentReference ref : def.getReferences()) {
                if (ref.getInjectionType() == InjectionType.ID && removedRef.containsKey(ref.getTargetId())) {
                    // 復活させるが、ID参照以外NGに設定。
                    ComponentDefinition oldDef = removedRef.remove(ref.getTargetId());
                    oldDef.setUseIdOnly(true);
                    list.add(oldDef);
                }
            }
        }
        
        return list;
    }

    /**
     * 古いコンポーネント定義に新しいコンポーネント定義をマージする。
     * @param definitions コンポーネント定義のリスト
     * @param oldDef 古いコンポーネント定義
     * @param newDef 新しいコンポーネント定義
     * @return マージしたコンポーネント定義
     */
    private ComponentDefinition marge(List<ComponentDefinition> definitions,
            ComponentDefinition oldDef, ComponentDefinition newDef) {
        if (!oldDef.getType().equals(newDef.getType())) {
            logWarning("override component classname was not matched. " 
                    + " replace all component configuration. "
                    + " component name = " + newDef.getName() 
                    + ", defined component definition classname = " + oldDef.getType().getName() 
                    + ", override component definition classname = " + newDef.getType().getName());
            // 強制上書き。
            
            return newDef;
        }

        Map<String, ComponentReference> overrideRefMap = new HashMap<String, ComponentReference>();

        for (ComponentReference newRef : newDef.getReferences()) {
            switch (newRef.getInjectionType()) {
            case ID:
            case REF:
                // idか参照設定は上書き

                logWarning("component property was overridden. "
                    + "component name = " + newDef.getName() 
                    + ", property = " + newRef.getPropertyName());

                overrideRefMap.put(newRef.getPropertyName(), newRef);
                break;
            default:
                // 自動インジェクションは上書きしない
                break;
            }
        }

        if (!overrideRefMap.isEmpty()) {
            Map<String, ComponentReference> mergedRefsMap = new LinkedHashMap<String, ComponentReference>();

            for (ComponentReference ref : oldDef.getReferences()) {
                mergedRefsMap.put(ref.getPropertyName(), ref);
            }
            
            // 元の設定になかったプロパティを追加
            mergedRefsMap.putAll(overrideRefMap);
            
            oldDef.updateReferences(new ArrayList<ComponentReference>(mergedRefsMap.values()));
        }
        return oldDef;
    }

    /**
     * コンポーネント間の名前の重複をチェックする。
     * 
     * @param definitions コンポーネント定義のリスト
     * 
     * @throws nablarch.core.repository.di.ConfigurationLoadException コンポーネント名の重複が検出された場合。
     */
    private void checkDuplicateName(List<ComponentDefinition> definitions) throws ConfigurationLoadException {
        Set<String> names = new HashSet<String>();
        
        for (ComponentDefinition def : definitions) {

            if (def.getName() == null) {
                continue;
            }
            
            if (names.contains(def.getName())) {
                throw new ConfigurationLoadException("component name was duplicated."
                        + " name = " + def.getName());
            }
            names.add(def.getName());
        }
    }

    /**
     * 環境設定ファイルの定義から、コンポーネントのローダを作成する。
     * <p/>
     * ディレクトリ指定で環境設定ファイルを読み込む場合は、dir属性にディレクトリパスを指定する。<br/>
     * dir属性に設定されたディレクトリパスは、dir属性が記述されているコンポーネント設定ファイルからの相対パスとして解釈する。
     * @param container DIコンテナ
     * @param configFile 設定ファイルの定義
     * @return インポートした設定ファイルの定義のリスト
     */
    private List<ComponentDefinition> prepareConfigFile(
            DiContainer container, ConfigFile configFile) {
        List<ComponentDefinition> values = new ArrayList<ComponentDefinition>();
        if (configFile.getDir() == null) {
            // dir指定なしの場合、リソースから取得
            String propFileUrl = configFile.getFile();

            if (!propFileUrl.contains(":")) {
                // スキーマ定義なしの場合、クラスパスから取得
                propFileUrl = "classpath:" + propFileUrl;
            }
            ComponentDefinition def = createConfigFileLoaderDefinition(
                    container, propFileUrl, configFile.getEncoding());
            values.add(def);
        } else {
            File parentDir = getParentDir(inputFileUrl);
            File[] listFiles = FileUtil.listFiles(new File(parentDir, configFile.getDir()).getAbsolutePath(), configFile.getFile());
            if (listFiles == null) {
                throwDirectoryNotFoundException(new File(parentDir, configFile.getDir()).getAbsolutePath());
            }
            for (File listFile : listFiles) {
                if (listFile.isFile()) {
                    ComponentDefinition def = createConfigFileLoaderDefinition(
                            container, listFile.toURI().toString(), configFile.getEncoding());
                    values.add(def);
                }
            }
        }
        return values;
    }

    /**
     * ディレクトリが見つからない場合の例外をスローする。
     * @param directoryPath ディレクトリパス
     */
    private void throwDirectoryNotFoundException(String directoryPath) {
        throw new IllegalStateException(Builder.concat("directory not found. path=[", directoryPath, "]."));
    }

    /**
     * 親ディレクトリを取得する。
     * @param url URL
     * @return 親ディレクトリ
     */
    private File getParentDir(String url) {
        String componentFilePath = FileUtil.getResourceURL(url).getFile();
        return new File(componentFilePath).getParentFile();
    }

    /**
     * 設定ファイルローダの定義を作成する。
     * 
     * @param container コンテナ
     * @param pathname 設定ファイル名
     * @param encoding ファイルの文字エンコーディング
     * @return 設定ファイルローダの定義
     */
    private ComponentDefinition createConfigFileLoaderDefinition(
            DiContainer container, String pathname, String encoding) {
        ObjectLoader loader;
        if(pathname.endsWith(".properties")){
            loader = new PropertiesFileLoader(pathname, encoding);
        }else{
            loader = new ConfigFileLoader(pathname, encoding);
        }
        int id = container.generateId();
        ComponentCreator creator = new StoredValueComponentCreator(loader);
        ComponentDefinition def = new ComponentDefinition(id, null, creator, loader.getClass());
        return def;
    }

    /**
     * インポートの定義を処理する。
     * 
     * @param definitions コンポーネント定義のリスト
     * @param container コンテナ
     * @param importDef インポート定義
     * @param inputFileUrl 入力ファイルのURL
     * @return インポートしたコンポーネント定義のリスト
     */
    private List<ComponentDefinition> prepareImport(List<ComponentDefinition> definitions, DiContainer container, Import importDef, String inputFileUrl) {
        String dir = importDef.getDir();
        String file = importDef.getFile();
        
        if (dir == null) {
            String fileUrl = file;
            if (!fileUrl.contains(":")) {
                // スキーマ定義なしの場合、クラスパスから取得
                fileUrl = "classpath:" + fileUrl;
            }
            InputStream in = null;
            URL url = FileUtil.getResourceURL(fileUrl);
            if (url == null) {
                throw new ConfigurationLoadException(
                        "file to import not found. path=[" + fileUrl + "]");
            }
            if (importFileNames.contains(url)) {
                throw new ConfigurationLoadException("import directive is circular.\n"
                       + "import stack = [" + importFileNames + "]");
            }
            importFileNames.push(url);
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.logDebug("load component config file."
                            + " file = " + fileUrl);
                }
                in = FileUtil.getResource(fileUrl);
                return loadInner(container, in, "file:" + inputFileUrl);
            } finally {
                FileUtil.closeQuietly(in);
                importFileNames.pop();
            }
            
        } else {
            List<ComponentDefinition> defs = new ArrayList<ComponentDefinition>();
            File parentDir = getParentDir(inputFileUrl);
            File[] listFiles = FileUtil.listFiles(new File(parentDir, dir).getAbsolutePath(), file);
            if (listFiles == null) {
                throwDirectoryNotFoundException(new File(parentDir, dir).getAbsolutePath());
            }
            for (File f : listFiles) {
                if (f.isFile()) {
                    InputStream in = null;
                    try {
                        in = new FileInputStream(f);
                        defs.addAll(loadInner(container, in, "file:" + f.getPath()));
                    } catch (FileNotFoundException e) {
                        // listFiles 実行後なので、通常到達不能コードです。
                        throw new ConfigurationLoadException("configuration load failed.", e);
                    } finally {
                        if (in != null) {
                            FileUtil.closeQuietly(in);
                        }
                    }
                }
            }
            return defs;
        }
    }

    /**
     * コンポーネントの定義を作成する。
     * 
     * @param definitions コンポーネント定義のリスト
     * @param container DIコンテナ
     * @param prefix コンポーネント名のプレフィクス
     * @param component xmlファイル中のコンポーネント定義
     * @return コンポーネントの定義
     */
    private ComponentDefinition createComponentDefinition(
            List<ComponentDefinition> definitions, DiContainer container,
            String prefix, Component component) {
        
        ComponentCreator creator = new BeanComponentCreator();
        
        Class<?> componentClass;
        try {
            componentClass = Class.forName(component.getClazz());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationLoadException("component class load failed. " 
                    + "component class name = " + component.getClazz(),
                    e);
        }
        Set<String> propertyNames = new HashSet<String>();
        String componentFullName = generateComponentFullname(prefix, component.getName());
        
        ComponentDefinition def = new ComponentDefinition(container
                .generateId(), componentFullName, creator, componentClass);
        for (Property prop : component.getProperty()) {
            if (prop.getValue() != null) {
                // literalのコンポーネントを登録
                ComponentDefinition valueComponent = createLiteralComponentDefinition(
                        container, prop.getValue(), componentClass, prop.getName());
                definitions.add(valueComponent);
                ComponentReference ref = new ComponentReference(prop.getName(),
                        null, null, InjectionType.ID, valueComponent.getId());
                def.addReference(ref);
            } else if (prop.getComponent() != null) {
                String childPrefix = componentFullName;
                ComponentDefinition propComponent = createComponentDefinition(
                        definitions, container, childPrefix, prop.getComponent());
                definitions.add(propComponent);
                ComponentReference ref = new ComponentReference(prop.getName(),
                        null, propComponent.getType(), InjectionType.ID,
                        propComponent.getId());
                def.addReference(ref);
            } else if (prop.getRef() != null) {
                ComponentReference ref = new ComponentReference(prop.getName(),
                        prop.getRef(), null, InjectionType.REF, -1);
                def.addReference(ref);
            } else if (prop.getMap() != null) {
                ComponentDefinition mapComponent = createMapDefinition(
                        definitions, container, prefix, prop.getMap());
                ComponentReference ref = new ComponentReference(prop.getName(),
                        null, mapComponent.getType(), InjectionType.ID,
                        mapComponent.getId());
                def.addReference(ref);
                definitions.add(mapComponent);
            } else if (prop.getList() != null) {
                ComponentDefinition listComponent = createListDefinition(
                        definitions, container, prefix, prop.getList());
                ComponentReference ref = new ComponentReference(prop.getName(),
                        null, listComponent.getType(), InjectionType.ID,
                        listComponent.getId());
                def.addReference(ref);
                definitions.add(listComponent);
            } else {
                throw new ConfigurationLoadException(
                        "property value was not found. " 
                        + "propertyName = " + prop.getName());
            }

            propertyNames.add(prop.getName());
        }

        for (String propertyName : ObjectUtil.getWritablePropertyNames(componentClass)) {
            if (!propertyNames.contains(propertyName)) {
                Method method = ObjectUtil.getSetterMethod(componentClass, propertyName);
                // 設定が書かれていないsetterはオートワイヤ対象
                setAutowireInjection(component, def, method, propertyName);
            }
        }
        
        return def;
    }

    /**
     * コンポーネントのフルネームを作成する。
     * 
     * @param prefix プレフィクス
     * @param componentName コンポーネント名
     * @return コンポーネントのフルネーム
     */
    private String generateComponentFullname(String prefix, String componentName) {
        String componentFullName;
        if (prefix == null || componentName == null) {
            componentFullName = null; 
        } else if (prefix.length() == 0) {
            componentFullName = componentName;
        } else {
            componentFullName = prefix + "." + componentName;
        }
        return componentFullName;
    }

    /**
     * Listの定義からComponentDefinitionを作成する。
     * @param definitions コンポーネント定義のリスト
     * @param container コンテナ
     * @param prefix プレフィクス
     * @param list  Listの定義
     * @return Mapの定義から作られたComponentDefinition
     */
    private ComponentDefinition createListDefinition(
            List<ComponentDefinition> definitions, DiContainer container,
            String prefix, nablarch.core.repository.di.config.xml.schema.List list) {

        String componentFullName = generateComponentFullname(prefix, list.getName());
        String childPrefix = componentFullName;
        List<ListElementDefinition> elementIds = new ArrayList<ListElementDefinition>();
        
        for (Object obj : list.getComponentOrValueOrComponentRef()) {
            if (obj instanceof Component) {
                Component comp = (Component) obj;
                ComponentDefinition elementDef = createComponentDefinition(definitions, container, childPrefix, comp);
                definitions.add(elementDef);
                elementIds.add(new ListElementDefinition(elementDef.getId(), null));
            } else if (obj instanceof ComponentRef) {
                ComponentRef ref = (ComponentRef) obj;
                elementIds.add(new ListElementDefinition(null, ref.getName()));
            } else if (obj instanceof String) {
                LiteralComponentCreator creator = new LiteralComponentCreator(String.class, (String) obj);
                ComponentDefinition elementDef = new ComponentDefinition(container.generateId(), null, creator, String.class);
                definitions.add(elementDef);
                elementIds.add(new ListElementDefinition(elementDef.getId(), null));
            } else {
                // XML がvalidであれば、ここには到達しない。
                throw new ConfigurationLoadException("illegal element found in list element.");
            }
        }

        ListComponentCreator creator = new ListComponentCreator(elementIds);
        ComponentDefinition def = new ComponentDefinition(container.generateId(), componentFullName, creator, List.class);
        def.setInjector(creator);
        return def;
    }

    /**
     * Mapの定義からComponentDefinitionを作成する。
     * 
     * @param definitions コンポーネント定義のリスト
     * @param container コンテナ
     * @param prefix プレフィクス
     * @param map Mapの定義
     * @return Mapの定義から作られたComponentDefinition
     */
    private ComponentDefinition createMapDefinition(List<ComponentDefinition> definitions,
            DiContainer container, String prefix, nablarch.core.repository.di.config.xml.schema.Map map) {
        List<MapEntryDefinition> entries = new ArrayList<MapEntryDefinition>();

        String componentFullName = generateComponentFullname(prefix, map.getName());
        
        for (Entry entry : map.getEntry()) {
            MapEntryDefinition entryDef = new MapEntryDefinition();
            if (entry.getKey() != null) {
                entryDef.setKeyType(DataType.STRING);
                entryDef.setKey(entry.getKey());
            } else if (entry.getKeyName() != null) {
                entryDef.setKeyType(DataType.REF);
                entryDef.setKeyRef(entry.getKeyName());
            } else if (entry.getKeyComponent() != null) {
                entryDef.setKeyType(DataType.COMPONENT);
                ComponentDefinition compDef = createComponentDefinition(definitions, container, componentFullName, entry.getKeyComponent());
                definitions.add(compDef);
                entryDef.setKeyId(compDef.getId());
            } else {
                throw new ConfigurationLoadException("map entry must have key value.");
            }

            if (entry.getValue() != null) {
                entryDef.setValueType(DataType.STRING);
                entryDef.setValue(entry.getValue());
            } else if (entry.getValueName() != null) {
                entryDef.setValueType(DataType.REF);
                entryDef.setValueRef(entry.getValueName());
            } else if (entry.getValueComponent() != null) {
                entryDef.setValueType(DataType.COMPONENT);
                ComponentDefinition compDef = createComponentDefinition(definitions, container, componentFullName, entry.getValueComponent());
                definitions.add(compDef);
                entryDef.setValueId(compDef.getId());
            } else {
                throw new ConfigurationLoadException("map entry must have key value.");
            }
            entries.add(entryDef);
        }
        
        MapComponentCreator creator = new MapComponentCreator(entries);
        ComponentDefinition def = new ComponentDefinition(container.generateId(), componentFullName, creator, java.util.Map.class);
        def.setInjector(creator);
        return def;
    }

    /**
     * オートワイヤの参照を設定する。
     * 
     * @param component XML上のコンポーネントの定義
     * @param def DIコンテナにわたすコンポーネントの定義
     * @param method セッタメソッド
     * @param propertyName プロパティ名
     */
    private void setAutowireInjection(Component component,
            ComponentDefinition def, Method method, String propertyName) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            return;
        }

        if (IGNORE_AUTOWIRE_CLASSES.contains(paramTypes[0])) {
            // autowireしないクラスはスキップ
            return;
        }

        if (component.getAutowireType() == AutowireType.BY_TYPE) {
            ComponentReference ref = new ComponentReference(
                    propertyName, null, paramTypes[0],
                    InjectionType.BY_TYPE, -1);
            def.addReference(ref);
        } else if (component.getAutowireType() == AutowireType.BY_NAME) {
            // 名前ベースのオートワイヤ
            ComponentReference ref = new ComponentReference(
                    propertyName, propertyName, null,
                    InjectionType.BY_NAME, -1);
            def.addReference(ref);

        }
    }

    /**
     * リテラルで記述されたオブジェクトの定義を作成する。
     * 
     * @param container コンテナ
     * @param literal 値のリテラル表現
     * @param componentClass インジェクト対象のコンポーネントのClass
     * @param propertyName プロパティ名
     * @return リテラルで記述されたオブジェクトの定義
     */
    private ComponentDefinition createLiteralComponentDefinition(
            DiContainer container, String literal, Class<?> componentClass, String propertyName) {
        Class<?> propertyType = ObjectUtil.getPropertyType(
                componentClass, propertyName);
        if (propertyType == null) {
            throw new ConfigurationLoadException("property not found in  class. "
                    + "propertyName = " + propertyName 
                    + ", className = " + componentClass.getName());
        }

        LiteralComponentCreator creator = new LiteralComponentCreator(propertyType,
                literal);
        ComponentDefinition def = new ComponentDefinition(container
                .generateId(), null, creator, propertyType);
        return def;
    }

    /**
     * ワーニングログを出力する。
     * @param message ログメッセージ
     */
    private void logWarning(String message) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.logWarn(message);
        }
    }
}
