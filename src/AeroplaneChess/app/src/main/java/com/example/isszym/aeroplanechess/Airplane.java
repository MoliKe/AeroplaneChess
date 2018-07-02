package com.example.isszym.aeroplanechess;
import android.os.Handler;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Airplane {
    private Board board;                // 调用棋盘方法，是board的一个引用
    private int camp;                   // 飞机阵营
    private int number;                 // 飞机编号，0~15
    private int portIndex;              // 停机处
    private int index;                  // 飞机所在位置0~97
    private int status;                 // 飞机状态（在机场，飞行中, 完成飞行）
    private float gridLength;           // 棋盘上一小格的长度
    private float xOffSet;              // 棋盘在屏幕X方向即右方向的偏移
    private float yOffSet;              // 棋盘在屏幕Y方向即下方向的偏移
    private ImageView planeView;        // 飞机的view
    private int curStep;                // 在己方路径上当前下标0~57
    private ArrayList<Integer> path;    // 飞行棋要走的路径
    private ArrayList<Integer> crack;   // 飞行中的碰撞类型
    private float targetX, targetY;     // 要去的坐标，用于迭子时偏移一点改变坐标

    Airplane(Board board, int camp, int number, int index, float gridLength, float xOffSet, float yOffSet, ImageView planeView){
        this.board = board;
        this.camp = camp;
        this.number = number;
        this.portIndex = index;
        this.index = index;
        this.status = Commdef.WAITING;
        this.gridLength = gridLength;
        this.xOffSet = xOffSet;
        this.yOffSet = yOffSet;
        this.planeView = planeView;
        this.curStep = -1;
        path = new ArrayList<Integer>();
        crack = new ArrayList<Integer>();
        // 根据gridLength来改变棋子的大小
        ViewGroup.LayoutParams params = planeView.getLayoutParams();
        params.width = (int)(2*gridLength);
        params.height = (int)(2*gridLength);
        planeView.setLayoutParams(params);
        // 根据位置调整飞机的角度
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        // 调整飞机的位置，getXFromIndex为根据index来确定在屏幕上的坐标
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
        planeView.setVisibility(View.VISIBLE);
    }

    // 骰子点数diceNumber应用在此飞机上
    public void receiveDiceNumber(int diceNumber){
        // 把飞机view提到布局最高层，从而实现飞过其他棋子时覆盖它们
        planeView.bringToFront();
        // 根据是否在机场来确定要走的步数，因为从机场起飞只能停在出发点
        int steps;
        if(isInAirport()) steps = 1;
        else steps = diceNumber;
        // 当前状态改为飞行
        status = Commdef.FLYING;
        // 调用setPath来获取要走的路径（注意：最后要回到机场比如撞子或到终点的情况，经过setPath后path的最后不会添加停机处的index）
        setPath(steps);
        // 离开当前位置时把当前位置的其他飞机重新调整位置
        board.adjustPosition(index, number);
        // 根据path来走棋
        move();
    }

    /* 规则
    1) 开局
    系统随机判定一个玩家第一个掷骰子，然后以他为基准按顺时针方向轮流掷骰。
    2) 起飞
    传统的规则只有在掷得6点方可起飞，为了提高游戏的激烈程度，增加掷得5点就可以将飞机送入起飞平台。掷得6点可以再掷骰子一次，确定棋子的前进步数；
    3) 连投奖励
    在游戏进行过程中，掷得6点的游戏者可以连续投掷骰子，直至显示点数不是6点或游戏结束。
    4) 迭子
    己方的棋子走至同一格内，可迭在一起，这类情况称为“迭子”。敌方的棋子不能在迭子上面飞过；
    当敌方的棋子正好停留在“迭子”上方时，敌方棋子与2架迭子棋子同时返回停机坪。若其它游戏者所掷点数大于他的棋子与迭子的相差步数，则多余格数为由迭子处返回的格数；
    当其它游戏者所掷点数是6而且大于他得棋子与迭子的相差步数时，那么其它游戏者的棋子可以停于迭子上面，但是当该游戏者依照规则自动再掷点的时候，服务器自动走刚才停于迭子上面的棋子。
    如果棋子在准备通过虚线时有其他棋子停留在虚线和通往终点线路的交叉点时：A、如果对方是一个棋子，则将该棋子逐回基地，本方棋子继续行进到对岸；B、如果对方是两个棋子重叠则该棋子不能穿越虚线、必须绕行。
    5) 撞子
    棋子在行进过程中走至一格时，若已有敌方棋子停留，可将敌方的棋子逐回基地。
    6) 跳子
    棋子在地图行走时，如果停留在和自己颜色相同格子，可以向前一个相同颜色格子作跳跃。
    7) 飞棋
    棋子若行进到颜色相同而有虚线连接的一格，可照虚线箭头指示的路线，通过虚线到前方颜色相同的的一格后，再跳至下一个与棋子颜色相同的格内；若棋子是由上一个颜色相同的格子跳至颜色相同而有虚线连接的一格内，则棋子照虚线箭头指示的路线，通过虚线到前方颜色相同的的一格后，棋子就不再移动。
    8) 终点
    “终点”就是游戏棋子的目的地。当玩家有棋子到达本格时候，表示到达终点，不能再控制该棋子。 玩家要刚好走到终点处才能算“到达”，如果玩家扔出的骰子点数无法刚好走到终点，棋子将往回退多出来的点数。
     */

    public void setPath(int steps){
        for(int i = 1; i <= steps; i++){
            // 先判断往前走一步会不会越过终点
            if(curStep + i < Commdef.PATH_LENGTH){
                // 如果不会，先走为敬
                path.add(Commdef.COLOR_PATH[camp][curStep + i]);
                // 判断往前走一步会不会碰上其他方的迭子
                if(board.isOverlap(Commdef.COLOR_PATH[camp][curStep + i])){
                    // 如果碰上其他方的迭子，判断是不是刚好会停在迭子的位置
                    if(i == steps){
                        // 如果会刚好停在其他方迭子的位置，增加一个同归于尽的碰撞，再结束path的设置
                        crack.add(Commdef.DOWN_TOGETHER);
                        break;
                    }
                    if (board.getDiceNumber() == 6) {
                        // 如果骰子点数为6并且与其他方迭子的距离小于6，那么按照规则要停在其他方的迭子上并再一次抛骰子决定前进步数，所以在board上对这架飞机进行标记，结束path设置
                        board.setMarkPlane(number);
                        break;
                    } else {
                        // 如果骰子点数不为6并且与其他方迭子的距离小于点数，那么要往回退剩余步数，如果回退时又碰上另外的其他方迭子就在此前进，如此反复直至用完步数
                        int tempStep = curStep + i; // 在己方路径上的下标
                        int count = steps - i;      // 剩余步数
                        int direction = -1;         // 往回走还是往前走，-1往回走
                        while(count > 0){
                            count--;
                            if(direction == -1) tempStep--;
                            else tempStep++;
                            path.add(Commdef.COLOR_PATH[camp][tempStep]);
                            // 如果又碰上迭子，方向反转
                            if(board.isOverlap(Commdef.COLOR_PATH[camp][tempStep])) direction = -direction;
                        }
                        // 判断最后到达的位置上有没有其他方的迭子，有则增加一个同归于尽的碰撞，没有其他方的迭子但是有其他方的飞机就增加一个横扫其他人的碰撞
                        if(board.isOverlap(Commdef.COLOR_PATH[camp][tempStep])) {
                            crack.add(Commdef.DOWN_TOGETHER);
                        }
                        else if(board.hasOtherPlane(Commdef.COLOR_PATH[camp][tempStep])){
                            crack.add(Commdef.SWEEP_OTHERS);
                        }
                        break;
                    }
                }
            }
            else{
                // 如果往前走一步会越过终点，那么往回退剩余步数，这个过程中不可能会发生碰撞，不做判断
                for (int j = 1; j <= steps - i + 1; j++) {
                    path.add(Commdef.COLOR_PATH[camp][curStep + i - j - 1]);
                }
                // 结束path设置
                break;
            }

            // 能到这里说明棋子走完步数都没有碰上其他方的迭子
            if(i == steps){
                int mIndex = Commdef.COLOR_PATH[camp][curStep + i];
                // 如果最后一步上有其他人的飞机就增加一个横扫其他人的碰撞
                if(board.hasOtherPlane(mIndex)) crack.add(Commdef.SWEEP_OTHERS);
                // 最后一步是不是大跳
                int index1 = isJetGrid(mIndex);
                if (index1 == -1) {
                    // 如果不是大跳，那是不是同色
                    int index2 = isSameColorGrid(mIndex);
                    if (index2 != -1) {
                        // 如果是同色，那么path增加下一个同色格的index
                        path.add(index2);
                        // 判断下一个同色格上有没有其他方的迭子
                        if(board.isOverlap(index2)) {
                            // 有则增加同归于尽的碰撞，结束path的设置
                            crack.add(Commdef.DOWN_TOGETHER);
                            break;
                        }
                        else if(board.hasOtherPlane(index2)) {
                            // 没有其他方的迭子但是有其他方的飞机就增加横扫碰撞
                            crack.add(Commdef.SWEEP_OTHERS);
                        }
                        else if(!crack.isEmpty()){
                            // 以上情况都没有发生但是前面已经有碰撞发生了，那么就增加一个无碰撞的碰撞，因为path的最后几步和crack一一对应的
                            crack.add(Commdef.NO_CRACK);
                        }
                        // 判断下一个同色格是不是大跳
                        int index3 = isJetGrid(index2);
                        if (index3 != -1) {
                            // 大跳路径上有迭子就不能大跳
                            if(board.isOverlap(Commdef.COLOR_JET[camp][1])) break;
                            // 否则
                            path.add(index3);
                            // 大跳交叉点上有棋子
                            if(board.hasOtherPlane(Commdef.COLOR_JET[camp][1])){
                                if(board.isOverlap(index3)) {
                                    // 并且大跳过去的位置上有迭子
                                    crack.add(Commdef.JET_CRACK_AND_DOWN_TOGETHER);
                                    // 结束path设置
                                    break;
                                }
                                else if(board.hasOtherPlane(index3)) {
                                    // 并且大跳过去没有迭子但是有其他方的棋子
                                    crack.add(Commdef.JET_CRACK_AND_SWEEP_OTHERS);
                                }
                                else{
                                    // 并且大跳过去没有其他人的棋子
                                    crack.add(Commdef.JET_CRACK);
                                }
                            }
                            // 大跳交叉点上没有棋子
                            else{
                                // 根据大跳过去的位置进行增加
                                if(board.isOverlap(index3)) {
                                    crack.add(Commdef.DOWN_TOGETHER);
                                    // 结束path设置
                                    break;
                                }
                                else if(board.hasOtherPlane(index3)) {
                                    crack.add(Commdef.SWEEP_OTHERS);
                                }
                                else if(!crack.isEmpty()){
                                    crack.add(Commdef.NO_CRACK);
                                }
                            }
                        }
                    }
                } else {    // 最后一步是大跳情况
                    // 大跳路径上有迭子就不能大跳
                    if(board.isOverlap(Commdef.COLOR_JET[camp][1])) break;
                    // 否则
                    path.add(index1);
                    // 大跳时交叉点有棋子
                    if(board.hasOtherPlane(Commdef.COLOR_JET[camp][1])){
                        if(board.isOverlap(index1)) {
                            crack.add(Commdef.JET_CRACK_AND_DOWN_TOGETHER);
                            break;
                        }
                        else if(board.hasOtherPlane(index1)) {
                            crack.add(Commdef.JET_CRACK_AND_SWEEP_OTHERS);
                        }
                        else{
                            crack.add(Commdef.JET_CRACK);
                        }
                    }
                    // 大跳交叉点没有棋子
                    else{
                        if(board.isOverlap(index1)) {
                            crack.add(Commdef.DOWN_TOGETHER);
                            break;
                        }
                        else if(board.hasOtherPlane(index1)) {
                            crack.add(Commdef.SWEEP_OTHERS);
                        }
                        else if(!crack.isEmpty()){
                            crack.add(Commdef.NO_CRACK);
                        }
                    }

                    // 大跳之后获取下一个同色格
                    int index4 = isSameColorGrid(index1);
                    path.add(index4);
                    // 根据同色格的情况增加crack
                    if(board.isOverlap(index4)) {
                        crack.add(Commdef.DOWN_TOGETHER);
                        break;
                    }
                    else if(board.hasOtherPlane(index4)) {
                        crack.add(Commdef.SWEEP_OTHERS);
                    }
                    else if(!crack.isEmpty()){
                        crack.add(Commdef.NO_CRACK);
                    }
                }
            }
        }
    }

    // 判断参数index是不是同色的格子(除去最后一个)，若是则返回下一个同色格子的index，否则返回-1
    public int isSameColorGrid(int index){
        int result = -1;
        for(int i = 0; i < Commdef.COLOR_GRID[camp].length; i++){
            if(index == Commdef.COLOR_GRID[camp][i] && i != Commdef.COLOR_GRID[camp].length - 1){
                result = Commdef.COLOR_GRID[camp][i+1];
                break;
            }
        }
        return result;
    }

    // 判断参数index是不是大跳的格子，若是则返回跳到格子的index，否则返回-1
    public int isJetGrid(int index){
        int result = -1;
        if(index == Commdef.COLOR_JET[camp][0]) result = Commdef.COLOR_JET[camp][2];
        return result;
    }

    // 根据已经设置好的path来走棋
    public void move(){
        index = path.get(0);
        // 根据要去的下一个位置调整飞机角度
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        // 如果最后一步上没有碰撞发生
        if(path.size() == 1 && (crack.isEmpty() || crack.get(0) == Commdef.NO_CRACK)){
            int planeNum = board.planeNumOnIndex(index);    // 获取最后一步位置上的飞机数目
            // 根据飞机数目对要去的坐标进行调整，避免几个棋子完全叠在一起看不到
            if(planeNum > 1){
                // Commdef.OVERLAP_DIRECTION中写了每个位置上迭子时飞机坐标的偏移方向
                switch (Commdef.OVERLAP_DIRECTION[index]){
                    case Commdef.UP:
                        targetX = getXFromIndex(index);
                        targetY = getYFromIndex(index) - Commdef.OVERLAP_DISTANCE * gridLength * (planeNum - 1);
                        break;
                    case Commdef.DOWN:
                        targetX = getXFromIndex(index);
                        targetY = getYFromIndex(index) + Commdef.OVERLAP_DISTANCE * gridLength * (planeNum - 1);
                        break;
                    case Commdef.LEFT:
                        targetX = getXFromIndex(index) - Commdef.OVERLAP_DISTANCE * gridLength * (planeNum - 1);
                        targetY = getYFromIndex(index);
                        break;
                    case Commdef.RIGHT:
                        targetX = getXFromIndex(index) + Commdef.OVERLAP_DISTANCE * gridLength * (planeNum - 1);
                        targetY = getYFromIndex(index);
                        break;
                }
            }
            else{
                targetX = getXFromIndex(index);
                targetY = getYFromIndex(index);
            }
        }
        else{
            targetX = getXFromIndex(index);
            targetY = getYFromIndex(index);
        }
        TranslateAnimation anim = new TranslateAnimation(0, targetX - planeView.getX(), 0, targetY - planeView.getY());
        anim.setDuration(500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                planeView.clearAnimation();
                path.remove(0);
                planeView.setX(targetX);
                planeView.setY(targetY);
                // 现在path.size()表示还要走的步数
                if (path.size() < crack.size()) {
                    int crackType = crack.get(0);
                    crack.remove(0);
                    // 根据碰撞类型执行不同操作
                    switch (crackType) {
                        case Commdef.NO_CRACK:
                            break;
                        case Commdef.SWEEP_OTHERS:
                            board.sweepOthers(index);
                            break;
                        case Commdef.DOWN_TOGETHER:
                            // 同归于尽的话在path中增加停机处
                            board.sweepOthers(index);
                            path.add(portIndex);
                            break;
                        case Commdef.JET_CRACK:
                            board.sweepOthers(Commdef.COLOR_JET[camp][1]);
                            break;
                        case Commdef.JET_CRACK_AND_SWEEP_OTHERS:
                            board.sweepOthers(Commdef.COLOR_JET[camp][1]);
                            board.sweepOthers(index);
                            break;
                        case Commdef.JET_CRACK_AND_DOWN_TOGETHER:
                            board.sweepOthers(Commdef.COLOR_JET[camp][1]);
                            board.sweepOthers(index);
                            path.add(portIndex);
                            break;
                    }
                }
                if (!path.isEmpty()) move();
                else {
                    curStep = getStepFromIndex(index);
                    path.clear();
                    crack.clear();
                    // 如果最后一步到达终点，飞机完成任务
                    if (index == Commdef.COLOR_DESTINATION[camp]) finishTask();
                    // 结束回合
                    board.endTurn();
                }
            }
        });
        planeView.startAnimation(anim);
    }

    // 此飞机是否在机场
    public boolean isInAirport(){
        if(status != Commdef.FLYING) return true;
        else return false;
    }

    // 通过index获取在自己路径上的下标
    public int getStepFromIndex(int index){
        int step = -1;
        for(int i = 0; i < Commdef.COLOR_PATH[camp].length; i++){
            if(index == Commdef.COLOR_PATH[camp][i]){
                step = i;
                break;
            }
        }
        return step;
    }

    // 通过index来获取在屏幕上的x坐标
    public float getXFromIndex(int index){
        return xOffSet + gridLength * Commdef.POSITIONS[index][0];
    }

    // 通过index来获取在屏幕上的y坐标
    public float getYFromIndex(int index){
        return yOffSet + gridLength * Commdef.POSITIONS[index][1];
    }

    // 等待被点击飞行
    public void getReadyToFly(){
        // 做一个不断重复的缩放动画，告诉玩家可以移动的棋子
        ScaleAnimation animation =new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.ABSOLUTE, planeView.getX()+gridLength, Animation.ABSOLUTE, planeView.getY()+gridLength);
        animation.setDuration(500);     //设置动画持续时间
        animation.setRepeatCount(-1);   //设置重复次数，-1无限循环
        animation.setRepeatMode(Animation.REVERSE); // 逆序重复
        animation.setFillAfter(false);              // 不用停在最后一帧
        planeView.startAnimation(animation);
        planeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 被点击时禁止其他的棋子再被点击
                board.forbidClick();
                // 调用函数来应用这个点数
                receiveDiceNumber(board.getDiceNumber());
            }
        });
    }

    // 发生撞子时调用（对自己的参数进行调整）
    public void crackByPlane(){
        this.status = Commdef.WAITING;
        index = portIndex;
        this.curStep = -1;
        path.clear();
        crack.clear();
        // 做个动画让它飞回停机处
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        TranslateAnimation anim = new TranslateAnimation(0, getXFromIndex(index) - planeView.getX(), 0, getYFromIndex(index) - planeView.getY());
        anim.setDuration(500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                planeView.clearAnimation();
                planeView.setX(getXFromIndex(index));
                planeView.setY(getYFromIndex(index));
            }
        });
        planeView.startAnimation(anim);
    }

    // 重置，在游戏重新开始时调用
    public void restore(){
        status = Commdef.WAITING;
        index = portIndex;
        curStep = -1;
        path.clear();
        crack.clear();
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
    }

    // 飞机到达终点完成任务时调用
    public void finishTask(){
        this.status = Commdef.FINISHED;
        index = portIndex;
        this.curStep = -1;
        path.clear();
        crack.clear();
        planeView.setBackgroundResource(R.drawable.finished);
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
    }

    public int getCamp() {
        return camp;
    }
    public int getNumber() {
        return number;
    }
    public int getIndex(){
        return index;
    }
    public int getStatus(){
        return status;
    }
    public ImageView getPlaneView() { return planeView; }

}