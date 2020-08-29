package core.di.factory;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@Getter
@EqualsAndHashCode(of = "clazz")
public class DefaultBeanDefinition implements BeanDefinition {
    private final Class<?> clazz;
    private final Constructor<?> injectedConstructor;
    private final Method beanCreationMethod;

    public DefaultBeanDefinition(Class<?> clazz) {
        this(clazz, null);
    }

    public DefaultBeanDefinition(Class<?> clazz, Method beanCreationMethod) {
        this.clazz = clazz;
        this.injectedConstructor = BeanInstantiateUtils.getInjectedConstructor(clazz);
        this.beanCreationMethod = beanCreationMethod;
    }
}
