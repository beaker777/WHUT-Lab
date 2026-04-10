package com.beaker.Lab03.backend.util;

/**
 * 欢乐连连看后端共享常量。
 * 统一维护棋盘尺寸、空白值和图块类型数量，避免魔法值分散在多个类中。
 */
public final class LinkGameConstants {

    public static final int BLANK = 0;
    public static final int ICON_TYPE_COUNT = 8;
    public static final String DIFFICULTY_EASY = "EASY";
    public static final String DIFFICULTY_NORMAL = "NORMAL";
    public static final String DIFFICULTY_HARD = "HARD";
    public static final int DEFAULT_ROWS = 10;
    public static final int DEFAULT_COLS = 16;
    public static final int DEFAULT_MAX_TURNS = 2;
    public static final int EASY_ROWS = 8;
    public static final int EASY_COLS = 12;
    public static final int EASY_MAX_TURNS = 3;
    public static final int HARD_ROWS = 12;
    public static final int HARD_COLS = 18;
    public static final int HARD_MAX_TURNS = 1;
    public static final int EASY_MIN_HINT_PAIR_COUNT = 8;
    public static final int NORMAL_MIN_HINT_PAIR_COUNT = 4;
    public static final int NORMAL_MAX_HINT_PAIR_COUNT = 7;
    public static final int HARD_MIN_HINT_PAIR_COUNT = 1;
    public static final int HARD_MAX_HINT_PAIR_COUNT = 3;
}
