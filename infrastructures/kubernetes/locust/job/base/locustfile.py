import importlib
import pip
import re
import site

pip.main(['install', 'boto3'])
importlib.reload(site)

import boto3

from datetime import datetime
import logging
import os
from locust import HttpUser, task, between, events
from locust.runners import WorkerRunner

LOCUST_REPORTS_BUCKET_PREFIX = 'spring-aurora-failover-locust-reports'
JOB_CONTROLLER_UID = os.getenv('JOB_CONTROLLER_UID')
if JOB_CONTROLLER_UID is None:
    raise Exception('Environment variable JOB_CONTROLLER_UID not found')

DATA_SOURCE_HEADER_VALUES = frozenset({'raw-direct', 'raw-proxy', 'wrapper-selfsplit', 'wrapper-driversplit'})
DATA_SOURCE_HEADER_VALUE = os.getenv('DATA_SOURCE_HEADER_VALUE', 'raw-direct')
if DATA_SOURCE_HEADER_VALUE not in DATA_SOURCE_HEADER_VALUES:
    raise Exception(f'Unexpected value for env: {DATA_SOURCE_HEADER_VALUE=}')
TARGET_DB_ROLES = frozenset({'READER', 'WRITER'})
TARGET_DB_ROLE = os.getenv('TARGET_DB_ROLE', 'READER')
if TARGET_DB_ROLE not in TARGET_DB_ROLES:
    raise Exception(f'Unexpected value for env: {TARGET_DB_ROLE=}')

s3_client = boto3.client('s3')
sts_client = boto3.client('sts')
caller_identity = sts_client.get_caller_identity()
aws_account_id = caller_identity['Account']
report_bucket = f'{LOCUST_REPORTS_BUCKET_PREFIX}-{aws_account_id}'
s3_report_dir = datetime.now().isoformat().replace(':', '_')

@events.init.add_listener
def s3_access_check(environment, **kwargs):
    if isinstance(environment.runner, WorkerRunner):
        logging.info('This instance is worker; skip access test for report bucket')
        return
    logging.info('Access test for report bucket')
    s3_client.put_object(
        Body=b'test',
        Bucket=report_bucket,
        Key=f'{s3_report_dir}/{JOB_CONTROLLER_UID}.accesstest'
    )
    logging.info('Successfully complete access test for report bucket')

@events.quitting.add_listener
def send_reports_to_s3(environment, **kwargs):
    if isinstance(environment.runner, WorkerRunner):
        logging.info('This instance is worker; skip reporting to report bucket')
        return
    logging.info('Start reporting to report bucket')
    reportdir_path = '/locustreports'
    if not os.path.exists(reportdir_path):
        msg = f'report directory: {reportdir_path} not found'
        logging.warning(msg)
        raise Exception(msg)
    reportdir_list = os.listdir(reportdir_path)
    logging.info(f'reportdir_list = {reportdir_list}')
    for report_filename in reportdir_list:
        report_path = f'{reportdir_path}/{report_filename}'
        with open(report_path, 'rb') as report_file:
            report_bytes = report_file.read()
            s3_client.put_object(
                Body=report_bytes,
                Bucket=report_bucket,
                Key=f'{s3_report_dir}/{report_filename}',
            )
    logging.info('Successfully complete reporting to report bucket')

class TestUser(HttpUser):
    # 0.2 RPS per user
    wait_time = between(4, 6)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._record_key = None

    def on_start(self):
        h = {
            'data-source': DATA_SOURCE_HEADER_VALUE,
        }
        resp = self.client.post('/test', headers=h)
        body_text = resp.text
        record_key_str = re.sub(r'^.*inserted id: ([0-9]*)$', r'\1', body_text)
        self._record_key = int(record_key_str)

    @task
    def test(self):
        h = {
            'data-source': DATA_SOURCE_HEADER_VALUE,
        }
        if TARGET_DB_ROLE == 'READER':
            self.client.get('/test', headers=h)
        else:
            self.client.put(f'/test/{self._record_key}', headers=h)
