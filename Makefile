# OrphanageHub - Production Workflow System
# ============================================
.DEFAULT_GOAL := help
SHELL := /bin/bash

# ============================================
# Configuration
# ============================================
APP_NAME := OrphanageHub
APP_MAIN_CLASS := com.orphanagehub.gui.OrphanageHubApp
DEBUG_PORT ?= 5005
JMX_PORT ?= 9010
MAVEN_OPTS := -Xmx1024m -Dfile.encoding=UTF-8
# Fixed VERSION extraction
VERSION := $(shell grep '<version>' pom.xml | head -1 | sed 's/.*<version>//' | sed 's/<\/version>.*//')

# ============================================
# OS Detection
# ============================================
ifeq ($(OS),Windows_NT)
    MAVEN := mvnw.cmd
    SEP := ;
    PYTHON := python
    RM := del /Q
    MKDIR := mkdir
    CP_CMD := copy
    NULL := nul
else
    MAVEN := ./mvnw
    SEP := :
    PYTHON := python3
    RM := rm -f
    MKDIR := mkdir -p
    CP_CMD := cp
    NULL := /dev/null
endif

# Check if Maven wrapper exists, fallback to system maven
ifeq ($(wildcard $(MAVEN)),)
    MAVEN := mvn
endif

# PID detection with error handling
get_pid = $(shell jps -l 2>$(NULL) | grep $(APP_MAIN_CLASS) | awk '{print $$1}' | head -1)

# ============================================
# PHONY Targets Declaration
# ============================================
.PHONY: help build run clean format check test ci run-dev debug jdb pid \
        thread-dump heap-dump diag python-setup db-reset db-ping db-sql \
        log-analyze setup sanitize section-compile section-watch section-clean \
        section-rebuild section-run classpath doctor doctor-diagnose \
        doctor-interactive doctor-fix doctor-watch doctor-report doctor-fortify \
        dr df dft install deps validate package quick-build coverage \
        security-check update-deps profile benchmark docker-build docker-run \
        release backup restore health-check

# ============================================
# HELP & INFO
# ============================================
help: ## Show this help message
	@echo "╔══════════════════════════════════════════════════════════════════════╗"
	@echo "║            OrphanageHub Management System - v$(VERSION)               ║"
	@echo "╚══════════════════════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@grep -E '^[a-zA-Z0-9_.-]+:.*?## ' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "Quick start: make setup && make run"
	@echo "Development: make run-dev"
	@echo "Testing:     make test"
	@echo ""

# ============================================
# CORE BUILD COMMANDS
# ============================================
install: ## Install Maven wrapper if missing
	@if [ ! -f "./mvnw" ]; then \
		echo "📦 Installing Maven wrapper..."; \
		mvn wrapper:wrapper -Dmaven=3.9.6; \
		chmod +x mvnw; \
	fi
	@echo "✅ Maven wrapper ready"

deps: ## Download all dependencies
	@echo "📥 Downloading dependencies..."
	$(MAVEN) dependency:resolve
	$(MAVEN) dependency:resolve-plugins
	@echo "✅ Dependencies resolved"

validate: ## Validate project structure
	@echo "🔍 Validating project..."
	$(MAVEN) validate
	@echo "✅ Project structure valid"

quick-build: ## Fast build without clean
	@echo "⚡ Quick build..."
	$(MAVEN) compile
	@echo "✅ Quick build complete"

package: ## Package without running tests
	@echo "📦 Building package..."
	$(MAVEN) clean package -DskipTests
	@echo "✅ Package built: target/$(APP_NAME)-$(VERSION).jar"

build: clean package ## Full build (clean + package)
	@echo "✅ Full build complete"

run: build ## Run application
	@echo "🚀 Starting $(APP_NAME)..."
	$(MAVEN) exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) \
		-Dexec.cleanupDaemonThreads=false

clean: ## Clean all build artifacts
	@echo "🧹 Cleaning project..."
	$(MAVEN) clean
	$(RM) -rf target/
	$(RM) -rf logs/*.log
	$(RM) -rf .section-cache/
	@echo "✅ Project cleaned"

# ============================================
# QUALITY & TESTING
# ============================================
format: ## Apply code formatting
	@echo "🎨 Formatting code..."
	@if $(MAVEN) help:effective-pom | grep -q spotless; then \
		$(MAVEN) spotless:apply; \
	else \
		echo "⚠️  Spotless not configured"; \
	fi

check: ## Run static analysis checks
	@echo "🔍 Running static analysis..."
	@if $(MAVEN) help:effective-pom | grep -q spotless; then \
		$(MAVEN) spotless:check; \
	fi
	@if $(MAVEN) help:effective-pom | grep -q spotbugs; then \
		$(MAVEN) spotbugs:check; \
	fi
	@echo "✅ Static analysis complete"

test: db-reset ## Run all tests
	@echo "🧪 Running tests..."
	$(MAVEN) test
	@echo "✅ All tests passed"

test-unit: ## Run unit tests only
	@echo "🧪 Running unit tests..."
	$(MAVEN) test -Dtest="!*IntegrationTest"

test-integration: db-reset ## Run integration tests only
	@echo "🧪 Running integration tests..."
	$(MAVEN) test -Dtest="*IntegrationTest"

coverage: test ## Generate test coverage report
	@echo "📊 Generating coverage report..."
	$(MAVEN) jacoco:report
	@echo "✅ Coverage report: target/site/jacoco/index.html"

ci: clean check test coverage build ## Full CI pipeline
	@echo "✅ CI pipeline complete"

# ============================================
# SECURITY & MAINTENANCE
# ============================================
security-check: ## Run OWASP dependency check
	@echo "🔒 Running security check..."
	$(MAVEN) org.owasp:dependency-check-maven:check
	@echo "✅ Security check complete: target/dependency-check-report.html"

update-deps: ## Check for dependency updates
	@echo "🔄 Checking for updates..."
	$(MAVEN) versions:display-dependency-updates
	$(MAVEN) versions:display-plugin-updates

# ============================================
# DEBUG & DIAGNOSTICS
# ============================================
run-dev: quick-build ## Run in development mode
	@echo "🔧 Starting in development mode..."
	MAVEN_OPTS="$(MAVEN_OPTS) -Dapp.env=dev" \
	$(MAVEN) exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) \
		-Dexec.cleanupDaemonThreads=false

debug: quick-build ## Run with remote debugging on port $(DEBUG_PORT)
	@echo "🐛 Starting in debug mode on port $(DEBUG_PORT)..."
	@echo "   Connect your debugger to localhost:$(DEBUG_PORT)"
	MAVEN_OPTS="$(MAVEN_OPTS) -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$(DEBUG_PORT)" \
	$(MAVEN) exec:java -Dexec.mainClass=$(APP_MAIN_CLASS) \
		-Dexec.cleanupDaemonThreads=false

jdb: ## Attach JDB debugger to port $(DEBUG_PORT)
	jdb -attach localhost:$(DEBUG_PORT)

pid: ## Show application PID
	@PID=$(call get_pid); \
	if [ -n "$$PID" ]; then \
		echo "✅ Application PID: $$PID"; \
		ps -p $$PID -o pid,vsz,rss,comm; \
	else \
		echo "❌ Application not running"; \
	fi

thread-dump: ## Generate thread dump
	@PID=$(call get_pid); \
	if [ -n "$$PID" ]; then \
		$(MKDIR) logs; \
		jcmd $$PID Thread.print > logs/thread-dump-$$(date +%Y%m%d-%H%M%S).txt && \
		echo "✅ Thread dump saved to logs/"; \
	else \
		echo "❌ Application not running"; \
	fi

heap-dump: ## Generate heap dump
	@PID=$(call get_pid); \
	if [ -n "$$PID" ]; then \
		$(MKDIR) logs; \
		jcmd $$PID GC.heap_dump logs/heap-$$(date +%Y%m%d-%H%M%S).hprof && \
		echo "✅ Heap dump saved to logs/"; \
	else \
		echo "❌ Application not running"; \
	fi

diag: thread-dump heap-dump ## Generate all diagnostics
	@echo "✅ Diagnostics complete"

health-check: ## Check application health
	@PID=$(call get_pid); \
	if [ -n "$$PID" ]; then \
		echo "✅ Application is running (PID: $$PID)"; \
		jcmd $$PID VM.uptime; \
		jcmd $$PID GC.heap_info; \
	else \
		echo "❌ Application not running"; \
	fi

# ============================================
# UTILITIES & SETUP
# ============================================
python-setup: scripts/.venv/bin/activate ## Setup Python environment

scripts/.venv/bin/activate: scripts/requirements.txt
	@echo "🐍 Setting up Python virtual environment..."
	@$(PYTHON) -m venv scripts/.venv
	@scripts/.venv/bin/pip install --upgrade pip
	@scripts/.venv/bin/pip install -r scripts/requirements.txt
	@touch scripts/.venv/bin/activate
	@echo "✅ Python environment ready"

setup: install python-setup ## Complete project setup
	@echo "🔧 Setting up $(APP_NAME) project..."
	@$(MKDIR) logs db target src/main/resources src/test/resources
	@$(MKDIR) src/main/java/com/orphanagehub
	@$(MKDIR) src/test/java/com/orphanagehub
	@if [ ! -f db/template.accdb ] && [ -f db/OrphanageHub.accdb ]; then \
		echo "📁 Creating DB template..."; \
		$(CP_CMD) db/OrphanageHub.sqlite db/template.sqlite; \
	fi
	@$(MAVEN) dependency:resolve
	@echo "✅ Setup complete! Run 'make run' to start."

sanitize: python-setup ## Clean source files
	@echo "🧹 Sanitizing source files..."
	@scripts/.venv/bin/python scripts/sanitize_sources.py

backup: ## Backup project data
	@echo "💾 Creating backup..."
	@$(MKDIR) backups
	@tar -czf backups/backup-$$(date +%Y%m%d-%H%M%S).tar.gz \
		--exclude=target --exclude=logs --exclude=.git \
		--exclude=scripts/.venv .
	@echo "✅ Backup created in backups/"

restore: ## Restore from latest backup
	@if [ -z "$(file)" ]; then \
		echo "Usage: make restore file=backups/backup-XXX.tar.gz"; \
		exit 1; \
	fi
	@echo "📂 Restoring from $(file)..."
	@tar -xzf $(file)
	@echo "✅ Restore complete"

# ============================================
# DATABASE MANAGEMENT
# ============================================
classpath: ## Get runtime classpath
	@$(MAVEN) -q dependency:build-classpath \
		-Dmdep.outputFile=target/classpath.txt \
		-Dmdep.includeScope=runtime \
		-Dmdep.pathSeparator='$(SEP)' 2>$(NULL) || true
	@if [ -f target/classpath.txt ]; then \
		echo "target/classes$(SEP)$$(cat target/classpath.txt)"; \
	else \
		echo "target/classes"; \
	fi

db-ping: quick-build ## Test database connectivity
	@echo "🔌 Testing database connection..."
	@CP=$$($(MAKE) -s classpath); \
	java -cp "$$CP" com.orphanagehub.tools.DbDoctor || \
	echo "❌ DB connection failed"

db-reset: ## Reset database from template
	@echo "🔄 Resetting database..."
	@if [ ! -f db/template.accdb ]; then \
		echo "❌ ERROR: db/template.sqlite"; \
		echo "Run 'make setup' first"; \
		exit 1; \
	fi
	@$(CP_CMD) -f db/template.sqlite db/OrphanageHub.sqlite
	@echo "✅ Database reset from template"

db-sql: ## Execute SQL query (usage: make db-sql q="SELECT * FROM users")
	@if [ -z "$(q)" ]; then \
		echo "Usage: make db-sql q=\"YOUR_SQL_QUERY\""; \
		exit 1; \
	fi
	@CP=$$($(MAKE) -s classpath); \
	java -cp "$$CP" com.orphanagehub.tools.DbShell "$(q)"

log-analyze: python-setup ## Analyze application logs
	@echo "📊 Analyzing logs..."
	@scripts/.venv/bin/python scripts/log_analyzer.py

# ============================================
# SECTION BUILD SYSTEM
# ============================================
section-compile: python-setup ## Fast incremental compilation
	@echo "⚡ Running section compiler..."
	@scripts/.venv/bin/python scripts/section_build.py

section-watch: python-setup ## Watch mode with live compilation
	@echo "👁️  Starting watch mode..."
	@scripts/.venv/bin/python scripts/section_build.py --watch

section-clean: python-setup ## Clean section build artifacts
	@echo "🧹 Cleaning section cache..."
	@scripts/.venv/bin/python scripts/section_build.py --clean

section-rebuild: python-setup ## Force rebuild all sections
	@echo "🔨 Rebuilding all sections..."
	@scripts/.venv/bin/python scripts/section_build.py --force

section-run: section-compile ## Run with section-compiled classes
	@CP=$$($(MAKE) -s classpath); \
	echo "🚀 Running with section-compiled classes..."; \
	java -cp "$$CP" $(APP_MAIN_CLASS)

# ============================================
# DOCTOR SYSTEM
# ============================================
doctor: doctor-interactive ## Run doctor interactively (default)

doctor-diagnose: python-setup ## Diagnose issues only
	@echo "🩺 Running diagnostics..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py java diagnose

doctor-interactive: python-setup ## Interactive diagnosis and fix
	@echo "👨‍⚕️ Starting interactive doctor..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py java interactive

doctor-fix: python-setup ## Auto-fix high-confidence issues
	@echo "🔧 Auto-fixing issues..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py java fix

doctor-watch: python-setup ## Watch mode with auto-fix
	@echo "👁️  Starting doctor watch mode..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py java watch

doctor-report: python-setup ## Generate health report
	@echo "📊 Generating health report..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py java report

doctor-fortify: python-setup ## Fortify specific file
	@if [ -z "$(file)" ]; then \
		echo "Usage: make doctor-fortify file=path/to/File.java"; \
		exit 1; \
	fi
	@echo "🛡️  Fortifying $(file)..."
	@scripts/.venv/bin/python scripts/doctor_chimera.py fortify $(file)

# Shortcuts
dr: doctor-interactive ## Shortcut for doctor-interactive
df: doctor-fix ## Shortcut for doctor-fix
dft: doctor-fortify ## Shortcut for doctor-fortify

# ============================================
# ADVANCED & DEPLOYMENT
# ============================================
profile: quick-build ## Run with profiling
	@echo "📈 Starting with profiling enabled..."
	MAVEN_OPTS="$(MAVEN_OPTS) \
		-Dcom.sun.management.jmxremote \
		-Dcom.sun.management.jmxremote.port=$(JMX_PORT) \
		-Dcom.sun.management.jmxremote.authenticate=false \
		-Dcom.sun.management.jmxremote.ssl=false" \
	$(MAVEN) exec:java -Dexec.mainClass=$(APP_MAIN_CLASS)

benchmark: quick-build ## Run benchmarks
	@echo "⏱️  Running benchmarks..."
	$(MAVEN) exec:java -Dexec.mainClass=com.orphanagehub.benchmark.BenchmarkRunner

docker-build: ## Build Docker image
	@echo "🐳 Building Docker image..."
	docker build -t orphanagehub:$(VERSION) -t orphanagehub:latest .
	@echo "✅ Docker image built: orphanagehub:$(VERSION)"

docker-run: ## Run in Docker
	@echo "🐳 Running in Docker..."
	docker run -it --rm \
		-p 8080:8080 \
		-v $$(pwd)/db:/app/db \
		-v $$(pwd)/logs:/app/logs \
		orphanagehub:latest

release: ci ## Create release package
	@echo "📦 Creating release package..."
	@$(MKDIR) release
	@$(CP_CMD) target/$(APP_NAME)-$(VERSION).jar release/
	@$(CP_CMD) README.md release/
	@$(CP_CMD) -r db/template.sqlite release/
	@tar -czf release/$(APP_NAME)-$(VERSION)-release.tar.gz -C release .
	@echo "✅ Release package: release/$(APP_NAME)-$(VERSION)-release.tar.gz"

# ============================================
# CONFIGURATION
# ============================================
.NOTPARALLEL: