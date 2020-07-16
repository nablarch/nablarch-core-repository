package nablarch.core.repository.test.component.abnormal.assignable;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.component.normal.TestReferenceInjectionDummyComponent;

@SystemRepositoryComponent
public class TestNotAssignableReferenceComponent {
    private final TestReferenceInjectionDummyComponent component;

    public TestNotAssignableReferenceComponent(@ComponentRef("dummyComponent") TestReferenceInjectionDummyComponent component) {
        this.component = component;
    }

    public TestReferenceInjectionDummyComponent getComponent() {
        return component;
    }
}
