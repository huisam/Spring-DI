package core.di.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import core.di.factory.exception.CircularReferenceException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
public class DefaultBeanFactory implements BeanFactory {
    private Map<Class<?>, Object> beans = Maps.newHashMap();

    private Deque<Class<?>> beanInitializeHistory = new ArrayDeque<>();
    private Map<Class<?>, BeanDefinition> beanDefinitions = Maps.newHashMap();

    @Override
    public void initialize() {
        for (Class<?> clazz : beanDefinitions.keySet()) {
            registerBean(clazz);
        }
    }

    @Override
    public void registerBeanDefinition(Class<?> clazz, BeanDefinition beanDefinition) {
        if (beanDefinitions.containsKey(clazz)) {
            throw new IllegalStateException("Bean Definition is Duplicate - " + clazz.getSimpleName());
        }

        this.beanDefinitions.put(clazz, beanDefinition);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    @Override
    public List<Class<?>> getBeanClasses() {
        return Lists.newArrayList(beans.keySet());
    }

    @Override
    public List<Object> getBeans() {
        return Lists.newArrayList(beans.values());
    }

    private Object registerBean(Class<?> preInstantiateBean) {
        if (beanInitializeHistory.contains(preInstantiateBean)) {
            throw new CircularReferenceException("Circular Reference can't add to Bean Factory: " + preInstantiateBean.getSimpleName());
        }

        if (beans.containsKey(preInstantiateBean)) {
            return beans.get(preInstantiateBean);
        }

        this.beanInitializeHistory.push(preInstantiateBean);
        final Object instance = registerBeanWithInstantiating(preInstantiateBean);
        this.beanInitializeHistory.pop();

        return instance;
    }

    private Object registerBeanWithInstantiating(Class<?> preInstantiateBean) {
        final Class<?> concreteBeanClass = BeanInstantiateUtils.findConcreteClass(preInstantiateBean, beanDefinitions.keySet());
        Object instance = instantiate(concreteBeanClass);

        this.beans.put(preInstantiateBean, instance);
        return instance;
    }

    private Object instantiate(Class<?> concreteBeanClass) {
        final BeanDefinition beanDefinition = this.beanDefinitions.get(concreteBeanClass);
        final Constructor<?> injectedConstructor = beanDefinition.getInjectedConstructor();
        final Method beanCreationMethod = beanDefinition.getBeanCreationMethod();

        if (injectedConstructor == null && beanCreationMethod == null) {
            return BeanUtils.instantiateClass(concreteBeanClass);
        }

        if (injectedConstructor != null) {
            Object[] parameterInstances = getParameterInstances(injectedConstructor);
            return BeanUtils.instantiateClass(injectedConstructor, parameterInstances);
        }

        Object[] parameterInstances = getParameterInstances(beanCreationMethod);
        return BeanInstantiateUtils.invokeMethod(beanCreationMethod, parameterInstances);
    }

    private Object[] getParameterInstances(Executable executable) {
        final Class<?>[] parameterTypes = executable.getParameterTypes();

        final Object[] parameterInstances = new Object[parameterTypes.length];
        for (int i = 0; i < parameterInstances.length; i++) {
            final Class<?> concreteParameterType = BeanInstantiateUtils.findConcreteClass(parameterTypes[i], beanDefinitions.keySet());
            parameterInstances[i] = registerBean(concreteParameterType);
        }
        return parameterInstances;
    }


    public Set<Object> getBeansAnnotatedWith(Class<? extends Annotation> annotation) {
        return this.beans.values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }
}
