package com.example.isszym.aeroplanechess;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v4.app.INotificationSideChannel;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class Board {
    private int status;         // 状态（游戏未开始，游戏已开始，游戏结束）
    private int turn;
    private float screenWidth;
    private float boardLength;
    private float gridLength;
    private float xOffSet;      // 棋盘在屏幕X方向即右方向的偏移
    private float yOffSet;      // 棋盘在屏幕Y方向即下方向的偏移
    private Context context;
    private ImageView boardView;
    private ImageView diceView;
    private int diceNumber;
    private Airplane[] planes;
    private int markPlane;      // 被标记的飞机，下次自动走，在迭在别人迭子上时用
    private int winner;
    private TextView[] playerViews;


    Board(ImageView boardView, ImageView diceView, float screenWidth, Context context, TextView[] playerViews){
        this.status = Commdef.GAME_NOT_START;
        this.screenWidth = screenWidth;
        this.boardView = boardView;
        this.diceView = diceView;
        this.context = context;
        boardLength = (int)(screenWidth / 18) * 18;
        gridLength = boardLength / 36;
        ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
        boardParams.width = (int)boardLength;
        boardParams.height = (int)boardLength;
        boardView.setLayoutParams(boardParams);
        this.playerViews = new TextView[4];
        this.playerViews[0] = playerViews[0];
        this.playerViews[1] = playerViews[1];
        this.playerViews[2] = playerViews[2];
        this.playerViews[3] = playerViews[3];
    }

    public void initPlanes(ImageView[] planeViews){
        planes = new Airplane[]{
                new Airplane(this, Commdef.BLUE, 0, 0, gridLength, xOffSet, yOffSet, planeViews[0]),
                new Airplane(this, Commdef.BLUE, 1, 1, gridLength, xOffSet, yOffSet, planeViews[1]),
                new Airplane(this, Commdef.BLUE, 2, 2, gridLength, xOffSet, yOffSet, planeViews[2]),
                new Airplane(this, Commdef.BLUE, 3, 3, gridLength, xOffSet, yOffSet, planeViews[3]),
                new Airplane(this, Commdef.GREEN, 4, 5, gridLength, xOffSet, yOffSet, planeViews[4]),
                new Airplane(this, Commdef.GREEN, 5, 6, gridLength, xOffSet, yOffSet, planeViews[5]),
                new Airplane(this, Commdef.GREEN, 6, 7, gridLength, xOffSet, yOffSet, planeViews[6]),
                new Airplane(this, Commdef.GREEN, 7, 8, gridLength, xOffSet, yOffSet, planeViews[7]),
                new Airplane(this, Commdef.RED, 8, 10, gridLength, xOffSet, yOffSet, planeViews[8]),
                new Airplane(this, Commdef.RED, 9, 11, gridLength, xOffSet, yOffSet, planeViews[9]),
                new Airplane(this, Commdef.RED, 10, 12, gridLength, xOffSet, yOffSet, planeViews[10]),
                new Airplane(this, Commdef.RED, 11, 13, gridLength, xOffSet, yOffSet, planeViews[11]),
                new Airplane(this, Commdef.YELLOW, 12, 15, gridLength, xOffSet, yOffSet, planeViews[12]),
                new Airplane(this, Commdef.YELLOW, 13, 16, gridLength, xOffSet, yOffSet, planeViews[13]),
                new Airplane(this, Commdef.YELLOW, 14, 17, gridLength, xOffSet, yOffSet, planeViews[14]),
                new Airplane(this, Commdef.YELLOW, 15, 18, gridLength, xOffSet, yOffSet, planeViews[15]),
        };
    }

    public void gameStart(){
        forbidClick();
        status = Commdef.GAME_START;
        // 还原飞机位置
        for (Airplane plane : planes) {
            plane.restore();
        }
        // 随机决定哪方先开始
        Random rand = new Random();
        turn = rand.nextInt(4);
        showInfo(Commdef.campName[turn] + "开始");
        markPlane = -1;
        winner = -1;
        beginTurn();
    }

    public void gameEnd(){
        winner = turn;
        showInfo("恭喜" + Commdef.campName[winner] + "获得胜利!!");
        status = Commdef.GAME_END;
    }

    public boolean checkGameEnd(){
        int finishPlaneNum = 0;
        for(int i : Commdef.COLOR_PLANE[turn]){
            if(planes[i].getStatus() == Commdef.FINISHED) finishPlaneNum++;
        }
        if(finishPlaneNum == 4) return true;
        else return false;
    }

    public void adjustPosition(int index, int number){
        int planeNum = 0;
        float indexX = getXFromIndex(index);
        float indexY = getYFromIndex(index);
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getNumber() != number){
                float adjustX = 0, adjustY = 0;
                switch (Commdef.OVERLAP_DIRECTION[index]){
                    case Commdef.UP:
                        adjustX = indexX;
                        adjustY = indexY - Commdef.OVERLAP_DISTANCE * gridLength * planeNum;
                        break;
                    case Commdef.DOWN:
                        adjustX = indexX;
                        adjustY = indexY + Commdef.OVERLAP_DISTANCE * gridLength * planeNum ;
                        break;
                    case Commdef.LEFT:
                        adjustX = indexX - Commdef.OVERLAP_DISTANCE * gridLength * planeNum;
                        adjustY = indexY;
                        break;
                    case Commdef.RIGHT:
                        adjustX = indexX + Commdef.OVERLAP_DISTANCE * gridLength * planeNum;
                        adjustY = indexY;
                        break;
                }
                plane.getPlaneView().setX(adjustX);
                plane.getPlaneView().setY(adjustY);
                planeNum++;
            }
        }
    }

    public void beginTurn(){
        diceView.setBackgroundResource(R.drawable.diceanim);
        // 调整骰子的位置
        if (turn == Commdef.BLUE || turn == Commdef.GREEN) {
            diceView.setX((float)(playerViews[turn].getX() + playerViews[turn].getWidth() * 0.64));
            diceView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
        } else {
            diceView.setX((float)(playerViews[turn].getX() + playerViews[turn].getWidth() * 0.07));
            diceView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
        }
        diceView.setVisibility(View.VISIBLE);
        diceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diceView.setClickable(false);
                final AnimationDrawable diceAnim = (AnimationDrawable) diceView.getBackground();
                diceAnim.start();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        diceAnim.stop();
                        Random rand = new Random();
                        diceNumber = rand.nextInt(6) + 1;
                        if(diceNumber == 1) diceView.setBackgroundResource(R.drawable.dice1);
                        else if(diceNumber == 2) diceView.setBackgroundResource(R.drawable.dice2);
                        else if(diceNumber == 3) diceView.setBackgroundResource(R.drawable.dice3);
                        else if(diceNumber == 4) diceView.setBackgroundResource(R.drawable.dice4);
                        else if(diceNumber == 5) diceView.setBackgroundResource(R.drawable.dice5);
                        else if(diceNumber == 6) diceView.setBackgroundResource(R.drawable.dice6);

                        ArrayList<Integer> outsidePlanes = new ArrayList<Integer>();
                        if(markPlane != -1){
                            planes[markPlane].receiveDiceNumber(diceNumber);
                            markPlane = -1;
                        }
                        else {
                            // 是否全在机场
                            boolean isAllInAirport = true;
                            for (int i : Commdef.COLOR_PLANE[turn]) {
                                if (!planes[i].isInAirport()) {
                                    isAllInAirport = false;
                                    outsidePlanes.add(i);
                                }
                            }
                            // 是否是起飞的点数
                            boolean ableToTakeOff = false;
                            for (int each : Commdef.TAKE_OFF_NUMBER) {
                                if (each == diceNumber) {
                                    ableToTakeOff = true;
                                    break;
                                }
                            }
                            if (ableToTakeOff) {
                                for (int i : Commdef.COLOR_PLANE[turn]) {
                                    if (planes[i].getStatus() != Commdef.FINISHED)
                                        planes[i].getReadyToFly();
                                }
                            } else {
                                if (isAllInAirport) {
                                    showInfo("无法起飞");
                                    new Handler().postDelayed(new Runnable() {
                                        public void run() {
                                            turn = (turn + 1) % Commdef.PLAYER_NUM;
                                            beginTurn();
                                        }

                                    }, 1000);   // 等待一秒后执行
                                } else {
                                    for (Integer i : outsidePlanes) {
                                        planes[i].getReadyToFly();
                                    }
                                    outsidePlanes.clear();
                                }
                            }
                        }
                    }

                }, 1000);   // 等待一秒后执行


            }
        });
    }

    public void endTurn(){
        if(checkGameEnd()){
            gameEnd();
        }
        else{
            if(diceNumber == 6){
                beginTurn();
            }
            else{
                turn = (turn + 1) % Commdef.PLAYER_NUM;
                beginTurn();
            }
        }
    }

    public void sweepOthers(int index){
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getCamp() != turn){
                showInfo("撞子啦");
                plane.crackByPlane();
            }
        }
    }

    public void downTogether(int index){
        for(Airplane plane : planes){
            if(plane.getIndex() == index){
                showInfo("撞子啦");
                plane.crackByPlane();
            }
        }
    }

    public void forbidClick(){
        for(Airplane plane : planes){
            plane.getPlaneView().setClickable(false);
            plane.getPlaneView().clearAnimation();
        }
    }

    public boolean isOverlap(int index){
        int planeNum = 0;
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getCamp() != turn) {
                planeNum++;
                if(planeNum >= 2) return true;
            }
        }
        return false;
    }

    public boolean hasOtherPlane(int index){
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getCamp() != turn) return true;
        }
        return false;
    }

    public int planeNumOnIndex(int index){
        int planeNum = 0;
        for(Airplane plane : planes){
            if(plane.getIndex() == index) {
                planeNum++;
            }
        }
        return  planeNum;
    }

    public void showInfo(String sentence){
        Toast.makeText(context, sentence, Toast.LENGTH_SHORT).show();
    }

    public int getDiceNumber(){
        return diceNumber;
    }

    public ImageView getBoardView() {
        return boardView;
    }

    public float getXFromIndex(int index){
        return xOffSet + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffSet + gridLength * Commdef.POSITIONS[index][1];
    }

    public float getGridLength() {
        return gridLength;
    }

    public int getTurn(){
        return turn;
    }

    public void setXOffSet(float xOffSet) {
        this.xOffSet = xOffSet;
    }

    public void setYOffSet(float yOffSet) {
        this.yOffSet = yOffSet;
        // 调整骰子大小
        ViewGroup.LayoutParams diceParams = diceView.getLayoutParams();
        diceParams.width = (int)(yOffSet * 0.4);
        diceParams.height = (int)(yOffSet * 0.4);
        diceView.setLayoutParams(diceParams);
        // 调整玩家信息框的大小
        for(int i = 0; i < 4; i++){
            ViewGroup.LayoutParams playerParams = playerViews[i].getLayoutParams();
            playerParams.width = (int)(yOffSet * 1.4);
            playerParams.height = (int)(yOffSet);
            playerViews[i].setLayoutParams(playerParams);
        }
    }

    public void setTurn(int turn){
        this.turn = turn;
    }

    public void setBoardView(ImageView boardView){
        this.boardView = boardView;
    }

    public void setDiceView(ImageView diceView){
        this.diceView = diceView;
    }

    public void setLength(float screenWidth){
        this.screenWidth = screenWidth;
        boardLength = (int)(screenWidth / 18) * 18;
        gridLength = boardLength / 36;
        ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
        boardParams.width = (int)boardLength;
        boardParams.height = (int)boardLength;
        boardView.setLayoutParams(boardParams);
    }

    public void setMarkPlane(int number){
        markPlane = number;
    }
}