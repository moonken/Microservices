package net.chrisrichardson.eventstore.examples.customersandorders.shipmentservice.web;

import net.chrisrichardson.eventstore.examples.customersandorders.shipmentservice.backend.ShipmentBackendConfiguration;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@ComponentScan
@Import(ShipmentBackendConfiguration.class)
public class ShipmentWebConfiguration {

    @Bean
    public HttpMessageConverters shipmentConverters() {
        HttpMessageConverter<?> additional = new MappingJackson2HttpMessageConverter();
        return new HttpMessageConverters(additional);
    }
}
