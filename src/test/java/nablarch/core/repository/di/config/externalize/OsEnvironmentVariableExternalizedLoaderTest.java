package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.SimpleComponentDefinitionLoader;
import nablarch.core.repository.test.OnMemoryLogWriter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link OsEnvironmentVariableExternalizedLoader}のテスト。
 *
 * @author Tomoyuki Tanaka
 */
public class OsEnvironmentVariableExternalizedLoaderTest {
    private OsEnvironmentVariableExternalizedLoader sut;
    private DiContainer container = new DiContainer(new SimpleComponentDefinitionLoader());
    private LoadedComponentsBuilder loadedComponentsBuilder = new LoadedComponentsBuilder(container);
    private ComponentValueExtractor extractor = new ComponentValueExtractor(container);
    private Map<String, String> env = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testOverriderOnlyReturnsOverriddenValue() {
        loadedComponentsBuilder.put("FIRST", "first");
        loadedComponentsBuilder.put("SECOND", "second");
        env.put("SECOND", "override!");
        env.put("THIRD", "no used");
        sut = new OsEnvironmentVariableExternalizedLoader(env);

        Map<String, Object> result = extractor.toMap(sut.load(container, loadedComponentsBuilder.build()));

        assertThat(result.size(), is(1));
        assertThat(result, hasEntry("SECOND", (Object)"override!"));
    }

    @Test
    public void testOverriderReplacesDotAndHyphenWithUnderscoreAndChangesToUppercaseAndSearchesValueFromEnvMap() {
        loadedComponentsBuilder.put("foo.bar-fizz.buzz", "original value");
        loadedComponentsBuilder.put("lower_case", "original value");
        loadedComponentsBuilder.put("dot.and-hyphen", "original value");
        env.put("FOO_BAR_FIZZ_BUZZ", "override!");
        env.put("lower_case", "not override...");
        env.put("DOT.AND-HYPHEN", "not override...");
        sut = new OsEnvironmentVariableExternalizedLoader(env);

        Map<String, Object> result = extractor.toMap(sut.load(container, loadedComponentsBuilder.build()));

        assertThat(result.size(), is(1));
        assertThat(result, hasEntry("foo.bar-fizz.buzz", (Object)"override!"));
    }

    @Test
    public void testLogAnEntryKeyIfOsEnvironmentVariableOverridesLoadedObjects() {
        loadedComponentsBuilder.put("first", "original");
        env.put("FIRST", "override!");
        sut = new OsEnvironmentVariableExternalizedLoader(env);

        sut.load(container, loadedComponentsBuilder.build());

        final List<String> log = OnMemoryLogWriter.getMessages("writer.appLog");

        assertThat(log, Matchers.<String>hasItem(
                containsString("value was overridden by os environment variable. key = first")));
    }

    @Test
    public void testExceptionIsThrownWhenNotStoredComponentCreatorIsOverridden() {
        loadedComponentsBuilder.putBeanComponentCreator("first", Object.class);
        env.put("FIRST", "override!");

        sut = new OsEnvironmentVariableExternalizedLoader(env);

        try {
            sut.load(container, loadedComponentsBuilder.build());
            fail("例外が発生するはず");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(),
                    is("illegal os environment variable was found which tries to override non-literal property." +
                            "os environment variable can override literal value only. key = [first] , previous class = [java.lang.Object]"));
        }
    }

    /*
     * このテストでは、実際のOS環境変数を参照できる必要があります。
     * Mavenから実行する場合は、surefireプラグインのenvironmentVariablesオプションで設定された環境変数が利用されます。
     * 一方で、Intellij IDEAなど他の方法で実行する場合は、個別に実行時の環境変数を指定する必要があります。
     */
    @Test
    public void testOverriderCreatedByDefaultConstructorUsesActualOsEnvironmentVariable() {
        loadedComponentsBuilder.put("TEST_OS_ENV_VAR", "old value");
        sut = new OsEnvironmentVariableExternalizedLoader();

        Map<String, Object> result = extractor.toMap(sut.load(container, loadedComponentsBuilder.build()));

        assertThat(result.size(), is(1));
        assertThat(result, hasEntry("TEST_OS_ENV_VAR", (Object)"override!"));
    }
}