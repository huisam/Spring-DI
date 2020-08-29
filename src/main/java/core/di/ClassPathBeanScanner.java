package core.di;

import com.google.common.collect.Sets;
import core.annotation.AnnotationScanner;
import core.annotation.Component;
import core.di.factory.BeanFactory;
import core.di.factory.DefaultBeanDefinition;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

@NoArgsConstructor
public class ClassPathBeanScanner implements BeanScanner<Class<?>> {

    private static final Class<Component> COMPONENT_ANNOTATION = Component.class;

    private BeanFactory beanFactory;

    public ClassPathBeanScanner(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Set<Class<?>> scan(Object... basePackage) {
        final Set<Class<? extends Annotation>> componentAnnotations = getComponentAnnotations();
        final Set<Class<?>> preInstantiateBeans = getTypeAnnotatedWith(componentAnnotations, basePackage);

        for (Class<?> clazz : preInstantiateBeans) {
            this.beanFactory.registerBeanDefinition(clazz, new DefaultBeanDefinition(clazz));
        }
        return preInstantiateBeans;
    }

    private Set<Class<? extends Annotation>> getComponentAnnotations() {
        final AnnotationScanner annotationScanner = new AnnotationScanner();

        return annotationScanner.scan(COMPONENT_ANNOTATION);
    }

    private Set<Class<?>> getTypeAnnotatedWith(Set<Class<? extends Annotation>> annotations, Object[] basePackage) {
        final Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> annotatedClass = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : annotations) {
            annotatedClass.addAll(reflections.getTypesAnnotatedWith(annotation, true));
        }
        return annotatedClass;
    }
}
