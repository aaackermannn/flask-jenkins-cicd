"""Простое Flask-приложение для демонстрации CI/CD-конвейера в Jenkins."""
import os

from flask import Flask, jsonify, request


def create_app():
    app = Flask(__name__)

    @app.get("/")
    def index():
        return jsonify(
            message="Hello from Flask CI/CD demo",
            env=os.getenv("APP_ENV", "local"),
            version=os.getenv("APP_VERSION", "dev"),
        )

    @app.get("/health")
    def health():
        return jsonify(status="ok")

    @app.get("/api/sum")
    def sum_numbers():
        try:
            a = int(request.args["a"])
            b = int(request.args["b"])
        except (KeyError, ValueError):
            return jsonify(error="parameters a and b must be integers"), 400
        return jsonify(a=a, b=b, result=a + b)

    return app


app = create_app()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
