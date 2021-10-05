#!/bin/bash
container_id=$(docker ps | grep spay-winter-fuel-service | awk '{print $1}')
docker exec -it $container_id sh