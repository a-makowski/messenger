# Messenger API

The Messenger API is a backend java application built using Spring Boot.

## Features:

- **Spring Boot**: Built on top of the Spring Boot framework.
- **SQL Database**: Uses a SQL database to store and manage messaging data efficiently.
- **JWT Authentication**: Implements JSON Web Tokens authentication for secure access to API endpoints.
- **Swagger Documentation**: Includes Swagger documentation for easy exploration and integration of API endpoints. [Click here to see documentation](https://messenger-00398fef4475.herokuapp.com/swagger-ui/index.html)
- **Deployment on Heroku**: Deployed on the Heroku platform, for testing and demo purposes.

## Continuous Integration:

- **Dependabot**: Uses Dependabot to automatically check for and apply updates to project dependencies, ensuring that application remains up-to-date with the latest security patches and enhancements.
- **GitHub Actions**: Uses GitHub Actions as the Continuous Integration (CI) tool for automated testing, building, and deployment processes.

## How to try application:

Application is deployed on [Heroku](https://www.heroku.com/) to try it out with Postman. There is a [postman_collection.json](https://github.com/a-makowski/messenger/blob/main/postman_collection_heroku.json) file in a root folder with all necessary requests to test Messenger app.

## Local deployment:

##### Quick start with h2 in memory database:
   - Application starts with h2 database by default. To start application use this command in a root folder: 

           mvn clean spring-boot:run

##### Deployment using MySQL Database in a Docker container: 
   - To start MySQL Database use this command in a root folder:

           docker-compose up

   - After successfully starting the container, you need to start the application with the dev-mysql profile. To start it, use this command in a root folder.

           mvn clean spring-boot:run -Dspring.profiles.active=dev-mysql