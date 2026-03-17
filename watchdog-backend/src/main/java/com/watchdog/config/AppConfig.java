package com.watchdog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(WatchdogProperties.class)
public class AppConfig {

    @Bean
    public WebClient elasticsearchWebClient(WatchdogProperties props) {
        return WebClient.builder()
                .baseUrl(props.getElasticsearch().getUrl())
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient jaegerWebClient(WatchdogProperties props) {
        return WebClient.builder()
                .baseUrl(props.getJaeger().getUrl())
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient grafanaWebClient(WatchdogProperties props) {
        return WebClient.builder()
                .baseUrl(props.getGrafana().getUrl())
                .defaultHeader("Authorization", "Bearer " + props.getGrafana().getApiKey())
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    @Bean
    public WebClient defaultWebClient() {
        return WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }
}
