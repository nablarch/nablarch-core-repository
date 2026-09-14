package nablarch.core.repository.test.component.abnormal.nonPublicRecord;

import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.component.normal.TestComponent;

@SystemRepositoryComponent
record TestNonPublicRecordComponent(TestComponent component) {
}
