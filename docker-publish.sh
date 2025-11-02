#!/bin/bash

set -e

# Get current git SHA
GIT_SHA=$(git rev-parse --short HEAD)

# Docker Hub username
DOCKER_USERNAME="nibbio84"

# Platforms to build for
PLATFORMS="linux/arm64,linux/amd64"

echo "Publishing images with tags: $GIT_SHA and latest"
echo "Platforms: $PLATFORMS"

# Ensure buildx is set up
echo "Setting up Docker buildx..."
docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder || true

# Build and publish common-api
echo "Building and pushing common-api for multiple platforms..."
docker buildx build \
  --platform ${PLATFORMS} \
  -t ${DOCKER_USERNAME}/common-api:${GIT_SHA} \
  -t ${DOCKER_USERNAME}/common-api:latest \
  -f Dockerfile \
  --push \
  .

echo "Successfully published:"
echo "  - ${DOCKER_USERNAME}/common-api:${GIT_SHA}"
echo "  - ${DOCKER_USERNAME}/common-api:latest"
echo "Platforms: $PLATFORMS"
