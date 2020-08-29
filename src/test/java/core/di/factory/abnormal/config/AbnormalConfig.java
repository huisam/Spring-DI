package core.di.factory.abnormal.config;

import core.annotation.Bean;
import core.annotation.Configuration;

@Configuration
public class AbnormalConfig {

    @Bean
    public ExampleController exampleController() {
        return new ExampleController();
    }
}
