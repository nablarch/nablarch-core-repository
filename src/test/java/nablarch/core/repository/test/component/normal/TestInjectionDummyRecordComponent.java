package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public record TestInjectionDummyRecordComponent (@ComponentRef("dummyComponent") TestReferenceInjectionDummyComponent component){}
