package com.demo.model.ResourcesModel;

public class StringPoolChuck {
    public ChuckHeader chuckHeader;
    public int stringCount;
    public int styleCount;
    // 标识字符串池编码 UT8/UTF16
    public boolean isUTF8;
    public int stringPoolOffset;
    public int stylePoolOffset;

    public String[] stringContent;
    public String[] styleContent;
}
