package org.runetools.api.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate buildRestTemplate(RestTemplateBuilder builder, BuildProperties buildProperties) {
        var userAgent = "web:" + buildProperties.getName() + ":" + buildProperties.getVersion();
        return builder.defaultHeader("User-Agent", userAgent).build();
    }
}
