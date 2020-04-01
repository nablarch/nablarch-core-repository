package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.StoredValueComponentCreator;
import nablarch.core.repository.di.config.BeanComponentCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * 読み込み済みコンポーネント定義の生成を補助するクラス。
 *
 * @author Tomoyuki Tanaka
 */
public class LoadedComponentsBuilder {
    private final DiContainer container;
    private final Map<String, ComponentHolder> components = new HashMap<String, ComponentHolder>();

    /**
     * コンストラクタ。
     * @param container DIコンテナ
     */
    public LoadedComponentsBuilder(DiContainer container) {
        this.container = container;
    }

    /**
     * {@link String}のコンポーネントを追加する。
     * @param name コンポーネント名
     * @param value 値
     */
    public void put(String name, String value) {
        StoredValueComponentCreator creator = new StoredValueComponentCreator(value);
        ComponentDefinition definition = new ComponentDefinition(container.generateId(), name, creator, String.class);
        ComponentHolder holder = new ComponentHolder(definition);
        components.put(name, holder);
    }

    /**
     * {@link BeanComponentCreator}のコンポーネント定義を追加する。
     * @param name コンポーネント名
     * @param componentType コンポーネントの型
     */
    public void putBeanComponentCreator(String name, Class<?> componentType) {
        BeanComponentCreator creator = new BeanComponentCreator();
        ComponentDefinition definition = new ComponentDefinition(container.generateId(), name, creator, componentType);
        ComponentHolder holder = new ComponentHolder(definition);
        components.put(name, holder);
    }

    /**
     * 構築したコンポーネント定義を取得する。
     * @return 構築したコンポーネント定義
     */
    public Map<String, ComponentHolder> build() {
        return components;
    }
}
