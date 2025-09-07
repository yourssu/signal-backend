# Multi-stage build for better caching
FROM openjdk:21-jdk-slim as base

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
FROM base as python-deps

WORKDIR /app

# Copy requirements file first for better Docker layer caching
COPY script/requirements.txt /tmp/requirements.txt

# Create Python virtual environment and install dependencies
# This layer will be cached unless requirements.txt changes
RUN python3 -m venv /app/venv && \
    /app/venv/bin/pip install --upgrade pip --no-cache-dir && \
    /app/venv/bin/pip install -r /tmp/requirements.txt --no-cache-dir

# Final runtime stage
FROM base as runtime

WORKDIR /app

# Copy Python virtual environment from previous stage
COPY --from=python-deps /app/venv /app/venv

# Copy pre-built JAR (built in GitHub Actions)
COPY build/libs/*-SNAPSHOT.jar app.jar

# Copy observer scripts
COPY script/ /app/script/

# Create logs directory
RUN mkdir -p /app/logs

# Create startup script
RUN printf '#!/bin/bash\n\
set -e\n\
\n\
# Start observer script first\n\
cd /app\n\
/app/venv/bin/python /app/script/observer.py &\n\
OBSERVER_PID=$!\n\
\n\
# Wait a moment for observer to initialize\n\
sleep 2\n\
\n\
# Start Spring Boot application\n\
java -Duser.timezone=Asia/Seoul -jar /app/app.jar --spring.profiles.active=${ENVIRONMENT:-dev} --server.port=${SERVER_PORT:-8080} &\n\
SPRING_PID=$!\n\
\n\
# Wait for either process to exit\n\
wait $SPRING_PID $OBSERVER_PID\n\
' > /app/start.sh && chmod +x /app/start.sh

# Expose port (default 8080, can be overridden by SERVER_PORT)
EXPOSE ${SERVER_PORT:-8080}

# Use the startup script as entrypoint
ENTRYPOINT ["/bin/bash", "/app/start.sh"]