package core.di;

import core.di.factory.BeanFactory;
import core.di.factory.DefaultBeanFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationContext {

    private final BeanFactory beanFactory;

    public ApplicationContext(Object... basePackages) {
        if (ArrayUtils.isEmpty(basePackages)) {
            final ComponentBasePackageScanner componentBasePackageScanner = new ComponentBasePackageScanner();
            basePackages = componentBasePackageScanner.scan().toArray();
        }

        beanFactory = new DefaultBeanFactory();

        final BeanScanners beanScanners = new BeanScanners(beanFactory);
        beanScanners.scan(basePackages);

        beanFactory.initialize();
    }

    public Set<Object> getBeansAnnotatedWith(Class<? extends Annotation> annotation) {
        return this.beanFactory.getBeans().stream()
                .filter(bean -> bean.getClass().isAnnotationPresent(annotation))
                .collect(Collectors.toSet());
    }

    public <T> T getBean(Class<T> requiredType) {
        return beanFactory.getBean(requiredType);
    }

    public List<Class<?>> getBeanClasses() {
        return beanFactory.getBeanClasses();
    }

    public List<Object> getBeans() {
        return beanFactory.getBeans();
    }

}
