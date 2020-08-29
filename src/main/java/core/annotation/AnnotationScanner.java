package core.annotation;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationScanner {
    private static final String ANNOTATION_BASE_PACKAGE = "core.annotation";

    public Set<Class<? extends Annotation>> scan(Class<? extends Annotation> targetAnnotation) {
        final Reflections reflections = new Reflections(ANNOTATION_BASE_PACKAGE);

        final Set<Class<?>> annotations = reflections.getTypesAnnotatedWith(targetAnnotation);

        if (annotations.isEmpty()) {
            return new HashSet<>(Collections.singleton(targetAnnotation));
        }

        final Set<Class<? extends Annotation>> scannedAnnotations = annotations.stream()
                .map(clazz -> this.scan((Class<? extends Annotation>) clazz))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        scannedAnnotations.add(targetAnnotation);
        return scannedAnnotations;
    }
}
