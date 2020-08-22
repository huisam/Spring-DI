package core.di.factory;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

@Slf4j
public class BeanFactory {
    private Set<Class<?>> preInstanticateBeans;

    private Map<Class<?>, Object> beans = Maps.newHashMap();

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
        if (beans.containsKey(preInstanticateBean)) {
            return beans.get(preInstanticateBean);
        }

        final Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(preInstanticateBean);
        if (injectedConstructor == null) {
            final Object instance = BeanUtils.instantiateClass(preInstanticateBean);
            this.beans.put(preInstanticateBean, instance);
            return instance;
        }

        final Class<?>[] parameterTypes = injectedConstructor.getParameterTypes();
        final Object[] parametersInstances = new Object[parameterTypes.length];
        for (int i = 0; i < parametersInstances.length; i++) {
            final Class<?> concreteParameterTypes = BeanFactoryUtils.findConcreteClass(parameterTypes[i], preInstanticateBeans);
            parametersInstances[i] = instantiate(concreteParameterTypes);
        }

        final Object instance = BeanUtils.instantiateClass(injectedConstructor, parametersInstances);
        this.beans.put(preInstanticateBean, instance);
        return instance;
    }
}
