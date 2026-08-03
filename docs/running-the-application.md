# Running the Application

This guide describes how to build and run Carbon Calculator Gateway (CCG) in a local development environment.

## Prerequisites

The following software must be installed before running the application:

* Java 17
* Apache Maven 3.3.0 or later
* Git

> [Note!]
> 
> The connection to Trace API requires an API key to be defined as an Environment Variable in the System. 
> You can request the key to Helpdesk or the Web Operations team.

## Clone the Repository

Clone the project from GitHub:

```bash
git clone  https://github.com/visitscotland/carbon-calulator-gateway.git
cd carbon-calulator-gateway
```

## Build the Application

Compile the project and execute all verification steps:

```bash
mvn clean verify
```

## Run from the Command Line

Local development should always use the `dev` Spring profile.

Run the application using:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Alternatively, after building the application:

```bash
java -jar target/vs-carbon-calculator-gateway.jar --spring.profiles.active=dev
```

## Run from IntelliJ IDEA

The repository includes a shared IntelliJ run configuration named:

```
VsCarbonCalculatorApplication
```

This configuration is preconfigured for local development and automatically:

* Uses the `dev` Spring profile.
* Starts the application with the correct configuration.

Provided that the API Key has been already set up, no additional configuration should be required after opening the 
project in IntelliJ IDEA.

## Testing the Application

Sample HTTP requests are provided in the repository:

```
.run/http/
```

These can be executed directly from IntelliJ IDEA using the built-in HTTP Client.

Refer to the sample requests when testing the `/register` endpoint locally.

## Additional Configuration

Application properties and environment-specific configuration are described in the [Application Configuration](configuration.md) documentation.
