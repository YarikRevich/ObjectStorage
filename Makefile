dev := $(or $(dev), 'false')

ifneq (,$(wildcard .env))
include .env
export
endif

.PHONY: help
.DEFAULT_GOAL := help
help:
	@grep -h -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: clean
clean: ## Clean project area
	@mvn clean

.PHONY: prepare
prepare: ## Install prerequisites
	@mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.0:tree -Dverbose=true

.PHONY: test
test: clean ## Run both unit and integration tests
	@mvn test
	@mvn verify

.PHONY: lint
lint: ## Run Apache Spotless linter
	@mvn spotless:apply

.PHONY: create-local-api-server
create-local-api-server: ## Create ObjectStorage local directory for API Server
	@mkdir -p $(HOME)/.objectstorage/config
	@mkdir -p $(HOME)/.objectstorage/diagnostics/prometheus/internal
	@mkdir -p $(HOME)/.objectstorage/diagnostics/prometheus/config
	@mkdir -p $(HOME)/.objectstorage/diagnostics/grafana/internal
	@mkdir -p $(HOME)/.objectstorage/diagnostics/grafana/config/dashboards
	@mkdir -p $(HOME)/.objectstorage/diagnostics/grafana/config/datasources
	@mkdir -p $(HOME)/.objectstorage/workspace
	@mkdir -p $(HOME)/.objectstorage/internal/database
	@mkdir -p $(HOME)/.objectstorage/internal/state

.PHONY: clone-client-config
clone-client-config: ## Clone configuration files to local directory
	@cp -r ./samples/config/client/user.yaml $(HOME)/.objectstorage/config

.PHONY: clone-api-server-config
clone-api-server-config: ## Clone ObjectStorage API Server configuration files to local directory
	@cp -r ./config/grafana/dashboards/dashboard.yml $(HOME)/.objectstorage/diagnostics/grafana/config/dashboards
	@cp -r ./config/grafana/dashboards/diagnostics.tmpl $(HOME)/.objectstorage/diagnostics/grafana/config/dashboards
	@cp -r ./config/grafana/datasources/datasource.tmpl $(HOME)/.objectstorage/diagnostics/grafana/config/datasources
	@cp -r ./config/prometheus/prometheus.tmpl $(HOME)/.objectstorage/diagnostics/prometheus/config
	@cp -r ./samples/config/api-server/api-server.yaml $(HOME)/.objectstorage/config

.PHONY: clone-cluster
clone-cluster: ## Clone Cluster JAR into a ObjectStorage local directory
ifeq (,$(wildcard $(HOME)/.objectstorage/bin/cluster))
	@mkdir -p $(HOME)/.objectstorage/bin
endif
	@cp -r ./bin/cluster $(HOME)/.objectstorage/bin/

.PHONY: clone-api-server
clone-api-server: ## Clone API Server JAR into a ObjectStorage local directory
ifeq (,$(wildcard $(HOME)/.objectstorage/bin/api-server))
	@mkdir -p $(HOME)/.objectstorage/bin
endif
	@cp -r ./bin/api-server $(HOME)/.objectstorage/bin/

.PHONY: build-api-server
build-api-server: clean create-local-api-server clone-api-server-config ## Build API Server application
ifneq (,$(wildcard ./bin/api-server))
	@rm -r ./bin/api-server
endif
ifeq ($(dev), 'false')
	@mvn -pl api-server -T10 install -U
else
	@mvn -P dev -pl api-server -T10 install -U
endif
	$(MAKE) clone-api-server

.PHONY: build-cli
build-cli: clean clone-client-config ## Build CLI application
ifneq (,$(wildcard ./bin/cli))
	@rm -r ./bin/cli
endif
ifeq ($(dev), 'false')
	@mvn -pl cli -T10 install -U
else
	@mvn -P dev -pl cli -T10 install -U
endif