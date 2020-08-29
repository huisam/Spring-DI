package core.di.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class BeanScanners implements Scanner<Class<?>> {

    private final BeanScanner beanScanner;
    private final ConfigurationBeanScanner configurationBeanScanner;

    private final Set<Class<?>> preInstantiateClasses = new HashSet<>();

    public BeanScanners(Object... basePackages) {
        this.beanScanner = new BeanScanner(basePackages);
        this.configurationBeanScanner = new ConfigurationBeanScanner(basePackages);
    }

    public Set<Class<?>> scan() {
        final Set<Class<?>> scannedBeanClasses = beanScanner.scan();
        final Set<Class<?>> scannedConfigurationBeanClasses = configurationBeanScanner.scan();

        this.preInstantiateClasses.addAll(scannedBeanClasses);
        for (Class<?> configurationBeanClass : scannedConfigurationBeanClasses) {
            if (this.preInstantiateClasses.contains(configurationBeanClass)) {
                throw new IllegalStateException("Already Created Bean - " + configurationBeanClass.getSimpleName());
            }
            this.preInstantiateClasses.add(configurationBeanClass);
        }

        return Collections.unmodifiableSet(preInstantiateClasses);
    }

    public Class<?>[] getParameterTypesForInstantiation(Class<?> clazz) {
        final Class<?> preInstantiateBean = BeanInstantiateUtils.findConcreteClass(clazz, preInstantiateClasses);
        if (containsOnBeanScanner(preInstantiateBean)) {
            final Constructor<?> injectedConstructor = BeanInstantiateUtils.getInjectedConstructor(preInstantiateBean);
            if (injectedConstructor == null) {
                return new Class<?>[0];
            }

            return injectedConstructor.getParameterTypes();
        }

        if (containsOnConfigurationBeanScanner(preInstantiateBean)) {
            final Method beanCreationMethod = configurationBeanScanner.getBeanCreationMethod(preInstantiateBean);
            return beanCreationMethod.getParameterTypes();
        }

        return new Class<?>[0];
    }

    public Object instantiate(Class<?> clazz, Object... parameterInstances) {
        final Class<?> preInstantiateBean = BeanInstantiateUtils.findConcreteClass(clazz, preInstantiateClasses);
        if (containsOnBeanScanner(preInstantiateBean)) {
            final Constructor<?> injectedConstructor = BeanInstantiateUtils.getInjectedConstructor(preInstantiateBean);
            if (injectedConstructor == null) {
                return BeanUtils.instantiateClass(preInstantiateBean);
            }

            return BeanUtils.instantiateClass(injectedConstructor, parameterInstances);
        }

        if (containsOnConfigurationBeanScanner(preInstantiateBean)) {
            final Method beanCreationMethod = configurationBeanScanner.getBeanCreationMethod(preInstantiateBean);
            return BeanInstantiateUtils.invokeMethod(beanCreationMethod, parameterInstances);
        }

        throw new IllegalStateException("illegal preInstantiate Beans - " + clazz.getSimpleName());
    }

    private boolean containsOnConfigurationBeanScanner(Class<?> preInstantiateBean) {
        return this.configurationBeanScanner.contains(preInstantiateBean);
    }

    private boolean containsOnBeanScanner(Class<?> preInstantiateBean) {
        return beanScanner.contains(preInstantiateBean);
    }

    public Class<?> findConcreteClass(Class<?> preInstantiateBean) {
        if (beanScanner.contains(preInstantiateBean)) {
            return beanScanner.findConcreteClass(preInstantiateBean);
        }

        return preInstantiateBean;
    }
}
