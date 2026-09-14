package nablarch.core.repository.test.component.normal;

import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestComponent {

    @SystemRepositoryComponent
    public static class TestInnerComponent {
    }

    @SystemRepositoryComponent
    public static record TestInnerRecordComponent(TestComponent component) {
    }
}
