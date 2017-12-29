package nablarch.core.repository.di;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.repository.di.staticprop.Bar;
import nablarch.core.repository.di.staticprop.Foo;
import nablarch.core.repository.test.SystemPropertyResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class StaticInjectionTest {

    private static final String PKG = "nablarch/core/repository/di/staticprop/";
    @Rule
    public SystemPropertyResource sysProps = new SystemPropertyResource();

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
    public void staticインジェクションを許容しない場合_staticなプロパティにはインジェクションされないこと() {
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"),
                              false);
        Bar barOfFoo = Foo.getBar();
        assertThat("コンポーネントfooのstaticプロパティにインジェクションがされていないこと",
                   barOfFoo, is(nullValue()));
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
    public void AutoWireでない場合_デフォルトではstaticプロパティにはインジェクションされないこと() {

        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-non-autowire.xml"),
                              false);
        Bar barOfFoo = Foo.getBar();
        assertThat(barOfFoo, is(nullValue()));
    }

    @Test
    public void SystemPropertyに許可設定がない場合_staticなプロパティにはインジェクションされないこと() {
        System.clearProperty(DiContainer.ALLOW_STATIC_INJECTION_SYSTEM_PROP_NAME);
        sut = new DiContainer(new XmlComponentDefinitionLoader(PKG + "static-property-injection-autowire.xml"));
        SystemRepository.load(sut);
        Foo foo = SystemRepository.get("foo");
        assertThat(foo, is(not(nullValue())));
        Bar barOfFoo = Foo.getBar();
        assertThat(barOfFoo, is(nullValue()));
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
