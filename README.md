# CDI Dev Mode

This project is focused on providing tools and extension capabilities for Contexts and Dependency Injection (CDI) in development mode. It features probing, registry management, observer patterns, and REST API integrations to facilitate enhanced development and debugging capabilities for Jakarta EE applications.

## Features

- **Bean Probing and Registry:**  
  The `ProbeExtension` class acts as a CDI extension to observe bean processing, observer method processing, injection target processing, and deployment lifecycle events. It uses `ProbeRegistry` to track beans, their injection dependencies (bean graph), security annotations, REST resources and methods, and observers in the application.
  
- **Bean Graph Model:**  
  The `BeanGraphDTO`, along with `BeanNode` and `BeanNodeAdapter`, models and serializes a graph of bean dependencies to visualize injection relationships.
  
- **REST API Exposure (`ProbeResource`):**  
  Provides endpoints to expose information like beans, observers, seen CDI types, bean dependency graph, REST resources, REST methods, and security annotations in JSON format for external tools or UI.
  
- **Security Annotations Audit:**  
  Tracks security annotations such as `@RolesAllowed`, `@PermitAll`, and `@DenyAll` on annotated types and methods.
  
- **Observer Methods Tracking:**  
  Captures observer methods registered in the application and exposes their metadata.
  
- **Wrapping Injection Target:**  
  `WrappingInjectionTarget` wraps CDI injection targets for potential custom behavior or proxying when enabled.
  
- **DTOs for Data Transfer:**  
  Includes DTOs such as `BeanDTO`, `SecurityAnnotationDTO`, `ObserverDTO`, `RestResourceDTO`, and `RestMethodDTO` to standardize data structures for REST responses.

- **Conditional Activation:**  
  The functionality can be enabled or disabled based on the system property `payara.dev.console`.

## Conclusion

Payara Dev Console is a comprehensive development utility for Jakarta EE applications. By instrumenting CDI lifecycle events and providing a REST API, it empowers developers with introspection and diagnostics tools for beans, observers, REST endpoints, and security annotations. This improves debugging, auditing, and understanding of complex CDI-based applications during development.



Dependency Conflicts
Dependency Graph
plant umll diagram 
