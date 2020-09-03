package nablarch.core.repository.disposal;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Disposable}を実装したコンポーネントを指定した順序で廃棄するクラス。<br>
 *
 * @author Tanaka Tomoyuki
 */
public class BasicApplicationDisposer implements ApplicationDisposer {
    private static final Logger LOGGER = LoggerManager.get(BasicApplicationDisposer.class);
    private List<Disposable> disposableList = new ArrayList<Disposable>();

    @Override
    public void dispose() {
        ArrayList<Disposable> copyList = new ArrayList<Disposable>(disposableList);
        Collections.reverse(copyList);

        for (Disposable disposable : copyList) {
            try {
                disposable.dispose();
            } catch (Exception exception) {
                LOGGER.logWarn("Failed to dispose component (disposable=" + disposable + ").", exception);
            }
        }
    }

    /**
     * {@link Disposable}のリストを設定する。
     * @param disposableList {@link Disposable}のリスト
     */
    public void setDisposableList(List<Disposable> disposableList) {
        this.disposableList = new ArrayList<Disposable>(disposableList);
    }

    @Override
    public void addDisposable(Disposable disposable) {
        disposableList.add(disposable);
    }
}
