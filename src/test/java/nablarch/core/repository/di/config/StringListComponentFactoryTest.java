package nablarch.core.repository.di.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * {@link StringListComponentFactory} の単体テストクラス。
 *
 * @author Tomoyuki Tanaka
 */
public class StringListComponentFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private StringListComponentFactory sut = new StringListComponentFactory();

    @Test
    public void testCreateObjectMethodSplitsValuesAndCreateStringList() {
        sut.setValues("one,two,three,four");

        List<String> actual = sut.createObject();
        assertThat(actual, contains("one", "two", "three", "four"));
    }

    @Test
    public void testThrowsExceptionIfValuesIsNull() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("values must not be null.");

        sut.setValues(null);
        sut.createObject();
    }

    @Test
    public void testReturnsEmptyListIfValuesIsEmpty() {
        sut.setValues("");

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