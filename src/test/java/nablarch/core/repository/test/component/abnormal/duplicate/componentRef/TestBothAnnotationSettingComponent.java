package nablarch.core.repository.test.component.abnormal.duplicate.componentRef;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestBothAnnotationSettingComponent {
    public TestBothAnnotationSettingComponent(@ComponentRef("config.value.string") @ConfigValue("${config.value.string}") String config) {

    }
}
