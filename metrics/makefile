# Variables
DOCKER_REPO ?= py846260131/sts-service-metrics
PLATFORM ?= arm
DOCKERFILE = Dockerfile

# Build variables
VERSION := $(shell \
  if [ -f "build.gradle.kts" ]; then \
    grep '^version\s*=' "build.gradle.kts" | awk -F'=' '{gsub(/["'\''\s]/, "", $$2); print $$2}'; \
  elif [ -f "build.gradle" ]; then \
    grep '^version\s*=' "build.gradle" | awk -F'=' '{gsub(/["'\''\s]/, "", $$2); print $$2}'; \
  else \
    echo "No build.gradle or build.gradle.kts found!"; \
    exit 1; \
  fi)
ARCH_SUFFIX := $(if $(filter arm,$(PLATFORM)),-arm,)
DOCKER_TAG := $(strip $(VERSION)$(ARCH_SUFFIX))
DOCKER_IMAGE := $(DOCKER_REPO):$(DOCKER_TAG)

# Targets
.PHONY: build
build:
	@echo $(DOCKER_IMAGE)
	@echo "Building Docker image for platform: $(PLATFORM)"
	docker buildx build --platform=$(if $(filter arm,$(PLATFORM)),linux/arm64,linux/amd64) -t $(DOCKER_IMAGE) -f $(DOCKERFILE) ../

.PHONY: push
push: 
	@echo "Pushing Docker image: $(DOCKER_IMAGE)"
	docker push $(DOCKER_IMAGE)

.PHONY: start
start:
	@echo "Starting services with docker-compose using image: $(DOCKER_IMAGE)"
	# Prompt for environment variables if not set
	@if [ -z "$$REDIS_PASSWORD" ]; then \
		read -p "Please enter REDIS_PASSWORD: " REDIS_PASSWORD; \
		export REDIS_PASSWORD; \
	fi
	@if [ -z "$$CLICKHOUSE_PASSWORD" ]; then \
		read -p "Please enter CLICKHOUSE_PASSWORD: " CLICKHOUSE_PASSWORD; \
		export CLICKHOUSE_PASSWORD; \
	fi
	DOCKER_IMAGE=$(DOCKER_IMAGE) REDIS_PASSWORD=$$REDIS_PASSWORD CLICKHOUSE_PASSWORD=$$CLICKHOUSE_PASSWORD docker-compose up -d

stop:
	@echo "Stopping container..."
	DOCKER_IMAGE=$(DOCKER_IMAGE) REDIS_PASSWORD="" CLICKHOUSE_PASSWORD="" docker-compose down

.PHONY: clean
clean:
	@echo "Cleaning up..."
	docker image prune -f
