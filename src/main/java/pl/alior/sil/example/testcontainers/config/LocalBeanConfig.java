package pl.alior.sil.example.testcontainers.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalBeanConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
