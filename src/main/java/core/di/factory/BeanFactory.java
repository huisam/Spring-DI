package core.di.factory;

import com.google.common.collect.Maps;
import core.di.factory.abnormal.CircularReferenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BeanFactory {
    private Set<Class<?>> preInstanticateBeans;

    private Map<Class<?>, Object> beans = Maps.newHashMap();

    private Deque<Class<?>> beanInitializeHistory = new ArrayDeque<>();

    public BeanFactory(Set<Class<?>> preInstanticateBeans) {
        this.preInstanticateBeans = preInstanticateBeans;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() {
        for (Class<?> preInstanticateBean : preInstanticateBeans) {
            instantiate(preInstanticateBean);
        }
    }



    private Object instantiate(Class<?> preInstanticateBean) {
        if (beanInitializeHistory.contains(preInstanticateBean)) {
            throw new CircularReferenceException("Circular Reference can't add to Bean Factory: " + preInstanticateBean.getSimpleName());
        }

        if (beans.containsKey(preInstanticateBean)) {
            return beans.get(preInstanticateBean);
        }

        this.beanInitializeHistory.push(preInstanticateBean);
        final Object instance = instantiateWith(preInstanticateBean);
        this.beanInitializeHistory.pop();
        return instance;
    }

    private Object instantiateWith(Class<?> preInstanticateBean) {
        final Constructor<?> injectedConstructor = BeanInstantiateUtils.getInjectedConstructor(preInstanticateBean);
        if (injectedConstructor == null) {
            return instantiateWithDefaultConstructor(preInstanticateBean);
        }

        final Class<?>[] parameterTypes = injectedConstructor.getParameterTypes();
        final Object[] parametersInstances = new Object[parameterTypes.length];
        for (int i = 0; i < parametersInstances.length; i++) {
            final Class<?> concreteParameterTypes = BeanInstantiateUtils.findConcreteClass(parameterTypes[i], preInstanticateBeans);
            parametersInstances[i] = instantiate(concreteParameterTypes);
        }

        final Object instance = BeanUtils.instantiateClass(injectedConstructor, parametersInstances);
        this.beans.put(preInstanticateBean, instance);
        return instance;
    }

    private Object instantiateWithDefaultConstructor(Class<?> preInstanticateBean) {
        final Object instance = BeanUtils.instantiateClass(preInstanticateBean);

        this.beans.put(preInstanticateBean, instance);
        return instance;
    }

    public Set<Object> getBeansAnnotatedWith(Class<? extends Annotation> annotation) {
        return this.beans.values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }
}
