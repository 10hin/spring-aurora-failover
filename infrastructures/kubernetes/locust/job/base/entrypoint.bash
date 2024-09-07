#!/usr/bin/env bash
set -euo pipefail

pip install boto3

if [ "${JOB_COMPLETION_INDEX}" = '0' ]
then
  locust \
    --locustfile /locustfiles/locustfile.py \
    --master \
    --headless \
    --expect-workers "${LOCUST_EXPECT_WORKERS}" \
    --host "${LOCUST_HOST}" \
    --users "${LOCUST_USERS}" \
    --spawn-rate "${LOCUST_SPAWN_RATE}" \
    --run-time "${LOCUST_RUN_TIME}" \
    --stop-timeout "${LOCUST_STOP_TIMEOUT}" \
    --reset-stats \
    --csv "/locustreports/report_${DATA_SOURCE_HEADER_VALUE}_${TARGET_DB_ROLE}_${JOB_CONTROLLER_UID}" \
    --csv-full-history \
    --html "/locustreports/report_${DATA_SOURCE_HEADER_VALUE}_${TARGET_DB_ROLE}_${JOB_CONTROLLER_UID}.html" \
    --exit-code-on-error 0 \
    ;
else
  locust \
    -f - \
    --worker \
    --master-host "locust-${DATA_SOURCE_HEADER_VALUE}-${TARGET_EB_ROLE,,}-master.locust.svc.cluster.local" \
    --headless \
    --reset-stats \
    --exit-code-on-error 0 \
    ;
fi

