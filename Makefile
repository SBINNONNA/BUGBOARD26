SHELL      := /bin/sh
COMPOSE    := docker compose
UI_CLASS   := com.bugboard.bugboard26.ui.SwingApp

.PHONY: up run down build logs shell-backend shell-db test ui

# Avvia tutto (db + backend), poi apre il frontend Swing
up:
	$(COMPOSE) up --build -d
	@echo "⏳ Attendo avvio backend..."
	@sleep 8
	@mvn exec:java -Dexec.mainClass="$(UI_CLASS)" -q &

# Avvia senza rebuild (lanci successivi) e apre il frontend Swing
run:
	$(COMPOSE) up -d
	@echo "⏳ Attendo avvio backend..."
	@sleep 5
	@mvn exec:java -Dexec.mainClass="$(UI_CLASS)" -q &

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

# Lancia solo il frontend Swing (senza toccare Docker)
ui:
	@mvn exec:java -Dexec.mainClass="$(UI_CLASS)" -q &