package nablarch.core.repository.test;

import org.junit.rules.ExternalResource;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * テスト用にコンテキストクラスローダーを差し替える{@link org.junit.Rule}。
 * {@link #before()}で元のコンテキストクラスローダーを保持し、
 * {@link #after()}で元のコンテキストクラスローダーに戻す。
 * テスト用にコンテキストクラスローダーを差し替える場合はサブディレクトリを指定して
 * {@link #setupContextClassLoader(String)}を呼び出す。
 */
public class ContextClassLoaderExchanger extends ExternalResource {
    private ClassLoader originalContextClassLoader;

    @Override
    protected void before() throws Throwable {
        originalContextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    protected void after() {
        Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }

    /**
     * コンテキストクラスローダーを差し替える。
     * <p/>
     * 差し替え後のコンテキストクラスローダーは、{@code "nablarch.core.repository.test.<subDirName>"}
     * をリソースルートとして振舞います。<br/>
     * これにより、テストケースごとにサービスプロバイダーの設定ファイルを分けることが可能になります。
     *
     * @param subDirName サブディレクトリ名
     */
    public void setupContextClassLoader(String subDirName) {
        URL customRootDir = ContextClassLoaderExchanger.class.getResource("./" + subDirName + "/");
        URLClassLoader customClassLoader = new URLClassLoader(new URL[]{customRootDir}, originalContextClassLoader);
        Thread.currentThread().setContextClassLoader(customClassLoader);
    }

}
