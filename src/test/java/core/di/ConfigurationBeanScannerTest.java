package core.di;

import core.di.factory.DefaultBeanFactory;
import core.di.factory.example.MyJdbcTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationBeanScannerTest {
    @Test
    @DisplayName("기준 Package 하위에 있는 Configuration Bean Method를 스캔하는지 테스트")
    void test_scan() {
        /* given */
        final ConfigurationBeanScanner configurationBeanScanner = new ConfigurationBeanScanner(new DefaultBeanFactory());

        /* when */
        final Set<Class<?>> preInstantiateClasses = configurationBeanScanner.scan("core.di.factory.example");

        /* then */
        assertThat(preInstantiateClasses).hasSize(2);
        assertThat(preInstantiateClasses).containsExactlyInAnyOrder(DataSource.class, MyJdbcTemplate.class);
    }
}