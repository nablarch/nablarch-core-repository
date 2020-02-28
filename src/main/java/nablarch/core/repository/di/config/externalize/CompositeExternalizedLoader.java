package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 複数の{@link ExternalizedComponentDefinitionLoader}を組み合わせたローダー。
 * <p/>
 * このクラスは、コンストラクタで指定したローダーのリストを順番に実行していき、
 * 各ローダーが読み込んだ結果を１つにマージして返却する。
 * <p/>
 * 既に読み込まれているコンポーネントと同じ名前のコンポーネントが読み込まれた場合は、
 * 後から読み込まれたコンポーネントで上書きされる。
 *
 * @author Tomoyuki Tanaka
 */
public class CompositeExternalizedLoader implements ExternalizedComponentDefinitionLoader {
    private final List<ExternalizedComponentDefinitionLoader> loaders;

    /**
     * コンストラクタ。
     * @param loaders {@link ExternalizedComponentDefinitionLoader}のリスト
     */
    public CompositeExternalizedLoader(List<ExternalizedComponentDefinitionLoader> loaders) {
        this.loaders = loaders;
    }

    @Override
    public List<ComponentDefinition> load(DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        Map<String, ComponentDefinition> result = new HashMap<String, ComponentDefinition>();
        Map<String, ComponentHolder> loadedCache = new HashMap<String, ComponentHolder>(loadedComponents);

        for (ExternalizedComponentDefinitionLoader loader : loaders) {
            List<ComponentDefinition> externalized = loader.load(container, loadedCache);
            for (ComponentDefinition component : externalized) {
                result.put(component.getName(), component);
                loadedCache.put(component.getName(), new ComponentHolder(component));
            }
        }

        return new ArrayList<ComponentDefinition>(result.values());
    }

    /**
     * このローダーが持つ{@link ExternalizedComponentDefinitionLoader}リストを取得する。
     * @return このローダーが持つ{@link ExternalizedComponentDefinitionLoader}リスト
     */
    public List<ExternalizedComponentDefinitionLoader> getLoaders() {
        return loaders;
    }
}
