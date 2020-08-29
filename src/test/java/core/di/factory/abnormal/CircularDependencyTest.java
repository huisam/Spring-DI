package core.di.factory.abnormal;

import core.di.factory.BeanFactory;
import core.di.factory.BeanScanners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CircularDependencyTest {

    private BeanFactory beanFactory;

    @BeforeEach
    void setUp() {
        final BeanScanners beanScanners = new BeanScanners("core.di.factory.abnormal");
        beanFactory = new BeanFactory(beanScanners);
    }

    @Test
    @DisplayName("순환 참조 관계일 때 예외를 던지는지 테스트")
    void when_circular_relation_it_throws_exception() {
        /* given & when & then */
        assertThrows(CircularReferenceException.class, () -> beanFactory.initialize());
    }
}