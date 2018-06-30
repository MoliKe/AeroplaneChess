package com.example.isszym.aeroplanechess;
import android.os.Handler;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;

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
    private int curStep;                // 己方路径上当前下标
    private ArrayList<Integer> path;    // 飞行棋要走的路径

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
        ViewGroup.LayoutParams params = planeView.getLayoutParams();
        params.width = (int)(2*gridLength);
        params.height = (int)(2*gridLength);
        planeView.setLayoutParams(params);
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
        path.clear();
        for(int i = 1; i <= steps; i++){
            path.add(Commdef.COLOR_PATH[camp][curStep + i]);
            if(i == steps){
                int mIndex = Commdef.COLOR_PATH[camp][curStep + i];
                if(isJetGrid(mIndex) == -1){
                    int jumpIndex = isSameColorGrid(mIndex);
                    if(jumpIndex != -1){
                        path.add(jumpIndex);
                        if(isJetGrid(jumpIndex) != -1){
                            path.add(isJetGrid(jumpIndex));
                        }
                    }
                }
                else{
                    path.add(isJetGrid(mIndex));
                    path.add(isSameColorGrid(isJetGrid(mIndex)));
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
        if(index == Commdef.COLOR_JET[camp][0]) result = Commdef.COLOR_JET[camp][1];
        return result;
    }

    public void move(){
        int preIndex = index;
        index = path.get(0);
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
                if(!path.isEmpty()) move();
                else{
                    curStep = getStepFromIndex(index);
                    if(board.getDiceNumber() == 6){
                        board.beginTurn();
                    }
                    else{
                        board.setTurn((board.getTurn() + 1) % 4);
                        board.beginTurn();
                    }
                }
            }
        });
        planeView.startAnimation(anim);
    }

    // 此飞机是否在机场
    public boolean isInAirport(){
        if(status == Commdef.WAITING) return true;
        else return false;
    }

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

    public void setListner(final int diceNumber){
        planeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                board.forbidClick();
                receiveDiceNumber(diceNumber);
                planeView.setClickable(false);
            }
        });
    }

    public void restore(){
        index = portIndex;
        status = Commdef.WAITING;
        curStep = -1;
        ViewGroup.LayoutParams params = planeView.getLayoutParams();
        params.width = (int)(2*gridLength);
        params.height = (int)(2*gridLength);
        planeView.setLayoutParams(params);
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