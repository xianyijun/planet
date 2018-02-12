package cn.xianyijun.planet.common.serialize.generic;

/**
 * The interface Generic data flags.
 */
public interface GenericDataFlags {
    /**
     * The constant VARINT.
     */
    byte VARINT = 0, /**
     * The Object.
     */
    OBJECT = (byte) 0x80;

    /**
     * The constant VARINT8.
     */
// varint tag
    byte VARINT8 = VARINT, /**
     * The Varint 16.
     */
    VARINT16 = VARINT | 1, /**
     * The Varint 24.
     */
    VARINT24 = VARINT | 2, /**
     * The Varint 32.
     */
    VARINT32 = VARINT | 3;

    /**
     * The constant VARINT40.
     */
    byte VARINT40 = VARINT | 4, /**
     * The Varint 48.
     */
    VARINT48 = VARINT | 5, /**
     * The Varint 56.
     */
    VARINT56 = VARINT | 6, /**
     * The Varint 64.
     */
    VARINT64 = VARINT | 7;

    /**
     * The constant VARINT_NF.
     */
// varint contants
    byte VARINT_NF = VARINT | 10, /**
     * The Varint ne.
     */
    VARINT_NE = VARINT | 11, /**
     * The Varint nd.
     */
    VARINT_ND = VARINT | 12;

    /**
     * The constant VARINT_NC.
     */
    byte VARINT_NC = VARINT | 13, /**
     * The Varint nb.
     */
    VARINT_NB = VARINT | 14, /**
     * The Varint na.
     */
    VARINT_NA = VARINT | 15, /**
     * The Varint n 9.
     */
    VARINT_N9 = VARINT | 16;

    /**
     * The constant VARINT_N8.
     */
    byte VARINT_N8 = VARINT | 17, /**
     * The Varint n 7.
     */
    VARINT_N7 = VARINT | 18, /**
     * The Varint n 6.
     */
    VARINT_N6 = VARINT | 19, /**
     * The Varint n 5.
     */
    VARINT_N5 = VARINT | 20;

    /**
     * The constant VARINT_N4.
     */
    byte VARINT_N4 = VARINT | 21, /**
     * The Varint n 3.
     */
    VARINT_N3 = VARINT | 22, /**
     * The Varint n 2.
     */
    VARINT_N2 = VARINT | 23, /**
     * The Varint n 1.
     */
    VARINT_N1 = VARINT | 24;

    /**
     * The constant VARINT_0.
     */
    byte VARINT_0 = VARINT | 25, /**
     * The Varint 1.
     */
    VARINT_1 = VARINT | 26, /**
     * The Varint 2.
     */
    VARINT_2 = VARINT | 27, /**
     * The Varint 3.
     */
    VARINT_3 = VARINT | 28;

    /**
     * The constant VARINT_4.
     */
    byte VARINT_4 = VARINT | 29, /**
     * The Varint 5.
     */
    VARINT_5 = VARINT | 30, /**
     * The Varint 6.
     */
    VARINT_6 = VARINT | 31, /**
     * The Varint 7.
     */
    VARINT_7 = VARINT | 32;

    /**
     * The constant VARINT_8.
     */
    byte VARINT_8 = VARINT | 33, /**
     * The Varint 9.
     */
    VARINT_9 = VARINT | 34, /**
     * The Varint a.
     */
    VARINT_A = VARINT | 35, /**
     * The Varint b.
     */
    VARINT_B = VARINT | 36;

    /**
     * The constant VARINT_C.
     */
    byte VARINT_C = VARINT | 37, /**
     * The Varint d.
     */
    VARINT_D = VARINT | 38, /**
     * The Varint e.
     */
    VARINT_E = VARINT | 39, /**
     * The Varint f.
     */
    VARINT_F = VARINT | 40;

    /**
     * The constant VARINT_10.
     */
    byte VARINT_10 = VARINT | 41, /**
     * The Varint 11.
     */
    VARINT_11 = VARINT | 42, /**
     * The Varint 12.
     */
    VARINT_12 = VARINT | 43, /**
     * The Varint 13.
     */
    VARINT_13 = VARINT | 44;

    /**
     * The constant VARINT_14.
     */
    byte VARINT_14 = VARINT | 45, /**
     * The Varint 15.
     */
    VARINT_15 = VARINT | 46, /**
     * The Varint 16.
     */
    VARINT_16 = VARINT | 47, /**
     * The Varint 17.
     */
    VARINT_17 = VARINT | 48;

    /**
     * The constant VARINT_18.
     */
    byte VARINT_18 = VARINT | 49, /**
     * The Varint 19.
     */
    VARINT_19 = VARINT | 50, /**
     * The Varint 1 a.
     */
    VARINT_1A = VARINT | 51, /**
     * The Varint 1 b.
     */
    VARINT_1B = VARINT | 52;

    /**
     * The constant VARINT_1C.
     */
    byte VARINT_1C = VARINT | 53, /**
     * The Varint 1 d.
     */
    VARINT_1D = VARINT | 54, /**
     * The Varint 1 e.
     */
    VARINT_1E = VARINT | 55, /**
     * The Varint 1 f.
     */
    VARINT_1F = VARINT | 56;

    /**
     * The constant OBJECT_REF.
     */
// object tag
    byte OBJECT_REF = OBJECT | 1, /**
     * The Object stream.
     */
    OBJECT_STREAM = OBJECT | 2, /**
     * The Object bytes.
     */
    OBJECT_BYTES = OBJECT | 3;

    /**
     * The constant OBJECT_VALUE.
     */
    byte OBJECT_VALUE = OBJECT | 4, /**
     * The Object values.
     */
    OBJECT_VALUES = OBJECT | 5, /**
     * The Object map.
     */
    OBJECT_MAP = OBJECT | 6;

    /**
     * The constant OBJECT_DESC.
     */
    byte OBJECT_DESC = OBJECT | 10, /**
     * The Object desc id.
     */
    OBJECT_DESC_ID = OBJECT | 11;

    /**
     * The constant OBJECT_NULL.
     */
// object constants
    byte OBJECT_NULL = OBJECT | 20, /**
     * The Object dummy.
     */
    OBJECT_DUMMY = OBJECT | 21;
}