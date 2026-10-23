import pytest

from app.app import create_app


@pytest.fixture
def client():
    app = create_app()
    app.config["TESTING"] = True
    return app.test_client()


def test_index_returns_message(client):
    resp = client.get("/")
    assert resp.status_code == 200
    data = resp.get_json()
    assert data["message"] == "Hello from Flask CI/CD demo"
    assert {"env", "version"} <= data.keys()


def test_index_reports_environment(client, monkeypatch):
    monkeypatch.setenv("APP_ENV", "staging")
    monkeypatch.setenv("APP_VERSION", "42")
    data = client.get("/").get_json()
    assert data["env"] == "staging"
    assert data["version"] == "42"


def test_health(client):
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.get_json() == {"status": "ok"}


@pytest.mark.parametrize("a,b,expected", [(2, 3, 5), (-1, 1, 0), (0, 0, 0)])
def test_sum(client, a, b, expected):
    resp = client.get(f"/api/sum?a={a}&b={b}")
    assert resp.status_code == 200
    assert resp.get_json()["result"] == expected


@pytest.mark.parametrize("query", ["", "?a=1", "?a=x&b=2"])
def test_sum_rejects_bad_input(client, query):
    resp = client.get(f"/api/sum{query}")
    assert resp.status_code == 400
    assert "error" in resp.get_json()
