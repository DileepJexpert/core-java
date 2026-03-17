package com.watchdog.remediation;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper around the Fabric8 Kubernetes client for pod and deployment operations.
 * All operations are guarded against errors and logged for audit purposes.
 */
@Slf4j
@Component
public class KubernetesClientWrapper {

    private final KubernetesClient client;

    public KubernetesClientWrapper() {
        this.client = new KubernetesClientBuilder().build();
    }

    /**
     * Scales a deployment to the specified replica count.
     */
    public boolean scaleDeployment(String namespace, String deploymentName, int replicas) {
        try {
            client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .scale(replicas);
            log.info("Scaled deployment {}/{} to {} replicas", namespace, deploymentName, replicas);
            return true;
        } catch (Exception e) {
            log.error("Failed to scale deployment {}/{}: {}", namespace, deploymentName, e.getMessage());
            return false;
        }
    }

    /**
     * Returns the current replica count for a deployment.
     */
    public int getCurrentReplicas(String namespace, String deploymentName) {
        try {
            Deployment deployment = client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();
            if (deployment == null) return -1;
            Integer replicas = deployment.getSpec().getReplicas();
            return replicas != null ? replicas : 1;
        } catch (Exception e) {
            log.warn("Failed to get replicas for {}/{}: {}", namespace, deploymentName, e.getMessage());
            return -1;
        }
    }

    /**
     * Deletes pods matching the given label selector to force restart.
     * Kubernetes will recreate them automatically.
     */
    public boolean restartPods(String namespace, String appLabel) {
        try {
            List<Pod> pods = client.pods()
                    .inNamespace(namespace)
                    .withLabel("app", appLabel)
                    .list()
                    .getItems();

            for (Pod pod : pods) {
                client.pods()
                        .inNamespace(namespace)
                        .withName(pod.getMetadata().getName())
                        .delete();
                log.info("Deleted pod {}/{} for restart", namespace, pod.getMetadata().getName());
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to restart pods for {}/{}: {}", namespace, appLabel, e.getMessage());
            return false;
        }
    }

    /**
     * Rolls back a deployment to the previous revision.
     */
    public boolean rollbackDeployment(String namespace, String deploymentName) {
        try {
            // Fabric8 rollback via annotation
            client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .rolling()
                    .undo();
            log.info("Rolled back deployment {}/{}", namespace, deploymentName);
            return true;
        } catch (Exception e) {
            log.error("Failed to rollback deployment {}/{}: {}", namespace, deploymentName, e.getMessage());
            return false;
        }
    }

    /**
     * Returns the current deployment image tag.
     */
    public Optional<String> getCurrentImageTag(String namespace, String deploymentName) {
        try {
            Deployment deployment = client.apps().deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();
            if (deployment == null) return Optional.empty();
            String image = deployment.getSpec().getTemplate().getSpec()
                    .getContainers().get(0).getImage();
            return Optional.ofNullable(image);
        } catch (Exception e) {
            log.warn("Could not get image tag for {}/{}: {}", namespace, deploymentName, e.getMessage());
            return Optional.empty();
        }
    }
}
