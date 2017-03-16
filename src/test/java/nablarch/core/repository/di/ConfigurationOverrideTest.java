package nablarch.core.repository.di;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.hamcrest.CoreMatchers;

import nablarch.core.repository.SystemRepository;
import nablarch.core.repository.di.config.DuplicateDefinitionPolicy;
import nablarch.core.repository.di.config.xml.XmlComponentDefinitionLoader;
import nablarch.core.repository.di.test.Component1;
import nablarch.core.repository.di.test.Component2;
import nablarch.core.repository.test.OnMemoryLogWriter;
import nablarch.core.repository.test.RepositoryInitializer;
import nablarch.core.repository.test.SystemPropertyResource;

import org.junit.Rule;
import org.junit.Test;

/**
 * 設定ファイルに上書き設定があった場合のテスト。
 *
 * @author Koichi Asano
 */
public class ConfigurationOverrideTest {

    @Rule
    public final SystemPropertyResource systemPropertyResource = new SystemPropertyResource();

    /**
     * システムプロパティによる上書きのテスト。
     *
     * @throws Throwable
     */
    @Test
    public void testLoadSystemProperty() throws Throwable {
        System.setProperty("any.key01", "overrided value 01!");
        System.setProperty("any.key03", "1");

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/ConfigurationOverrideTest/testLoadSystemProperty/test.xml");
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        Component1 comp2 = container.getComponentByName("comp2");

        final List<String> log = OnMemoryLogWriter.getMessages("writer.appLog");

        boolean found = false;

        for (String line : log) {
            if (line.contains("value was overridden by system property. "
                    + " key = any.key01"
                    + ", previous value = [value 01!]"
                    + ", new value = [overrided value 01!]"
            )) {
                found = true;
                break;
            }
        }
        assertThat("上書きログが出力されているはず", found, is(true));
        assertThat(comp1.getProp1(), is("overrided value 01!"));
        assertThat(comp1.getIntProp(), is(1));
        assertThat(comp2.getProp1(), is("value 02!"));
    }


    /**
     * コンポーネント設定の上書き(既に設定のあるプロパティの場合)テスト。
     *
     * @throws Throwable
     */
    @Test
    public void testLoadDuplicateDefinitionPolicyOverride() throws Throwable {

        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/ConfigurationOverrideTest/testLoadDuplicateDefinitionPolicyOverride/test.xml",
                DuplicateDefinitionPolicy.OVERRIDE);
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        assertThat(comp1.getProp1(), CoreMatchers.is("string value2"));
    }


    /**
     * コンポーネント設定の上書き(元のコンポーネントに設定していないプロパティの場合)テスト。
     *
     * @throws Throwable
     */
    @Test
    public void testLoadDuplicateDefinitionPolicyOverride2() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/ConfigurationOverrideTest/testLoadDuplicateDefinitionPolicyOverride2/test.xml",
                DuplicateDefinitionPolicy.OVERRIDE);
        DiContainer container = new DiContainer(loader);

        Component1 comp1 = container.getComponentByName("comp1");
        assertThat(comp1.getProp1(), CoreMatchers.is("base value"));
        assertThat(comp1.isBoolProp(), CoreMatchers.is(true));
        assertThat(comp1.getIntProp(), CoreMatchers.is(100));
    }


    /**
     * 同名で別クラスのコンポーネントが登録されていた場合のテスト。
     *
     * @throws Throwable
     */
    @Test
    public void testLoadOverrideConflictConfiguration() throws Throwable {
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/ConfigurationOverrideTest/testLoadOverrideConflictConfiguration/test.xml",
                DuplicateDefinitionPolicy.OVERRIDE);
        DiContainer container = new DiContainer(loader);
        Object comp1 = container.getComponentByName("comp1");

        assertThat(comp1, instanceOf(Component2.class));
    }


    /**
     * システムプロパティでコンポーネントの上書きを使用とした場合、例外が発生するテスト。
     */
    @Test
    public void testLoadSystemPropertyFail() throws Throwable {
        System.setProperty("comp1", "overrided value 01!");
        XmlComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/ConfigurationOverrideTest/testLoadSystemPropertyFail/test.xml");
        try {
            new DiContainer(loader);
            fail("例外が発生するはず");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), containsString("key = [comp1]"));
            assertThat(e.getMessage(), containsString("previous class = [nablarch.core.repository.di.test.Component1]"));
        }
    }


    @Test
    public void testOverride() {
        SystemRepository.clear();
        ComponentDefinitionLoader loader = new XmlComponentDefinitionLoader(
                "nablarch/core/repository/di/config_override/override.xml");
        DiContainer diContainer = new DiContainer(loader);
        SystemRepository.load(diContainer);

        RepositoryInitializer.recreateRepository("nablarch/core/repository/di/config_override/override.xml");
        //        RepositoryInitializer.recreateRepository("nablarch/core/repository/di/config_override/override.xml");
        assertThat(SystemRepository.getString("key1"), is("overridden"));
        assertThat(SystemRepository.getString("key2"), is("original"));
    }


}

