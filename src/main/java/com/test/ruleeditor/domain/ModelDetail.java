package com.test.ruleeditor.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Srilakshmi on 26/08/17.
 */
public class ModelDetail {
    private String name;
    private List<Property> properties;

    public ModelDetail() {
        properties = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public boolean add(Property property) {
        return properties.add(property);
    }
}
