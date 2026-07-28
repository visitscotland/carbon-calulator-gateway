# Application Configuration

This document describes the configuration required to run Carbon Calculator Gateway (CCG).

## Configuration Files

The application uses Spring Boot profile-based configuration.

- *application.properties*: Contains the default application configuration. This file should contain values common across 
environments. It should also allow the application to run when no profile is defined
- *application-dev.properties*: Contains configuration specific to local development and development environments. The 
development properties file overrides only values that differ from the default configuration.

---

## Properties

### General Configuration

Properties set as general configuration for the application

| Property                            | Description                                          |
|-------------------------------------|------------------------------------------------------|
| `spring.application.name`           | Application name used by Spring Boot.                |
| `server.port`                       | 8077                                                 |
| `spring.mvc.log-resolved-exception` | Controls whether resolved MVC exceptions are logged. |
| `allowed_origin`                    | Comma-separated list of allowed origins.             |

---

### reCAPTCHA Configuration

Properties controlling user submission validation.

| Property              | Description                                            |
|-----------------------|--------------------------------------------------------|
| `recaptcha.enabled`   | Enables or disables reCAPTCHA validation.              |
| `recaptcha.publickey` | Public key used by the frontend reCAPTCHA integration. |
| `recaptcha.secretkey` | Secret key used to validate reCAPTCHA responses.       |

---

### BREG Configuration

Configuration for integration with BloomReach Engagement through BREG.

| Property                 | Description                                                         |
|--------------------------|---------------------------------------------------------------------|
| `breg.enabled`           | Enables or disables BREG submissions.                               |
| `breg.service-url`       | URL of the BREG service endpoint.                                   |
| `breg.remove-properties` | Comma-separated list of fields removed before sending data to BREG. |

---

### Isla / Trace API Configuration

Configuration for submitting registrations to Isla through the Trace API.

| Property                      | Description                                                              |
|-------------------------------|--------------------------------------------------------------------------|
| `trace-api.enabled`           | Enables or disables Trace API submissions.                               |
| `trace-api.base-url`          | Base URL of the Trace API service.                                       |
| `trace-api.api-key`           | API key used to authenticate with Trace API.                             |
| `trace-api.remove-properties` | Comma-separated list of fields removed before sending data to Trace API. |

---

### Email Notification Configuration

Configuration for support notifications. Please note that the properties prefixed by `spring.mail` are defined by the
Spring Framework

| Property                                           | Description                                    |
|----------------------------------------------------|------------------------------------------------|
| `spring.mail.host`                                 | SMTP server hostname.                          |
| `spring.mail.port`                                 | SMTP server port.                              |
| `spring.mail.properties.mail.smtp.auth`            | Enables SMTP authentication.                   |
| `spring.mail.properties.mail.smtp.starttls.enable` | Enables SMTP STARTTLS.                         |
| `notification.email.enabled`                       | Enables or disables error notification emails. |
| `notification.email.subject`                       | Subject used for notification emails.          |
| `notification.email.recipients`                    | Recipients of notification emails.             |