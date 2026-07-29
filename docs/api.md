# API Reference

This document describes the REST API exposed by Carbon Calculator Gateway (CCG).

## Overview

CCG exposes a small REST API consisting of:

* A business endpoint used to register businesses with the Isla Carbon Calculator and BloomReach Engagement.
* A health and monitoring endpoint for troubleshooting.

---

## POST /register

Registers a business with the Isla Carbon Calculator and submits the engagement data to BloomReach Engagement via BREG.

The endpoint accepts a JSON object containing the registration data collected by the Carbon Calculator registration form.

The service is intentionally data-agnostic. Unknown fields are preserved unless explicitly removed during downstream 
payload processing.

### Processing

The request is processed as follows:

1. Validate the reCAPTCHA token (if enabled).
2. Generate a unique submission identifier.
3. Submit the registration to the Trace API.
4. Submit the engagement data to BREG.
5. Consolidate the downstream responses into a unified response.
6. Send a notification email if manual intervention is required.

#### Notes
- The Trace API and BREG submissions are independent but are processed sequentially. If the Trace API submission fails, a traceApiFailure attribute is included in the payload sent to BREG.
- A failure in one downstream service does not prevent the other submission from being attempted.
- A notification email is sent whenever either downstream service returns an error. If both services reject the submission, manual recovery may not be possible.

### Requests

Sample HTTP requests for local development are available in [local.http](../.run/http/local.http)

## Responses

The response will contain a status code and a submission ID, the later only when it is applicable. Please note that the

Please note: A `submissionId` may be returned regardless of the outcome of the registration. Its purpose is to correlate 
submissions across downstream services and to assist with troubleshooting.

```json
{
  "code": "BOTH_SUBMISSIONS_FAILED",
  "submissionId": "08a8c891-9945-460a-9eab-545941479892"
}
```

### Response codes
| HTTP Status | Code                      | Description                                                                 |
| ----------- | ------------------------- | --------------------------------------------------------------------------- |
| `200`       | `SUCCESS`                 | Registration completed successfully.                                        |
| `202`       | `TRACE_API_FAILED`        | Registration with Isla failed. The submission requires manual intervention. |
| `202`       | `BREG_FAILED`             | Submission to BREG failed. The submission requires manual intervention.     |
| `401`       | `FORBIDDEN`               | reCAPTCHA validation failed.                                                |
| `409`       | `EXISTING_USER`           | The user is already registered with Isla.                                   |
| `500`       | `BOTH_SUBMISSIONS_FAILED` | Neither downstream service accepted the submission.                         |


***Note:** The response codes above describe the expected business outcomes. The service may also return standard HTTP 
error responses (for example 400, 404, or 500) caused by invalid requests, infrastructure failures, or unexpected 
application errors. These responses are not part of the business contract and may not follow the JSON structure 
described above.* 

## GET /health

Returns the application health status. 

## Response

```text
Status OK!
```

This is a common practice in VisitScotland and is intended for load balancers, monitoring tools, and deployment health 
checks.

---

## GET /info/properties

Returns the active application configuration in a JSON format

> **Note**
>
> This endpoint is only available when the `dev` Spring profile is active.



