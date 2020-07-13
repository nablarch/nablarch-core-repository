package nablarch.core.repository.di.config.externalize;

import nablarch.core.exception.IllegalConfigurationException;
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
        assertNotNull(((TestInjectionComponent) injectedComponent).getComponent());
        assertNotNull(((TestInjectionComponent) injectedComponent).getComponent().getComponent());
        assertEquals("value", ((TestInjectionComponent) injectedComponent).getConfig());

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
    public void testAbnormalComponentIsNotRegistered() {
        expectedException.expect(IllegalConfigurationException.class);
        expectedException.expectMessage("'config.value' of configuration value is not found.");

        exchanger.setupContextClassLoader("normalAnnotation");
        new DiContainer(new SimpleComponentDefinitionLoader());
        fail("ここに到達したらExceptionが発生していない。");
    }

    @Test
    public void testAbnormalComponentIsNotStringInstance() {
        expectedException.expect(IllegalConfigurationException.class);
        expectedException.expectMessage("'config.value' of configuration value is not a String.");

        exchanger.setupContextClassLoader("normalAnnotation");
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config/externalize/test-abnormal.xml");
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
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(isA(InvocationTargetException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/invocationTarget");
        new DiContainer(new SimpleComponentDefinitionLoader());
        fail("ここに到達したらExceptionが発生していない。");
    }

    public static class TestIllegalAccessExceptionLoader extends AnnotationComponentDefinitionLoader {
        @Override
        protected String getBasePackage() {
            return "nablarch.core.repository.test.component.abnormal.illegalAccess";
        }
    }

    @Test
    public void testAbnormalConstructorThrowsIllegalAccessException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(isA(IllegalAccessException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/illegalAccess");
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
    public void testAbnormalConstructorThrowsInstantiationException() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(isA(InstantiationException.class));

        exchanger.setupContextClassLoader("abnormalAnnotation/instantiation");
        new DiContainer(new SimpleComponentDefinitionLoader());
        fail("ここに到達したらExceptionが発生していない。");
    }

    @Test
    public void testAbnormalClassNotFoundException() {
        exchanger.setupContextClassLoader("normalAnnotation");
        try {
            // 存在しないクラスをClassHandlerに渡すResourcesを設定
            ResourcesUtil.addResourcesFactory("file", new ResourcesUtil.ResourcesFactory() {
                @Override
                public ResourcesUtil.Resources create(final URL url, final String rootPackage, final String rootDir) {
                    return new DummyFileResource();
                }
            });
            new DiContainer(new SimpleComponentDefinitionLoader());
        } catch (RuntimeException e) {
            assertThat(e.getCause(), instanceOf(ClassNotFoundException.class));
            return;
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

    private static class DummyFileResource implements ResourcesUtil.Resources {

        @Override
        public void forEach(ClassTraversal.ClassHandler classHandler) {
            classHandler.process("dummy", "Class");
        }

        @Override
        public void close() {
            // NOP
        }
    }
}