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
 * Rule: Sustained High CPU
 * Triggers when CPU >90% is observed across multiple metric events in the window.
 */
@Component
public class HighCpuSustainedRule implements CorrelationRule {

    @Override
    public String getName() { return "HIGH_CPU_SUSTAINED"; }

    @Override
    public Optional<IncidentEntity> evaluate(List<NormalizedEvent> windowEvents, String serviceName) {
        long highCpuCount = windowEvents.stream()
                .filter(e -> e.signalType() == SignalType.METRIC)
                .filter(e -> Boolean.TRUE.equals(e.attributes().get("high_cpu")))
                .count();

        if (highCpuCount >= 3) { // Sustained over at least 3 polling intervals
            IncidentEntity incident = new IncidentEntity();
            incident.setServiceName(serviceName);
            incident.setTitle("Sustained high CPU on " + serviceName + " (" + highCpuCount + " consecutive readings)");
            incident.setSeverity(Severity.P2_HIGH);
            incident.setStatus(IncidentStatus.OPEN);
            incident.setCorrelationRule(getName());
            incident.setCorrelatedSignalIds(windowEvents.stream().map(NormalizedEvent::id).toList());
            return Optional.of(incident);
        }
        return Optional.empty();
    }
}
