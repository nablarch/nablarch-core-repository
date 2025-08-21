package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.annotation.DummyAnnotation;

@SystemRepositoryComponent
public record TestInjectionRecordComponent(TestInjectionDummyRecordComponent component, String stringConfig,
                                           String[] stringArrayConfig, int intConfig, int[] intArrayConfig,
                                           long longConfig, boolean booleanConfig, String dummy) {

    public TestInjectionRecordComponent(TestInjectionDummyRecordComponent component
            , @ConfigValue("${config.value.string}") String stringConfig
            , @ConfigValue("${config.value.string.array}") String[] stringArrayConfig
            , @ConfigValue("${config.value.integer}") int intConfig
            , @ConfigValue("${config.value.integer.array}") int[] intArrayConfig
            , @ConfigValue("${config.value.long}") long longConfig
            , @ConfigValue("${config.value.boolean}") boolean booleanConfig
            , @DummyAnnotation("dummy") String dummy) {
        this.component = component;
        this.stringConfig = stringConfig;
        this.stringArrayConfig = stringArrayConfig;
        this.intConfig = intConfig;
        this.intArrayConfig = intArrayConfig;
        this.longConfig = longConfig;
        this.booleanConfig = booleanConfig;
        this.dummy = dummy;
    }
}
