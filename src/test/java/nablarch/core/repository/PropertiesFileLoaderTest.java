package nablarch.core.repository;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

public class PropertiesFileLoaderTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

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
    public void testPropertiesFileLoaderStringEncodingMs932() throws Throwable {
        File sjisFile = temp.newFile("PropertiesFileLoaderTest.properties");

        FileOutputStream sjisOut = null;
        try {
            sjisOut = new FileOutputStream(sjisFile);
            sjisOut.write("key1=値1\nkey2=値2\n".getBytes("MS932"));
        } finally {
            sjisOut.close();
        }

        PropertiesFileLoader sjisLoader = new PropertiesFileLoader(sjisFile.toURI()
                                                                   .toString(), "MS932");
        Map<String, Object> sjisValues = sjisLoader.load();

        assertEquals("値1", sjisValues.get("key1"));
        assertEquals("値2", sjisValues.get("key2"));
    }

    @Test
    public void testPropertiesFileLoaderStringEncodingUtf8() throws Throwable {
        File utf8File = temp.newFile("PropertiesFileLoaderTest.properties");

        FileOutputStream utf8Out = null;
        try {
            utf8Out = new FileOutputStream(utf8File);
            utf8Out.write("key1=値1\nkey2=値2\n".getBytes("UTF-8"));
        } finally {
            utf8Out.close();
        }

        PropertiesFileLoader utf8Loader = new PropertiesFileLoader(utf8File.toURI()
                .toString(), "UTF-8");
        Map<String, Object> utf8Values = utf8Loader.load();

        assertEquals("値1", utf8Values.get("key1"));
        assertEquals("値2", utf8Values.get("key2"));
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
    public void testPropertiesFileLoaderFail() throws Throwable {
        PropertiesFileLoader loader = new PropertiesFileLoader(createPropertiesFileName(), "unknown encoding");
        try {
            loader.load();
            fail("例外が発生するはず");
        } catch (RuntimeException re) {
            // OK
        }
    }

    @Test
    public void testPropertiesFileLoaderUrlNull() throws Throwable {
        try {
            new PropertiesFileLoader(null);
            fail("例外が発生するはず");
        } catch (IllegalArgumentException re) {
            assertThat(re.getMessage(), CoreMatchers.containsString("url is required.  url = null"));
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
