package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.StoredValueComponentCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * テスト用にロード結果を簡単に設定できるようにした{@link ExternalizedComponentDefinitionLoader}。
 * @author Tomoyuki Tanaka
 */
public class SimpleExternalizedLoader implements ExternalizedComponentDefinitionLoader {
    private final Map<String, String> components = new HashMap<String, String>();
    private Map<String, ComponentHolder> capturedLoadedComponents;

    /**
     * コンポーネントを登録する。
     * @param name コンポーネント名
     * @param value コンポーネントの値
     */
    public void put(String name, String value) {
        this.components.put(name, value);
    }

    @Override
    public List<ComponentDefinition> load(DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        capturedLoadedComponents = new HashMap<String, ComponentHolder>(loadedComponents);

        List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();

        for (Map.Entry<String, String> entry : components.entrySet()) {
            String componentName = entry.getKey();
            String componentValue = entry.getValue();
            StoredValueComponentCreator creator = new StoredValueComponentCreator(componentValue);
            ComponentDefinition definition = new ComponentDefinition(container.generateId(), componentName, creator, String.class);
            definitions.add(definition);
        }

        return definitions;
    }

    /**
     * 最後に{@link #load(DiContainer, Map)}を実行されたときに受け取った読み込み済みコンポーネント定義のコピーを取得する。
     * @return 最後に受け取った読み込み済みコンポーネント定義
     */
    public Map<String, ComponentHolder> getCapturedLoadedComponents() {
        return capturedLoadedComponents;
    }
}
