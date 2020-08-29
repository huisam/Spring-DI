package core.di.factory;

import com.google.common.collect.Maps;
import core.di.factory.abnormal.CircularReferenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class BeanFactory {
    private final BeanScanners beanScanners;
    private Set<Class<?>> preInstantiateBeans;

    private Map<Class<?>, Object> beans = Maps.newHashMap();

    private Deque<Class<?>> beanInitializeHistory = new ArrayDeque<>();

    public BeanFactory(BeanScanners beanScanners) {
        this.beanScanners = beanScanners;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() {
        this.preInstantiateBeans = beanScanners.scan();
        for (Class<?> preInstantiateBean : preInstantiateBeans) {
            instantiate(preInstantiateBean);
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
        final Class<?>[] parameterTypes = beanScanners.getParameterTypesForInstantiation(preInstanticateBean);
        if (ArrayUtils.isEmpty(parameterTypes)) {
            return registerBeanWithIntantiating(preInstanticateBean);
        }

        final Object[] parametersInstances = new Object[parameterTypes.length];
        for (int i = 0; i < parametersInstances.length; i++) {
            final Class<?> concreteParameterTypes = beanScanners.findConcreteClass(parameterTypes[i]);
            parametersInstances[i] = instantiate(concreteParameterTypes);
        }

        return registerBeanWithIntantiating(preInstanticateBean, parametersInstances);
    }

    private Object registerBeanWithIntantiating(Class<?> preInstanticateBean, Object... parametersInstances) {
        final Object instance = beanScanners.instantiate(preInstanticateBean, parametersInstances);

        this.beans.put(preInstanticateBean, instance);
        return instance;
    }

    public Set<Object> getBeansAnnotatedWith(Class<? extends Annotation> annotation) {
        return this.beans.values().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }
}
