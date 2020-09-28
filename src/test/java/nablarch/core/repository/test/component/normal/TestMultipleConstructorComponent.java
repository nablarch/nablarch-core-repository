package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestMultipleConstructorComponent {

    private final TestComponent component;

    public TestMultipleConstructorComponent() {
        this.component = null;
    }

    public TestMultipleConstructorComponent(TestComponent component) {
        this.component = component;
    }

    public TestComponent getComponent() {
        return component;
    }
}
