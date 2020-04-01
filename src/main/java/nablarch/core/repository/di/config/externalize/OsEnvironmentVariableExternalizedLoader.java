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
 * OS環境変数をコンポーネント定義として読み込む{@link ExternalizedComponentDefinitionLoader}。
 * <p/>
 * このローダーは、読み込み済みのコンポーネントの名前を元に、OS環境変数を検索する。<br/>
 * このとき、OS環境変数で使用できる文字種に制限があることを踏まえて、コンポーネント名を
 * 次のように変換してから検索する。
 * <ol>
 *   <li>ドット({@code "."})とハイフン({@code "-"})をアンダーバー({@code "_"})に置換する</li>
 *   <li>小文字を大文字に変換する({@link String#toUpperCase()})</li>
 * </ol>
 * <p/>
 * つまり、{@code "foo-bar.fizz-buzz"}という名前のコンポーネントが存在した場合は、
 * {@code "FOO_BAR_FIZZ_BUZZ"}という名前でOS環境変数が検索されることになる。
 * <p/>
 * 変換後の名前でOS環境変数が見つかった場合は、その値を{@code String}のコンポーネントとして読み込む。
 *
 * @author Tomoyuki Tanaka
 */
public class OsEnvironmentVariableExternalizedLoader implements ExternalizedComponentDefinitionLoader {
    private static final Logger LOGGER = LoggerManager.get(OsEnvironmentVariableExternalizedLoader.class);

    private final Map<String, String> env;

    /**
     * コンストラクタ。
     */
    public OsEnvironmentVariableExternalizedLoader() {
        this(System.getenv());
    }

    /**
     * 環境変数の{@link Map}を外部から指定するコンストラクタ。
     * <p/>
     * このコンストラクタは単体テスト用のため、アプリケーションのコードからは利用しないこと。
     *
     * @param env 環境変数の{@link Map}
     */
    OsEnvironmentVariableExternalizedLoader(Map<String, String> env) {
        this.env = env;
    }

    @Override
    public List<ComponentDefinition> load(DiContainer container, Map<String, ComponentHolder> loadedComponents) {
        List<ComponentDefinition> definitions = new ArrayList<ComponentDefinition>();

        for (Map.Entry<String, ComponentHolder> entry : loadedComponents.entrySet()) {
            String componentName = entry.getKey();
            String envName = convertToEnvName(componentName);
            String envValue = env.get(envName);

            if (envValue != null) {
                // 環境変数で上書き定義されている
                ComponentCreator creator = new StoredValueComponentCreator(envValue);
                ComponentDefinition def = new ComponentDefinition(container.generateId(), componentName, creator, String.class);

                if (!(entry.getValue().getDefinition().getCreator() instanceof StoredValueComponentCreator)) {
                    // StoredValueComponentCreator 以外のプロパティを StoredValueComponentCreator で上書きするのはおかしいので例外。
                    throw new RuntimeException("illegal os environment variable was found which tries to override non-literal property."
                            + "os environment variable can override literal value only."
                            + " key = [" + def.getName() + "]"
                            + " , previous class = [" + entry.getValue().getDefinition().getType().getName() + "]");
                }

                // 環境変数での上書きは通常運用で利用することがあるため、INFOレベルでログ出力する。
                // ただし、DBのパスワードなどを上書きすることが考えられるので、値は出力しない。
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.logInfo("value was overridden by os environment variable. key = " + def.getName());
                }

                definitions.add(def);
            }
        }

        return definitions;
    }

    /**
     * コンポーネント名を、検索用のOS環境変数名に変換する。
     * @param componentName コンポーネント名
     * @return 変換後のOS環境変数名
     */
    private String convertToEnvName(String componentName) {
        return componentName
                    .replace(".", "_")
                    .replace("-", "_")
                    .toUpperCase();
    }
}
