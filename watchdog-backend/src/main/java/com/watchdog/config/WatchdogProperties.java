package com.watchdog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "watchdog")
public class WatchdogProperties {

    private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();
    private JaegerConfig jaeger = new JaegerConfig();
    private GrafanaConfig grafana = new GrafanaConfig();
    private HealthProbeConfig healthProbe = new HealthProbeConfig();
    private CorrelationConfig correlation = new CorrelationConfig();
    private AlertingConfig alerting = new AlertingConfig();
    private RemediationConfig remediation = new RemediationConfig();
    private AnomalyConfig anomaly = new AnomalyConfig();

    @Data
    public static class ElasticsearchConfig {
        private String url = "http://localhost:9200";
        private String indexPattern = "logs-*";
        private int pollIntervalSeconds = 30;
        private String username = "";
        private String password = "";
    }

    @Data
    public static class JaegerConfig {
        private String url = "http://localhost:16686";
        private long slowTraceThresholdMs = 2000;
        private int pollIntervalSeconds = 60;
    }

    @Data
    public static class GrafanaConfig {
        private String url = "http://localhost:3000";
        private String apiKey = "";
        private int pollIntervalSeconds = 15;
        private List<String> datasourceUids = new ArrayList<>();
    }

    @Data
    public static class HealthProbeConfig {
        private int intervalSeconds = 10;
        private int timeoutSeconds = 5;
        private List<ProbeTarget> targets = new ArrayList<>();

        @Data
        public static class ProbeTarget {
            private String name;
            private String url;
            private String type = "HTTP"; // HTTP or GRPC
        }
    }

    @Data
    public static class CorrelationConfig {
        private int windowMinutes = 5;
        private int minSignalsForCorrelation = 2;
    }

    @Data
    public static class AlertingConfig {
        private SlackConfig slack = new SlackConfig();
        private PagerDutyConfig pagerduty = new PagerDutyConfig();
        private OpsGenieConfig opsgenie = new OpsGenieConfig();

        @Data
        public static class SlackConfig {
            private String webhookUrl = "";
            private String channel = "#watchdog-alerts";
        }

        @Data
        public static class PagerDutyConfig {
            private String integrationKey = "";
            private String eventsUrl = "https://events.pagerduty.com/v2/enqueue";
        }

        @Data
        public static class OpsGenieConfig {
            private String apiKey = "";
            private String alertsUrl = "https://api.opsgenie.com/v2/alerts";
        }
    }

    @Data
    public static class RemediationConfig {
        private boolean dryRun = true;
        private double maxScaleFactor = 2.0;
        private int restartCooldownMinutes = 20;
        private int maxRestartsPerHour = 3;
        private int circuitBreakerRetryMinutes = 5;
        private int maxCircuitBreakerCycles = 3;
    }

    @Data
    public static class AnomalyConfig {
        private double zScoreThreshold = 3.0;
        private int retrainingIntervalHours = 24;
        private int minSamplesForDetection = 30;
    }
}
