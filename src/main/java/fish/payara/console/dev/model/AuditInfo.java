/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.model;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Gaurav Gupta
 */
public class AuditInfo {

    private final Set<Annotation> securityAnnotations = new HashSet<>();
    private final Set<Annotation> httpMethodAnnotations = new HashSet<>();
    private final Set<Annotation> pathAnnotations = new HashSet<>();
    private final Set<Annotation> producesAnnotations = new HashSet<>();

    public AuditInfo() {
    }

    public AuditInfo(Set<Annotation> security,
            Set<Annotation> httpMethods,
            Set<Annotation> paths,
            Set<Annotation> produces) {

        if (security != null) {
            this.securityAnnotations.addAll(security);
        }
        if (httpMethods != null) {
            this.httpMethodAnnotations.addAll(httpMethods);
        }
        if (paths != null) {
            this.pathAnnotations.addAll(paths);
        }
        if (produces != null) {
            this.producesAnnotations.addAll(produces);
        }
    }

    public void addAnnotation(Annotation ann) {
        Class<? extends Annotation> type = ann.annotationType();

        // SECURITY
        if (type == RolesAllowed.class
                || type == PermitAll.class
                || type == DenyAll.class) {
            securityAnnotations.add(ann);
            return;
        }

        // HTTP METHODS (jakarta.ws.rs.GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
        if (type.getPackageName().equals("jakarta.ws.rs")
                && !type.equals(Path.class)
                && !type.equals(Produces.class)) {
            httpMethodAnnotations.add(ann);
            return;
        }

        // PATH
        if (type == Path.class) {
            pathAnnotations.add(ann);
            return;
        }

        // PRODUCES
        if (type == Produces.class) {
            producesAnnotations.add(ann);
        }
    }

    public Set<Annotation> getSecurityAnnotations() {
        return Collections.unmodifiableSet(securityAnnotations);
    }

    public Set<Annotation> getHttpMethodAnnotations() {
        return Collections.unmodifiableSet(httpMethodAnnotations);
    }

    public Set<Annotation> getPathAnnotations() {
        return Collections.unmodifiableSet(pathAnnotations);
    }

    public Set<Annotation> getProducesAnnotations() {
        return Collections.unmodifiableSet(producesAnnotations);
    }

    public boolean isEmpty() {
        return securityAnnotations.isEmpty()
                && httpMethodAnnotations.isEmpty()
                && pathAnnotations.isEmpty()
                && producesAnnotations.isEmpty();
    }
}
