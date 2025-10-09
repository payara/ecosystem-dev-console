/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fish.payara.console.dev.cdi.dto;

import fish.payara.console.dev.cdi.dto.BeanGraphDTO.BeanNode;
import jakarta.json.bind.adapter.JsonbAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Gaurav Gupta
 */
public class BeanNodeAdapter implements JsonbAdapter<BeanNode, Map<String, Object>> {
    public Map<String, Object> adaptToJson(BeanNode beanNode) {
        Map<String, Object> map = new HashMap<>();
        map.put("beanId", beanNode.getBeanId());
        map.put("beanType", beanNode.getBeanType());
        map.put("description", beanNode.getDescription());
        List<String> dependencyIds = beanNode.getDependencies().stream()
            .map(BeanNode::getBeanId)
            .collect(Collectors.toList());
        map.put("dependencies", dependencyIds);
        return map;
    }

    public BeanNode adaptFromJson(Map<String, Object> map) {
        // implement if deserialization needed, else throw UnsupportedOperationException
        throw new UnsupportedOperationException("Deserialization not supported");
    }
}