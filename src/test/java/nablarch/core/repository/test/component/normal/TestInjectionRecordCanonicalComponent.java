package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.annotation.DummyAnnotation;

@SystemRepositoryComponent
public record TestInjectionRecordCanonicalComponent(TestInjectionDummyRecordComponent component
            , @ConfigValue("${config.value.string}") String stringConfig
            , @ConfigValue("${config.value.string.array}") String[] stringArrayConfig
            , @ConfigValue("${config.value.integer}") int intConfig
            , @ConfigValue("${config.value.integer.array}") int[] intArrayConfig
            , @ConfigValue("${config.value.long}") long longConfig
            , @ConfigValue("${config.value.boolean}") boolean booleanConfig
            , @DummyAnnotation("dummy") String dummy) {
}
