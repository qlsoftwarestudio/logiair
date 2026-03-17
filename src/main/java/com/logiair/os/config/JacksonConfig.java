package com.logiair.os.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.logiair.os.models.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Configure enum deserialization to be case-insensitive
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Role.class, new com.fasterxml.jackson.databind.JsonDeserializer<Role>() {
            @Override
            public Role deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) 
                throws java.io.IOException, com.fasterxml.jackson.core.JsonProcessingException {
                String value = p.getValueAsString().toUpperCase();
                try {
                    return Role.valueOf(value);
                } catch (IllegalArgumentException e) {
                    throw new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        p, "Invalid role: " + value + ". Valid roles are: " + java.util.Arrays.toString(Role.values()), 
                        null, Role.class);
                }
            }
        });
        
        mapper.registerModule(module);
        return mapper;
    }
}
