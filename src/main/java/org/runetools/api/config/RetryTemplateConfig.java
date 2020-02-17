package org.runetools.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

@Configuration
public class RetryTemplateConfig {
    @Bean
    @Primary
    public RetryTemplate buildDefault() {
        var retryTemplate = new RetryTemplate();
        retryTemplate.setBackOffPolicy(new FixedBackOffPolicy());
        return retryTemplate;
    }

    @Bean("voiceOfSeren")
    public RetryTemplate buildVoiceOfSeren() {
        var retryTemplate = new RetryTemplate();

        var backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(Duration.ofSeconds(15).toMillis());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        var retryPolicy = new TimeoutRetryPolicy();
        retryPolicy.setTimeout(Duration.ofMinutes(5).toMillis());
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
