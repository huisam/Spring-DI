package core.di;

import com.google.common.collect.Sets;
import core.annotation.Bean;
import core.annotation.Configuration;
import core.di.factory.BeanFactory;
import core.di.factory.DefaultBeanDefinition;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigurationBeanScanner implements BeanScanner<Class<?>> {

    public static final Class<Configuration> CONFIGURATION_CLASS = Configuration.class;
    public static final Class<Bean> BEAN_CLASS = Bean.class;

    private final BeanFactory beanFactory;

    @Override
    public Set<Class<?>> scan(Object... basePackage) {
        final Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> preInstantiateBeans = Sets.newHashSet();

        final Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(CONFIGURATION_CLASS, true);
        for (Class<?> configurationClass : configurationClasses) {
            final Set<Method> beanMethods = findBeanCreationMethods(configurationClass);
            registerBeanMethods(beanMethods);

            preInstantiateBeans.addAll(getReturnTypeOf(beanMethods));
        }

        return preInstantiateBeans;
    }

    private void registerBeanMethods(Set<Method> beanMethods) {
        for (Method beanMethod : beanMethods) {
            final Class<?> beanType = beanMethod.getReturnType();
            this.beanFactory.registerBeanDefinition(beanType, new DefaultBeanDefinition(beanType, beanMethod));
        }
    }

    private Set<Method> findBeanCreationMethods(Class<?> configurationClass) {
        return Arrays.stream(configurationClass.getMethods())
                .filter(method -> method.isAnnotationPresent(BEAN_CLASS))
                .collect(Collectors.toSet());
    }

    private Collection<? extends Class<?>> getReturnTypeOf(Set<Method> methods) {
        return methods.stream()
                .map(Method::getReturnType)
                .collect(Collectors.toSet());
    }

}
