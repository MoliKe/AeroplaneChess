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
    private int curStep;                // 己方路径上当前下标0~57
    private ArrayList<Integer> path;    // 飞行棋要走的路径
    private ArrayList<Integer> crack;   // 飞行中的碰撞类型
    private float targetX, targetY;

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
        ViewGroup.LayoutParams params = planeView.getLayoutParams();
        params.width = (int)(2*gridLength);
        params.height = (int)(2*gridLength);
        planeView.setLayoutParams(params);
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
        planeView.setVisibility(View.VISIBLE);
    }

    public void receiveDiceNumber(int diceNumber){
        planeView.bringToFront();
        int steps;
        if(isInAirport()) steps = 1;
        else steps = diceNumber;
        status = Commdef.FLYING;
        setPath(steps);
        board.adjustPosition(index, number);
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
            if(curStep + i < Commdef.PATH_LENGTH){
                path.add(Commdef.COLOR_PATH[camp][curStep + i]);
                if(board.isOverlap(Commdef.COLOR_PATH[camp][curStep + i])){
                    if(i == steps){
                        crack.add(Commdef.DOWN_TOGETHER);
                        break;
                    }
                    if (board.getDiceNumber() == 6) {
                        board.setMarkPlane(number);
                        break;
                    } else {
                        // 往回走step-i步
                        int tempStep = curStep + i;
                        int count = steps - i;
                        int direction = -1;     // 往回走还是往前走
                        while(count > 0){
                            count--;
                            if(direction == -1) tempStep--;
                            else tempStep++;
                            path.add(Commdef.COLOR_PATH[camp][tempStep]);
                            if(board.isOverlap(Commdef.COLOR_PATH[camp][tempStep])) direction = -direction;
                        }
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
                // 超过终点往回走step-i+1步
                for (int j = 1; j <= steps - i + 1; j++) {
                    path.add(Commdef.COLOR_PATH[camp][curStep + i - j - 1]);
                }
                break;
            }

            // 能到这里说明棋子安全地走到了最后一步,最后一步没有迭子
            if(i == steps){
                int mIndex = Commdef.COLOR_PATH[camp][curStep + i];
                if(board.hasOtherPlane(mIndex)) crack.add(Commdef.SWEEP_OTHERS);
                // 最后一步是不是大跳
                int index1 = isJetGrid(mIndex);
                if (index1 == -1) {
                    // 如果不是大跳，是不是同色
                    int index2 = isSameColorGrid(mIndex);
                    if (index2 != -1) {
                        path.add(index2);
                        if(board.isOverlap(index2)) {
                            crack.add(Commdef.DOWN_TOGETHER);
                            break;
                        }
                        else if(board.hasOtherPlane(index2)) {
                            crack.add(Commdef.SWEEP_OTHERS);
                        }
                        else if(!crack.isEmpty()){
                            crack.add(Commdef.NO_CRACK);
                        }
                        // 下一个同色格是不是大跳
                        int index3 = isJetGrid(index2);
                        if (index3 != -1) {
                            // 大跳路径上有迭子就不能大跳
                            if(board.isOverlap(Commdef.COLOR_JET[camp][1])) break;
                            // 否则
                            path.add(index3);
                            // 看交叉点上有没有棋子
                            if(board.hasOtherPlane(Commdef.COLOR_JET[camp][1])){
                                if(board.isOverlap(index3)) {
                                    crack.add(Commdef.JET_CRACK_AND_DOWN_TOGETHER);
                                    break;
                                }
                                else if(board.hasOtherPlane(index3)) {
                                    crack.add(Commdef.JET_CRACK_AND_SWEEP_OTHERS);
                                }
                                else{
                                    crack.add(Commdef.JET_CRACK);
                                }
                            }
                            else{
                                if(board.isOverlap(index3)) {
                                    crack.add(Commdef.DOWN_TOGETHER);
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
                } else {
                    // 大跳路径上有迭子就不能大跳
                    if(board.isOverlap(Commdef.COLOR_JET[camp][1])) break;
                    // 最后一步是大跳
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

                    int index4 = isSameColorGrid(index1);
                    path.add(index4);
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

    public void move(){
        index = path.get(0);
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        if(path.size() == 1 && (crack.isEmpty() || crack.get(0) == Commdef.NO_CRACK)){
            int planeNum = board.planeNumOnIndex(index);
            if(planeNum > 1){
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
                if(path.isEmpty()){
                    planeView.setX(targetX);
                    planeView.setY(targetY);
                }
                else{
                    planeView.setX(getXFromIndex(index));
                    planeView.setY(getYFromIndex(index));
                }
                // 现在path.size()表示还要走的步数
                if(path.size() < crack.size()){
                    int crackType = crack.get(0);
                    crack.remove(0);
                    switch (crackType){
                        case Commdef.NO_CRACK:
                            break;
                        case Commdef.SWEEP_OTHERS:
                            board.sweepOthers(index);
                            break;
                        case Commdef.DOWN_TOGETHER:
                            board.downTogether(index);
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
                            board.downTogether(index);
                            break;
                    }
                }
                if(!path.isEmpty()) move();
                else{
                    curStep = getStepFromIndex(index);
                    path.clear();
                    crack.clear();
                    if(index == Commdef.COLOR_DESTINATION[camp]) finishTask();
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

    public float getXFromIndex(int index){
        return xOffSet + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffSet + gridLength * Commdef.POSITIONS[index][1];
    }

    public void getReadyToFly(){
        if(status == Commdef.FINISHED) return;
        ScaleAnimation animation =new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.ABSOLUTE, planeView.getX()+gridLength, Animation.ABSOLUTE, planeView.getY()+gridLength);
        animation.setDuration(500);//设置动画持续时间
        animation.setRepeatCount(-1);//设置重复次数
        animation.setRepeatMode(Animation.REVERSE);
        animation.setFillAfter(false);
        planeView.startAnimation(animation);
        planeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                board.forbidClick();
                receiveDiceNumber(board.getDiceNumber());
            }
        });
    }

    public void crackByPlane(){
        this.status = Commdef.WAITING;
        index = portIndex;
        this.curStep = -1;
        path.clear();
        crack.clear();
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

    public void finishTask(){
        this.status = Commdef.FINISHED;
        index = portIndex;
        this.curStep = -1;
        path.clear();
        crack.clear();
        planeView.setRotation(Commdef.POSITION_ANGLE[index] + 180); // 暂时用翻转机头方向表示完成
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

    public int getCamp() {
        return camp;
    }
    public int getNumber() {
        return number;
    }
    public int getPortIndex(){
        return portIndex;
    }
    public int getIndex(){
        return index;
    }
    public int getStatus(){
        return status;
    }
    public ImageView getPlaneView() { return planeView; }
    public int getCurStep(){
        return curStep;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void setPortIndex(int portIndex){
        this.portIndex = portIndex;
    }
    public void setIndex(int index){
        this.index = index;
    }
    public void setStatus(int status){
        this.status = status;
    }
    public void setPlaneView(ImageView planeView) {
        this.planeView = planeView;
    }
    public void setCurStep(int curStep){
        this.curStep = curStep;
    }

}