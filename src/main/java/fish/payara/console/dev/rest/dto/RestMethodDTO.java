package fish.payara.console.dev.rest.dto;

public class RestMethodDTO {
    private String methodSignature;
    private String path;
    private String httpMethodAndProduces; 

    public RestMethodDTO() {}

    public RestMethodDTO(String methodSignature, String path, String httpMethodAndProduces) {
        this.methodSignature = methodSignature;
        this.path = path;
        this.httpMethodAndProduces = httpMethodAndProduces;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public String getPath() {
        return path;
    }

    public String getHttpMethodAndProduces() {
        return httpMethodAndProduces;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setHttpMethodAndProduces(String httpMethodAndProduces) {
        this.httpMethodAndProduces = httpMethodAndProduces;
    }
}
