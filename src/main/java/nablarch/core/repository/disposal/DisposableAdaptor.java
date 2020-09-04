package nablarch.core.repository.disposal;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;

import java.io.Closeable;

/**
 * {@link Closeable}オブジェクトを{@link Disposable}として扱うためのアダプタ。
 * <p>
 * Nablarch 5uXXはJava SE 6以上をサポートしているため、{@code AutoCloseable}(Java SE 7 で追加)ではなく
 * {@link Closeable}を対象としている。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class DisposableAdaptor implements Disposable {
    private static final Logger LOGGER = LoggerManager.get(DisposableAdaptor.class);

    private Closeable target;

    @Override
    public void dispose() throws Exception {
        target.close();
    }

    /**
     * 廃棄処理対象を設定する。
     * @param target 廃棄処理対象
     */
    public void setTarget(Closeable target) {
        this.target = target;
    }
}
