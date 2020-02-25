package nablarch.core.repository.di.config.externalize;

import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.SimpleComponentDefinitionLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

/**
 * {@link CompositeExternalizedLoader}のテスト。
 *
 * @author Tomoyuki Tanaka
 */
public class CompositeExternalizedLoaderTest {
    private SimpleExternalizedLoader firstLoader = new SimpleExternalizedLoader();
    private SimpleExternalizedLoader secondLoader = new SimpleExternalizedLoader();
    private CompositeExternalizedLoader sut = new CompositeExternalizedLoader(
            Arrays.<ExternalizedComponentDefinitionLoader>asList(firstLoader, secondLoader));
    private DiContainer container = new DiContainer(new SimpleComponentDefinitionLoader());
    private LoadedComponentsBuilder loadedComponentsBuilder = new LoadedComponentsBuilder(container);
    private ComponentValueExtractor extractor = new ComponentValueExtractor(container);

    @Before
    public void setUp() {
        loadedComponentsBuilder.put("one", "ONE(loaded)");
        loadedComponentsBuilder.put("two", "TWO(loaded)");
        loadedComponentsBuilder.put("three", "THREE(loaded)");
    }

    @Test
    public void testDefinitionsLoadedByEachLoadersAreMergedAndReturned() {
        firstLoader.put("four", "FOUR(firstLoader)");
        firstLoader.put("five", "FIVE(firstLoader)");
        secondLoader.put("five", "FIVE(secondLoader)");
        secondLoader.put("six", "SIX(secondLoader)");

        List<ComponentDefinition> definitions = sut.load(container, loadedComponentsBuilder.build());
        Map<String, Object> result = extractor.toMap(definitions);

        assertThat(result.size(), is(3));
        assertThat(result, hasEntry("four", (Object)"FOUR(firstLoader)"));
        assertThat(result, hasEntry("five", (Object)"FIVE(secondLoader)"));
        assertThat(result, hasEntry("six", (Object)"SIX(secondLoader)"));
    }

    @Test
    public void testDefinitionsLoadedByEachLoadersArePutIntoLoadedComponentsEachTime() {
        firstLoader.put("one", "ONE(firstLoader)");
        firstLoader.put("four", "FOUR(firstLoader)");

        sut.load(container, loadedComponentsBuilder.build());

        Map<String, Object> loadedComponentsInFirstLoader = extractor.toMap(firstLoader.getCapturedLoadedComponents());
        assertThat(loadedComponentsInFirstLoader.size(), is(3));
        assertThat(loadedComponentsInFirstLoader, hasEntry("one", (Object)"ONE(loaded)"));
        assertThat(loadedComponentsInFirstLoader, hasEntry("two", (Object)"TWO(loaded)"));
        assertThat(loadedComponentsInFirstLoader, hasEntry("three", (Object)"THREE(loaded)"));

        Map<String, Object> loadedComponentsInSecondLoader = extractor.toMap(secondLoader.getCapturedLoadedComponents());
        assertThat(loadedComponentsInSecondLoader.size(), is(4));
        assertThat(loadedComponentsInSecondLoader, hasEntry("one", (Object)"ONE(firstLoader)"));
        assertThat(loadedComponentsInSecondLoader, hasEntry("two", (Object)"TWO(loaded)"));
        assertThat(loadedComponentsInSecondLoader, hasEntry("three", (Object)"THREE(loaded)"));
        assertThat(loadedComponentsInSecondLoader, hasEntry("four", (Object)"FOUR(firstLoader)"));
    }
}