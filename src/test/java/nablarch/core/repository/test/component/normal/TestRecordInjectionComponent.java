package nablarch.core.repository.test.component.normal;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public record TestRecordInjectionComponent(TestInjectionDummyComponent component
        , @ConfigValue("${config.value.string}") String stringConfig
        , @ConfigValue("${config.value.integer}") int intConfig
        , @ComponentRef("dummyComponent") TestReferenceInjectionDummyComponent refComponent) {
}
