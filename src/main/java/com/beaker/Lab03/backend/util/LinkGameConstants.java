package com.beaker.Lab03.backend.util;

/**
 * 欢乐连连看后端共享常量。
 * 统一维护棋盘尺寸、空白值和图块类型数量，避免魔法值分散在多个类中。
 */
public final class LinkGameConstants {

    public static final int BLANK = 0;
    public static final int EXPECTED_ROWS = 10;
    public static final int EXPECTED_COLS = 16;
    public static final int ICON_TYPE_COUNT = 8;
}
