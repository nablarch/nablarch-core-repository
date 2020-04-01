package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;

import java.util.List;
import java.util.Map;

/**
 * 外部化されたコンポーネント定義をロードするインタフェース。
 * <p/>
 * 外部化されたコンポーネント定義には、設定ファイル以外の例えばシステムプロパティや
 * OS環境変数などで指定された値などが該当する。
 *
 * @author Tomoyuki Tanaka
 */
public interface ExternalizedComponentDefinitionLoader {

    /**
     * 外部化されたコンポーネントを読み込む。
     * @param container DIコンテナ
     * @param loadedComponents 読み込み済みのコンポーネント（マップのキーはコンポーネントの名前）
     * @return 読み込んだコンポーネント定義のリスト
     */
    List<ComponentDefinition> load(DiContainer container, Map<String, ComponentHolder> loadedComponents);
}
