# GitHub Repositories API

This is a simple Spring Boot API that lists all non-forked repositories of a given GitHub user, along with their branches and last commit SHA.

## Features
- Fetches all non-forked repositories for a given user.
- Retrieves branch names and their last commit SHA.
- Returns 404 response for non-existent users.
- Includes a fully functional integration test.
- Minimalistic, single-file implementation.

## Tech Stack
- Java 17+
- Spring Boot
- WebClient (for GitHub API calls)
- Spring Boot Test (for integration testing)

## How to Run

1. Clone the repository
   ```sh
   https://github.com/Herbert-Moore/Atipera.git

2. Run the application
   mvn spring-boot:run

3. Run the integration test
   mvn test