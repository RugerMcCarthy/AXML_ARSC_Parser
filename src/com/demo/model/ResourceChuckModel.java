package com.demo.model;

public class ResourceChuckModel extends ChuckModel {
    public String[] resourceIds;

    @Override
    public String toString() {
        return "resourceIds: \n" + stringArray2String(resourceIds);
    }
}
