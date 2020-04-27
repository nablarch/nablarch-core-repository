package nablarch.core.repository.di.config;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * {@link StringListComponentFactory} の単体テストクラス。
 *
 * @author Tomoyuki Tanaka
 */
public class StringListComponentFactoryTest {
    private StringListComponentFactory sut = new StringListComponentFactory();

    @Test
    public void testCreateObjectMethodSplitsValuesAndCreateStringList() {
        sut.setValues("one,two,three,four");

        List<String> actual = sut.createObject();
        assertThat(actual, contains("one", "two", "three", "four"));
    }

    @Test
    public void testReturnEmptyListIfValuesIsNull() {
        sut.setValues(null);

        List<String> actual = sut.createObject();
        assertThat(actual, is(empty()));
    }

    @Test
    public void testListElementsAreTrimmed() {
        sut.setValues("  one  ,\ttwo\t, \tthree\t ");

        List<String> actual = sut.createObject();
        assertThat(actual, contains("one", "two", "three"));
    }
}