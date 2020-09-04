package nablarch.core.repository.disposal;

import nablarch.core.repository.test.OnMemoryLogWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link DisposableAdaptor}の単体テスト。
 * @author Tanaka Tomoyuki
 */
public class DisposableAdaptorTest {

    @Before
    public void setUp() {
        OnMemoryLogWriter.clear();
    }

    @Test
    public void testDisposeDelegatesToClose() throws Exception {
        DisposableAdaptor sut = new DisposableAdaptor();

        MockCloseable target = new MockCloseable();
        sut.setTarget(target);

        sut.dispose();

        assertThat(target.closed, is(true));
    }

    @Test
    public void testLogMessageIfThrowsExceptionAtClose() throws Exception {
        DisposableAdaptor sut = new DisposableAdaptor();

        ErrorMockCloseable target = new ErrorMockCloseable(new IOException("test IOException"));
        sut.setTarget(target);

        sut.dispose();

        OnMemoryLogWriter.assertLogContains("writer.appLog",
                "Failed to close target (target=nablarch.core.repository.disposal.DisposableAdaptorTest$ErrorMockCloseable@",
                "test IOException");
    }

    private static class MockCloseable implements Closeable {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    private static class ErrorMockCloseable implements Closeable {
        private final IOException exception;

        private ErrorMockCloseable(IOException exception) {
            this.exception = exception;
        }

        @Override
        public void close() throws IOException {
            throw exception;
        }
    }
}