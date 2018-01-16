package nablarch.core.repository;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 設定ファイルから文字列の設定値を読み込むクラス。
 *
 * propertiesファイルをjava.util.Propertiesを使ってloadするクラス。
 *
 */
@Published(tag = "architect")
public class PropertiesFileLoader implements ObjectLoader {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager.get(PropertiesFileLoader.class);

    /**
     * 設定ファイルのデフォルトエンコーディング。
     */
    private static final String DEFAULT_PROPERTIES_FILE_ENCODING = "UTF-8";

    /**
     * 入力ファイル。
     */
    private String url;
    /**
     * 入力ストリーム。
     */
    private InputStream inStream;

    /**
     * 入力ストリームのエンコーディング。
     */
    final String encoding;

    /**
     * コンストラクタ。
     *
     * @param url ロードするファイル。
     */
    public PropertiesFileLoader(String url) {
        this(url, null);
    }

    /**
     * コンストラクタ。
     * @param url ロードするファイルを表すURL表現。
     * @param encoding ファイルのエンコーディング。
     */
    public PropertiesFileLoader(String url, String encoding) {
        this.url = url;
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc} <br/>
     *
     * PropertiesFileLoaderでは、プロパティファイルに書かれたキーと値の組合せを
     * Propertiesでloadしてkey&valueの文字列としてMapに格納して返す。
     */
    @Override
    public Map<String, Object> load() {
        Map<String, Object> values = new HashMap<String, Object>();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug("load environment properties file."
                    + " file = " + url);
        }
        if (url != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.logTrace(" properties file opened. "
                        + " url = " + url + "");
            }
            if (inStream == null) {
                inStream = FileUtil.getResource(url);
            }
        }

        String propertiesFileEncoding;
        if (this.encoding != null) {
            propertiesFileEncoding = this.encoding;
        } else {
            propertiesFileEncoding = DEFAULT_PROPERTIES_FILE_ENCODING;
        }

        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new InputStreamReader(inStream,
                    propertiesFileEncoding));

            Properties prop = new Properties();
            prop.load(reader);
            for (Entry<Object, Object> e : prop.entrySet()) {
                values.put((String) e.getKey(),e.getValue());
            }

        } catch (IOException e) {
            // readFile のエラーなので、到達不能です。
            throw new RuntimeException(
                    "properties file read failed.", e);
        } finally {
            FileUtil.closeQuietly(reader);

            if (url != null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.logTrace(" properties file closed. ");
                }
            }
        }

        return values;
    }

}
