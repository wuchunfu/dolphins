#!/usr/bin/env bash

DOCKER_BUILDKIT=1 docker build --build-arg MODULE=dolphin-saas-dashboard -t dolphin-saas-dashboard:latest .
DOCKER_BUILDKIT=1 docker build --build-arg MODULE=dolphin-saas-cronjob -t dolphin-saas-cronjob:latest .
DOCKER_BUILDKIT=1 docker build --build-arg MODULE=dolphin-saas-backend -t dolphin-saas-backend:latest .