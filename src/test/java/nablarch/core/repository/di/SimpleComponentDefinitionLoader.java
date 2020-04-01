package nablarch.core.repository.di;

import java.util.ArrayList;
import java.util.List;

/**
 * ロードするコンポーネント定義を外部から設定できるテスト用の{@link ComponentDefinitionLoader}。
 *
 * @author Tomoyuki Tanaka
 */
public class SimpleComponentDefinitionLoader extends ArrayList<ComponentDefinition> implements ComponentDefinitionLoader {

    @Override
    public List<ComponentDefinition> load(DiContainer container) {
        return this;
    }
}
