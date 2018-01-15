package nablarch.core.repository;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.util.FileUtil;
import nablarch.core.util.annotation.Published;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 設定ファイルから文字列の設定値を読み込むクラス。
 * 
 * このクラスで使用する特殊文字は '=' '#' '\' の3文字（下記参照）。
 * <dl>
 *         <dt>デリミタ文字（'='）
 *             <dd>デリミタ文字は'='のみで、空白（タブを含む）や":"も文字列の一部とみなす。
 *                 (いわゆるpropertiesファイルとは異なる。）
 *                 但し、キー及び値はそれぞれ前後の空白（タブを含む）をトリミングする。
 *                 (" A B "(スペースAスペースBスペース)という文字列は
 *                 "A B"(AスペースB)となる。キーの'A'と'a'は区別される。)
 *                 デリミタ文字'='で区切られた３つめ以降のトークンは無視する。
 *                 <br>'='をキーまたは値に含めたい場合は前に'\'を付加する。
 *         <dt>コメント文字（'#'）
 *             <dd>コメント文字'#'を使用するとその行の以降の文字列はコメントとみなす。
 *                 '#'によるコメントを除去する処理は行連結の前に行われるので、
 *                 継続行中でも使用可能（下記「使用例」参照）。
 *                 <br>'#'をキーまたは値に含めたい場合は前に'\'を付加する。
 *         <dt>改行文字（'\'）
 *             <dd>キーと値のセットは行末に'\'を指定することによって行をまたがることが可能。
 *                 その場合'\'を除いた文字列と次の行の先頭の空白（タブを含む）を除いた
 *                 文字列を連結する。（'\'を除いた文字列の後方の空白は維持する。）
 *                 <br>キーまたは値の行末に'\'を含めたい場合は前に'\'を付加する。
 *         <dt>エスケープ文字（'\'）
 *             <dd>'\'を記述すると次の１文字を特殊文字ではなく一般文字として扱う。
 *                 <br>'\'をキーまたは値に含めたい場合は前に'\'を付加する。
 * </dl>
 * 読み込むファイルの記述例：<br><pre>
 *  # キー＝"key"、値＝"value"の場合
 *  key = value # comment
 *  key = value = comment
 *
 *  # キー＝"key"、値＝"value1 = value2"の場合
 *  key = value1 \= value2  #comment
 *  key = \
 *      value1 \= value2
 *
 *  # キー＝"key"、値＝"value1,value2,value3"の場合
 *  key =   value1,value2,value3    # comment
 *  key =   value1,\
 *          value2,\
 *          value3 # comment
 *  key =   value1,\    # comment
 *          value2,\    # comment
 *          value3      # comment
 * 
 *  # 下記はNG。
 *  key =   value1,     # comment \
 *          value2,     # comment \
 *          value3      # comment
 * </PRE>
 * <p/>
 * なお、本クラスはデフォルトでは設定ファイルをUTF-8エンコーディングで読み込む。
 * エンコーディングを変更する場合は、ConfigFileクラスのencodingプロパティにエンコーディングを設定してから load() メソッドを呼び出すこと。
 * 
 * @author Koichi Asano 
 * @see nablarch.core.repository.di.config.xml.schema.ConfigFile
 */
@Published(tag = "architect")
public class PropertiesFileLoader implements ObjectLoader {

    /**
     * ロガー。
     */
    private static final Logger LOGGER = LoggerManager.get(PropertiesFileLoader.class);

    /**
     * エスケープ文字＆行連結文字。
     */
    private static final char ESC_CHAR = '\\';

    /**
     * デリミタ文字。
     */
    private static final char DELIMITER_CHAR = '=';

    /**
     * コメント文字。
     */
    private static final char COMMENT_CHAR = '#'; // コメント文字

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
    private String encoding;


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
     * コンストラクタ。
     *
     * @param stream ロードするファイルのストリーム。
     */
    public PropertiesFileLoader(InputStream stream) {
        this.inStream = stream;
    }

    /**
     * コンストラクタ。
     *
     * @param stream ロードするファイルのストリーム。
     * @param encoding ファイルのエンコーディング。
     */
    public PropertiesFileLoader(InputStream stream, String encoding) {
        this.inStream = stream;
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc} <br/>
     *
     * PropertiesFileLoaderでは、プロパティファイルに書かれたキーと値の組合せを
     * そのままMapを返す。
     * このため、値は常に文字列となる。
     */
    public Map<String, Object> load() {
        Map<String, Object> values = new HashMap<String, Object>();
        Properties prop = new Properties();
        BufferedReader reader = null;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.logDebug("load environment properties file."
                    + " file = " + url);
        }
        try {
            if (url != null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.logTrace(" properties file opened. "
                            + " url = " + url + "");
                }
            }

            if (inStream == null) {
                inStream = FileUtil.getResource(url);
            }

            String propertiesFileEncoding;
            if (this.encoding != null) {
                propertiesFileEncoding = this.encoding;
            } else {
                propertiesFileEncoding = DEFAULT_PROPERTIES_FILE_ENCODING;
            }

            reader = new BufferedReader(new InputStreamReader(inStream,
                    propertiesFileEncoding));

            prop.load(reader);
            for (Entry<Object, Object> e : prop.entrySet()) {
                values.put((String) e.getKey(),e.getValue());
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "properties file read failed.", e);
        } catch (IOException e) {
            // readFile のエラーなので、到達不能です。
            throw new RuntimeException(
                    "properties file read failed.", e);
        } finally {
            FileUtil.closeQuietly(reader);

            if (url != null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.logTrace(" properties file closed. "
                            + " url = " + url + "");
                }
            }
        }

        return values;
    }

}
