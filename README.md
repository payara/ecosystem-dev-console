# Dev Console – CDI Dev Mode

This project provides a development-time console for inspecting and diagnosing
Contexts and Dependency Injection (CDI) applications. It instruments CDI lifecycle
events and exposes structured metadata through a set of JSON endpoints under
a unified `/dev` application path.

The Dev Console is intended strictly for development and debugging purposes.

---

## Key Features

- **Bean Probing & Registry**
  - Tracks discovered CDI beans, scopes, qualifiers, stereotypes, and producers
  - Captures lifecycle statistics (creation, destruction, invocation)

- **Bean Dependency Graph**
  - Builds a directed graph of injection relationships
  - Detects circular dependencies
  - Supports full graph and per-bean subgraph views

- **Scoped Bean Analysis**
  - Lists all scoped beans with lifecycle counters
  - Provides per-scope summaries with active contextual instance counts

- **Interceptors & Decorators**
  - Lists registered interceptors and decorators
  - Exposes invocation statistics and execution chains
  - Supports class-level and method-level resolution

- **Injection Point Diagnostics**
  - Lists all injection points
  - Highlights unresolved, ambiguous, and unprocessed injection points
  - Supports per-bean injection point inspection

- **Observers & Events**
  - Tracks observer methods and their metadata
  - Records recently fired CDI events and resolved observers

- **REST Introspection**
  - Lists REST resources and methods
  - Tracks REST method invocation metrics
  - Exposes registered exception mappers

- **Security Audit**
  - Captures security annotations such as `@RolesAllowed`, `@PermitAll`,
    and `@DenyAll`
  - Reports affected classes, methods, HTTP verbs, and paths

- **Seen Types**
  - Reports Java types and annotations observed by the CDI container

- **Conditional Activation**
  - Enabled only when the system property `payara.dev.console` is set

---

## Endpoint Overview

All endpoints are rooted under `/dev`.

### CDI
- `/dev/cdi/beans/*`
- `/dev/cdi/bean-graph/*`
- `/dev/cdi/scoped-beans`
- `/dev/cdi/scoped-beans/detail`
- `/dev/cdi/injection-points/*`
- `/dev/cdi/interceptors/*`
- `/dev/cdi/intercepted-classes`
- `/dev/cdi/decorators/*`
- `/dev/cdi/decorated-classes`
- `/dev/cdi/producers`
- `/dev/cdi/observers`
- `/dev/cdi/events`
- `/dev/cdi/extensions`
- `/dev/cdi/seen-types`

### REST
- `/dev/rest/resources`
- `/dev/rest/methods/*`
- `/dev/rest/exception-mappers`

### Security
- `/dev/security/audit`

### Metadata
- `/dev/metadata`

---

## Summary

The CDI Dev Console provides deep visibility into CDI wiring, lifecycle behavior,
REST exposure, and security configuration. By combining runtime instrumentation
with structured JSON endpoints, it enables developers to better understand,
debug, and validate complex Jakarta EE applications during development.
