package com.demo.model;

public class ChuckModel {
    public byte[] chuckType = new byte[4];
    public int chuckSize;
    public String intArray2String(int[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("【");
        for (int i = 0; i < array.length; ++i) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(array[i]);
        }
        stringBuilder.append("】");
        return stringBuilder.toString();
    }
    public String StringArray2String(String[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("【");
        for (int i = 0; i < array.length; ++i) {
            if (i != 0) {
                stringBuilder.append(",\n");
            }
            stringBuilder.append(array[i]);
        }
        stringBuilder.append("】");
        return stringBuilder.toString();
    }
}
