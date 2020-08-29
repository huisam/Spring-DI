package core.di;

import core.di.factory.example.*;
import core.di.factory.exception.CircularReferenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationContextTest {

    @Test
    @DisplayName("BeanScanner와 Configuration Scanner를 통해 모든 Bean을 스캔한다")
    void test_scan() {
        /* given */
        final ApplicationContext applicationContext = new ApplicationContext("core.di.factory.example");

        /* when */
        final List<Class<?>> preInstantiateBeans = applicationContext.getBeanClasses();

        /* then */
        assertThat(preInstantiateBeans).hasSize(6);
        assertThat(preInstantiateBeans).containsExactlyInAnyOrder(JdbcQuestionRepository.class, MyJdbcTemplate.class,
                QnaController.class, JdbcUserRepository.class, MyQnaService.class, DataSource.class);
    }

    @Test
    @DisplayName("Bean 스캔 시 중복되는 후보가 있으면 에러를 반환한다")
    void test_scan_exception() {
        /* given */
        final String basePackage = "core.di.factory.abnormal.config";
        /* when & then */
        assertThrows(IllegalStateException.class, () -> new ApplicationContext(basePackage));
    }

    @Test
    @DisplayName("순환 참조 관계일 때 예외를 던지는지 테스트")
    void when_circular_relation_it_throws_exception() {
        /* given */
        String basePackage = "core.di.factory.abnormal.circular";
        /* given & when & then */
        assertThrows(CircularReferenceException.class, () -> new ApplicationContext(basePackage));
    }

    @Test
    @DisplayName("특정 Bean의 파라미터 타입 가져오는지 테스트")
    void test_getParameter_type_for_instantiation() {
        /* given */
        final ApplicationContext applicationContext = new ApplicationContext("core.di.factory.example");

        /* when */
        final QnaController qnaController = applicationContext.getBean(QnaController.class);

        /* then */
        assertThat(qnaController).isNotNull();

        final MyQnaService qnaService = qnaController.getQnaService();
        assertThat(qnaService).isNotNull();
        assertThat(qnaService.getQuestionRepository()).isNotNull();
        assertThat(qnaService.getUserRepository()).isNotNull();
    }

}