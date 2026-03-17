package com.watchdog.correlation.rules;

import com.watchdog.model.NormalizedEvent;
import com.watchdog.model.entity.IncidentEntity;
import com.watchdog.model.enums.IncidentStatus;
import com.watchdog.model.enums.Severity;
import com.watchdog.model.enums.SignalType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Rule: Graceful Degradation Failure
 * Triggers when a fallback/circuit breaker is open AND users are experiencing errors.
 */
@Component
public class GracefulDegradationRule implements CorrelationRule {

    @Override
    public String getName() { return "GRACEFUL_DEGRADATION_FAILURE"; }

    @Override
    public Optional<IncidentEntity> evaluate(List<NormalizedEvent> windowEvents, String serviceName) {
        boolean fallbackActive = windowEvents.stream()
                .filter(e -> e.signalType() == SignalType.LOG)
                .anyMatch(e -> {
                    String msg = e.message() != null ? e.message().toLowerCase() : "";
                    return msg.contains("fallback") || msg.contains("circuit open") ||
                            msg.contains("bulkhead");
                });

        boolean userFacingErrors = windowEvents.stream()
                .filter(e -> e.signalType() == SignalType.PROBE)
                .anyMatch(e -> Boolean.TRUE.equals(e.attributes().get("5xx_error")) ||
                        Boolean.TRUE.equals(e.attributes().get("service_down")));

        if (fallbackActive && userFacingErrors) {
            IncidentEntity incident = new IncidentEntity();
            incident.setServiceName(serviceName);
            incident.setTitle("Graceful degradation failing for " + serviceName + " - fallback insufficient");
            incident.setSeverity(Severity.P2_HIGH);
            incident.setStatus(IncidentStatus.OPEN);
            incident.setCorrelationRule(getName());
            incident.setCorrelatedSignalIds(windowEvents.stream().map(NormalizedEvent::id).toList());
            return Optional.of(incident);
        }
        return Optional.empty();
    }
}
