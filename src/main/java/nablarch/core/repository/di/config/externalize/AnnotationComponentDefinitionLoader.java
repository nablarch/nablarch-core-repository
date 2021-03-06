package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentCreator;
import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.ConstructorInjectionComponentCreator;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.util.ClassTraversal.ClassHandler;
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
    /**
     * スキャン対象のパッケージを返す。
     *
     * @return スキャン対象のパッケージ
     */
    protected abstract String getBasePackage();

    @Override
    public List<ComponentDefinition> load(final DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        ResourceClassHandler classHandler = new ResourceClassHandler(container, newComponentCreator(), getClass().getClassLoader());
        for (Resources resources : ResourcesUtil.getResourcesTypes(getBasePackage())) {
            resources.forEach(classHandler);

        }
        return Collections.unmodifiableList(classHandler.getDefinitions());
    }

    protected ComponentCreator newComponentCreator() {
        return new ConstructorInjectionComponentCreator();
    }

    /**
     * {@link SystemRepositoryComponent}で修飾されたコンポーネントを見つけ、コンストラクタで指定された{@link ComponentCreator}を持つ
     * {@link ComponentDefinition}を生成してListに保持する{@link nablarch.core.util.ClassTraversal.ClassHandler}実装クラス。
     */
    private static class ResourceClassHandler implements ClassHandler {
        private final List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();
        private final ComponentCreator componentCreator;
        private final DiContainer container;
        private final ClassLoader classLoader;

        ResourceClassHandler(DiContainer container, ComponentCreator componentCreator, ClassLoader classLoader) {
            this.container = container;
            this.componentCreator = componentCreator;
            this.classLoader = classLoader;
        }

        @Override
        public void process(String packageName, String shortClassName) {
            try {
                Class<?> type = classLoader.loadClass(packageName + "." + shortClassName);
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
