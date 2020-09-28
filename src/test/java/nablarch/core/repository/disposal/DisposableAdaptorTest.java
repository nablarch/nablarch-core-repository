package nablarch.core.repository.disposal;

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

    @Test
    public void testDisposeDelegatesToClose() throws Exception {
        DisposableAdaptor sut = new DisposableAdaptor();

        MockCloseable target = new MockCloseable();
        sut.setTarget(target);

        sut.dispose();

        assertThat(target.closed, is(true));
    }

    private static class MockCloseable implements Closeable {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }
}