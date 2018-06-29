package com.example.isszym.aeroplanechess;
import android.os.Handler;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class Airplane {
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
    private int steps;                  // 需要走的步数

    Airplane(int camp, int number, int index, float gridLength, float xOffset, float yOffset, ImageView planeView){
        this.camp = camp;
        this.number = number;
        this.portIndex = index;
        this.index = index;
        this.gridLength = gridLength;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.planeView = planeView;
        this.curStep = -1;
        ViewGroup.LayoutParams params = planeView.getLayoutParams();
        params.width = (int)(2*gridLength);
        params.height = (int)(2*gridLength);
        planeView.setLayoutParams(params);
        planeView.setX(getXFromIndex(index));
        planeView.setY(getYFromIndex(index));
        planeView.setVisibility(View.VISIBLE);
    }

    public void receiveDiceNumber(int diceNumber){
        if(isInAirport()) steps = 1;
        else steps = diceNumber;
        moveSteps();
    }

    public void moveSteps(){
        int preIndex = index;
        index = Commdef.COLOR_PATH[camp][curStep + 1];
        TranslateAnimation anim = new TranslateAnimation(0, getXFromIndex(index) - getXFromIndex(preIndex), 0, getYFromIndex(index) - getYFromIndex(preIndex));
        anim.setDuration(1000);
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
                curStep++;
                steps--;
                if(steps != 0) moveSteps();
            }
        });
        planeView.startAnimation(anim);
    }

    // 此飞机是否在机场
    public boolean isInAirport(){
        boolean flag = false;
        for(int port : Commdef.COLOR_AIRPORT[camp]){
            if(port == index){
                flag = true;
                break;
            }
        }
        if(flag) return true;
        else return false;
    }

    public float getXFromIndex(int index){
        return xOffset + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffset + gridLength * Commdef.POSITIONS[index][1];
    }

    public void setListner(final int diceNumber){
        planeView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                receiveDiceNumber(diceNumber);
                planeView.setClickable(false);
            }
        });
    }

    public void restore(){
        index = portIndex;
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

    public int getIndex(){
        return index;
    }

    public ImageView getPlaneView() { return planeView; }



}