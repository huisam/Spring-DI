package core.di.factory;

import com.google.common.collect.Sets;
import core.annotation.Component;
import core.annotation.WebApplication;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class BeanScanner {

    public static final String ANNOTATION_BASE_PACKAGE = "core.annotation";
    public static final Class<WebApplication> WEB_APPLICATION_CLASS = WebApplication.class;
    public static final Class<Component> COMPONENT_ANNOTATION = Component.class;

    private Object[] basePackages;
    private Reflections reflections;

    public BeanScanner(Object... basePackages) {
        this.basePackages = basePackages;
    }

    public Set<Class<?>> scan() {
        this.basePackages = findBasePackages();
        this.reflections = new Reflections(basePackages, new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner());

        final Set<Class<? extends Annotation>> componentAnnotations = getComponentAnnotations();
        return getTypeAnnotatedWith(componentAnnotations);
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

    private Object[] findBasePackages() {
        if (ArrayUtils.isNotEmpty(this.basePackages)) {
            return this.basePackages;
        }

        Reflections allReflections = new Reflections("");
        Object[] allBasePackages = allReflections.getTypesAnnotatedWith(WEB_APPLICATION_CLASS)
                .stream()
                .map(this::findBasePackages)
                .flatMap(Arrays::stream)
                .toArray();

        if (allBasePackages.length == 0) {
            throw new IllegalStateException("Base package is not initialized");
        }
        return allBasePackages;
    }

    private String[] findBasePackages(Class<?> clazz) {
        final String[] basePackages = clazz.getAnnotation(WEB_APPLICATION_CLASS).basePackages();

        return basePackages.length == 0 ? new String[]{clazz.getPackage().getName()} : basePackages;
    }

    private Set<Class<?>> getTypeAnnotatedWith(Set<Class<? extends Annotation>> annotations) {
        Set<Class<?>> annotatedClass = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : annotations) {
            annotatedClass.addAll(reflections.getTypesAnnotatedWith(annotation, true));
        }
        return annotatedClass;
    }
}
