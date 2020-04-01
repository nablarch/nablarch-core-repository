package nablarch.core.repository.di.config.externalize;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.di.ComponentCreator;
import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.StoredValueComponentCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * システムプロパティをコンポーネント定義として読み込む{@link ExternalizedComponentDefinitionLoader}。
 * <p/>
 * このローダーは、システムプロパティで指定されている値をすべて{@link String}のコンポーネントとしてロードする。
 *
 * @author Tomoyuki Tanaka
 */
public class SystemPropertyExternalizedLoader implements ExternalizedComponentDefinitionLoader {
    private static final Logger LOGGER = LoggerManager.get(SystemPropertyExternalizedLoader.class);

    @Override
    public List<ComponentDefinition> load(DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();

        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            ComponentCreator creator = new StoredValueComponentCreator(value);
            ComponentDefinition def = new ComponentDefinition(container.generateId(), key, creator, String.class);

            if (loadedComponents.containsKey(key) && !(loadedComponents.get(key).getDefinition().getCreator() instanceof StoredValueComponentCreator)) {
                // StoredValueComponentCreator 以外のプロパティを StoredValueComponentCreator で上書きするのはおかしいので例外。
                throw new RuntimeException("illegal system property was found which tries to override non-literal property."
                        + "system property can override literal value only."
                        + " key = [" + def.getName() + "]"
                        + " , previous class = [" + loadedComponents.get(key).getDefinition().getType().getName() + "]");
            }

            if (loadedComponents.containsKey(key)) {
                ComponentHolder previous = loadedComponents.get(key);

                // プロパティの上書きが発生
                // システムプロパティでの上書きは通常運用で利用することがあるため、INFOレベルでログ出力する。
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.logInfo("value was overridden by system property. "
                            + " key = " + def.getName()
                            + ", previous value = [" + previous.getDefinition().getCreator().createComponent(container, previous.getDefinition()) + "]"
                            + ", new value = [" + value + "]");
                }
            }

            definitions.add(def);
        }

        return definitions;
    }
}
