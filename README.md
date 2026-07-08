# Java Test Task S3 New Department

## Description

Spring Boot application that regularly generates random customer and order data and saves it into PostgreSQL.

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Lombok
- Scheduler

## Database

PostgreSQL database:
        - KundenDB

### Tables
- kunde
- auftrage
 
### Relations
auftrage.kunde_id → kunde.kunde_id

Kunde 1 -(has)- mc Auftage

## Configuration 

- Database credentials are stored in .env

## Run from terminal

- create .env file as in .env.example

set -a
source .env
set +a
mvn -U clean install
mvn spring-boot:run