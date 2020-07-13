package nablarch.core.repository.test.component.abnormal.illegalAccess;

import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.component.normal.TestComponent;

@SystemRepositoryComponent
public class TestIllegalAccessComponent {
    private TestIllegalAccessComponent(TestComponent component) throws IllegalAccessException {
    }
}
