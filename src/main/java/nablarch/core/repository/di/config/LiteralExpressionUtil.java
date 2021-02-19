package nablarch.core.repository.di.config;

import nablarch.core.log.Logger;
import nablarch.core.log.LoggerManager;
import nablarch.core.repository.di.ConfigurationLoadException;
import nablarch.core.repository.di.DiContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * リテラル表現を解決するユーティリティクラス。
 * @author Koichi Asano
 */
public final class LiteralExpressionUtil {

    /**
     * 文字列分割のパターン。
     */
    static final String STRING_ARRAY_SEPARATE_PATTERN = ",";
    /**
     * ロガー。
     */
    static final Logger LOGGER = LoggerManager
            .get(LiteralExpressionUtil.class);
    /**
     * 文字列のパターン。
     */
    private static final Pattern VARIABLE_FIND_PATTERN = Pattern.compile("(\\$\\{[^}]*\\})");
    /**
     * ブレースをとりのぞくパターン。
     */
    private static final Pattern STRIP_BRACE = Pattern.compile("\\$\\{(.*)\\}");

    /**
     * 隠蔽コンストラクタ。
     */
    private LiteralExpressionUtil() {
        
    }

    /**
     * リテラル表現をオブジェクトに変換する。
     * リテラル表現に現れる変数をDiContainerを使用して文字列に置き換え、置き換え後の文字列をオブジェクトに変換する。
     * 
     * @param container コンテナ
     * @param literal 値のリテラル表現
     * @param type 値の型
     * @return 変換後のオブジェクト
     */
    static Object convertLiteralExpressionToObject(DiContainer container, String literal, Class<?> type) {
        String resolved = resolveVariable(container, literal);
        Object converted = convertTo(resolved, type);
        return converted;
    }

    /**
     * リテラルに含まれる ${ } にはさまれた変数部分をコンテナが持つ文字列設定で置き換える。
     * 
     * @param container コンテナ
     * @param literal 文字列表現
     * @return 変数を解決した文字列
     */
    static String resolveVariable(DiContainer container, String literal) {
        StringBuilder builder = new StringBuilder(literal);
        Matcher matcher = VARIABLE_FIND_PATTERN.matcher(literal);
        while (matcher.find()) {
            String group = matcher.group(1);
            Matcher keyMatcher = STRIP_BRACE.matcher(group);
            keyMatcher.matches();
            String key = keyMatcher.group(1);
            
            Object value = container.getComponentByName(key);
            if (value == null) {
                if (isAllowEmptyValue(container)) {
                    logWarn("property value was not found."
                            + " parameter = " + group);
                    continue;
                } else {
                    throw new ConfigurationLoadException("property value was not found."
                            + " parameter = " + group);
                }
            }
            if (!(value instanceof String)) {
                logWarn("property type was not string."
                        + " parameter = " + group);
                continue;
            }
            String valueStr = (String) value;
            
            // ${xxx} を置き換える
            int pos = builder.indexOf(group);
            builder.delete(pos, pos + group.length());
            builder.insert(pos, valueStr);
        }
        
        return builder.toString();
    }

    /**
     * リテラル表現に合致する環境依存値が取得できないことを許容するか。
     * {@link LiteralExpressionUtil#resolveVariable(DiContainer, String)}では
     * リテラル表現に合致する環境依存値がDIコンテナから取得できない場合、例外が発生する。
     * 後方互換性を維持するするため、環境依存値{@literal "nablarch.diContainer.allowEmptyValue"}に
     * {@code true}を設定することで、リテラル表現に合致する環境依存値が取得できない場合にも処理を続行する。
     * その場合、リテラル表現の解決は行われず ${hoge} のようなリテラル表現がそのまま設定値として採用される。
     * {@literal "nablarch.diContainer.allowEmptyValue"}の後方互換性維持以外の目的での使用は推奨しない。
     * @param container DIコンテナ
     * @return 許容する場合 {@code true} 、許容しない場合 {@code false}
     */
    private static boolean isAllowEmptyValue(DiContainer container) {
        Object allowEmptyValue = container.getComponentByName("nablarch.diContainer.allowEmptyValue");
        return allowEmptyValue instanceof String
                && Boolean.parseBoolean((String) allowEmptyValue);
    }

    /**
     * 文字列表現をオブジェクトに変換する。
     * 
     * @param value 文字列表現
     * @param type 変換後の型
     * @return 変換したオブジェクト
     */
    private static Object convertTo(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == Boolean.class) {
            return Boolean.valueOf(value);
        } else if (type == int.class) {
            return Integer.valueOf(value);
        } else if (type == Integer.class) {
            return Integer.valueOf(value);
        } else if (type == long.class) {
            return Long.valueOf(value);
        } else if (type == Long.class) {
            return Long.valueOf(value);
        } else if (type.isArray()) {
            Class<?> arrayType = type.getComponentType();
            if (arrayType == String.class) {
                String[] values = splitString(value);
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim();
                }
                return values;
            } else if (arrayType == int.class) {
    
                String[] values = splitString(value);
                int[] returnValue = new int[values.length];
                String currentValue = null;
    
                try {
                    for (int i = 0; i < values.length; i++) {
                        currentValue = values[i].trim();
                        returnValue[i] = Integer.valueOf(currentValue);
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationLoadException("property value conversion failed."
                            + " class name = " + type.getName()
                            + " ,value = " + currentValue
                            , e);
                }
                return returnValue;
            } else if (arrayType == Integer.class) {
                String[] values = splitString(value);
                Integer[] returnValue = new Integer[values.length];
                String currentValue = null;
    
                try {
                    for (int i = 0; i < values.length; i++) {
                        currentValue = values[i].trim();
                        returnValue[i] = Integer.valueOf(currentValue);
                    }
                } catch (NumberFormatException e) {
                    throw new ConfigurationLoadException("property value conversion failed."
                            + " class name = " + type.getName()
                            + " ,value = " + currentValue
                            , e);
                }
                return returnValue;
                
            }
        } 
        
        throw new ConfigurationLoadException("property type was not supported."
                + " class name = " + type.getName());
    }

    /**
     * 文字列をカンマで分割する。
     * 
     * @param value 分割する文字列
     * @return 分割した結果の文字列の配列
     */
    static String[] splitString(String value) {
        String[] values;
        if (value.endsWith(",")) {
            String[] split = value.split(STRING_ARRAY_SEPARATE_PATTERN);
            values = new String[split.length + 1];
            System.arraycopy(split, 0, values, 0, split.length);
            values[values.length - 1] = "";
        } else {
            values = value.split(STRING_ARRAY_SEPARATE_PATTERN);
        }
        return values;
    }

    /**
     * ワーニングレベルのログを出力する。
     * 
     * @param message ログのメッセージ
     */
    private static void logWarn(String message) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.logWarn(message);
        }
    }
}
