package com.demo.model;

public class ChuckModel {
    public static final int START_NAMESPACE_CHUCK = 0x00100100;
    public static final int START_TAG_CHUCK = 0x00100102;
    public static final int END_NAMESPACE_CHUCK = 0x00100101;
    public static final int END_TAG_CHUCK = 0x00100103;

    public int chuckType;
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
    public String stringArray2String(String[] array) {
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
