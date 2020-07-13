package nablarch.core.repository.test.component.normal;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestReferenceInjectionComponent {
    private final TestReferenceInjectionDummyComponent component;

    public TestReferenceInjectionComponent(@ComponentRef("dummyComponent") TestReferenceInjectionDummyComponent component) {
        this.component = component;
    }

    public TestReferenceInjectionDummyComponent getComponent() {
        return component;
    }
}
