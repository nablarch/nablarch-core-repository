package nablarch.core.repository.test.component.normal;


import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent(name = "literal")
public class TestNamingComponent {

    @SystemRepositoryComponent(nameFromType = TestNamingComponent.class)
    public static class TestNamingInnerComponent {
    }
}
