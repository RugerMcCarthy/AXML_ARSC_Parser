package com.demo.model.ResourcesModel;

public class TypeInfoValue {
    public final static int TYPE_NULL = 0x00;
    public final static int TYPE_REFERENCE = 0x01;
    public final static int TYPE_ATTRIBUTE = 0x02;
    public final static int TYPE_STRING = 0x03;
    public final static int TYPE_FLOAT = 0x04;
    public final static int TYPE_DIMENSION = 0x05;
    public final static int TYPE_FRACTION = 0x06;
    public final static int TYPE_FIRST_INT = 0x10;
    public final static int TYPE_INT_DEC = 0x10;
    public final static int TYPE_INT_HEX = 0x11;
    public final static int TYPE_INT_BOOLEAN = 0x12;
    public final static int TYPE_FIRST_COLOR_INT = 0x1c;
    public final static int TYPE_INT_COLOR_ARGB8 = 0x1c;
    public final static int TYPE_INT_COLOR_RGB8 = 0x1d;
    public final static int TYPE_INT_COLOR_ARGB4 = 0x1e;
    public final static int TYPE_INT_COLOR_RGB4 = 0x1f;
    public final static int TYPE_LAST_COLOR_INT = 0x1f;
    public final static int TYPE_LAST_INT = 0x1f;
    public static final int
            COMPLEX_UNIT_PX			=0,
            COMPLEX_UNIT_DIP		=1,
            COMPLEX_UNIT_SP			=2,
            COMPLEX_UNIT_PT			=3,
            COMPLEX_UNIT_IN			=4,
            COMPLEX_UNIT_MM			=5,
            COMPLEX_UNIT_SHIFT		=0,
            COMPLEX_UNIT_MASK		=15,
            COMPLEX_UNIT_FRACTION	=0,
            COMPLEX_UNIT_FRACTION_PARENT=1,
            COMPLEX_RADIX_23p0		=0,
            COMPLEX_RADIX_16p7		=1,
            COMPLEX_RADIX_8p15		=2,
            COMPLEX_RADIX_0p23		=3,
            COMPLEX_RADIX_SHIFT		=4,
            COMPLEX_RADIX_MASK		=3,
            COMPLEX_MANTISSA_SHIFT	=8,
            COMPLEX_MANTISSA_MASK	=0xFFFFFF;

    public String getTypeStr(){
        switch(dataType){
            case TYPE_NULL:
                return "TYPE_NULL";
            case TYPE_REFERENCE:
                return "TYPE_REFERENCE";
            case TYPE_ATTRIBUTE:
                return "TYPE_ATTRIBUTE";
            case TYPE_STRING:
                return "TYPE_STRING";
            case TYPE_FLOAT:
                return "TYPE_FLOAT";
            case TYPE_DIMENSION:
                return "TYPE_DIMENSION";
            case TYPE_FRACTION:
                return "TYPE_FRACTION";
            case TYPE_FIRST_INT:
                return "TYPE_FIRST_INT";
            case TYPE_INT_HEX:
                return "TYPE_INT_HEX";
            case TYPE_INT_BOOLEAN:
                return "TYPE_INT_BOOLEAN";
            case TYPE_FIRST_COLOR_INT:
                return "TYPE_FIRST_COLOR_INT";
            case TYPE_INT_COLOR_RGB8:
                return "TYPE_INT_COLOR_RGB8";
            case TYPE_INT_COLOR_ARGB4:
                return "TYPE_INT_COLOR_ARGB4";
            case TYPE_INT_COLOR_RGB4:
                return "TYPE_INT_COLOR_RGB4";
        }
        return "";
    }


    public short valueSize;
    public int keepRegion;
    public int dataType;
    public int data;

}
