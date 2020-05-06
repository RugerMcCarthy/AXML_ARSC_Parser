package com.demo.Parser;

import com.demo.Utils.Utils;

public abstract class Parser {
    protected byte[] fileBytes;
    public Parser(String filePath) {
        fileBytes = Utils.readFile(filePath);
        if (fileBytes == null) {
            System.out.println("打开文件失败");
        }
    }

    abstract protected void parseTo(String outPath);
}
