package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ComponentHolder;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.SimpleComponentDefinitionLoader;
import nablarch.core.repository.test.OnMemoryLogWriter;
import nablarch.core.repository.test.SystemPropertyResource;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * {@link SystemPropertyExternalizedLoader}のテスト。
 *
 * @author Tomoyuki Tanaka
 */
public class SystemPropertyExternalizedLoaderTest {
    private DiContainer container = new DiContainer(new SimpleComponentDefinitionLoader());
    private SystemPropertyExternalizedLoader sut = new SystemPropertyExternalizedLoader();
    private LoadedComponentsBuilder loadedComponentsBuilder = new LoadedComponentsBuilder(container);
    private ComponentValueExtractor extractor = new ComponentValueExtractor(container);

    @Rule
    public final SystemPropertyResource systemPropertyResource = new SystemPropertyResource();

    @Before
    public void setUp() {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testSystemPropertiesAreLoaded() {
        System.setProperty("first", "FIRST");
        System.setProperty("second", "SECOND");

        Map<String, Object> result = extractor.toMap(sut.load(container, loadedComponentsBuilder.build()));

        assertThat(result, hasEntry("first", (Object)"FIRST"));
        assertThat(result, hasEntry("second", (Object)"SECOND"));
    }

    @Test
    public void testLogAnEntryKeyAndValuesIfSystemPropertyOverridesLoadedObjects() {
        loadedComponentsBuilder.put("first", "FIRST");
        System.setProperty("first", "OVERRIDE");

        sut.load(container, loadedComponentsBuilder.build());

        final List<String> log = OnMemoryLogWriter.getMessages("writer.appLog");

        assertThat(log, Matchers.<String>hasItem(
                containsString("value was overridden by system property.  "
                        + "key = first, previous value = [FIRST], new value = [OVERRIDE]")));
    }

    @Test
    public void testExceptionIsThrownWhenNotStoredComponentCreatorIsOverridden() {
        loadedComponentsBuilder.putBeanComponentCreator("first", Object.class);
        System.setProperty("first", "OVERRIDE");

        try {
            sut.load(container, loadedComponentsBuilder.build());
            fail("例外が発生するはず");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(),
                is("illegal system property was found which tries to override non-literal property." +
                        "system property can override literal value only. key = [first] , previous class = [java.lang.Object]"));
        }
    }
}