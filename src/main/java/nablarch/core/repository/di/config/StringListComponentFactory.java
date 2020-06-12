package nablarch.core.repository.di.config;

import nablarch.core.repository.di.ComponentFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 半角カンマ ({@cpde ","}) 区切りの文字列を {@code List<String>} のコンポーネントとして
 * 生成する {@link ComponentFactory} の実装。
 * <p>
 * このクラスは、 {@code values} に設定された文字列を半角カンマ ({@code ","}) で分割し、
 * 各要素を {@link String#trim()} でトリムした結果を {@code List<String>} のコンポーネントとして
 * 生成します。
 * </p>
 * <p>
 * {@code values} が {@code null} の場合は、空のリストが生成されます。
 * </p>
 *
 * @author Tomoyuki Tanaka
 */
public class StringListComponentFactory implements ComponentFactory<List<String>> {

    private String values;

    /**
     * このファクトリが生成する {@code List<String>} の各要素を
     * 半角カンマで連結した文字列を設定する。
     *
     * @param values 半角カンマ区切りの文字列
     */
    public void setValues(String values) {
        this.values = values;
    }

    @Override
    public List<String> createObject() {
        if (values == null) {
            throw new IllegalStateException("values must not be null.");
        }
        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();

        for (String element : values.split(",")) {
            result.add(element.trim());
        }

        return result;
    }
}
