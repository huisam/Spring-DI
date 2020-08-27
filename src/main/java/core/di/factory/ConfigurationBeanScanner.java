package core.di.factory;

import core.annotation.Bean;
import core.annotation.Configuration;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ConfigurationBeanScanner implements Scanner<Class<?>> {

    public static final Class<Configuration> CONFIGURATION_CLASS = Configuration.class;
    public static final Class<Bean> BEAN_CLASS = Bean.class;

    private Reflections reflections = new Reflections("");
    private final Map<Class<?>, Method> beanCreationMethods = new HashMap<>();

    public ConfigurationBeanScanner(Object... basePackages) {
        this.reflections = new Reflections(basePackages);
    }

    @Override
    public Set<Class<?>> scan() {
        final Set<Class<?>> configurationClasses = this.reflections.getTypesAnnotatedWith(CONFIGURATION_CLASS, true);
        for (Class<?> configurationClass : configurationClasses) {
            registerBeanMethods(configurationClass);
        }
        return beanCreationMethods.keySet();
    }

    private void registerBeanMethods(Class<?> configurationClass) {
        Set<Method> beanMethods = findBeanCreationMethods(configurationClass);
        for (Method beanMethod : beanMethods) {
            registerBeanCreationMethod(beanMethod);
        }
    }

    private Set<Method> findBeanCreationMethods(Class<?> configurationClass) {
        return Arrays.stream(configurationClass.getMethods())
                .filter(method -> method.isAnnotationPresent(BEAN_CLASS))
                .collect(Collectors.toSet());
    }

    private void registerBeanCreationMethod(Method beanMethod) {
        final Class<?> beanType = beanMethod.getReturnType();
        if (this.beanCreationMethods.containsKey(beanType)) {
            throw new IllegalStateException("Bean has already created - " + beanType.getSimpleName());
        }

        this.beanCreationMethods.put(beanType, beanMethod);
    }

    public Method getBeanCreationMethod(Class<?> preInstantiateBean) {
        return this.beanCreationMethods.get(preInstantiateBean);
    }

    public boolean contains(Class<?> preInstantiateBean) {
        return this.beanCreationMethods.containsKey(preInstantiateBean);
    }
}
