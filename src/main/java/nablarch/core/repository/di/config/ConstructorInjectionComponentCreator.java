package nablarch.core.repository.di.config;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.di.ComponentDefinition;
import nablarch.core.repository.di.ContainerProcessException;
import nablarch.core.repository.di.DiContainer;
import nablarch.core.repository.di.config.externalize.annotation.ComponentRef;
import nablarch.core.repository.di.config.externalize.annotation.ConfigValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * コンストラクタインジェクションできるよう拡張した{@link BeanComponentCreator}。
 */
public class ConstructorInjectionComponentCreator extends BeanComponentCreator {

    @Override
    public Object createComponent(DiContainer container, ComponentDefinition def) {
        Constructor<?>[] constructors = def.getType().getConstructors();
        if (constructors.length == 1 && constructors[0].getParameterTypes().length != 0) {
            // 生成時に利用するコンストラクタが特定できるなら、コンストラクタインジェクションを行う
            return createComponentWithConstructorInjection(container, constructors[0]);
        }
        return super.createComponent(container, def);
    }

    /**
     * コンポーネントの生成とコンストラクタインジェクションを行う。
     *
     * @param container   DIコンテナ
     * @param constructor コンストラクタ
     * @return インジェクション済のコンポーネント
     */
    private Object createComponentWithConstructorInjection(DiContainer container, Constructor<?> constructor) {
        final Class<?>[] types = constructor.getParameterTypes();
        final Annotation[][] annotations = constructor.getParameterAnnotations();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            try {
                args[i] = getComponent(container, types[i], annotations[i]);
            } catch (IllegalConfigurationException e) {
                throw newContainerProcessException(constructor, e);
            }
        }
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
            throw newContainerProcessException(constructor, e);
        } catch (IllegalAccessException e) {
            // getConstructors()でpublicなコンストラクタしか扱わないためここにはこない。
            throw newContainerProcessException(constructor, e);
        } catch (InvocationTargetException e) {
            throw newContainerProcessException(constructor, e);
        }
    }

    private ContainerProcessException newContainerProcessException(Constructor<?> constructor, Exception cause) {
        return new ContainerProcessException(
                "component instantiation failed."
                        + " component class name = " + constructor.getDeclaringClass().getName()
                , cause);
    }

    /**
     * コンストラクタの引数にインジェクトするコンポーネントを取得する。
     * {@link ConfigValue}、{@link ComponentRef}が付与された引数の場合名前でコンポーネントを取得
     * それ以外の引数は型でコンポーネントを取得する。
     *
     * @param container   DIコンテナ
     * @param type        コンストラクタ引数の型
     * @param annotations コンストラクタ引数に付与されたアノテーション
     * @return インジェクトするコンポーネント
     */
    private Object getComponent(DiContainer container, Class<?> type, Annotation[] annotations) {
        Object component = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof ConfigValue) {
                if (component != null) {
                    throw new IllegalConfigurationException("both @ConfigValue and @ComponentRef are set.");
                }
                component = getConfigValueComponent(container, type, (ConfigValue) annotation);
            }
            if (annotation instanceof ComponentRef) {
                if (component != null) {
                    throw new IllegalConfigurationException("both @ConfigValue and @ComponentRef are set.");
                }
                component = getReferenceComponent(container, type, (ComponentRef) annotation);
            }
        }
        if (component != null) {
            return component;
        }
        return container.getComponentByType(type);
    }

    /**
     * {@link ConfigValue}の情報をもとに設定値を取得する。
     *
     * @param container  DIコンテナ
     * @param annotation アノテーション
     * @return {@link ConfigValue}に指定した変数を解決した設定値
     */
    private Object getConfigValueComponent(DiContainer container, Class<?> type, ConfigValue annotation) {
        try {
            return LiteralExpressionUtil.convertLiteralExpressionToObject(container, annotation.value(), type);
        } catch (NumberFormatException e) {
            throw new ContainerProcessException("config value is not number. name = " + annotation.value(), e);
        }
    }

    /**
     * {@link ComponentRef}の情報をもとにコンポーネントを取得する。
     *
     * @param container  DIコンテナ
     * @param annotation アノテーション
     * @return {@link ComponentRef}の名前で取得したコンポーネント
     */
    private Object getReferenceComponent(DiContainer container, Class<?> type, ComponentRef annotation) {
        String name = annotation.value();
        Object component = container.getComponentByName(name);
        if (component == null) {
            throw new ContainerProcessException("component name to reference was not found. name = " + name);
        }
        if (!type.isAssignableFrom(component.getClass())) {
            throw new ContainerProcessException("referenced component type mismatch."
                    + " name = [" + name + "]"
                    + " parameter type = [" + type.getName() + "]"
                    + " component type = [" + component.getClass().getName() + "]");
        }
        return component;
    }
}
