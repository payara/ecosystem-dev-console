package fish.payara.console.dev.rest.dto;

public class RestResourceDTO {
    private String className;
    private String path;

    public RestResourceDTO() {}

    public RestResourceDTO(String className, String path) {
        this.className = className;
        this.path = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
