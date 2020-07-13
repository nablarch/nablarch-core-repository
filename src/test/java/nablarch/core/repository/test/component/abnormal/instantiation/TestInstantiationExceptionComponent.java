package nablarch.core.repository.test.component.abnormal.instantiation;

import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.component.normal.TestComponent;

@SystemRepositoryComponent
public abstract class TestInstantiationExceptionComponent {
    public TestInstantiationExceptionComponent(TestComponent component) throws InstantiationException {
        throw new InstantiationException("cannot instantiate");
    }
}
