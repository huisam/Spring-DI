package core.di.factory.abnormal;

import core.annotation.Repository;
import core.annotation.Service;
import core.annotation.web.Controller;
import core.di.factory.BeanFactory;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CircularDependencyTest {

    private Reflections reflections;

    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        reflections = new Reflections("core.di.factory.abnormal");
        final Set<Class<?>> preInstantiateClazz = getTypesAnnotatedWith(Controller.class, Service.class, Repository.class);
        beanFactory = new BeanFactory(preInstantiateClazz);
    }

    @Test
    @DisplayName("순환 참조 관계일 때 예외를 던지는지 테스트")
    void when_circular_relation_it_throws_exception() {
        /* given & when & then */
        assertThrows(CircularReferenceException.class, () -> beanFactory.initialize());
    }

    @SafeVarargs
    private final Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation>... annotations) {
        Set<Class<?>> beans = Sets.newHashSet();
        for (Class<? extends Annotation> annotation : annotations) {
            beans.addAll(reflections.getTypesAnnotatedWith(annotation));
        }
        
        return beans;
    }
}