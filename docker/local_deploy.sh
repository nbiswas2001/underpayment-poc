#!/bin/bash
cd ~/Dev/java/underpayments
./mvnw clean package
 docker build -t underpayments:latest -f docker/Dockerfile .
 docker run underpayments:latest -p 9007:9007 --name underpayments-service