package com.ecommerce.project.config;

import com.ecommerce.project.model.Performance;
import com.ecommerce.project.payload.PerformanceDTO;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        // OVDJE dodajemo skip:
        modelMapper.typeMap(Performance.class, PerformanceDTO.class)
                .addMappings(m -> m.skip(PerformanceDTO::setQuantityInCart));

        return  modelMapper;
    }


}
