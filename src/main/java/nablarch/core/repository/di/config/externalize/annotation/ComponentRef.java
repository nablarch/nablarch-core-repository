package nablarch.core.repository.di.config.externalize.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンストラクタ引数がコンポーネント参照によるインジェクト対象であることを表すアノテーション。
 *
 * @see nablarch.core.repository.di.config.ConstructorInjectionComponentCreator
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentRef {

    String value();
}
