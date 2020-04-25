package com.demo.Utils;

import com.demo.model.ManifestModel;

public class ManifestParser {
    private ManifestModel manifestModel;
    private byte[] fileBytes;
    private int fileHeaderOffset;
    public ManifestParser(String filePath) {
        fileBytes = Utils.readFile(filePath);
        if (fileBytes == null) {
            System.out.println("打开文件失败");
        }
        manifestModel = new ManifestModel();
    }

    public void parseManifestHeader() {
        if (fileBytes == null) {
            System.out.println("打开文件失败");
            return;
        }
        manifestModel.magicNumber = Utils.copyBytes(fileBytes, 0, 4);
        manifestModel.fileSize = Utils.byte2Int(Utils.copyBytes(fileBytes, 4, 8));
        fileHeaderOffset = 8;
    }

    public void parseStringChuck() {
        if (fileBytes == null) {
            System.out.println("打开文件失败");
            return;
        }
        byte[] chuckType = Utils.copyBytes(fileBytes, fileHeaderOffset, 4);
        int chuckSize = Utils.byte2Int(Utils.copyBytes(fileBytes, fileHeaderOffset + 4, 4));
        int stringCount = Utils.byte2Int(Utils.copyBytes(fileBytes, fileHeaderOffset + 8, 4));
        // 样式个数，正常应该为零
        int styleCount = Utils.byte2Int(Utils.copyBytes(fileBytes, fileHeaderOffset + 12, 4));
        // 跳过4字节unkown区域
        int stringPoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, fileHeaderOffset + 20, 4));
        int stylePoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, fileHeaderOffset + 24, 4));
        int OffsetsCursor = fileHeaderOffset + 28;
        int[] stringOffsets = new int[stringCount];
        for (int i = 0; i < stringCount; ++i) {
            stringOffsets[i] = Utils.byte2Int(Utils.copyBytes(fileBytes, OffsetsCursor, 4));
            OffsetsCursor += 4;
        }
        int[] styleOffsets = new int[styleCount];
        for (int i = 0; i < styleCount; ++i) {
            styleOffsets[i] = Utils.byte2Int(Utils.copyBytes(fileBytes, OffsetsCursor, 4));
            OffsetsCursor += 4;
        }
        int stringContentStartPoint = stringPoolOffset + fileHeaderOffset;
        String[] stringContent = new String[stringCount];
        byte[] stringContentBytes = Utils.copyBytes(fileBytes, stringContentStartPoint, chuckSize);
        int stringStartPoint = 0;
        for (int i = 0; i < stringCount; ++i) {
            // 字符串中前两位bit记录所包含的字符数量
            // 在UTF-16编码中单个字符占两个字节，所以计算得出实际字节数量应是字符数量的双倍
            int stringSize = Utils.byte2Short(Utils.copyBytes(stringContentBytes, stringStartPoint, 2)) * 2;
            // 由于前两位bit记录所包含的字符数量不包含结尾0x0000, 所以实际读取字符数量应为stringSize + 2
            byte[] firstStringByte = Utils.copyBytes(stringContentBytes, stringStartPoint + 2, stringSize + 2);
            stringContent[i] = new String(firstStringByte);
            stringStartPoint += (2 + stringSize + 2);
        }

        manifestModel.stringChuckModel.chuckType = chuckType;
        manifestModel.stringChuckModel.chuckSize = chuckSize;
        manifestModel.stringChuckModel.stringCount = stringCount;
        manifestModel.stringChuckModel.styleCount = styleCount;
        manifestModel.stringChuckModel.stringPoolOffset = stringPoolOffset;
        manifestModel.stringChuckModel.stylePoolOffset = stylePoolOffset;
        manifestModel.stringChuckModel.stringOffsets = stringOffsets;
        manifestModel.stringChuckModel.styleOffsets = styleOffsets;
        manifestModel.stringChuckModel.stringContentStartPoint = stringContentStartPoint;
        manifestModel.stringChuckModel.stringContent = stringContent;

        System.out.println(manifestModel.stringChuckModel.toString());
    }
}
