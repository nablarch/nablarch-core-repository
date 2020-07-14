package nablarch.core.repository.di.config.externalize.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンストラクタ引数が設定値のインジェクト対象であることを表すアノテーション。
 *
 * @see nablarch.core.repository.di.config.ConstructorInjectionComponentCreator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigValue {
    /** コンポーネントを取り出すキー */
    String value();
}
