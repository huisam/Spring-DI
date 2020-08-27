package core.di.factory;

import com.google.common.collect.Sets;
import core.annotation.Component;
import lombok.NoArgsConstructor;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class BeanScanner implements Scanner<Class<?>> {

    public static final String ANNOTATION_BASE_PACKAGE = "core.annotation";
    public static final Class<Component> COMPONENT_ANNOTATION = Component.class;

    private Reflections reflections = new Reflections("");

    private Set<Class<?>> preInstantiateBeans;

    public BeanScanner(Object... basePackages) {
        this.reflections = new Reflections(basePackages);
    }

    @Override
    public Set<Class<?>> scan() {
        final Set<Class<? extends Annotation>> componentAnnotations = getComponentAnnotations();
        this.preInstantiateBeans = getTypeAnnotatedWith(componentAnnotations);
        return this.preInstantiateBeans;
    }

    private Set<Class<? extends Annotation>> getComponentAnnotations() {
        final Reflections annotationReflections = new Reflections(ANNOTATION_BASE_PACKAGE);
        final Set<Class<?>> componentClasses = annotationReflections.getTypesAnnotatedWith(COMPONENT_ANNOTATION);

        final Set<Class<? extends Annotation>> annotations = componentClasses.stream()
                .filter(Class::isAnnotation)
                .map(clazz -> (Class<? extends Annotation>) clazz)
                .collect(Collectors.toSet());
        annotations.add(COMPONENT_ANNOTATION);
        return annotations;
    }

    public boolean contains(Class<?> preInstantiateBean) {
        return preInstantiateBeans.contains(preInstantiateBean);
    }

    public Class<?> findConcreteClass(Class<?> preInstantiateBean) {
        return BeanFactoryUtils.findConcreteClass(preInstantiateBean, preInstantiateBeans);
    }

    private Set<Class<?>> getTypeAnnotatedWith(Set<Class<? extends Annotation>> annotations) {
        Set<Class<?>> annotatedClass = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : annotations) {
            annotatedClass.addAll(reflections.getTypesAnnotatedWith(annotation, true));
        }
        return annotatedClass;
    }
}
