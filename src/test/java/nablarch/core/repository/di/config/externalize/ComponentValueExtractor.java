package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * テストで検証しやすいように、{@link ComponentDefinition}や{@link ComponentHolder}からコンポーネントの値を
 * 抽出するユーティリティメソッドを提供するクラス。
 *
 * @author Tomoyuki Tanaka
 */
public class ComponentValueExtractor {
    private final DiContainer container;

    /**
     * コンストラクタ。
     * @param container DIコンテナ
     */
    public ComponentValueExtractor(DiContainer container) {
        this.container = container;
    }

    /**
     * コンポーネント定義のリストを、キーをコンポーネント名、値をコンポーネント定義から生成した値のマップに変換する。
     * @param definitions コンポーネント定義のリスト
     * @return 変換後のマップ
     */
    public Map<String, Object> toMap(List<ComponentDefinition> definitions) {
        Map<String, Object> result = new HashMap<String, Object>();

        for (ComponentDefinition definition : definitions) {
            Object value = definition.getCreator().createComponent(container, definition);
            result.put(definition.getName(), value);
        }

        return result;
    }

    /**
     * {@link ComponentHolder}のマップを受け取り、値を{@link ComponentHolder}から生成した値に置き換えて返却する。
     * @param components キーにコンポーネント名、値に{@link ComponentHolder}を持つマップ
     * @return 値のみを、その{@link ComponentHolder}から生成した値に変換したマップ
     */
    public Map<String, Object> toMap(Map<String, ComponentHolder> components) {
        Map<String, Object> result = new HashMap<String, Object>();

        for (Map.Entry<String, ComponentHolder> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentHolder holder = entry.getValue();
            ComponentDefinition definition = holder.getDefinition();
            Object value = definition.getCreator().createComponent(container, definition);
            result.put(name, value);
        }

        return result;
    }
}
