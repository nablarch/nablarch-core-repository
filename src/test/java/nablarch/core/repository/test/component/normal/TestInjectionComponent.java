package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestInjectionComponent {

    private final TestInjectionDummyComponent component;

    private final String config;

    public TestInjectionComponent(TestInjectionDummyComponent component, @ConfigValue("config.value") String config) {
        this.component = component;
        this.config = config;
    }

    public TestInjectionDummyComponent getComponent() {
        return component;
    }

    public String getConfig() {
        return config;
    }
}
