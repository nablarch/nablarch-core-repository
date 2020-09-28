package nablarch.core.repository.test.component.abnormal.invocationTarget;

import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;
import nablarch.core.repository.test.component.normal.TestComponent;

import java.lang.reflect.InvocationTargetException;

@SystemRepositoryComponent
public class TestInvocationExceptionComponent {
    public TestInvocationExceptionComponent(TestComponent component) throws InvocationTargetException {
        throw new RuntimeException("invocation error");
    }
}
