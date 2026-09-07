.PHONY: up down logs build test

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f

build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

run:
	./mvnw spring-boot:run