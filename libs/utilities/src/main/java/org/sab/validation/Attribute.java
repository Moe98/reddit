package org.sab.validation;

public class Attribute {

    String attributeName;
    DataType dataType;
    boolean isRequired = false;

    public String getAttributeName() {
        return attributeName;
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public Attribute(String attributeName, DataType dataType) {
        this(attributeName, dataType, false);
    }

    public Attribute(String attributeName, DataType dataType, boolean isRequired) {
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.isRequired = isRequired;
    }
}
