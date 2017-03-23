package nablarch.core.repository.jndi;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.hamcrest.CoreMatchers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * {@link JndiHelper}のテスト
 */
public class JndiHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private JndiHelper sut = new JndiHelper();

    @Test
    public void デフォルトのプロパティファイルの設定を元にBeanをルックアップできること() throws Exception {
        final Bean bean = sut.lookUp("nablarch_test");

        assertThat(bean, hasProperty("name", is("abcdefg")));
    }

    @Test
    public void デフォルトのリソース名を設定した場合そのBeanがルックアップできること() throws Exception {
        sut.setJndiResourceName("default");
        final Bean bean = sut.lookUp();

        assertThat(bean, hasProperty("name", is("12345")));
    }

    @Test
    public void 指定したプロパティの情報を使ってBeanがルックアップ出来ること() throws Exception {

        final Map<String, String> property = new HashMap<String, String>();
        property.put("java.naming.factory.initial", "com.sun.jndi.fscontext.RefFSContextFactory");
        property.put("java.naming.provider.url", "file:./src/test/java/nablarch/core/repository/jndi");
        sut.setJndiProperties(property);
        sut.setJndiResourceName("bean");

        final Bean bean = sut.lookUp();

        assertThat(bean, hasProperty("name", is("aaaa")));
    }

    @Test
    public void 存在しないリソースを指定した場合例外が送出されること() throws Exception {
        final Map<String, String> property = new HashMap<String, String>();
        property.put("java.naming.factory.initial", "com.sun.jndi.fscontext.RefFSContextFactory");
        property.put("java.naming.provider.url", "file:./src/test/java/nablarch/core/repository/jndi");
        sut.setJndiProperties(property);
        
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("looking up a resource in JNDI failed. resource name=[notfound] JNDI Properties={");
        expectedException.expectMessage("java.naming.provider.url=file:./src/test/java/nablarch/core/repository/jndi");
        expectedException.expectMessage("java.naming.factory.initial=com.sun.jndi.fscontext.RefFSContextFactory");
        expectedException.expectCause(CoreMatchers.<Throwable>instanceOf(NameNotFoundException.class));
        sut.lookUp("notfound");
    }

    public static class Bean {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public static class BeanFactory implements ObjectFactory {

        @Override
        public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx,
                final Hashtable<?, ?> environment) throws
                Exception {

            final Reference reference = (Reference) obj;
            final Bean bean = new Bean();
            bean.setName(((String) reference.get("name")
                                            .getContent()));
            return bean;
        }
    }

}