# Makefile for OrphanageHub - The Definitive Workflow
.DEFAULT_GOAL := help
SHELL := /bin/bash

# --- Configuration ---
APP_MAIN_CLASS := com.orphanagehub.gui.OrphanageHubApp
DEBUG_PORT ?= 5005
PYTHON := scripts/.venv/bin/python

# --- OS & PID Detection ---
ifeq ($(OS),Windows_NT)
    MAVEN := ./mvnw.cmd
    PID := $(shell tasklist | grep javaw.exe | awk '{print $$2}')
else
    MAVEN := ./mvnw
    PID := $(shell jps -l | grep $(APP_MAIN_CLASS) | awk '{print $$1}')
endif

.PHONY: all help build run clean format check test ci \
        run-dev debug jdb pid jstack histo diag \
        python-setup db-reset db-ping db-sql log-analyze

# ==============================================================================
# =                            CORE WORKFLOW                                   =
# ==============================================================================

all: run ## ✨ Default task: Build and run the application

help: ## 🆘 Show this help message
	@echo "OrphanageHub Project Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

build: ## 🚀 Compile, check quality, and package the application
	@echo "Building..."
	@$(MAVEN) -B package -DskipTests

run: build ## ▶️ Run the application normally
	@echo "Launching..."
	@$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS)

clean: ## 🧹 Delete all build artifacts from the target/ directory
	@echo "Cleaning..."
	@$(MAVEN) -B clean

# ==============================================================================
# =                        QUALITY & TESTING                                   =
# ==============================================================================

format: ## 🎨 Apply Google Java Format to all source files
	@echo "Formatting code..."
	@$(MAVEN) -B spotless:apply

check: format ## 🔎 Run all code quality checks (format + static analysis)
	@echo "Checking code quality..."
	@$(MAVEN) -B spotless:check spotbugs:check

test: db-reset ## 🧪 Run all JUnit tests against a clean database
	@echo "Running tests..."
	@$(MAVEN) -B test

ci: ## 🤖 Run the full Continuous Integration pipeline
	@echo "Running CI Pipeline..."
	@$(MAKE) check
	@$(MAKE) test
	@$(MAKE) build

# ==============================================================================
# =                      DEBUG & DIAGNOSTICS                                   =
# ==============================================================================

run-dev: build ## 👨‍💻 Run app in dev mode (assertions enabled, dev env flag)
	@echo "Launching in Developer Mode..."
	@$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) -Dexec.jvmArgs="-ea -Dapp.env=dev"

debug: build ## 🪲 Run with JDWP debug port (5005) open for attaching a debugger
	@echo "Launching in DEBUG mode on port $(DEBUG_PORT)..."
	@$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) \
		-Dexec.jvmArgs="-ea -Dapp.env=dev -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"

jdb: ## 🔌 Attach the command-line Java debugger (jdb) to a debug session
	@jdb -attach localhost:$(DEBUG_PORT)

pid: ## 🆔 Print the application's Process ID (PID), if running
	@if [ -n "$(PID)" ]; then echo "Application PID: $(PID)"; else echo "Application not running or PID not found."; fi

jstack: ## 🧵 Create a full thread dump for diagnosing freezes/hangs
	@[ -n "$(PID)" ] && jcmd $(PID) Thread.print > logs/thread-dump-`date +%s`.txt && echo "✅ Thread dump saved to logs/" || echo "❌ App not running."

histo: ## 🗑️ Create a memory usage histogram for analyzing memory leaks
	@[ -n "$(PID)" ] && jcmd $(PID) GC.class_histogram > logs/heap-histo-`date +%s`.txt && echo "✅ Heap histogram saved to logs/" || echo "❌ App not running."

diag: ## 🚑 Create a full diagnostic bundle (threads + heap) for triage
	@echo "Generating diagnostic bundle..."
	@$(MAKE) jstack && $(MAKE) histo

# ==============================================================================
# =                          DATABASE & UTILITIES                              =
# ==============================================================================

db-reset: python-setup ## 🔄 Resets the database from a clean template
	@echo "Resetting database..."
	@$(PYTHON) scripts/db_manager.py reset

db-ping: ## 🩺 Headlessly check database connectivity without starting the UI
	@$(MAVEN) -q -B exec:java -Dexec.mainClass=com.orphanagehub.tools.DbDoctor

db-sql: ## 💬 Headlessly run SQL: make db:sql q="SELECT COUNT(*) FROM TblUsers"
	@[ -n "$(q)" ] || (echo "Usage: make db:sql q=\"YOUR_QUERY\""; exit 2)
	@$(MAVEN) -q -B exec:java -Dexec.mainClass=com.orphanagehub.tools.DbShell -Dexec.args="$(q)"

log-analyze: python-setup ## 🐍 Analyze log files for errors using Python
	@echo "Analyzing application logs for errors..."
	@$(PYTHON) scripts/log_analyzer.py

python-setup: scripts/.venv/bin/activate ## 🐍 Sets up the isolated Python virtual environment
	@echo "Python environment is ready."

scripts/.venv/bin/activate: scripts/requirements.txt
	@echo "Creating Python virtual environment..."
	@python3 -m venv scripts/.venv
	@./scripts/.venv/bin/pip install -r scripts/requirements.txt
	@touch scripts/.venv/bin/activate

