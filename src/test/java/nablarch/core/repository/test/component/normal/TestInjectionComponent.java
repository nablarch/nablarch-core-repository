package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestInjectionComponent {

    private final TestInjectionDummyComponent component;

    private final String stringConfig;
    private final String[] stringArrayConfig;
    private final int intConfig;
    private final int[] intArrayConfig;
    private final long longConfig;
    private final boolean booleanConfig;

    public TestInjectionComponent(TestInjectionDummyComponent component
            , @ConfigValue("${config.value.string}") String stringConfig
            , @ConfigValue("${config.value.string.array}") String[] stringArrayConfig
            , @ConfigValue("${config.value.integer}") int intConfig
            , @ConfigValue("${config.value.integer.array}") int[] intArrayConfig
            , @ConfigValue("${config.value.long}") long longConfig
            , @ConfigValue("${config.value.boolean}") boolean booleanConfig) {
        this.component = component;
        this.stringConfig = stringConfig;
        this.stringArrayConfig = stringArrayConfig;
        this.intConfig = intConfig;
        this.intArrayConfig = intArrayConfig;
        this.longConfig = longConfig;
        this.booleanConfig = booleanConfig;
    }

    public TestInjectionDummyComponent getComponent() {
        return component;
    }

    public String getStringConfig() {
        return stringConfig;
    }

    public String[] getStringArrayConfig() {
        return stringArrayConfig;
    }

    public int getIntConfig() {
        return intConfig;
    }

    public int[] getIntArrayConfig() {
        return intArrayConfig;
    }

    public long getLongConfig() {
        return longConfig;
    }

    public boolean isBooleanConfig() {
        return booleanConfig;
    }
}
