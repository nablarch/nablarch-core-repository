package nablarch.core.repository;

import nablarch.core.util.FileUtil;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PropertiesFileLoaderTest {

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testPropertiesFileLoaderString() throws Throwable {

        PropertiesFileLoader loader = new PropertiesFileLoader(createPropertiesFileName());
        Map<String, Object> valueMap = loader.load();

        assertThat(valueMap.get("key1\\"), CoreMatchers.<Object>is("value1\\"));
        assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2"));
    }

    @Test
    public void testPropertiesFileLoaderStringString() throws Throwable {
        File sjisFile = File.createTempFile("PropertiesFileLoaderTest", ".properties");
        sjisFile.deleteOnExit();
        File utf8File = File.createTempFile("PropertiesFileLoaderTest", ".properties");
        utf8File.deleteOnExit();

        FileOutputStream sjisOut = null;
        try {
            sjisOut = new FileOutputStream(sjisFile);
            sjisOut.write("key1=値1\nkey2=値2\n".getBytes("MS932"));
        } finally {
            sjisOut.close();
        }

        FileOutputStream utf8Out = null;
        try {
            utf8Out = new FileOutputStream(utf8File);
            utf8Out.write("key1=値1\nkey2=値2\n".getBytes("UTF-8"));
        } finally {
            utf8Out.close();
        }

        PropertiesFileLoader sjisLoader = new PropertiesFileLoader(sjisFile.toURI()
                                                                   .toString(), "MS932");
        Map<String, Object> sjisValues = sjisLoader.load();
        PropertiesFileLoader utf8Loader = new PropertiesFileLoader(utf8File.toURI()
                                                                   .toString(), "UTF-8");
        Map<String, Object> utf8Values = utf8Loader.load();


        sjisFile.delete();
        utf8File.delete();

        assertEquals("値1", sjisValues.get("key1"));
        assertEquals("値2", sjisValues.get("key2"));
        assertEquals("値1", utf8Values.get("key1"));
        assertEquals("値2", utf8Values.get("key2"));
    }


    @Test
    public void testPropertiesFileLoaderStream() throws Throwable {

        final InputStream resource = FileUtil.getResource(createPropertiesFileName());
        PropertiesFileLoader loader;
        try {
            loader = new PropertiesFileLoader(resource);
            Map<String, Object> valueMap = loader.load();
            assertThat(valueMap.get("key1"), CoreMatchers.<Object>is("value1"));
            assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2"));
        } finally {
            resource.close();
        }
    }

    @Test
    public void testPropertiesFileLoaderStreamString() throws Throwable {
        final String baseFileName = createPropertiesFileName();

        final PropertiesFileLoader ms932 = new PropertiesFileLoader(baseFileName + ".ms932.properties", "MS932");
        final PropertiesFileLoader utf8 = new PropertiesFileLoader(baseFileName + ".utf-8.properties", "utf-8");

        assertThat(ms932.load()
                        .get("key1"), CoreMatchers.<Object>is("値1"));
        assertThat(utf8.load()
                       .get("key1"), CoreMatchers.<Object>is("値1"));
    }

    @Test
    public void testPropertiesFileLoaderFileGrammar() throws Throwable {

        PropertiesFileLoader loader = new PropertiesFileLoader(createPropertiesFileName());
        Map<String, Object> valueMap = loader.load();

        assertThat(valueMap.get("key1"), CoreMatchers.<Object>is("value1 test"));
        assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2-2"));
        assertThat(valueMap.get("key3"), CoreMatchers.<Object>is("value3\\test"));
        assertThat(valueMap.get("key4"), CoreMatchers.<Object>is("value4=test"));
        assertThat(valueMap.get("key5"), CoreMatchers.<Object>is("value5"));
        assertThat(valueMap.get("key6"), CoreMatchers.<Object>is("test test#Propertiesでは解釈される"));
        assertThat(valueMap.get("key7"), CoreMatchers.<Object>is("test #test"));
        assertThat(valueMap.get("key8"), CoreMatchers.<Object>is(" "));
        assertThat(valueMap.containsKey("novalue"), is(true));

        for (String key : valueMap.keySet()) {
            if (key.contains("comment_key")) {
                fail("コメント行の中身が解釈されている");
            }
        }
    }

    @Test
    public void testPropertiesFileLoaderFail() throws Throwable {
        PropertiesFileLoader loader = new PropertiesFileLoader(createPropertiesFileName(), "unkown encoding");
        try {
            loader.load();
            fail("例外が発生するはず");
        } catch (RuntimeException re) {
            // OK
        }
    }

    /**
     * 存在しないファイルパスを指定。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotExistsPropertiesFile() {
        File file = new File("notFound.properties");
        PropertiesFileLoader loader = new PropertiesFileLoader(file.toURI()
                                                           .toString(),
                "utf-8");
        loader.load();
    }

    /**
     * encodingがnullの場合（設定されていない場合）に、UTF-8が固定で使用されることのテスト。
     */
    @Test
    public void testDefaultEncodingRead() throws Exception {
        PropertiesFileLoader loader = new PropertiesFileLoader(createPropertiesFileName(), null);
        Map<String, Object> valueMap = loader.load();
        assertThat(valueMap.get("key"), CoreMatchers.<Object>is("あいうえお"));
    }

    @Test
    public void testSurrogatePair() {
        final PropertiesFileLoader sut = new PropertiesFileLoader(createPropertiesFileName());
        final Map<String, Object> result = sut.load();

        assertThat("propertiesファイル内の値にサロゲートペアが使用できること",
                result, Matchers.<String, Object>hasEntry("key", "[\ud840\udc0b\uD83C\uDF63]"));
        assertThat("properties内のキー値にサロゲートペアが使用できること",
                result, Matchers.<String, Object>hasEntry("\ud840\udc0b\uD83C\uDF63", "値"));
    }

    private String createPropertiesFileName() {
        return "classpath:" + getClass()
                .getName()
                .replace('.', '/')
                + '/'
                + testName.getMethodName()
                + ".properties";
    }

}
