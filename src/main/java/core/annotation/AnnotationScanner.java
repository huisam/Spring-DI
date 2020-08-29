package core.annotation;

import core.di.factory.Scanner;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AnnotationScanner implements Scanner<Class<? extends Annotation>> {
    private static final String ANNOTATION_BASE_PACKAGE = "core.annotation";

    private final Class<? extends Annotation> target;

    @Override
    public Set<Class<? extends Annotation>> scan() {
        final Reflections reflections = new Reflections(ANNOTATION_BASE_PACKAGE);

        return findAnnotationByScan(target, reflections);
    }

    private Set<Class<? extends Annotation>> findAnnotationByScan(Class<? extends Annotation> annotation, Reflections reflections) {
        final Set<Class<?>> annotations = reflections.getTypesAnnotatedWith(annotation);

        if (annotations.isEmpty()) {
            return new HashSet<>(Collections.singleton(annotation));
        }

        final Set<Class<? extends Annotation>> scannedAnnotations = annotations.stream()
                .map(clazz -> findAnnotationByScan((Class<? extends Annotation>) clazz, reflections))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        scannedAnnotations.add(annotation);
        return scannedAnnotations;
    }
}
