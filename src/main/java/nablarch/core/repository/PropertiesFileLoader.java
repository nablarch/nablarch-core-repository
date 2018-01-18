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
 * propertiesファイルを{@link Properties}を使ってloadするクラス。
 *
 * @author Takao Inaba
 * @see java.util.Properties
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
    private final String url;

    /**
     * 入力ストリームのエンコーディング。
     */
    private final String encoding;

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
     * {@link java.util.Properties}からロードしたkeyとvalueをMapに格納して返す。
     * @return プロパティのkeyとvalueを文字列として格納したMap
     */
    @Override
    public Map<String, Object> load() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug("load environment properties file."
                    + " file = " + url);
        }
        InputStream inStream = null;
        if (url != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.logTrace(" properties file opened. "
                        + " url = " + url);
            }
            inStream = FileUtil.getResource(url);
        } else {
            throw new RuntimeException("url is required. "
                    + " url = null");
        }

        String propertiesFileEncoding;
        if (this.encoding != null) {
            propertiesFileEncoding = this.encoding;
        } else {
            propertiesFileEncoding = DEFAULT_PROPERTIES_FILE_ENCODING;
        }

        Map<String, Object> values = new HashMap<String, Object>();
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
            throw new RuntimeException(
                    "properties file read failed.", e);
        } finally {
            FileUtil.closeQuietly(reader);

            if (url != null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.logTrace(" properties file closed. "
                            + " url = " + url);
                }
            }
        }

        return values;
    }

}
