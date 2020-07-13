package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestInjectionDummyComponent {

    private final TestComponent component;

    public TestInjectionDummyComponent(TestComponent component) {
        this.component = component;
    }

    public TestComponent getComponent() {
        return component;
    }
}
