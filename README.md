# VisitScotland Carbon Calculator Gateway Service

- Intermediary service
- Submits to ICC
- Submits to breg
- Respond to the user

## Purpose

[TBD]

### Key Features

[TBD]

## Architecture & Data Flow

[TBD]

### Request Processing Pipeline

[TBD]

## Configuration

[TBD]

## API Endpoints

### Public Endpoints

#### 1. Health Check

<pre>   
GET /health
</pre>

**Description**: Service health status check
**Response**:
- Status: 200 OK
- Body: "Status OK!"

Example:

<pre>
curl http://localhost:8096/health
</pre>

#### 2. **Submit Form **

[TBD]

### Release Process

To release a new version of the service, follow these steps:

1. Ensure all changes are committed and pushed to the main branch and you don't have any uncommitted changes in your
   local repository.
2. Make sure you are on the main branch and have the latest changes pulled from the remote repository.2.
3. Run the Maven release plugin to prepare and perform the release:
   ```
   mvn clean verify release:prepare release:perform
   ```

This command will ask you to confirm the release version, the next development version and the tag name for the release.
The plugin will then create a release branch, update the version numbers in the POM files, commit the changes, create a
tag for the release and push everything to the remote repository.


