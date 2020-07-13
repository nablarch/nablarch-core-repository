package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentCreator;
import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.ConstructorInjectionComponentCreator;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.util.ClassTraversal;
import nablarch.core.util.ResourcesUtil;
import nablarch.core.util.ResourcesUtil.Resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * アノテーションが付与されたクラスをコンポーネントとして読み込む{@link ExternalizedComponentDefinitionLoader}。
 * <p/>
 * このローダーは{@link SystemRepositoryComponent}が付与されたクラスをコンポーネントとして読み込む。
 * 読み込む対象となるパッケージは{@link #getBasePackage()}で取得する。
 * ローダーの使用時にサブクラスを作成し、オーバーライドすること。
 */
public abstract class AnnotationComponentDefinitionLoader implements ExternalizedComponentDefinitionLoader {
    /** スキャンの基点となるパッケージ */
    private final String basePackage;

    /**
     * コンストラクタ。
     * サブクラスでオーバーライドした{@link #getBasePackage()}を呼び出し
     * {@link #basePackage}に設定する。
     */
    public AnnotationComponentDefinitionLoader() {
        this.basePackage = getBasePackage();
    }

    /**
     * スキャン対象のパッケージを返す。
     *
     * @return スキャン対象のパッケージ
     */
    protected abstract String getBasePackage();

    @Override
    public List<ComponentDefinition> load(final DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        AnnotatedComponentFinder classHandler = new AnnotatedComponentFinder(container);
        for (Resources resources : ResourcesUtil.getResourcesTypes(basePackage)) {
            resources.forEach(classHandler);

        }
        return Collections.unmodifiableList(classHandler.getDefinitions());
    }

    /**
     * {@link SystemRepositoryComponent}で修飾されたコンポーネントを見つけ、{@link ConstructorInjectionComponentCreator}を持つ
     * {@link ComponentDefinition}を生成してListに保持する{@link nablarch.core.util.ClassTraversal.ClassHandler}実装クラス。
     */
    private static class AnnotatedComponentFinder implements ClassTraversal.ClassHandler {
        private final List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();
        private final ComponentCreator componentCreator = new ConstructorInjectionComponentCreator();
        private final DiContainer container;

        AnnotatedComponentFinder(DiContainer container) {
            this.container = container;
        }

        @Override
        public void process(String packageName, String shortClassName) {
            try {
                Class<?> type = this.getClass().getClassLoader().loadClass(packageName + "." + shortClassName);
                if (type.isAnnotationPresent(SystemRepositoryComponent.class)) {
                    definitions.add(new ComponentDefinition(container.generateId(),
                            getComponentName(type), componentCreator, type));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * {@link ComponentDefinition}リストを返す。
         *
         * @return {@link ComponentDefinition}リスト
         */
        List<ComponentDefinition> getDefinitions() {
            return definitions;
        }

        /**
         * コンポーネントの名前を取得する。
         *
         * @param type コンポーネントの型
         * @return {@link SystemRepositoryComponent}に名前指定がある場合はその名前を、ない場合は型の名前を返す。
         */
        private String getComponentName(Class<?> type) {
            SystemRepositoryComponent annotation = type.getAnnotation(SystemRepositoryComponent.class);
            if (!annotation.name().isEmpty()) {
                return annotation.name();
            }
            if (!annotation.nameFromType().equals(SystemRepositoryComponent.class)) {
                return annotation.nameFromType().getName();
            }
            return type.getName();
        }
    }
}
