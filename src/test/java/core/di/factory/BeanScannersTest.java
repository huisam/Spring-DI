package core.di.factory;

import core.di.factory.example.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanScannersTest {

    @Test
    @DisplayName("BeanScanners는 BeanScanner와 Configuration Scanner를 통해 모든 Bean을 스캔한다")
    void test_scan() {
        /* given */
        final BeanScanners beanScanners = new BeanScanners("core.di.factory.example");

        /* when */
        final Set<Class<?>> preInstantiateBeans = beanScanners.scan();

        /* then */
        assertThat(preInstantiateBeans).hasSize(6);
        assertThat(preInstantiateBeans).containsExactlyInAnyOrder(JdbcQuestionRepository.class, MyJdbcTemplate.class,
                QnaController.class, JdbcUserRepository.class, MyQnaService.class, DataSource.class);
    }

    @Test
    @DisplayName("Bean 스캔 시 중복되는 후보가 있으면 에러를 반환한다")
    void test_scan_exception() {
        /* given */
        final BeanScanners beanScanners = new BeanScanners("core.di.factory.abnormal");

        /* when & then */
        assertThrows(IllegalStateException.class, beanScanners::scan);
    }

    @Test
    @DisplayName("특정 Bean의 파라미터 타입 가져오는지 테스트")
    void test_getParameter_type_for_instantiation() {
        /* given */
        final BeanScanners beanScanners = new BeanScanners("core.di.factory.example");
        beanScanners.scan();

        /* when */
        final Class<?>[] parameterTypes = beanScanners.getParameterTypesForInstantiation(MyJdbcTemplate.class);

        /* then */
        assertThat(parameterTypes).hasSize(1);
        assertThat(parameterTypes).containsExactly(DataSource.class);
    }

    @Test
    @DisplayName("인스턴스가 제대로 생성되는지 테스트")
    void test() {
        /* given */
        final BeanScanners beanScanners = new BeanScanners("core.di.factory.example");
        beanScanners.scan();

        final DataSource dataSource = new IntegrationConfig().dataSource();

        /* when */
        Object instance = beanScanners.instantiate(MyJdbcTemplate.class, dataSource);

        /* then */
        assertThat(instance).isNotNull();
    }
}