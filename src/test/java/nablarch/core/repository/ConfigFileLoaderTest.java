package nablarch.core.repository;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

import org.hamcrest.CoreMatchers;

import nablarch.core.util.FileUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class ConfigFileLoaderTest {

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testConfigFileLoaderString() throws Throwable {

        ConfigFileLoader loader = new ConfigFileLoader(createConfigFileName());
        Map<String, Object> valueMap = loader.load();

        assertThat(valueMap.get("key1\\"), CoreMatchers.<Object>is("value1\\"));
        assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2"));
    }

    @Test
    public void testConfigFileLoaderStringString() throws Throwable {
        File sjisFile = File.createTempFile("ConfigFileLoaderTest", ".properties");
        sjisFile.deleteOnExit();
        File utf8File = File.createTempFile("ConfigFileLoaderTest", ".properties");
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

        ConfigFileLoader sjisLoader = new ConfigFileLoader(sjisFile.toURI()
                                                                   .toString(), "MS932");
        Map<String, Object> sjisValues = sjisLoader.load();
        ConfigFileLoader utf8Loader = new ConfigFileLoader(utf8File.toURI()
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
    public void testConfigFileLoaderStream() throws Throwable {

        final InputStream resource = FileUtil.getResource(createConfigFileName());
        ConfigFileLoader loader;
        try {
            loader = new ConfigFileLoader(resource);
            Map<String, Object> valueMap = loader.load();
            assertThat(valueMap.get("key1"), CoreMatchers.<Object>is("value1"));
            assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2"));
        } finally {
            resource.close();
        }
    }

    @Test
    public void testConfigFileLoaderStreamString() throws Throwable {
        final String baseFileName = createConfigFileName();

        final ConfigFileLoader ms932 = new ConfigFileLoader(baseFileName + ".ms932.config", "MS932");
        final ConfigFileLoader utf8 = new ConfigFileLoader(baseFileName + ".utf-8.config", "utf-8");

        assertThat(ms932.load()
                        .get("key1"), CoreMatchers.<Object>is("値1"));
        assertThat(utf8.load()
                       .get("key1"), CoreMatchers.<Object>is("値1"));
    }

    @Test
    public void testConfigFileLoaderFileGrammar() throws Throwable {

        ConfigFileLoader loader = new ConfigFileLoader(createConfigFileName());
        Map<String, Object> valueMap = loader.load();

        assertThat(valueMap.get("key1"), CoreMatchers.<Object>is("value1 test"));
        assertThat(valueMap.get("key2"), CoreMatchers.<Object>is("value2-2"));
        assertThat(valueMap.get("key3"), CoreMatchers.<Object>is("value3\\test"));
        assertThat(valueMap.get("key4"), CoreMatchers.<Object>is("value4=test"));
        assertThat(valueMap.get("key5"), CoreMatchers.<Object>is("value5"));
        assertThat(valueMap.get("key6"), CoreMatchers.<Object>is("test test"));
        assertThat(valueMap.get("key7"), CoreMatchers.<Object>is("test #test"));
        assertThat(valueMap.containsKey("novalue"), is(false));

        for (String key : valueMap.keySet()) {
            if (key.contains("comment_key")) {
                fail("コメント行の中身が解釈されている");
            }
        }
    }

    @Test
    public void testConfigFileLoaderFail() throws Throwable {
        ConfigFileLoader loader = new ConfigFileLoader(createConfigFileName(), "unkown encoding");
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
    public void testNotExistsConfigFile() {
        File file = new File("notFound.config");
        ConfigFileLoader loader = new ConfigFileLoader(file.toURI()
                                                           .toString(),
                "utf-8");
        loader.load();
    }

    /**
     * encodingがnullの場合（設定されていない場合）に、UTF-8が固定で使用されることのテスト。
     */
    @Test
    public void testDefaultEncodingRead() throws Exception {
        ConfigFileLoader loader = new ConfigFileLoader(createConfigFileName(), null);
        Map<String, Object> valueMap = loader.load();
        assertThat(valueMap.get("key"), CoreMatchers.<Object>is("あいうえお"));
    }

    private String createConfigFileName() {
        return "classpath:" + getClass()
                                  .getName()
                                  .replace('.', '/')
                + '/'
                + testName.getMethodName()
                + ".config";
    }

}
