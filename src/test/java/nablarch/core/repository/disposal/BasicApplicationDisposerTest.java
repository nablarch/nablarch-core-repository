package nablarch.core.repository.disposal;

import nablarch.core.repository.test.OnMemoryLogWriter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
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
    public void testDisposeListInReverseOrder() {
        final List<String> disposedOrder = new ArrayList<String>();

        sut.setDisposableList(Arrays.<Disposable>asList(
            new Disposable() {
                @Override
                public void dispose() throws Exception {
                    disposedOrder.add("third");
                }
            },
            new Disposable() {
                @Override
                public void dispose() throws Exception {
                    disposedOrder.add("second");
                }
            },
            new Disposable() {
                @Override
                public void dispose() throws Exception {
                    disposedOrder.add("first");
                }
            }
        ));

        sut.dispose();

        assertThat(disposedOrder, contains("first", "second", "third"));
    }

    @Test
    public void testAddDisposable() {
        sut.addDisposable(disposable2);
        sut.addDisposable(disposable3);
        sut.addDisposable(disposable4);

        sut.dispose();

        assertThat(disposable1.invoked, is(false));
        assertThat(disposable2.invoked, is(true));
        assertThat(disposable3.invoked, is(true));
        assertThat(disposable4.invoked, is(true));
    }

    @Test
    public void testAddDisposableAfterSetDisposableList() {
        sut.setDisposableList(Arrays.<Disposable>asList(disposable1, disposable2));
        sut.addDisposable(disposable4);

        sut.dispose();

        assertThat(disposable1.invoked, is(true));
        assertThat(disposable2.invoked, is(true));
        assertThat(disposable3.invoked, is(false));
        assertThat(disposable4.invoked, is(true));
    }

    @Test
    public void testLogThrownExceptionAndContinueNextDisposable() {
        sut.addDisposable(new ErrorDisposable(new NullPointerException("test NullPointerException")));
        sut.addDisposable(new ErrorDisposable(new IllegalArgumentException("test IllegalArgumentException")));
        sut.addDisposable(disposable1);

        sut.dispose();

        OnMemoryLogWriter.assertLogContains("writer.appLog",
                "Failed to dispose component (disposable=nablarch.core.repository.disposal.BasicApplicationDisposerTest$ErrorDisposable",
                "test NullPointerException",
                "Failed to dispose component (disposable=nablarch.core.repository.disposal.BasicApplicationDisposerTest$ErrorDisposable",
                "test IllegalArgumentException");
        assertThat(disposable1.invoked, is(true));
    }

    @Test
    public void testDisposeIsIgnoredAfterFirstTime() {
        CountUpMockDisposable disposable = new CountUpMockDisposable();

        sut.addDisposable(disposable);

        // first time
        sut.dispose();

        assertThat(disposable.count, is(1));

        // after first time
        sut.dispose();
        sut.dispose();
        sut.dispose();
        sut.dispose();

        assertThat(disposable.count, is(1));
    }

    private static class MockDisposable implements Disposable {
        private boolean invoked;

        @Override
        public void dispose() throws Exception {
            invoked = true;
        }
    }

    private static class CountUpMockDisposable implements Disposable {
        private int count;

        @Override
        public void dispose() throws Exception {
            count++;
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