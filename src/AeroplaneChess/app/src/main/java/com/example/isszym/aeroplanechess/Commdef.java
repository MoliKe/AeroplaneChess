package com.example.isszym.aeroplanechess;
// 各种固定参数
public class Commdef {
    // 四个阵营
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int YELLOW = 3;

    // 阵营名称
    public static final String[] campName = {
            "蓝方阵营",
            "绿方阵营",
            "红方阵营",
            "黄方阵营",
    };

    // 游戏类型 （单机，联网）
    public static final int LOCAL_GAME = 0;
    public static final int ONLINE_GAME = 1;

    // 玩家类型
    public static final int HUMAN = 0;
    public static final int AI = 1;

    // 碰撞类型
    public static final int NO_CRACK = 0;
    public static final int SWEEP_OTHERS = 1;
    public static final int DOWN_TOGETHER = 2;
    public static final int JET_CRACK = 3;
    public static final int JET_CRACK_AND_SWEEP_OTHERS = 4;
    public static final int JET_CRACK_AND_DOWN_TOGETHER = 5;

    // 各色飞机编号0~15
    public static final int[] BLUE_PLANE = {0, 1, 2, 3};
    public static final int[] GREEN_PLANE = {4, 5, 6, 7};
    public static final int[] RED_PLANE = {8, 9 ,10, 11};
    public static final int[] YELLOW_PLANE = {12, 13, 14, 15};

    // 各色飞机编号数组
    public static int[][] COLOR_PLANE = {
            {0, 1, 2, 3},       // 蓝
            {4, 5, 6, 7},       // 绿
            {8, 9 ,10, 11},     // 红
            {12, 13, 14, 15}    // 黄
    };

    // 玩家数目
    public static final int PLAYER_NUM = 4;

    // 飞机数目
    public static final int TOTAL_PLANE_NUM = 16;

    // 飞机起飞的骰子点数
    public static final int[] TAKE_OFF_NUMBER = {5, 6};

    // 骰子大小所占格数
    public static final int DICE_GRID_NUM = 6;

    // 所有格子数
    public static final int GIRD_NUM = 97;

    // 游戏状态
    public static final int GAME_NOT_START = 0;
    public static final int GAME_START = 1;
    public static final int GAME_END = 2;

    // 飞机状态
    public static final int WAITING = 0;
    public static final int FLYING = 1;
    public static final int FINISHED = 2;

    // 飞行棋所有能到达的位置,{a,b}表示位置左上角在x方向下标为a，y方向下标为b的格，一共97个位置，按下列顺序编号
    public static final int[][] POSITIONS = {
            {3,29}, {3,31}, {5,29}, {5,31}, {9,33},        // 蓝方机场及出发点
            {3,3}, {3,5}, {5,3}, {5,5}, {1,9},             // 绿方机场及出发点
            {29,3}, {29,5}, {31,3}, {31,5}, {25,1},        // 红方机场及出发点
            {29,29}, {29,31}, {31,29}, {31,31}, {33,25},   // 黄方机场及出发点
            // 下面从左下角开始顺时针写圆圈路径上每个点的位置
            {11,31}, {10,29}, {10,27}, {11,25}, {9,23}, {7,24}, {5,24}, {3,23},
            {2,21}, {2,19}, {2,17}, {2,15}, {2,13},
            {3,11}, {5,10}, {7,10}, {9,11}, {11,9}, {10,7}, {10,5}, {11, 3},
            {13,2}, {15,2}, {17,2}, {19,2}, {21,2},
            {23,3}, {24,5}, {24, 7}, {23, 9}, {25,11}, {27,10}, {29, 10}, {31,11},
            {32,13}, {32,15}, {32,17}, {32,19}, {32,21},
            {31,23}, {29,24}, {27,24}, {25,23}, {23,25}, {24,27}, {24,29}, {23,31},
            {21,32}, {19,32}, {17,32}, {15,32}, {13,32},
            // 下面从蓝色开始顺时针写四个最终路段上每个点的位置
            {17,29}, {17,27}, {17,25}, {17,23}, {17,21}, {17,19},   // 蓝
            {5,17}, {7,17}, {9,17}, {11,17}, {13,17}, {15,17},      // 绿
            {17,5}, {17,7}, {17,9}, {17,11}, {17,13}, {17,15},      // 红
            {29,17}, {27,17}, {25,17}, {23,17}, {21,17}, {19,17},   // 黄
            {17,17}     // 中间点
    };


    // 飞机在每个位置的角度
    public static final int[] POSITION_ANGLE = {
            0, 0, 0, 0, 0,
            90, 90, 90, 90, 90,
            180, 180, 180, 180, 180,
            270, 270, 270, 270, 270,
            0, 0, 0, 270, 270, 270, 270, 0,
            0, 0, 0, 0, 0,
            90, 90, 90, 0, 0, 0, 0, 90,
            90, 90 ,90, 90, 90,
            180, 180, 180, 90, 90, 90, 90, 180,
            180, 180, 180, 180, 180,
            270, 270, 270, 180, 180, 180, 180, 270,
            270, 270, 270, 270, 270,
            0, 0, 0, 0, 0, 0,
            90, 90, 90, 90, 90, 90,
            180, 180, 180, 180, 180, 180,
            270, 270, 270, 270, 270, 270,
            0
    };

    // 迭子是两个棋子之间相差的格子数
    public static final float OVERLAP_DISTANCE = 0.5f;

    // 迭子方向
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    // 每个位置的迭子方向
    public static final int[] OVERLAP_DIRECTION = {
            UP, UP, UP, UP, LEFT,
            UP, UP, UP, UP, UP,
            UP, UP, UP, UP, RIGHT,
            UP, UP, UP, UP, DOWN,
            LEFT, RIGHT, RIGHT, RIGHT, UP, UP, UP, LEFT,
            RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
            UP, DOWN, DOWN, DOWN, RIGHT, RIGHT, RIGHT, UP,
            DOWN, DOWN, DOWN, DOWN, DOWN,
            RIGHT, LEFT, LEFT, LEFT, DOWN, DOWN, DOWN, RIGHT,
            LEFT, LEFT, LEFT, LEFT, LEFT,
            DOWN, UP, UP, UP, LEFT, LEFT, LEFT, DOWN,
            UP, UP, UP, UP, UP,
            RIGHT, RIGHT, RIGHT, RIGHT, RIGHT, RIGHT,
            DOWN, DOWN, DOWN, DOWN, DOWN, DOWN,
            LEFT, LEFT, LEFT, LEFT, LEFT, LEFT,
            UP, UP, UP, UP, UP, UP,
            UP
    };

    // 圆圈路径上的各色方块编号
    public static final int[] BLUE_GRID = {21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69};
    public static final int[] GREEN_GRID = {34, 38, 42, 46, 50, 54, 58, 62, 66, 70, 22, 26, 30};
    public static final int[] RED_GRID = {47, 51, 55, 59, 63, 67, 71, 23, 27, 31, 35, 39, 43};
    public static final int[] YELLOW_GRID = {60, 64, 68, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56};

    // 各色的大跳经过的编号
    public static final int[] BLUE_JET = {37, 86, 49};
    public static final int[] GREEN_JET = {50, 92, 62};
    public static final int[] RED_JET = {63, 74, 23};
    public static final int[] YELLOW_JET = {24, 80, 36};

    // 各色的机场位置编号
    public static final int[] BLUE_AIRPORT = {0, 1, 2, 3};
    public static final int[] GREEN_AIRPORT = {5, 6, 7, 8};
    public static final int[] RED_AIRPORT = {10, 11, 12, 13};
    public static final int[] YELLOW_AIRPORT = {15, 16, 17, 18};

    // 各色的行走路径(没有发生跳步)的编号
    public static final int[] BLUE_PATH = {
            4, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
            51, 52, 53, 54, 55, 56, 57, 58 ,59, 60, 61, 62, 63, 64, 65, 66,
            67, 68, 69, 72, 73, 74, 75, 76, 77
    };
    public static final int[] GREEN_PATH = {
            9, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 ,59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 69, 70, 71, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 78, 79, 80, 81, 82, 83
    };
    public static final int[] RED_PATH = {
            14, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 ,59, 60,
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 84, 85, 86, 87, 88, 89
    };
    public static final int[] YELLOW_PATH = {
            19, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 20, 21,
            22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
            54, 55, 56, 90, 91, 92, 93, 94, 95
    };

    // 下面数组全部按照蓝、绿、红、黄的顺序

    // 圆圈路径上的各色方块编号数组
    public static final int[][] COLOR_GRID = {
            {21, 25, 29, 33, 37, 41, 45, 49, 53, 57, 61, 65, 69},   // 蓝
            {34, 38, 42, 46, 50, 54, 58, 62, 66, 70, 22, 26, 30},   // 绿
            {47, 51, 55, 59, 63, 67, 71, 23, 27, 31, 35, 39, 43},   // 红
            {60, 64, 68, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56}    // 黄
    };

    // 各色的大跳方块的编号数组
    public static final int[][] COLOR_JET = {
            {37, 86, 49},
            {50, 92, 62},
            {63, 74, 23},
            {24, 80, 36}
    };


    // 各色的机场位置编号数组
    public static final int[][] COLOR_AIRPORT = {
            {0, 1, 2, 3},       // 蓝
            {5, 6, 7, 8},       // 绿
            {10, 11, 12, 13},   // 红
            {15, 16, 17, 18}    // 黄
    };

    // 各色的路径数组
    public static final int[][] COLOR_PATH = {
            // 蓝
            {4, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
            35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
            51, 52, 53, 54, 55, 56, 57, 58 ,59, 60, 61, 62, 63, 64, 65, 66,
            67, 68, 69, 72, 73, 74, 75, 76, 77},
            // 绿
            {9, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 ,59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 69, 70, 71, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 78, 79, 80, 81, 82, 83},
            // 红
            {14, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 ,59, 60,
            61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 20, 21, 22, 23, 24,
            25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
            41, 42, 43, 84, 85, 86, 87, 88, 89},
            // 黄
            {19, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 20, 21,
            22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
            54, 55, 56, 90, 91, 92, 93, 94, 95}
    };

    // 各色的终点
    public static final int[] COLOR_DESTINATION = {77, 83, 89, 95};

    // 各色路径的长度
    public static final int PATH_LENGTH = 57;

}