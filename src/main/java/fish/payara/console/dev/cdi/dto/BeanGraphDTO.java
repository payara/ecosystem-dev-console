package fish.payara.console.dev.cdi.dto;

import jakarta.json.bind.annotation.JsonbTypeAdapter;
import java.util.*;

/**
 * Data Transfer Object representing a graph of bean dependencies.
 * Nodes represent beans, edges represent injection dependencies.
 */
public class BeanGraphDTO {
    private Map<String, BeanNode> nodes = new HashMap<>();

    public void addNode(String beanId, String beanType, String description) {
        nodes.putIfAbsent(beanId, new BeanNode(beanId, beanType, description));
    }

    public void addDependency(String fromBeanId, String toBeanId) {
        BeanNode fromNode = nodes.get(fromBeanId);
        BeanNode toNode = nodes.get(toBeanId);
        if(fromNode != null && toNode != null) {
            fromNode.dependencies.add(toNode);
        }
    }

    public Map<String, BeanNode> getNodes() {
        return nodes;
    }

    @JsonbTypeAdapter(BeanNodeAdapter.class)
    public static class BeanNode {
        private String beanId;
        private String beanType;
        private String description;
        private List<BeanNode> dependencies = new ArrayList<>();

        public BeanNode(String beanId, String beanType, String description) {
            this.beanId = beanId;
            this.beanType = beanType;
            this.description = description;
        }

        public String getBeanId() {
            return beanId;
        }

        public String getBeanType() {
            return beanType;
        }

        public String getDescription() {
            return description;
        }

        public List<BeanNode> getDependencies() {
            return dependencies;
        }
    }
}
