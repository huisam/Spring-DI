package core.di.factory;

import core.di.factory.example.JdbcQuestionRepository;
import core.di.factory.example.JdbcUserRepository;
import core.di.factory.example.MyQnaService;
import core.di.factory.example.QnaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BeanScannerTest {

    @Test
    @DisplayName("BeanScanner 가 제대로 스캔하는지 테스트")
    void test_bean_scanner() {
        /* given */
        BeanScanner beanScanner = new BeanScanner("core.di.factory.example");

        /* when */
        final Set<Class<?>> preInstantiateClazz = beanScanner.scan();

        /* then */
        assertThat(preInstantiateClazz).hasSize(4);
        assertThat(preInstantiateClazz).containsExactlyInAnyOrder(QnaController.class, MyQnaService.class,
                JdbcUserRepository.class, JdbcQuestionRepository.class);
    }
}