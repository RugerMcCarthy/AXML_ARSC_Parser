package com.demo.Parser;

import com.demo.Utils.Utils;
import com.demo.model.ResourcesModel.*;

import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

public class ResourceParser extends Parser{
    ResourceTableChuckHeader resTableChuckHeader;
    StringPoolChuck globalStringPoolChuck;
    PackageChuckHeader packageChuckHeader;
    StringPoolChuck typeStringPoolChuck;
    StringPoolChuck keyStringPoolChuck;
    TypeSpecChuck typeSpecChuck;
    TypeInfoChuck typeInfoChuck;
    Map<Integer, TypeInfoEntry> resIdEntryMap = new HashMap<>();
    private StringBuilder resContent = new StringBuilder();

    public ResourceParser(String filePath) {
        super(filePath);
    }

    @Override
    public void parseTo(String outPath) {
        if (fileBytes == null) {
            return;
        }
        parseResTableChuckHeader(0);
        int globalStringPoolOffset = resTableChuckHeader.chuckHeader.chuckHeaderSize;
        parseResGlobalStringPoolChuck(globalStringPoolOffset);
        int packageOffset = 12 + globalStringPoolChuck.chuckHeader.chuckSize;
        for (int i = 0; i < resTableChuckHeader.packageCount; ++i) {
            parsePackageChuckHeader(packageOffset);
            parseResTypeStringPoolChuck(packageOffset + packageChuckHeader.typeStringPoolOffset);
            parseResKeyStringPoolChuck(packageOffset + packageChuckHeader.keyStringPoolOffset);
            int typeInfoCursor = packageOffset + packageChuckHeader.keyStringPoolOffset + keyStringPoolChuck.chuckHeader.chuckSize;
            while (typeInfoCursor < packageOffset + packageChuckHeader.chuckHeader.chuckSize) {
                if (isTypeSpec(typeInfoCursor)) {
                    parseResTypeSpec(typeInfoCursor);
                    typeInfoCursor += typeSpecChuck.chuckHeader.chuckSize;
                } else {
                    parseResTypeInfo(typeInfoCursor);
                    typeInfoCursor += typeInfoChuck.chuckHeader.chuckSize;
                }
            }
            packageOffset += packageChuckHeader.chuckHeader.chuckSize;
        }
        Utils.writeFile(outPath, resContent.toString().getBytes());
    }

    private void parsePackageChuckHeader(int offset) {
        packageChuckHeader = new PackageChuckHeader();
        packageChuckHeader.chuckHeader = parseChuckHeader(offset);
        packageChuckHeader.packageId = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize(), 4));
        packageChuckHeader.packageName = new String(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize() + 4, 256));
        packageChuckHeader.typeStringPoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize() + 260, 4));
        packageChuckHeader.lastPublicKey = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize() + 264, 4));
        packageChuckHeader.keyStringPoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize() + 268, 4));
        packageChuckHeader.lastPublicKey = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + packageChuckHeader.chuckHeader.getSize() + 272, 4));
    }

    private void parseResTableChuckHeader(int offset) {
        resTableChuckHeader = new ResourceTableChuckHeader();
        resTableChuckHeader.chuckHeader = parseChuckHeader(offset);
        resTableChuckHeader.packageCount = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + resTableChuckHeader.chuckHeader.getSize(), 4));
    }

    private void parseResGlobalStringPoolChuck(int offset) {
        globalStringPoolChuck = new StringPoolChuck();
        parseStringPoolChuck(offset, globalStringPoolChuck);
    }

    private void parseResTypeStringPoolChuck(int offset) {
        typeStringPoolChuck = new StringPoolChuck();
        parseStringPoolChuck(offset, typeStringPoolChuck);
    }

    private void parseResKeyStringPoolChuck(int offset) {
        keyStringPoolChuck = new StringPoolChuck();
        parseStringPoolChuck(offset, keyStringPoolChuck);
    }

    private void parseResTypeSpec(int offset) {
        typeSpecChuck = new TypeSpecChuck();
        typeSpecChuck.chuckHeader = parseChuckHeader(offset);
        typeSpecChuck.typeId = fileBytes[offset + typeSpecChuck.chuckHeader.getSize()] & 0xFF;
        typeSpecChuck.keepRegion = Utils.copyBytes(fileBytes, offset + typeSpecChuck.chuckHeader.getSize() + 1, 3);
        typeSpecChuck.specCount = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + typeSpecChuck.chuckHeader.getSize() + 4, 4));
        typeSpecChuck.specContent = new int[typeSpecChuck.specCount];
        int typeSpecCursor = offset + typeSpecChuck.chuckHeader.getSize() + 8;
        for (int i = 0; i < typeSpecChuck.specCount; ++i) {
            typeSpecChuck.specContent[i] = Utils.byte2Int(Utils.copyBytes(fileBytes, typeSpecCursor, 4));
            typeSpecCursor += 4;
        }
    }

    private void parseResTypeInfo(int offset) {
        typeInfoChuck = new TypeInfoChuck();
        typeInfoChuck.chuckHeader = parseChuckHeader(offset);
        typeInfoChuck.typeId = fileBytes[offset + typeInfoChuck.chuckHeader.getSize()] & 0xFF;
        typeInfoChuck.keepRegion = Utils.copyBytes(fileBytes, offset + typeInfoChuck.chuckHeader.getSize() + 1, 3);
        typeInfoChuck.specCount = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + typeInfoChuck.chuckHeader.getSize() + 4, 4));
        typeInfoChuck.typeInfoOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + typeInfoChuck.chuckHeader.getSize() + 8, 4));
        typeInfoChuck.config = parseTypeInfoConfig(offset + typeInfoChuck.chuckHeader.getSize() + 12);
        int typeInfoEntryOffsets[] = new int[typeInfoChuck.specCount];
        for (int i = 0; i < typeInfoChuck.specCount; ++i) {
            typeInfoEntryOffsets[i] = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + typeSpecChuck.chuckHeader.getSize() + 12 + typeInfoChuck.config.size + i * 4, 4));
        }
        int typeInfoCursor = offset + typeInfoChuck.typeInfoOffset;
        resContent.append("config: \n");
        for (int i = 0; i < typeInfoChuck.specCount; ++i) {
            if (typeInfoCursor >= offset + typeInfoChuck.chuckHeader.chuckSize) {
                break;
            }
            if (typeInfoEntryOffsets[i] == -1) {
                continue;
            }
            int resId = getResID(i);
            if (isTypeMap(typeInfoCursor)) {
                TypeInfoMapEntry entry = new TypeInfoMapEntry();
                parseTypeInfoMapEntry(typeInfoCursor, entry);
                String parentName = "";
                if ((entry.ident >>> 24) == 0x01) {
                    parentName = "SYSTEM RESOURCE";
                } else if (entry.ident == 0) {
                    parentName = "NO PARENT";
                } else {
                    TypeInfoEntry parent = resIdEntryMap.get(entry.ident);
                    if (parent != null) {
                        parentName = getKeyString(parent.keyNameIndex);
                    } else {
                        parentName = Utils.bytes2HexString(Utils.int2Byte(entry.ident));
                    }
                }
                System.out.println("<" + getTypeString(typeInfoChuck.typeId - 1) + " name=\"" + getKeyString(entry.keyNameIndex) +"\" parent=\"" + parentName + "\" id=\"" + Utils.int2HexString(resId) + "\" />");
                resContent.append("<" + getTypeString(typeInfoChuck.typeId - 1) + " name=\"" + getKeyString(entry.keyNameIndex) +"\" parent=\"" + parentName + "\" id=\"" + Utils.int2HexString(resId) + "\" />\n");
                typeInfoCursor += entry.getSize();
                for (int j = 0; j < entry.count; ++j) {
                    if (typeInfoCursor >= offset + typeInfoChuck.chuckHeader.chuckSize) {
                        break;
                    }
                    TypeInfoMapSubEntry subEntry = new TypeInfoMapSubEntry();
                    parseTypeInfoMapSubEntry(typeInfoCursor, subEntry);
                    typeInfoCursor += subEntry.getSize();

                    String indentName = "";
                    if ((subEntry.ident >>> 24) == 0x01) {
                        indentName = "SYSTEM RESOURCE";
                    } else if (resIdEntryMap.containsKey(subEntry.ident)) {
                        indentName = getKeyString(resIdEntryMap.get(subEntry.ident).keyNameIndex);
                    } else {
                        indentName = "NO FOUND";
                    }
                    resContent.append("\t");
                    resContent.append("<item name=\"" + indentName + "\" id=\"" + Utils.int2HexString(subEntry.ident) + "\" type=\"" + subEntry.value.getTypeStr() + "\">" + getValueDataStr(subEntry.value) + "</item>\n");
                }
                resIdEntryMap.put(resId, entry);
            } else {
                TypeInfoNormalEntry entry = new TypeInfoNormalEntry();
                parseTypeInfoEntry(typeInfoCursor, entry);
                if ("attr".equals(getTypeString(typeInfoChuck.typeId - 1))) {
                    int size = Utils.byte2Short(Utils.copyBytes(fileBytes, typeInfoCursor + 12, 2));
                    // 神奇的计算公式，根据二进制推导得到
                    size = 6 * size + 8;
                    typeInfoCursor += (size * 2);
                    resContent.append("<" + getTypeString(typeInfoChuck.typeId - 1) + " name = \"" + getKeyString(entry.keyNameIndex) + "\" id=\"" + Utils.int2HexString(resId) +"\" />\n");
                } else {
                    typeInfoCursor += entry.getSize();
                    resContent.append("<" + getTypeString(typeInfoChuck.typeId - 1) + " name = \"" + getKeyString(entry.keyNameIndex) + "\" id=\"" + Utils.int2HexString(resId) + "\" type=\"" + entry.value.getTypeStr() + "\">" + getValueDataStr(entry.value) + "</" + getTypeString(typeInfoChuck.typeId - 1) + ">\n");
                }
                resIdEntryMap.put(resId, entry);
            }

            // 文件记录的count可能有误, 自己进行越界判断
            if (typeInfoCursor >= offset + typeInfoChuck.chuckHeader.chuckSize) {
                break;
            }
        }
    }

    private void parseStringPoolChuck(int offset, StringPoolChuck stringPoolChuck) {
        stringPoolChuck.chuckHeader = parseChuckHeader(offset);
        stringPoolChuck.stringCount = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + stringPoolChuck.chuckHeader.getSize(), 4));
        stringPoolChuck.stringContent = new String[stringPoolChuck.stringCount];
        stringPoolChuck.styleCount = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + stringPoolChuck.chuckHeader.getSize() + 4, 4));
        stringPoolChuck.styleContent = new String[stringPoolChuck.styleCount];
        byte[] flagBytes = Utils.copyBytes(fileBytes, offset + stringPoolChuck.chuckHeader.getSize() + 8, 4);
        if ((flagBytes[1] & 0xFF) == 0x1) {
            stringPoolChuck.isUTF8 = true;
        }
        stringPoolChuck.stringPoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + stringPoolChuck.chuckHeader.getSize() + 12, 4));
        stringPoolChuck.stylePoolOffset = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + stringPoolChuck.chuckHeader.getSize() + 16, 4));
        int stringPoolCursor = offset + stringPoolChuck.stringPoolOffset;
        for (int i = 0; i < stringPoolChuck.stringCount; ++i) {
            int stringSize = 0;
            byte[] stringSizeBytes = Utils.copyBytes(fileBytes, stringPoolCursor, 2);
            if (stringPoolChuck.isUTF8) {
                // 有出现二进制错误的case，长度记录了两遍，暂时跳过重复记录的长度
//                if ((stringSizeBytes[0] & 0XFF) == 128 && (stringSizeBytes[1] & 0XFF) == 233) {
//                    stringPoolCursor += 2;
//                }
                stringSize = (stringSizeBytes[1] & 0xFF);
                if (stringSize != 0) {
                    // 有出现记录长度不正确的case，会影响后面二进制解析，暂时自己计算长度
                    while (fileBytes[stringPoolCursor + 2 + stringSize] != 0) {
                        stringSize++;
                    }

                    try {
                        stringPoolChuck.stringContent[i] = new String(Utils.copyBytes(fileBytes, stringPoolCursor + 2, stringSize), "utf-8");
                    } catch (Exception e) {
                        stringPoolChuck.stringContent[i] = "";
                        System.out.println("解析utf-8字符串失败");
                    }
                } else {
                    stringPoolChuck.stringContent[i] = "";
                }
                stringPoolCursor += (2 + stringSize + 1);
            } else {
                stringSize = Utils.byte2Short(stringSizeBytes) * 2;
                if (stringSize != 0) {
                    byte[] stringContentBytes = Utils.copyBytes(fileBytes, stringPoolCursor + 2, stringSize + 2);
                    stringPoolChuck.stringContent[i] = Utils.filterStringNull(stringContentBytes);
                } else {
                    stringPoolChuck.stringContent[i] = "";
                }
                stringPoolCursor += (2 + stringSize + 2);
            }
        }
    }

    private ChuckHeader parseChuckHeader(int offset) {
        ChuckHeader chuckHeader = new ChuckHeader();
        chuckHeader.chuckType = Utils.byte2Short(Utils.copyBytes(fileBytes, offset, 2));
        chuckHeader.chuckHeaderSize = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 2, 2));
        chuckHeader.chuckSize = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 4, 4));
        return chuckHeader;
    }

    private TypeInfoConfig parseTypeInfoConfig(int offset) {
        TypeInfoConfig typeInfoConfig = new TypeInfoConfig();
        typeInfoConfig.size = Utils.byte2Int(Utils.copyBytes(fileBytes,  offset, 4));
        typeInfoConfig.mcc = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 4, 2));
        typeInfoConfig.mnc = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 6, 2));
        typeInfoConfig.language = new String(Utils.copyBytes(fileBytes, offset + 8, 2));
        typeInfoConfig.country = new String(Utils.copyBytes(fileBytes, offset + 10, 2));
        typeInfoConfig.screenType = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 12, 4));
        typeInfoConfig.input = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 16, 4));
        typeInfoConfig.screenWidth = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 20, 2));
        typeInfoConfig.screenHeight = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 22, 2));
        typeInfoConfig.version = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 24, 4));
        typeInfoConfig.screenConfig = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 28, 4));
        typeInfoConfig.screenSizeDp = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 32, 4));
        return typeInfoConfig;
    }

    private void parseTypeInfoMapEntry(int offset, TypeInfoMapEntry entry) {
        entry.entrySize = Utils.byte2Short(Utils.copyBytes(fileBytes, offset, 2));
        entry.flag = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 2, 2));
        entry.keyNameIndex = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 4, 4));
        entry.ident = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 8, 4));
        entry.count = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 12, 4));
    }

    private void parseTypeInfoMapSubEntry(int offset, TypeInfoMapSubEntry entry) {
        entry.ident = Utils.byte2Int(Utils.copyBytes(fileBytes, offset, 4));
        entry.value = parseTypeInfoValue(offset + 4);
    }

    private void parseTypeInfoEntry(int offset, TypeInfoNormalEntry entry) {
        entry.entrySize = Utils.byte2Short(Utils.copyBytes(fileBytes, offset, 2));
        entry.flag = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 2, 2));
        entry.keyNameIndex = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 4, 4));
        entry.value = parseTypeInfoValue(offset + 8);
    }

    private TypeInfoValue parseTypeInfoValue(int offset) {
        TypeInfoValue typeInfoValue = new TypeInfoValue();
        typeInfoValue.valueSize = Utils.byte2Short(Utils.copyBytes(fileBytes, offset, 2));
        typeInfoValue.keepRegion = fileBytes[offset + 2] & 0xFF;
        typeInfoValue.dataType = fileBytes[offset + 3] & 0xFF;
        typeInfoValue.data = Utils.byte2Int(Utils.copyBytes(fileBytes, offset + 4, 4));
        return typeInfoValue;
    }

    public boolean isTypeSpec(int offset) {
        int headType = Utils.byte2Short(Utils.copyBytes(fileBytes, offset, 2));
        if (headType == 0x202) {
            return true;
        }
        return false;
    }

    public boolean isTypeMap(int offset) {
        int entryType = Utils.byte2Short(Utils.copyBytes(fileBytes, offset + 2, 2));
        if (entryType == 1) {
            return true;
        }
        return false;
    }

    public String getValueDataStr(TypeInfoValue typeInfoValue) {
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_STRING) {
            return globalStringPoolChuck.stringContent[typeInfoValue.data];
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_ATTRIBUTE) {
            return String.format("?%s%08X", Utils.getPackage(typeInfoValue.data), typeInfoValue.data);
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_REFERENCE) {
            return String.format("@%s%08X",Utils.getPackage(typeInfoValue.data),typeInfoValue.data);
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_FLOAT) {
            return String.valueOf(Float.intBitsToFloat(typeInfoValue.data));
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_INT_HEX) {
            return String.format("0x%08X",typeInfoValue.data);
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_INT_BOOLEAN) {
            return typeInfoValue.data!=0?"true":"false";
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_DIMENSION) {
            return Float.toString(Utils.complexToFloat(typeInfoValue.data))+
                    Utils.DIMENSION_UNITS[typeInfoValue.data & TypeInfoValue.COMPLEX_UNIT_MASK];
        }
        if (typeInfoValue.dataType == TypeInfoValue.TYPE_FRACTION) {
            return Float.toString(Utils.complexToFloat(typeInfoValue.data))+
                    Utils.FRACTION_UNITS[typeInfoValue.data & TypeInfoValue.COMPLEX_UNIT_MASK];
        }
        if (typeInfoValue.dataType >= TypeInfoValue.TYPE_FIRST_COLOR_INT && typeInfoValue.dataType <= TypeInfoValue.TYPE_LAST_COLOR_INT) {
            return String.format("#%08X",typeInfoValue.data);
        }
        if (typeInfoValue.dataType >= TypeInfoValue.TYPE_FIRST_INT && typeInfoValue.dataType <= TypeInfoValue.TYPE_LAST_INT) {
            return String.valueOf(typeInfoValue.data);
        }

        return String.format("<0x%X, type 0x%02X>",typeInfoValue.data, typeInfoValue.dataType);
    }

    private String getKeyString(int index) {
        if (keyStringPoolChuck == null || keyStringPoolChuck.stringContent == null) {
            return null;
        }
        if (index < 0 || index >= keyStringPoolChuck.stringContent.length) {
            return null;
        }
        return keyStringPoolChuck.stringContent[index];
    }

    private String getTypeString(int index) {
        if (typeStringPoolChuck == null || typeStringPoolChuck.stringContent == null) {
            return null;
        }
        if (index < 0 || index >= typeStringPoolChuck.stringContent.length) {
            return null;
        }
        return typeStringPoolChuck.stringContent[index];
    }

    private int getResID(int keyId) {
       int resID = packageChuckHeader.packageId << 24 | typeInfoChuck.typeId << 16 | keyId;
       return resID;
    }
}
