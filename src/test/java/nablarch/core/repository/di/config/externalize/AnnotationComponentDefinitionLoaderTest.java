package nablarch.core.repository.di.config.externalize;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.di.ContainerProcessException;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.SimpleComponentDefinitionLoader;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.repository.test.ContextClassLoaderExchanger;
import nablarch.core.repository.test.component.normal.TestComponent;
import nablarch.core.repository.test.component.normal.TestInjectionComponent;
import nablarch.core.repository.test.component.normal.TestNamingComponent;
import nablarch.core.repository.test.component.normal.TestReferenceInjectionComponent;
import nablarch.core.util.ClassTraversal;
import nablarch.core.util.ResourcesUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import static nablarch.core.util.ResourcesUtil.getBaseDir;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link AnnotationComponentDefinitionLoader}のテスト。
 */
public class AnnotationComponentDefinitionLoaderTest {
    @Rule
    public ContextClassLoaderExchanger exchanger = new ContextClassLoaderExchanger();

    public static class SystemUnderTest extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.normal";
        }
    }

    @Test
    public void testNormal() {
        // ExternalizedComponentDefinitionLoaderとしてSystemUnderTestを読み込む
        exchanger.setupContextClassLoader("normalAnnotation");
        // コンストラクタインジェクション用の設定値を読み込むローダー
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test.xml");
        DiContainer container = new DiContainer(loader);

        // デフォルトコンストラクタのコンポーネント
        Object testComponent = container.getComponentByName(TestComponent.class.getName());
        assertNotNull(testComponent);
        assertEquals(TestComponent.class, testComponent.getClass());

        // インナークラスのコンポーネント
        Object innerComponent = container.getComponentByName(TestComponent.TestInnerComponent.class.getName());
        assertNotNull(innerComponent);
        assertEquals(TestComponent.TestInnerComponent.class, innerComponent.getClass());

        // 名前指定のコンポーネント
        Object literalNamingComponent = container.getComponentByName("literal");
        assertNotNull(literalNamingComponent);
        assertEquals(TestNamingComponent.class, literalNamingComponent.getClass());

        Object outerClassNamingComponent = container.getComponentByName(TestNamingComponent.class.getName());
        assertEquals(TestNamingComponent.TestNamingInnerComponent.class, outerClassNamingComponent.getClass());

        // コンストラクタインジェクションのコンポーネント
        Object injectedComponent = container.getComponentByName(TestInjectionComponent.class.getName());
        assertNotNull(injectedComponent);
        assertEquals(TestInjectionComponent.class, injectedComponent.getClass());
        TestInjectionComponent testInjectionComponent = (TestInjectionComponent) injectedComponent;
        assertNotNull(testInjectionComponent.getComponent());
        assertNotNull(testInjectionComponent.getComponent().getComponent());
        assertThat(testInjectionComponent.getStringConfig(), is("value"));
        assertThat(testInjectionComponent.getStringArrayConfig(), is(new String[]{"a", "b", "c"}));
        assertThat(testInjectionComponent.getIntConfig(), is(2));
        assertThat(testInjectionComponent.getIntArrayConfig(), is(new int[]{1, 2, 3}));
        assertThat(testInjectionComponent.getLongConfig(), is(8L));
        assertTrue(testInjectionComponent.isBooleanConfig());

        // コンポーネント参照によるコンストラクタインジェクションのコンポーネント
        Object refInjectedComponent = container.getComponentByName(TestReferenceInjectionComponent.class.getName());
        assertNotNull(refInjectedComponent);
        assertEquals(TestReferenceInjectionComponent.class, refInjectedComponent.getClass());
        assertNotNull(((TestReferenceInjectionComponent) refInjectedComponent).getComponent());
        assertEquals("dummy", ((TestReferenceInjectionComponent) refInjectedComponent).getComponent().getProperty());
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAbnormalConfigValueIsNotNumber() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("config value is not number. name = ${config.value.integer}");
        expectedException.expectCause(isA(NumberFormatException.class));

        exchanger.setupContextClassLoader("normalAnnotation");
        // コンストラクタインジェクション用の設定値を読み込むローダー
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test-abnormal-config-value-is-not-number.xml");
        DiContainer container = new DiContainer(loader);
        fail("ここに到達したらExceptionが発生していない。");
    }

    @Test
    public void testAbnormalReferenceComponentIsNotRegistered() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("component name to reference was not found. name = dummyComponent");

        exchanger.setupContextClassLoader("normalAnnotation");
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test-abnormal-reference-component-not-found.xml");
        new DiContainer(loader);
        fail("ここに到達したらExceptionが発生していない。");
    }

    public static class TestReferenceComponentIsNotAssignableLoader extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.abnormal.assignable";
        }
    }

    @Test
    public void testAbnormalReferenceComponentIsNotAssignable() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("referenced component type mismatch." +
                " name = [dummyComponent]" +
                " parameter type = [nablarch.core.repository.test.component.normal.TestReferenceInjectionDummyComponent]" +
                " component type = [nablarch.core.repository.test.component.normal.TestComponent]");

        exchanger.setupContextClassLoader("abnormalAnnotation/assignable");
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test-abnormal-reference-component-not-assignable.xml");
        new DiContainer(loader);
        fail("ここに到達したらExceptionが発生していない。");
    }

    public static class TestDuplicateSettingLoader extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.abnormal.duplicate";
        }
    }

    @Test
    public void testAbnormalDuplicateSetting() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("component instantiation failed. " +
                "component class name = nablarch.core.repository.test.component.abnormal.duplicate.TestBothAnnotationSettingComponent");
        expectedException.expectCause(isA(IllegalConfigurationException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/duplicate");
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test.xml");
        new DiContainer(loader);
        fail("ここに到達したらExceptionが発生していない。");
    }

    public static class TestInvocationTargetExceptionLoader extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.abnormal.invocationTarget";
        }
    }

    @Test
    public void testAbnormalConstructorThrowsInvocationTargetException() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("component instantiation failed. " +
                "component class name = nablarch.core.repository.test.component.abnormal.invocationTarget.TestInvocationExceptionComponent");
        expectedException.expectCause(isA(InvocationTargetException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/invocationTarget");
        new DiContainer(new SimpleComponentDefinitionLoader());
        fail("ここに到達したらExceptionが発生していない。");
    }

    public static class TestInstantiationExceptionLoader extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.abnormal.instantiation";
        }
    }

    @Test
    public void testAbnormalAbstractClassConstructor() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("component instantiation failed. " +
                "component class name = nablarch.core.repository.test.component.abnormal.instantiation.TestInstantiationExceptionComponent");
        expectedException.expectCause(isA(InstantiationException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/instantiation");
        new DiContainer(new SimpleComponentDefinitionLoader());
        fail("ここに到達したらExceptionが発生していない。");
    }

    @Test
    public void testAbnormalClassNotFoundException() {
        exchanger.setupContextClassLoader("normalAnnotation");
        // 存在しないクラスをClassHandlerに渡すResourcesを設定
        ResourcesUtil.addResourcesFactory("file", new ResourcesUtil.ResourcesFactory() {
            @Override
            public ResourcesUtil.Resources create(URL url, String rootPackage, String rootDir) {
                return new ResourcesUtil.Resources() {
                    @Override
                    public void forEach(ClassTraversal.ClassHandler handler) {
                        handler.process("not_exists_package", "NotExistsClass");
                    }

                    @Override
                    public void close() {
                    }
                };
            }
        });
        try {
            expectedException.expect(RuntimeException.class);
            expectedException.expectCause(isA(ClassNotFoundException.class));

            new DiContainer(new SimpleComponentDefinitionLoader());
        } finally {
            ResourcesUtil.addResourcesFactory("file", new ResourcesUtil.ResourcesFactory() {
                @Override
                public ResourcesUtil.Resources create(final URL url, final String rootPackage, final String rootDir) {
                    return new ResourcesUtil.FileSystemResources(getBaseDir(url, rootDir), rootPackage, rootDir);
                }
            });
        }
        fail("ここに到達したらExceptionが発生していない。");
    }
}