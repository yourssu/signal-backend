# Multi-stage build for better caching
FROM eclipse-temurin:21-jre-jammy AS base

# Set timezone to KST
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Install system packages (this layer will be cached)
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Python dependencies stage
FROM base AS python-deps

WORKDIR /app

# Copy requirements file first for better Docker layer caching
COPY observer/script/requirements.txt /tmp/requirements.txt

# Create Python virtual environment and install dependencies
# This layer will be cached unless requirements.txt changes
RUN python3 -m venv /app/venv && \
    /app/venv/bin/pip install --upgrade pip --no-cache-dir && \
    /app/venv/bin/pip install -r /tmp/requirements.txt --no-cache-dir

# Final runtime stage
FROM base AS runtime

WORKDIR /app

# Copy Python virtual environment from previous stage
COPY --from=python-deps /app/venv /app/venv

# Copy pre-built JAR (built in GitHub Actions)
COPY app/build/libs/*-SNAPSHOT.jar app.jar

# Copy observer scripts
COPY observer/script/ /app/script/

# Create logs directory
RUN mkdir -p /app/logs/events && chmod 750 /app/logs /app/logs/events

# The same immutable image runs one independently supervised component.
RUN printf '#!/bin/bash\n\
set -eu\n\
umask 027\n\
cd /app\n\
case "${COMPONENT:-spring}" in\n\
  spring) exec java -Duser.timezone=Asia/Seoul -jar /app/app.jar --spring.profiles.active=${ENVIRONMENT:-dev} --server.port=${SERVER_PORT:-8080} ;;\n\
  observer) exec /app/venv/bin/python /app/script/observer.py ;;\n\
  *) echo "unknown COMPONENT" >&2; exit 64 ;;\n\
esac\n\
' > /app/start.sh && chmod +x /app/start.sh

# Expose port (default 8080, can be overridden by SERVER_PORT)
EXPOSE ${SERVER_PORT:-8080}

# Use the startup script as entrypoint
ENTRYPOINT ["/bin/bash", "/app/start.sh"]
