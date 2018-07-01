package com.example.isszym.aeroplanechess;

import android.content.Context;
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
    private float xOffset;      // 棋盘在屏幕X方向即右方向的偏移
    private float yOffset;      // 棋盘在屏幕Y方向即下方向的偏移
    private Context context;
    private ImageView boardView;
    private TextView diceView;
    private int diceNumber;
    private Airplane[] planes;
    private int markPlane;      // 被标记的飞机，下次自动走
    private int winner;

    Board(ImageView boardView, TextView diceView, float screenWidth, Context context){
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
        // 调整骰子大小
        ViewGroup.LayoutParams params = diceView.getLayoutParams();
        params.width = (int)(Commdef.DICE_GRID_NUM*gridLength);
        params.height = (int)(Commdef.DICE_GRID_NUM*gridLength);
        diceView.setLayoutParams(params);
    }

    public void initPlanes(ImageView[] planeViews){
        planes = new Airplane[]{
                new Airplane(this, Commdef.BLUE, 0, 0, gridLength, xOffset, yOffset, planeViews[0]),
                new Airplane(this, Commdef.BLUE, 1, 1, gridLength, xOffset, yOffset, planeViews[1]),
                new Airplane(this, Commdef.BLUE, 2, 2, gridLength, xOffset, yOffset, planeViews[2]),
                new Airplane(this, Commdef.BLUE, 3, 3, gridLength, xOffset, yOffset, planeViews[3]),
                new Airplane(this, Commdef.GREEN, 4, 5, gridLength, xOffset, yOffset, planeViews[4]),
                new Airplane(this, Commdef.GREEN, 5, 6, gridLength, xOffset, yOffset, planeViews[5]),
                new Airplane(this, Commdef.GREEN, 6, 7, gridLength, xOffset, yOffset, planeViews[6]),
                new Airplane(this, Commdef.GREEN, 7, 8, gridLength, xOffset, yOffset, planeViews[7]),
                new Airplane(this, Commdef.RED, 8, 10, gridLength, xOffset, yOffset, planeViews[8]),
                new Airplane(this, Commdef.RED, 9, 11, gridLength, xOffset, yOffset, planeViews[9]),
                new Airplane(this, Commdef.RED, 10, 12, gridLength, xOffset, yOffset, planeViews[10]),
                new Airplane(this, Commdef.RED, 11, 13, gridLength, xOffset, yOffset, planeViews[11]),
                new Airplane(this, Commdef.YELLOW, 12, 15, gridLength, xOffset, yOffset, planeViews[12]),
                new Airplane(this, Commdef.YELLOW, 13, 16, gridLength, xOffset, yOffset, planeViews[13]),
                new Airplane(this, Commdef.YELLOW, 14, 17, gridLength, xOffset, yOffset, planeViews[14]),
                new Airplane(this, Commdef.YELLOW, 15, 18, gridLength, xOffset, yOffset, planeViews[15]),
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
        showInfo("是你了, " + Commdef.campName[turn]);
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

    public void beginTurn(){
        diceView.setText("骰子?");
        // 调整骰子的位置
        if (turn == Commdef.BLUE) {
            diceView.setX(0);
            diceView.setY(yOffset + boardLength);
        } else if (turn == Commdef.GREEN) {
            diceView.setX(0);
            diceView.setY(yOffset - Commdef.DICE_GRID_NUM*gridLength);
        } else if (turn == Commdef.RED) {
            diceView.setX(screenWidth - Commdef.DICE_GRID_NUM*gridLength);
            diceView.setY(yOffset - Commdef.DICE_GRID_NUM*gridLength);
        } else if (turn == Commdef.YELLOW) {
            diceView.setX(screenWidth - Commdef.DICE_GRID_NUM*gridLength);
            diceView.setY(yOffset + boardLength);
        }
        diceView.setVisibility(View.VISIBLE);
        diceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diceView.setClickable(false);
                Random rand = new Random();
                diceNumber = rand.nextInt(6) + 1;
                diceView.setText(String.valueOf(diceNumber));
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
                        showInfo("飞");
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
                            showInfo("飞");
                            for (Integer i : outsidePlanes) {
                                planes[i].getReadyToFly();
                            }
                            outsidePlanes.clear();
                        }
                    }
                }
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
//                turn = (turn + 1) % Commdef.PLAYER_NUM;
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
        return xOffset + gridLength * Commdef.POSITIONS[index][0];
    }

    public float getYFromIndex(int index){
        return yOffset + gridLength * Commdef.POSITIONS[index][1];
    }

    public float getGridLength() {
        return gridLength;
    }

    public int getTurn(){
        return turn;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    public void setTurn(int turn){
        this.turn = turn;
    }

    public void setBoardView(ImageView boardView){
        this.boardView = boardView;
    }

    public void setDiceView(TextView diceView){
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