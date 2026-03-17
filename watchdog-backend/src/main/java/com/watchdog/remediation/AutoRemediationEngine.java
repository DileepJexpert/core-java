package com.watchdog.remediation;

import com.watchdog.config.WatchdogProperties;
import com.watchdog.model.entity.IncidentEntity;
import com.watchdog.model.entity.RemediationLogEntity;
import com.watchdog.model.enums.IncidentStatus;
import com.watchdog.model.enums.RemediationActionType;
import com.watchdog.remediation.actions.RemediationActionExecutor;
import com.watchdog.repository.IncidentRepository;
import com.watchdog.repository.RemediationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates automated remediation for detected incidents.
 * Executes applicable actions, enforces guardrails, and logs all outcomes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoRemediationEngine {

    private final List<RemediationActionExecutor> actionExecutors;
    private final GuardrailService guardrailService;
    private final RemediationRepository remediationRepository;
    private final IncidentRepository incidentRepository;
    private final WatchdogProperties properties;

    /**
     * Evaluates and executes applicable remediation actions for an incident.
     */
    public void remediate(IncidentEntity incident) {
        boolean dryRun = properties.getRemediation().isDryRun();
        boolean anyActionTaken = false;

        for (RemediationActionExecutor executor : actionExecutors) {
            if (!executor.appliesTo(incident)) continue;

            RemediationActionType actionType = executor.getActionType();

            if (!guardrailService.isAllowed(incident, actionType)) {
                logAction(incident, actionType, "SKIPPED", "Guardrail blocked action", dryRun);
                continue;
            }

            try {
                boolean success = executor.execute(incident, dryRun);
                String outcome = dryRun ? "DRY_RUN" : (success ? "SUCCESS" : "FAILED");
                logAction(incident, actionType, outcome, null, dryRun);

                if (!dryRun && success) {
                    guardrailService.recordAction(incident, actionType);
                    anyActionTaken = true;
                }
            } catch (Exception e) {
                log.error("Remediation action {} failed for incident {}: {}",
                        actionType, incident.getId(), e.getMessage());
                logAction(incident, actionType, "FAILED", e.getMessage(), dryRun);
            }
        }

        // Update incident status if any action was taken
        if (anyActionTaken && !dryRun) {
            incident.setAutoRemediated(true);
            incident.setStatus(IncidentStatus.AUTO_REMEDIATED);
            incidentRepository.save(incident);
        }
    }

    private void logAction(IncidentEntity incident, RemediationActionType actionType,
                           String outcome, String failureReason, boolean dryRun) {
        RemediationLogEntity log = new RemediationLogEntity();
        log.setIncidentId(incident.getId());
        log.setActionType(actionType);
        log.setServiceName(incident.getServiceName());
        log.setParameters(Map.of(
                "incident_title", incident.getTitle(),
                "correlation_rule", incident.getCorrelationRule() != null ? incident.getCorrelationRule() : "",
                "dry_run", dryRun
        ));
        log.setOutcome(outcome);
        log.setExecutedBy("AUTO");
        log.setFailureReason(failureReason);
        remediationRepository.save(log);
    }
}
