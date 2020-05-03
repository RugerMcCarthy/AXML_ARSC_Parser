package com.demo.Utils;

import com.demo.model.ManifestModel.AttributeData;
import com.demo.model.ManifestModel.AttributeType;
import com.demo.model.ManifestModel.ChuckModel;
import com.demo.model.ManifestModel.ManifestModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManifestParser {
    private ManifestModel manifestModel;
    private byte[] fileBytes;
    private int stringChuckOffset;
    private int resourceChuckOffset;
    private int xmlContentChuckOffset;
    private boolean isFirstString = true;
    private boolean isUTF8 = false;
    private HashMap<String, String> prefixUriMap = new HashMap<>();
    private HashMap<String, String> uriPrefixMap = new HashMap<>();
    private StringBuilder xmlContent = new StringBuilder();
    private int indentCount = 0;
    public ManifestParser(String filePath) {
        fileBytes = Utils.readFile(filePath);
        if (fileBytes == null) {
            System.out.println("打开文件失败");
        }
        manifestModel = new ManifestModel();
    }

    public void parseTo(String outPath) {
        if (fileBytes == null) {
            System.out.println("打开文件失败");
            return;
        }
        parseManifestHeader();
        parseStringChuck();
        parseResourceChuck();
        parseXmlContentChuck();
        Utils.writeFile(outPath, xmlContent.toString().getBytes());
    }

    private void parseManifestHeader() {
        manifestModel.magicNumber = Utils.copyBytes(fileBytes, 0, 4);
        manifestModel.fileSize = Utils.byte2Int(Utils.copyBytes(fileBytes, 4, 8));
        stringChuckOffset = 8;
    }

    private void parseStringChuck() {
        int chuckType = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset, 4));
        int chuckSize = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset + 4, 4));
        int stringCount = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset + 8, 4));
        // 样式个数，正常应该为零
        int styleCount = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset + 12, 4));
        // 跳过4字节unkown区域
        int stringPoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset + 20, 4));
        int stylePoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, stringChuckOffset + 24, 4));
        int OffsetsCursor = stringChuckOffset + 28;
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
        int stringContentStartPoint = stringPoolOffset + stringChuckOffset;
        String[] stringContent = new String[stringCount];
        byte[] stringContentBytes = Utils.copyBytes(fileBytes, stringContentStartPoint, chuckSize - stringPoolOffset);
        int stringStartPoint = 0;
        for (int i = 0; i < stringCount; ++i) {
            // 字符串中前两位bit记录所包含的字符数量
            byte[] stringLengthBytes = Utils.copyBytes(stringContentBytes, stringStartPoint, 2);
            if (isFirstString) {
                // utf-8字符串前两字节  stringLengthBytes[0]：字符串字符数量 stringLengthBytes[1]: 字符串所占字节数量
                // utf-16字符串前两字节 stringLengthBytes[0]：字符串字符数量（由于UTF-16固定编码，所以所占字节数量为字符数量二倍） stringLengthBytes[1]: 默认为零，猜测应该是与utf-8作区分
                // 通过第一个字符串来判断是UTF-8还是UTF-16
                if ((stringLengthBytes[1] & 0xff) != 0) {
                    isUTF8 = true;
                }
                isFirstString = false;
            }
            if (isUTF8) {
                // UTF-8编码
                int stringSize = (stringLengthBytes[1] & 0xff);
                if (stringSize != 0) {
                    byte[] stringBytes = Utils.copyBytes(stringContentBytes, stringStartPoint + 2, stringSize + 1);
                    try {
                        stringContent[i] = new String(stringBytes, "utf-8");
                    } catch (Exception e) {
                        stringContent[i] = "";
                        e.printStackTrace();
                    }
                } else {
                    stringContent[i] = "";
                }
                stringStartPoint += (2 + stringSize + 1);
            } else {
                // UTF-16编码
                // 固定编码单字符占两字节，实际字节数量应为字符数量二倍
                int stringSize = Utils.byte2Short(stringLengthBytes) * 2;
                // 由于前两位bit记录所包含的字符数量不包含结尾0x0000, 所以实际读取字符数量应为stringSize + 2
                byte[] stringBytes = Utils.copyBytes(stringContentBytes, stringStartPoint + 2, stringSize + 2);
                stringContent[i] = Utils.filterStringNull(stringBytes);
                stringStartPoint += (2 + stringSize + 2);
            }
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
        resourceChuckOffset = stringChuckOffset + chuckSize;
    }

    private void parseResourceChuck() {
        int chuckType = Utils.byte2Int(Utils.copyBytes(fileBytes, resourceChuckOffset, 4));
        int chuckSize = Utils.byte2Int(Utils.copyBytes(fileBytes, resourceChuckOffset + 4, 4));
        byte[] resourceIdsBytes = Utils.copyBytes(fileBytes, resourceChuckOffset + 8, chuckSize - 8);
        // 每个resourceId 占4个字节
        int resourceIdsCount = resourceIdsBytes.length / 4;
        String[] resourceIds = new String[resourceIdsCount];
        for (int i = 0; i < resourceIdsCount; ++i) {
            byte[] resourceId = Utils.copyBytes(resourceIdsBytes, i * 4, 4);
            resourceIds[i] = Utils.bytes2HexString(resourceId);
        }
        manifestModel.resourceChuckModel.chuckType = chuckType;
        manifestModel.resourceChuckModel.chuckSize = chuckSize;
        manifestModel.resourceChuckModel.resourceIds = resourceIds;
        xmlContentChuckOffset = resourceChuckOffset + chuckSize;
    }

    private void parseXmlContentChuck() {
        int xmlContentOffsetCursor = xmlContentChuckOffset;
        while (xmlContentOffsetCursor < fileBytes.length) {
            int chuckType = Utils.byte2Int(Utils.copyBytes(fileBytes, xmlContentOffsetCursor, 4));
            int chuckSize = Utils.byte2Int(Utils.copyBytes(fileBytes, xmlContentOffsetCursor + 4, 4));
            byte[] chuckContent = Utils.copyBytes(fileBytes, xmlContentOffsetCursor + 8,  chuckSize - 8);
            switch (chuckType) {
                case ChuckModel.START_NAMESPACE_CHUCK:
                    // 开始
                    parseStartNamespaceChuck(chuckContent);
                    break;
                case ChuckModel.START_TAG_CHUCK:
                    parseStartTagChuck(chuckContent);
                    break;
                case ChuckModel.END_NAMESPACE_CHUCK:
                    // 结束
                    parseEndNamespaceChuck(chuckContent);
                    break;
                case ChuckModel.END_TAG_CHUCK:
                    parseEndTagChuck(chuckContent);
                    break;
            }
            xmlContentOffsetCursor += chuckSize;
        }
    }

    private String getString(int index) {
        if (manifestModel.stringChuckModel.stringContent == null) {
            return null;
        }
        if (index < 0 || index >= manifestModel.stringChuckModel.stringContent.length) {
            return null;
        }
        return manifestModel.stringChuckModel.stringContent[index];
    }

    private void parseStartNamespaceChuck(byte[] chuckContent) {
        int lineNumber = Utils.byte2Int(Utils.copyBytes(chuckContent, 0, 4));
        // 跳过4字节的unkown区域
        int prefixIndex = Utils.byte2Int(Utils.copyBytes(chuckContent, 8, 4));
        int uriIndex = Utils.byte2Int(Utils.copyBytes(chuckContent, 12, 4));
        String prefix = getString(prefixIndex);
        String uri = getString(uriIndex);
        if (prefix != null && uri != null) {
            prefixUriMap.put(prefix, uri);
            uriPrefixMap.put(uri, prefix);
        }
    }

    private void parseStartTagChuck(byte[] chuckContent) {
        int lineNumber = Utils.byte2Int(Utils.copyBytes(chuckContent, 0, 4));
        // 跳过4字节的unkown区域
        int namespaceUriIndex = Utils.byte2Int(Utils.copyBytes(chuckContent, 8, 4));
        int tagNameIndex = Utils.byte2Int(Utils.copyBytes(chuckContent, 12, 4));
        int flag = Utils.byte2Int(Utils.copyBytes(chuckContent, 16, 4));
        int attributeCount = Utils.byte2Int(Utils.copyBytes(chuckContent, 20, 4));
        int classAttribute = Utils.byte2Int(Utils.copyBytes(chuckContent, 24, 4));
        String tagName = getString(tagNameIndex);
        ArrayList<AttributeData> attrs = new ArrayList<>();
        for (int i = 0; i < attributeCount; ++i) {
            AttributeData attr = new AttributeData();
            for (int j = 0; j < 5; ++j) {
                int value = Utils.byte2Int(Utils.copyBytes(chuckContent, 28 + i * 20 + 4 * j, 4));
                switch (j) {
                    case 0:
                        attr.nameSpaceUri = getString(value);
                        break;
                    case 1:
                        attr.name = getString(value);
                        break;
                    case 2:
                        attr.valueString = getString(value);
                        break;
                    case 3:
                        value = value >> 24;
                        attr.type = value;
                        break;
                    case 4:
                        attr.data = value;
                        break;
                }
            }
            attrs.add(attr);
        }
        AttributeType.setStringContent(manifestModel.stringChuckModel.stringContent);
        xmlContent.append(createStartTagXml(tagName, attrs));
        indentCount++;
    }

    private String createStartTagXml(String tagName, List<AttributeData> attrList){
        StringBuilder tagSb = new StringBuilder();
        StringBuilder indentString = getIndentString();
        tagSb.append(indentString);
        if("manifest".equals(tagName)){
            tagSb.append("<manifest xmls:");
            StringBuilder prefixSb = new StringBuilder();
            boolean isFirst = true;
            for(String key : prefixUriMap.keySet()){
                if (!isFirst) {
                    prefixSb.append("\n");
                }
                prefixSb.append(indentString);
                prefixSb.append(key+":\""+prefixUriMap.get(key)+"\"");
                isFirst = false;
            }
            tagSb.append(prefixSb.toString());
        }else{
            tagSb.append("<"+tagName);
        }

        //构建属性值
        if(attrList.size() == 0){
            tagSb.append(">\n");
        }else{
            tagSb.append("\n");
            for(int i=0;i<attrList.size();i++){
                tagSb.append(indentString);
                AttributeData attr = attrList.get(i);
                String prefixName = uriPrefixMap.get(attr.nameSpaceUri);
                //这里需要注意的是有的地方没有前缀的
                if(prefixName == null){
                    prefixName = "";
                }
                tagSb.append("\t");
                tagSb.append(prefixName+(prefixName.length() > 0 ? ":" : "")+attr.name+"=");
                tagSb.append("\""+ AttributeType.getAttributeData(attr)+"\"");
                if(i == (attrList.size()-1)){
                    tagSb.append(">");
                }
                tagSb.append("\n");
            }
        }
        return tagSb.toString();
    }

    private void parseEndTagChuck(byte[] chuckContent) {
        int lineNumber = Utils.byte2Int(Utils.copyBytes(chuckContent, 0, 4));
        // 跳过四字节unkown区域
        int namespaceUriIndex = Utils.byte2Int(Utils.copyBytes(chuckContent, 8, 4));
        String tagName = getString(Utils.byte2Int(Utils.copyBytes(chuckContent, 12, 4)));
        indentCount--;
        xmlContent.append(createEndTagXml(tagName));
    }

    private String createEndTagXml(String tagName){
        StringBuilder indentString = getIndentString();
        return indentString.toString() + "</" + tagName + ">\n";
    }

    private void parseEndNamespaceChuck(byte[] chuckContent) {
        uriPrefixMap.clear();
        prefixUriMap.clear();
    }

    private StringBuilder getIndentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentCount; ++i) {
            sb.append("\t");
        }
        return sb;
    }
}
