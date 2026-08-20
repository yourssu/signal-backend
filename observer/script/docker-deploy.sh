#!/bin/bash

# Load environment variables
source .env

# Variables
SPRING_CONTAINER="${PROJECT_NAME}-spring"
OBSERVER_CONTAINER="${PROJECT_NAME}-observer"
IMAGE_TAG="${IMAGE_TAG:-latest}"
IMAGE_NAME="$ECR_REGISTRY/yourssu/${PROJECT_NAME}:${IMAGE_TAG}"

echo "Starting deployment process..."
echo "Image name: $IMAGE_NAME"

# Authenticate to ECR Public (token expires every 12 hours)
echo "Logging in to Amazon ECR Public..."
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws

# Pull the latest image
echo "Pulling the latest image..."
docker pull $IMAGE_NAME

# Check if container is running
for container in "$SPRING_CONTAINER" "$OBSERVER_CONTAINER"; do
  if [ "$(docker ps -aq -f name=^/${container}$)" ]; then docker rm -f "$container"; fi
done

# Remove old images (keep only the 1 most recent)
echo "Cleaning up old images..."
docker images $ECR_REGISTRY/yourssu/${PROJECT_NAME} --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}" | tail -n +2 | sort -k4 -r | tail -n +2 | awk '{print $3}' | xargs -r docker rmi

# Run the new container
echo "Starting new container..."
mkdir -p "$(pwd)/logs/state"
docker run -d \
  --name "$OBSERVER_CONTAINER" \
  --restart no \
  -v $(pwd)/logs:/app/logs \
  --env-file .env \
  -e COMPONENT=observer \
  --health-cmd "/app/venv/bin/python /app/script/healthcheck.py observer" \
  --health-interval 30s --health-retries 3 --health-timeout 10s --health-start-period 30s \
  $IMAGE_NAME

docker run -d \
  --name "$SPRING_CONTAINER" \
  --restart no \
  -p $SERVER_PORT:$SERVER_PORT \
  -v $(pwd)/logs:/app/logs \
  --env-file .env \
  -e COMPONENT=spring \
  --health-cmd "/app/venv/bin/python /app/script/healthcheck.py spring" \
  --health-interval 30s --health-retries 3 --health-timeout 10s --health-start-period 60s \
  $IMAGE_NAME

if [ -f "$(pwd)/supervisor.pid" ]; then kill "$(cat "$(pwd)/supervisor.pid")" 2>/dev/null || true; fi
nohup bash "$(pwd)/supervise.sh" > "$(pwd)/logs/supervisor.log" 2>&1 &
echo $! > "$(pwd)/supervisor.pid"

echo "Deployment completed successfully!"
echo "Container status:"
docker ps -f name="$PROJECT_NAME"
