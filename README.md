# WATCHDOG — Unified API Monitoring & Auto-Remediation Platform

> **Codename: WATCHDOG** — Integrating Kibana · Jaeger · Grafana into a Single Intelligent Monitoring Engine

## Overview

WATCHDOG is a Java Spring Boot platform that:
- **Ingests** data from Elasticsearch/Kibana (logs), Jaeger (traces), and Grafana/Prometheus (metrics)
- **Correlates** signals across sources using a sliding-window engine with 20 predefined rules
- **Detects** anomalies using Z-score statistical models
- **Alerts** via Slack, Email, PagerDuty, and OpsGenie with tiered severity routing
- **Remediates** known issues automatically (pod scale, restart, rollback, circuit breaker)
- **Displays** everything in a React dashboard — the single pane of glass

## Business Impact

| Metric | Before (Manual) | With WATCHDOG |
|--------|----------------|---------------|
| Dedicated monitoring staff | 4–5 FTEs | 0 (on-call model) |
| Mean time to detect | 15–45 min | &lt; 2 min |
| Mean time to resolve | 30–60 min | &lt; 5 min (auto) |

## Architecture

```
Layer 1: Ingestion       → Elasticsearch, Jaeger, Grafana, Health Probes
Layer 2: Processing      → Event normalization, Kafka streaming
Layer 3: Intelligence    → Correlation engine, Z-score anomaly detection, rule engine
Layer 4: Action          → Auto-remediation (K8s), alerting, dashboard
```

## Quick Start (Docker Compose)

```bash
# Start all services (PostgreSQL, Redis, Kafka, Elasticsearch, Jaeger, Grafana)
docker compose up -d

# Access WATCHDOG Dashboard
open http://localhost:3000

# Access WATCHDOG API
curl http://localhost:8080/api/dashboard/summary
```

## Project Structure

```
watchdog-backend/           # Spring Boot 3.x backend
  src/main/java/com/watchdog/
    ingestion/              # Elasticsearch, Jaeger, Grafana connectors
    correlation/            # Correlation engine + 20 rules
    intelligence/           # Static rule engine + Z-score anomaly detection
    remediation/            # Auto-remediation with K8s client + guardrails
    alerting/               # Slack, Email, PagerDuty, OpsGenie
    api/                    # REST API + WebSocket controllers
    scheduler/              # Weekly digest + anomaly retraining
  src/main/resources/
    application.yml         # Configuration
    rules/default-rules.yml # 10 static YAML alert rules

watchdog-frontend/          # React + TypeScript dashboard
  src/
    components/             # ServiceHealthMap, ActiveIncidents, LatencyHeatmap, etc.
    hooks/                  # useWebSocket (real-time updates)

k8s/                        # Kubernetes manifests
docker-compose.yml          # Local development environment
```

## Configuration

All config is environment-variable driven:

| Variable | Description | Default |
|----------|-------------|---------|
| `ES_URL` | Elasticsearch URL | `http://localhost:9200` |
| `JAEGER_URL` | Jaeger Query API URL | `http://localhost:16686` |
| `GRAFANA_URL` | Grafana URL | `http://localhost:3000` |
| `GRAFANA_API_KEY` | Grafana API key | — |
| `SLACK_WEBHOOK_URL` | Slack incoming webhook | — |
| `PAGERDUTY_KEY` | PagerDuty integration key | — |
| `REMEDIATION_DRY_RUN` | Dry run mode (no K8s actions) | `true` |

## Correlation Rules (20 predefined)

1. Memory Leak / Resource Exhaustion · 2. Database Connectivity Issue · 3. Cascading Failure
4. CrashLoop / Unstable Deployment · 5. High HTTP Error Rate · 6. Latency Degradation
7. TLS Certificate Expiry · 8. Traffic Anomaly · 9. Pod OOMKill · 10. Deployment Regression
11. Service Down · 12. High CPU Sustained · 13. Upstream Dependency Failure
14. Slow Database Queries · 15. Circuit Breaker Triggered · 16. Low Disk Space
17. Message Queue Backlog · 18. Connection Pool Exhaustion · 19. External API Throttling
20. Security Anomaly / Auth Failure Spike

## Technology Stack

Java 17 · Spring Boot 3.2 · Spring Kafka · PostgreSQL + TimescaleDB · Redis · Fabric8 K8s Client · React 18 · TypeScript · Recharts