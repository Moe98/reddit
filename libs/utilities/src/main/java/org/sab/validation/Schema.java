package org.sab.validation;

import java.util.List;

public class Schema {
    List<Attribute> attributeList;

    public Schema(List<Attribute> attributeList) {
        this.attributeList = attributeList;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public boolean isEmpty() {
        return attributeList.isEmpty();
    }
}
