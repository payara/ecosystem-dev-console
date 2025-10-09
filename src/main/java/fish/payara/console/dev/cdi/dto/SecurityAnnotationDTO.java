package fish.payara.console.dev.cdi.dto;

import java.lang.annotation.Annotation;
import java.util.Set;

public class SecurityAnnotationDTO {
    private String key;
    private Set<String> annotationClassNames;

    public SecurityAnnotationDTO(String key, Set<Annotation> annotations) {
        this.key = key;
        this.annotationClassNames = annotations.stream()
                .map(annotation -> annotation.annotationType().getName())
                .collect(java.util.stream.Collectors.toSet());
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Set<String> getAnnotationClassNames() {
        return annotationClassNames;
    }

    public void setAnnotationClassNames(Set<String> annotationClassNames) {
        this.annotationClassNames = annotationClassNames;
    }
}
