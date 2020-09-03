package nablarch.core.repository.disposal;

/**
 * コンポーネントの廃棄を行うインタフェース。
 *
 * @author Tanaka Tomoyuki
 */
public interface ApplicationDisposer {

    /**
     * 廃棄処理を行う。
     */
    void dispose();

    /**
     * {@link Disposable}を追加する。
     * @param disposable 追加する{@link Disposable}
     */
    void addDisposable(Disposable disposable);
}
