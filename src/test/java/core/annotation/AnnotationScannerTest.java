package core.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationScannerTest {

    @Test
    @DisplayName("어노테이션 package 에서 특정 annotation을 상속하는 모든 어노테이션 찾기")
    void test_annotation_scan() {
        /* given */
        final AnnotationScanner annotationScanner = new AnnotationScanner(ComponentScan.class);

        /* when */
        final Set<Class<? extends Annotation>> annotations = annotationScanner.scan();

        /* then */
        assertThat(annotations).containsExactlyInAnyOrder(ComponentScan.class, WebApplication.class);
    }
}