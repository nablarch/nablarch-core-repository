package nablarch.core.repository.test.component.abnormal.duplicate.configValue;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;
import nablarch.core.repository.di.config.externalize.annotation.SystemRepositoryComponent;

@SystemRepositoryComponent
public class TestBothAnnotationSettingComponent {
    public TestBothAnnotationSettingComponent(@ConfigValue("${config.value.string}") @ComponentRef("config.value.string") String config) {

    }
}
