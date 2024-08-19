import time
from locust import HttpUser, task, between

class TestUser(HttpUser):
    wait_time = between(1, 5)

    @task
    def test(self):
        h = {
            'data-source': 'raw-direct',
            # 'data-source': 'raw-proxy',
            # 'data-source': 'wrapper-selfsplit',
            # 'data-source': 'wrapper-driversplit',
        }
        # self.client.get("/test", headers=h)
        self.client.post("/test", headers=h)
