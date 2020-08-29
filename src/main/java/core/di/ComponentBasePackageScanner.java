package core.di;

import core.annotation.AnnotationScanner;
import core.annotation.ComponentScan;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentBasePackageScanner implements Scanner<Object> {

    private static final Class<ComponentScan> COMPONENT_SCAN_ANNOTATION = ComponentScan.class;
    private Reflections allReflections = new Reflections("");

    private Set<Object> basePackages = new HashSet<>();

    public ComponentBasePackageScanner(Object... object) {
        this.allReflections = new Reflections(object);
    }

    @Override
    public Set<Object> scan(Object... object) {
        final AnnotationScanner annotationScanner = new AnnotationScanner();
        final Set<Class<? extends Annotation>> scannedAnnotations = annotationScanner.scan(COMPONENT_SCAN_ANNOTATION);

        final Set<Class<?>> classesAnnotatedComponentScan = allReflections.getTypesAnnotatedWith(COMPONENT_SCAN_ANNOTATION, true);

        registerBasePackageOfComponentScan(classesAnnotatedComponentScan);
        registerPackageOfClassesWithOutBasePackage(classesAnnotatedComponentScan);
        registerBasePackageOfAnnotations(scannedAnnotations);

        return new HashSet<>(this.basePackages);
    }

    private void registerBasePackageOfComponentScan(Set<Class<?>> classesAnnotatedComponentScan) {
        for (Class<?> componentScanClass : classesAnnotatedComponentScan) {
            final ComponentScan componentScanAnnotation = componentScanClass.getAnnotation(COMPONENT_SCAN_ANNOTATION);
            final String[] basePackage = componentScanAnnotation.basePackages();

            this.basePackages.addAll(Arrays.asList(basePackage));
        }

    }

    private void registerPackageOfClassesWithOutBasePackage(Set<Class<?>> classesAnnotatedComponentScan) {
        final Set<Class<?>> classesWithOutBasePackage = classesAnnotatedComponentScan.stream()
                .filter(clazz -> !clazz.isAnnotation())
                .filter(clazz -> isEmptyBasePackageOf(clazz.getAnnotation(COMPONENT_SCAN_ANNOTATION)))
                .collect(Collectors.toSet());

        registerBasePackageOf(classesWithOutBasePackage);
    }

    private boolean isEmptyBasePackageOf(ComponentScan componentScan) {
        return ArrayUtils.isEmpty(componentScan.basePackages());
    }

    private void registerBasePackageOfAnnotations(Set<Class<? extends Annotation>> scannedAnnotations) {
        for (Class<? extends Annotation> annotation : scannedAnnotations) {
            final Set<Class<?>> annotatedClasses = allReflections.getTypesAnnotatedWith(annotation, true);
            registerBasePackageOf(annotatedClasses);
        }
    }

    private void registerBasePackageOf(Set<Class<?>> classes) {
        classes.stream()
                .filter(clazz -> !clazz.isAnnotation())
                .forEach(clazz -> this.basePackages.add(clazz.getPackage().getName()));
    }
}
