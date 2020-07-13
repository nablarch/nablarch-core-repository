package nablarch.core.repository.di.config;

import nablarch.core.exception.IllegalConfigurationException;
import nablarch.core.repository.di.ComponentDefinition;
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
        Constructor<?>[] constructors = def.getType().getDeclaredConstructors();
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
            args[i] = getComponent(container, types[i], annotations[i]);
        }
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * コンストラクタの引数にインジェクトするコンポーネントを取得する。
     * {@link ConfigValue}が付与された引数の場合名前でコンポーネントを取得し
     * それ以外の引数は型でコンポーネントを取得する。
     *
     * @param container   DIコンテナ
     * @param type        コンストラクタ引数の型
     * @param annotations コンストラクタ引数に付与されたアノテーション
     * @return インジェクトするコンポーネント
     */
    private Object getComponent(DiContainer container, Class<?> type, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ConfigValue) {
                return getConfigValueComponent(container, (ConfigValue) annotation);
            }
            if (annotation instanceof ComponentRef) {
                return container.getComponentByName(((ComponentRef) annotation).value());
            }
        }
        return container.getComponentByType(type);
    }

    /**
     * {@link ConfigValue}の情報をもとにコンポーネントを取得する。
     *
     * @param container             DIコンテナ
     * @param configValueAnnotation アノテーション
     * @return {@link ConfigValue}の名前で取得したコンポーネント
     */
    private Object getConfigValueComponent(DiContainer container, ConfigValue configValueAnnotation) {
        String name = configValueAnnotation.value();
        Object component = container.getComponentByName(name);
        if (component == null) {
            throw new IllegalConfigurationException("'" + name + "' of configuration value is not found.");
        }
        if (!(component instanceof String)) {
            throw new IllegalConfigurationException("'" + name + "' of configuration value is not a String.");
        }
        return component;
    }
}
