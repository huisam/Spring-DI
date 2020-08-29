package core.di;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentBasePackageScannerTest {
    @Test
    @DisplayName("특정 패키지 아래에 있는 모든 Component Bean을 스캔하는지 테스트")
    void test_component_bean_scan() {
        /* given */
        final ComponentBasePackageScanner componentBasePackageScanner = new ComponentBasePackageScanner("core.di.factory.componentscan");

        /* when */
        final Set<Object> basePackages = componentBasePackageScanner.scan();

        /* then */
        assertThat(basePackages).containsExactlyInAnyOrder("core", "core.di.factory.componentscan");
    }
}