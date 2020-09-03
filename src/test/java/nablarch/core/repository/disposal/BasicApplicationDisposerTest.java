package nablarch.core.repository.disposal;

import nablarch.core.repository.test.OnMemoryLogWriter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link BasicApplicationDisposer}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class BasicApplicationDisposerTest {
    private BasicApplicationDisposer sut = new BasicApplicationDisposer();
    private MockDisposable disposable1 = new MockDisposable();
    private MockDisposable disposable2 = new MockDisposable();
    private MockDisposable disposable3 = new MockDisposable();
    private MockDisposable disposable4 = new MockDisposable();

    @Before
    public void setUp() {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testSetDisposableList() {
        sut.setDisposableList(Arrays.<Disposable>asList(disposable1, disposable2, disposable3));

        sut.dispose();

        assertThat(disposable1.invoked, is(true));
        assertThat(disposable2.invoked, is(true));
        assertThat(disposable3.invoked, is(true));
        assertThat(disposable4.invoked, is(false));
    }

    @Test
    public void testAddDisposable() {
        sut.add(disposable2);
        sut.add(disposable3);
        sut.add(disposable4);

        sut.dispose();

        assertThat(disposable1.invoked, is(false));
        assertThat(disposable2.invoked, is(true));
        assertThat(disposable3.invoked, is(true));
        assertThat(disposable4.invoked, is(true));
    }

    @Test
    public void testAddDisposableAfterSetDisposableList() {
        sut.setDisposableList(Arrays.<Disposable>asList(disposable1, disposable2));
        sut.add(disposable4);

        sut.dispose();

        assertThat(disposable1.invoked, is(true));
        assertThat(disposable2.invoked, is(true));
        assertThat(disposable3.invoked, is(false));
        assertThat(disposable4.invoked, is(true));
    }

    @Test
    public void testLogThrownExceptionAndContinueNextDisposable() {
        sut.add(new ErrorDisposable(new NullPointerException("test NullPointerException")));
        sut.add(new ErrorDisposable(new IllegalArgumentException("test IllegalArgumentException")));
        sut.add(disposable1);

        sut.dispose();

        OnMemoryLogWriter.assertLogContains("writer.appLog",
                "Failed to dispose component (disposable=nablarch.core.repository.disposal.BasicApplicationDisposerTest$ErrorDisposable",
                "test NullPointerException",
                "Failed to dispose component (disposable=nablarch.core.repository.disposal.BasicApplicationDisposerTest$ErrorDisposable",
                "test IllegalArgumentException");
        assertThat(disposable1.invoked, is(true));
    }

    private static class MockDisposable implements Disposable {
        private boolean invoked;

        @Override
        public void dispose() throws Exception {
            invoked = true;
        }
    }

    private static class ErrorDisposable implements Disposable {

        private final Exception exception;

        private ErrorDisposable(Exception exception) {
            this.exception = exception;
        }

        @Override
        public void dispose() throws Exception {
            throw exception;
        }
    }
}