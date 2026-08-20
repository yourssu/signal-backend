#!/bin/bash
set -eu

source .env

LEGACY_CONTAINER="${PROJECT_NAME}-container"
SPRING_CONTAINER="${PROJECT_NAME}-spring"
OBSERVER_CONTAINER="${PROJECT_NAME}-observer"
ADMIN_CONTAINER="${PROJECT_NAME}-admin"
IMAGE_TAG="${IMAGE_TAG:-latest}"
IMAGE_NAME="$ECR_REGISTRY/yourssu/${PROJECT_NAME}:${IMAGE_TAG}"

echo "Starting deployment process..."
echo "Image name: $IMAGE_NAME"

echo "Logging in to Amazon ECR Public..."
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws

echo "Pulling the latest image..."
docker pull "$IMAGE_NAME"

for container in "$LEGACY_CONTAINER" "$SPRING_CONTAINER" "$OBSERVER_CONTAINER" "$ADMIN_CONTAINER"; do
  if [ -n "$(docker ps -aq -f name=^/${container}$)" ]; then
    docker rm -f "$container"
  fi
done

echo "Cleaning up old images..."
docker images "$ECR_REGISTRY/yourssu/${PROJECT_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}" | tail -n +2 | sort -k4 -r | tail -n +2 | awk '{print $3}' | xargs -r docker rmi

echo "Starting independently restartable components..."
mkdir -p "$(pwd)/logs"

docker run -d \
  --name "$OBSERVER_CONTAINER" \
  --restart unless-stopped \
  -v "$(pwd)/logs:/app/logs" \
  --env-file .env \
  -e COMPONENT=observer \
  "$IMAGE_NAME"

docker run -d \
  --name "$SPRING_CONTAINER" \
  --restart unless-stopped \
  -p "$SERVER_PORT:$SERVER_PORT" \
  -v "$(pwd)/logs:/app/logs" \
  --env-file .env \
  -e COMPONENT=spring \
  "$IMAGE_NAME"

docker run -d \
  --name "$ADMIN_CONTAINER" \
  --restart unless-stopped \
  -p 127.0.0.1:3005:3005 \
  --env-file .env \
  -e COMPONENT=admin \
  "$IMAGE_NAME"

echo "Deployment completed successfully!"
docker ps -f name="$PROJECT_NAME"
