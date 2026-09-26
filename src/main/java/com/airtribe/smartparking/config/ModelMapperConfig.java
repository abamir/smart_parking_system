package com.airtribe.smartparking.config;

import com.airtribe.smartparking.dto.response.PaymentResponse;
import com.airtribe.smartparking.entity.Payment;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(
                        org.modelmapper.config.Configuration.AccessLevel.PRIVATE
                );

        // Custom mapping for Payment -> PaymentResponse
        modelMapper.createTypeMap(Payment.class, PaymentResponse.class)
                .addMapping(src -> src.getParkingTicket().getTicketNumber(), PaymentResponse::setTicketNumber);

        return modelMapper;
    }
}
