package com.example.isszym.aeroplanechess;
import android.os.Handler;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Random;

public class Airplane {
    private Board board;                // 调用棋盘方法，是board的一个引用
    private int camp;                   // 飞机阵营
    private int number;                 // 飞机编号，0~15
    private int portIndex;              // 停机处
    private int index;                  // 飞机所在位置0~97
    private int status;                 // 飞机状态（在机场，飞行中, 完成飞行）
    private float gridLength;           // 棋盘上一小格的长度
    private float xOffset;              // 棋盘在屏幕X方向即右方向的偏移
    private float yOffset;              // 棋盘在屏幕Y方向即下方向的偏移
    private ImageView planeView;        // 飞机的view
    private int curStep;                // 己方路径上当前下标0~57
    private ArrayList<Integer> path;    // 飞行棋要走的路径
    private int crackNum;               // 飞行棋要走的路径最后多少步可能会碰撞

    Airplane(Board board, int camp, int number, int index, float gridLength, float xOffset, float yOffset, ImageView planeView){
        this.board = board;
        this.camp = camp;
        this.number = number;
        this.portIndex = index;
        this.index = index;
        this.status = Commdef.WAITING;
        this.gridLength = gridLength;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.planeView = planeView;
        this.curStep = -1;
        path = new ArrayList<Integer>();
        crackNum = 0;
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
        int steps;
        if(isInAirport()) steps = 1;
        else steps = diceNumber;
        status = Commdef.FLYING;
        setPath(steps);
        move();
    }

    public void setPath(int steps){
        for(int i = 1; i <= steps; i++){
            if(curStep + i >= Commdef.PATH_LENGTH){
                path.add(Commdef.COLOR_PATH[camp][2 * Commdef.PATH_LENGTH - curStep - i - 2]);
            }
            else{
                path.add(Commdef.COLOR_PATH[camp][curStep + i]);
                if(i == steps){
                    // 最后一步是终点
                    if(curStep + i == Commdef.PATH_LENGTH - 1){
                        path.add(portIndex);
                        status = Commdef.FINISHED;
                    }
                    // 最后一步不是终点
                    else {
                        crackNum += 1;
                        int mIndex = Commdef.COLOR_PATH[camp][curStep + i];
                        // 最后一步是不是大跳
                        if (isJetGrid(mIndex) == -1) {
                            // 如果不是大跳，最后一步是不是同色
                            int jumpIndex = isSameColorGrid(mIndex);
                            if (jumpIndex != -1) {
                                path.add(jumpIndex);
                                crackNum += 1;
                                // 最后一步是同色，那下一个同色格是不是大跳
                                if (isJetGrid(jumpIndex) != -1) {
                                    path.add(isJetGrid(jumpIndex));
                                    crackNum += 1;
                                }
                            }
                        } else {
                            // 最后一步是大跳
                            path.add(isJetGrid(mIndex));
                            path.add(isSameColorGrid(isJetGrid(mIndex)));
                            crackNum += 2;
                        }
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
        int preIndex = index;
        index = path.get(0);
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        TranslateAnimation anim = new TranslateAnimation(0, getXFromIndex(index) - getXFromIndex(preIndex), 0, getYFromIndex(index) - getYFromIndex(preIndex));
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
                path.remove(0);
                // path.size()表示还要走的步数
                if(path.size() < crackNum){
                    if(index == Commdef.COLOR_JET[camp][2]){
                        board.sweepIndex(Commdef.COLOR_JET[camp][1]);
                        board.sweepIndex(Commdef.COLOR_JET[camp][2]);
                    }
                    else{
                        board.sweepIndex(index);
                    }
                }
                if(!path.isEmpty()) move();
                else{
                    curStep = getStepFromIndex(index);
                    path.clear();
                    crackNum = 0;
                    if(board.getDiceNumber() == 6){
                        board.beginTurn();
                    }
                    else{
                        board.setTurn((board.getTurn() + 1) % Commdef.PLAYER_NUM);
                        board.beginTurn();
                    }
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
        return xOffset + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffset + gridLength * Commdef.POSITIONS[index][1];
    }

    public void getReadyToFly(final int diceNumber){
        if(status == Commdef.FINISHED) return;
        planeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                board.forbidClick();
                receiveDiceNumber(diceNumber);
                planeView.setClickable(false);
            }
        });
    }

    public void crackByPlane(){
        int preIndex = index;
        this.status = Commdef.WAITING;
        index = portIndex;
        this.curStep = -1;
        path = new ArrayList<Integer>();
        crackNum = 0;
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        TranslateAnimation anim = new TranslateAnimation(0, getXFromIndex(index) - getXFromIndex(preIndex), 0, getYFromIndex(index) - getYFromIndex(preIndex));
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
        crackNum = 0;
        planeView.setRotation(Commdef.POSITION_ANGLE[index]);
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
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