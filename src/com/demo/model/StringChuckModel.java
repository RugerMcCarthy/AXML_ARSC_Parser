package com.demo.model;

import com.sun.xml.internal.ws.api.ha.StickyFeature;

public class StringChuckModel extends ChuckModel{
    public int stringCount;
    public int styleCount;
    public int stringPoolOffset;
    public int stylePoolOffset;
    public int[] stringOffsets;
    public int[] styleOffsets;
    public int stringContentStartPoint;
    public String[] stringContent;

    @Override
    public String toString() {
        return "stringCount: " + stringCount + "\n" +
                "styleCount: " + styleCount + "\n" +
                "stringPoolOffset: " + stringPoolOffset + "\n" +
                "stylePoolOffset: " + stylePoolOffset + "\n" +
                "stringOffsets: " + intArray2String(stringOffsets) + "\n" +
                "styleOffsets: " + intArray2String(styleOffsets) + "\n" +
                "stringContentStartPoint: " + stringContentStartPoint + "\n" +
                "stringContent: \n" + stringArray2String(stringContent);
    }
}
