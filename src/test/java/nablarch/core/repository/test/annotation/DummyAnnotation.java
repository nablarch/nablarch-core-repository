package nablarch.core.repository.test.annotation;

import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * コンストラクタの引数に付与されたアノテーションが{@link ComponentRef}か
 * {@link ConfigValue}以外の場合何もしないパターンのテスト用アノテーション
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DummyAnnotation {

    String value();
}
