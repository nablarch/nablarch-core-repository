package nablarch.core.repository.di.config.externalize.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link nablarch.core.repository.SystemRepository}登録対象のコンポーネントを表すアノテーション。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemRepositoryComponent {
    /** コンポーネント名 */
    String name() default "";

    /** コンポーネント名をクラスで指定する */
    Class<?> nameFromType() default SystemRepositoryComponent.class;
}
