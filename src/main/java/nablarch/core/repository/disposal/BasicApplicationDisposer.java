package nablarch.core.repository.disposal;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link Disposable}を実装したコンポーネントを指定した順序で廃棄するクラス。<br>
 * <p>
 * このクラスはマルチスレッド下で安全に操作できるように、すべてのメソッドを {@code synchronized} で宣言している。
 * </p>
 * @author Tanaka Tomoyuki
 */
public class BasicApplicationDisposer implements ApplicationDisposer {
    private static final Logger LOGGER = LoggerManager.get(BasicApplicationDisposer.class);
    private List<Disposable> disposableList = new ArrayList<Disposable>();

    /**
     * {@inheritDoc}
     * <p>
     * このメソッドを一度実行すると{@link Disposable}の参照は破棄されるため、
     * 二回以上実行しても同じ廃棄処理が何度も繰り返されることはない。
     * </p>
     */
    @Override
    public synchronized void dispose() {
        /*
         * ■逆順に dispose している理由
         * 一般的に廃棄しなければならないオブジェクトは、開いた順番にインスタンスが生成される。
         * そして、その順番で disposableList に設定される可能性が考えられる。
         * この場合、廃棄処理は開いた順番とは逆の順序で行うことが望ましい（例：JDBC の Connection, Statement, ResultSet）。
         * したがって、 dispose はリストを逆順にして実行している。
         */
        ArrayList<Disposable> copyList = new ArrayList<Disposable>(disposableList);
        Collections.reverse(copyList);

        for (Disposable disposable : copyList) {
            try {
                disposable.dispose();
            } catch (Exception exception) {
                LOGGER.logWarn("Failed to dispose component (disposable=" + disposable + ").", exception);
            }
        }

        disposableList.clear();
    }

    /**
     * {@link Disposable}のリストを設定する。
     * @param disposableList {@link Disposable}のリスト
     */
    public synchronized void setDisposableList(List<Disposable> disposableList) {
        this.disposableList = new ArrayList<Disposable>(disposableList);
    }

    @Override
    public synchronized void addDisposable(Disposable disposable) {
        disposableList.add(disposable);
    }
}
