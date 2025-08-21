package nablarch.core.repository.test.component.normal;

import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public record TestReferenceInjectionDummyRecordComponent (@ConfigValue("${config.value.property}") String property){}
