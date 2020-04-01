package nablarch.core.repository.di;

import nablarch.core.repository.di.config.externalize.CompositeExternalizedLoader;
import nablarch.core.repository.di.config.externalize.ExternalizedComponentDefinitionLoader;
import nablarch.core.repository.di.config.externalize.OsEnvironmentVariableExternalizedLoader;
import nablarch.core.repository.di.config.externalize.SystemPropertyExternalizedLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * {@link DiContainer}の、{@link ExternalizedComponentDefinitionLoader}をサービスローダーを使って初期化する処理のテスト。
 *
 * @author Tomoyuki Tanaka
 */
public class ExternalizedLoaderTest {
    private ClassLoader originalContextClassLoader;

    @Before
    public void setUp() {
        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    /*
     * コンテキストクラスローダーの差し替えを行っていない理由。
     *
     * このテストでは、後方互換を確認するためにサービスプロバイダの設定ファイルが無い場合の動作を検証しています。
     * しかし、コンテキストクラスローダーを検証用の URLClassLoader に差し替えると、
     * ServiceLoader から取得した Iterator をイテレートしようとしたときに内部で NullPointerException が
     * スローされてテストが落ちます。
     *
     * 一方で、デフォルトのコンテキストクラスローダーの場合は、 NullPointerException はスローされず、
     * テストは正常に動作します。
     *
     * 理想としては、単体テストごとに閉じたコンテキストクラスローダーを使ってテストを
     * 行った方が他のテストと影響し合うことがなく安全です。
     * しかし、上記理由により、設定が無い場合を URLClassLoader で確認することができません。
     *
     * このため、このテストに限り、コンテキストクラスローダーの差し替えは行っていません。
     */
    @Test
    public void testSystemPropertyExternalizedLoaderIsLoadedInDefault() {
        DiContainer container = new DiContainer(new SimpleComponentDefinitionLoader());

        ExternalizedComponentDefinitionLoader loader = container.getExternalizedComponentDefinitionLoader();

        assertThat(loader, instanceOf(SystemPropertyExternalizedLoader.class));
    }

    @Test
    public void testExternalizedLoadersAreLoadedByServiceLoaderMechanism() {
        setupContextClassLoader("testServiceLoader");
        DiContainer container = new DiContainer(new SimpleComponentDefinitionLoader());

        ExternalizedComponentDefinitionLoader loader = container.getExternalizedComponentDefinitionLoader();

        assertThat(loader, instanceOf(CompositeExternalizedLoader.class));

        List<ExternalizedComponentDefinitionLoader> loaders = ((CompositeExternalizedLoader) loader).getLoaders();

        assertThat(loaders, contains(
                instanceOf(OsEnvironmentVariableExternalizedLoader.class),
                instanceOf(SystemPropertyExternalizedLoader.class)));
    }

    /**
     * コンテキストクラスローダーをテスト用に差し替える。
     * <p/>
     * 差し替え後のコンテキストクラスローダーは、{@code "nablarch.core.repository.di.ExternalizedLoaderTest.<subDirName>"}
     * をリソースルートとして振舞います。<br/>
     * これにより、テストケースごとにサービスプロバイダーの設定ファイルを分けることが可能になります。
     *
     * @param subDirName サブディレクトリ名
     */
    private void setupContextClassLoader(String subDirName) {
        URL customRootDir = ExternalizedLoaderTest.class.getResource("./ExternalizedLoaderTest/" + subDirName + "/");
        URLClassLoader customClassLoader = new URLClassLoader(new URL[] {customRootDir}, originalContextClassLoader);
        Thread.currentThread().setContextClassLoader(customClassLoader);
    }

    @After
    public void tearDown() {
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }
}
