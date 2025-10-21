from locust import HttpUser, task, between
import os

BASE = os.getenv("GATEWAY_BASE_URL", "http://localhost:8080")

class EcommerceUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self):
        self.client.base_url = BASE

    @task(3)
    def list_products(self):
        self.client.get("/product-service/api/products")

    @task(2)
    def list_users(self):
        self.client.get("/user-service/api/users")

    @task(1)
    def get_orders(self):
        self.client.get("/order-service/api/orders")

    @task(1)
    def get_payments(self):
        self.client.get("/payment-service/api/payments")

    @task(1)
    def get_favourites(self):
        self.client.get("/favourite-service/api/favourites")
