SHELL := /bin/sh
COMPOSE := docker compose

.PHONY: up down build logs shell-backend shell-db test

# Avvia tutto (db + backend + sonarqube)
up:
	$(COMPOSE) up --build -d

# Ferma tutto
down:
	$(COMPOSE) down

# Rebuilda solo il backend
build:
	$(COMPOSE) build backend

# Segui i log
logs:
	$(COMPOSE) logs --tail=50 --follow

# Apri shell nel container backend
shell-backend:
	$(COMPOSE) exec backend sh

# Apri shell nel container db
shell-db:
	$(COMPOSE) exec db psql -U admin -d bugboard26

# Esegui i test Maven
test:
	.\mvnw.cmd test
