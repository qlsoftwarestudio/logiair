package com.logiair.os.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MapperFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Enable case-insensitive enum deserialization
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        
        return mapper;
    }
}
