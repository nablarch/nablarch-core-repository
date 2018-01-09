package nablarch.core.repository.di;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.repository.di.staticprop.Bar;
import nablarch.core.repository.di.staticprop.Foo;
import nablarch.core.repository.test.SystemPropertyResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class StaticInjectionTest {

    private static final String PKG = "nablarch/core/repository/di/staticprop/";
    @Rule
    public SystemPropertyResource sysProps = new SystemPropertyResource();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /** テスト対象 */
    private DiContainer sut;

    /** テストケース内で書き換えられるstaticプロパティをクリアしておく */
    @Before
    public void clearFooStaticProperty() {
        Foo.setBar(null);
    }

    @Test
    public void staticインジェクションを許容する場合_staticなプロパティにインジェクションされること() {
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"),
                              true);
        Bar bar = sut.getComponentByName("bar");
        Bar barOfFoo = Foo.getBar();
        assertThat("コンポーネントfooのstaticプロパティにautowireでコンポーネントbarが設定されること",
                   bar, is(sameInstance(barOfFoo)));
    }

    @Test
    public void staticインジェクションを許容しない場合_staticなプロパティにはインジェクションされず例外が発生すること() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("static property injection not allowed. " +
                                                "component=[foo] property=[bar]");
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"),
                              false);
    }

    @Test
    public void AutoWireでない場合かつstaticインジェクションを許容する場合_staticなプロパティにインジェクションされること() {
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-non-autowire.xml"),
                              true);
        Bar bar = sut.getComponentByName("bar");
        Bar barOfFoo = Foo.getBar();
        assertThat("コンポーネントfooのstaticプロパティにコンポーネントbarが設定されること",
                   bar, is(sameInstance(barOfFoo)));
    }

    @Test
    public void AutoWireでない場合_デフォルトではstaticプロパティにはインジェクションされず例外が発生すること() {
        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("static property injection not allowed. " +
                                                "component=[foo] property=[bar]");
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-non-autowire.xml"),
                              false);
    }

    @Test
    public void SystemPropertyに許可設定がない場合_staticなプロパティにはインジェクションされず例外が発生すること() {
        System.clearProperty(DiContainer.ALLOW_STATIC_INJECTION_SYSTEM_PROP_NAME);

        expectedException.expect(ContainerProcessException.class);
        expectedException.expectMessage("static property injection not allowed. " +
                                                "component=[foo] property=[bar]");

        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"));
    }

    @Test
    public void SystemProperty経由でstaticインジェクションを許可できること() {
        System.setProperty(DiContainer.ALLOW_STATIC_INJECTION_SYSTEM_PROP_NAME, "true");
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"));
        SystemRepository.load(sut);
        Bar bar = SystemRepository.get("bar");
        Bar barOfFoo = Foo.getBar();
        assertThat("コンポーネントfooのstaticプロパティにautowireでコンポーネントbarが設定されること",
                   bar, is(sameInstance(barOfFoo)));
    }

}
