# OrphanageHub - Definitive Workflow
.DEFAULT_GOAL := help
SHELL := /bin/bash

APP_MAIN_CLASS := com.orphanagehub.gui.OrphanageHubApp
DEBUG_PORT ?= 5005

# OS detection
ifeq ($(OS),Windows_NT)
	MAVEN := ./mvnw.cmd
	SEP := ;
	PYTHON := python
else
	MAVEN := ./mvnw
	SEP := :
	PYTHON := python3
endif

# PID detection
ifeq ($(OS),Windows_NT)
	PID := $(shell jps -l | findstr $(APP_MAIN_CLASS) | awk '{print $$1}')
else
	PID := $(shell jps -l | grep $(APP_MAIN_CLASS) | awk '{print $$1}')
endif

.PHONY: help build run clean format check test ci run-dev debug jdb pid thread-dump heap-dump diag \
        python-setup db-reset db-ping db-sql log-analyze setup sanitize \
        section-compile section-watch section-clean section-rebuild section-run classpath \
        doctor doctor-diagnose doctor-interactive doctor-fix doctor-watch doctor-report doctor-fortify dr df dft

# =============================================================================
# HELP
# =============================================================================
help:
	@echo "OrphanageHub Project Commands"
	@echo ""
	@grep -E '^[a-zA-Z0-9_.-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-22s\033[0m %s\n", $$1, $$2}'

# =============================================================================
# CORE
# =============================================================================
build: ## Build with Maven (package, skip tests)
	$(MAVEN) -B -q package -DskipTests

run: build ## Run via Maven (slower)
	$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS)

clean: ## Clean Maven artifacts
	$(MAVEN) -B -q clean

# =============================================================================
# QUALITY & TESTS
# =============================================================================
format: ## Apply Spotless formatting
	$(MAVEN) -B spotless:apply

check: ## Spotless + SpotBugs check
	$(MAVEN) -B spotless:check spotbugs:check

test: db-reset ## Run all JUnit tests
	$(MAVEN) -B test

ci: ## Full CI pipeline
	$(MAKE) check
	$(MAKE) test
	$(MAKE) build

# =============================================================================
# DEBUG & DIAGNOSTICS
# =============================================================================
run-dev: build ## Run with assertions and dev flag
	$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) -Dexec.jvmArgs="-ea -Dapp.env=dev"

debug: build ## Run with JDWP open on $(DEBUG_PORT)
	$(MAVEN) -q exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) -Dexec.jvmArgs="-ea -Dapp.env=dev -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${DEBUG_PORT}"

jdb: ## Attach CLI debugger
	jdb -attach localhost:$(DEBUG_PORT)

pid: ## Print PID
	@if [ -n "$(PID)" ]; then echo "PID: $(PID)"; else echo "App not running."; fi

thread-dump: ## Thread dump
	@if [ -n "$(PID)" ]; then jcmd $(PID) Thread.print > logs/thread-dump-`date +%s`.txt && echo "Saved to logs/"; else echo "App not running."; fi

heap-dump: ## Heap histogram
	@if [ -n "$(PID)" ]; then jcmd $(PID) GC.class_histogram > logs/heap-histo-`date +%s`.txt && echo "Saved to logs/"; else echo "App not running."; fi

diag: ## Bundle diagnostics
	$(MAKE) thread-dump && $(MAKE) heap-dump

# =============================================================================
# UTILITIES & SETUP
# =============================================================================
python-setup: scripts/.venv/bin/activate ## Setup venv and deps
	@echo "Python environment is ready."

scripts/.venv/bin/activate: scripts/requirements.txt
	@echo "Creating Python virtual environment..."
	@$(PYTHON) -m venv scripts/.venv
	@./scripts/.venv/bin/pip install -r scripts/requirements.txt
	@touch scripts/.venv/bin/activate

setup: python-setup ## Create dirs and DB template if not present
	@mkdir -p logs db target
	@if [ ! -f db/template.accdb ] && [ -f db/OrphanageHub.accdb ]; then \
		echo "Creating template from OrphanageHub.accdb"; \
		cp db/OrphanageHub.accdb db/template.accdb; \
	fi
	@echo "✅ Setup complete."

sanitize: python-setup ## Remove fence artifacts and fix package lines
	@./scripts/.venv/bin/python scripts/sanitize_sources.py

# =============================================================================
# DATABASE (raw java, no Maven exec plugin confusion)
# =============================================================================
classpath: ## Print runtime classpath for raw java
	@$(MAVEN) -q -B dependency:build-classpath \
		-Dmdep.outputFile=target/ext-cp.txt \
		-Dmdep.includeScope=compile \
		-Dmdep.outputAbsoluteArtifactFilename=true \
		-Dmdep.pathSeparator='$(SEP)' >/dev/null 2>&1 || true
	@CP="target/section-classes$(SEP)target/classes"; \
	if [ -f target/ext-cp.txt ]; then \
		EXT=$$(cat target/ext-cp.txt); \
		CP="$$CP$(SEP)$$EXT"; \
	fi; \
	echo $$CP

db-ping: ## Test DB connectivity headlessly
	@CP=$$($(MAKE) -s classpath); \
	java -cp "$$CP" com.orphanagehub.tools.DbDoctor

db-reset: ## Reset DB from template
	@if [ ! -f db/template.accdb ]; then echo "ERROR: db/template.accdb not found"; exit 2; fi
	@cp db/template.accdb db/OrphanageHub.accdb
	@echo "✅ Database reset."

db-sql: ## Run SQL: make db-sql q="SELECT COUNT(*) FROM TblUsers"
	@[ -n "$(q)" ] || (echo 'Usage: make db-sql q="YOUR_QUERY"'; exit 2)
	@CP=$$($(MAKE) -s classpath); \
	java -cp "$$CP" com.orphanagehub.tools.DbShell "$(q)"

log-analyze: python-setup ## Analyze logs via Python
	@./scripts/.venv/bin/python scripts/log_analyzer.py

# =============================================================================
# ULTRA-FAST SECTION-BASED COMPILATION
# =============================================================================
section-compile: python-setup ## Fast incremental compile
	@./scripts/.venv/bin/python scripts/section_build.py

section-watch: python-setup ## Watch compile with dashboard
	@./scripts/.venv/bin/python scripts/section_build.py --watch

section-clean: python-setup ## Clean section artifacts
	@./scripts/.venv/bin/python scripts/section_build.py --clean

section-rebuild: python-setup ## Force rebuild all
	@./scripts/.venv/bin/python scripts/section_build.py --force

section-run: ## Run instantly using precompiled classes
	@CP=$$($(MAKE) -s classpath); \
	echo "🚀 Launching with pre-compiled classes..."; \
	java -cp "$$CP" $(APP_MAIN_CLASS)

# =============================================================================
# DOCTOR CHIMERA
# =============================================================================
doctor: doctor-interactive ## Run interactively

doctor-diagnose: python-setup ## Diagnose only
	@./scripts/.venv/bin/python scripts/doctor_chimera.py java diagnose

doctor-interactive: python-setup ## Diagnose & fix interactively
	@./scripts/.venv/bin/python scripts/doctor_chimera.py java interactive

doctor-fix: python-setup ## Auto-fix (high-confidence)
	@./scripts/.venv/bin/python scripts/doctor_chimera.py java fix

doctor-watch: python-setup ## Watch and fix on save
	@./scripts/.venv/bin/python scripts/doctor_chimera.py java watch

doctor-report: python-setup ## Generate report
	@./scripts/.venv/bin/python scripts/doctor_chimera.py java report

doctor-fortify: python-setup ## Fortify file (AI optional)
	@./scripts/.venv/bin/python scripts/doctor_chimera.py fortify $(file)

# Short aliases
dr: doctor-interactive
df: doctor-fix
dft: doctor-fortify
