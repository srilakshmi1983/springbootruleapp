package com.test.ruleeditor.domain;

/**
 * Created by Srilakshmi on 26/08/17.
 */
public class Property {

    private String name;
    private String dataType;
    private boolean isList;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isList() {
        return isList;
    }

    public void setList(boolean list) {
        isList = list;
    }
}
