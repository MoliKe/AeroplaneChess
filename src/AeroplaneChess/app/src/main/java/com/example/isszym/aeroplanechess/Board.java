package com.example.isszym.aeroplanechess;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
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
import java.util.HashMap;
import java.util.Random;

public class Board {
    private int status;             // 状态（游戏未开始，游戏已开始，游戏结束）
    private int turn;               // 当前回合
    private float screenWidth;      // 屏幕宽度
    private float boardLength;      // 棋盘宽度
    private float gridLength;       // 棋盘上一小格的宽度
    private float xOffSet;          // 棋盘在屏幕X方向即右方向的偏移
    private float yOffSet;          // 棋盘在屏幕Y方向即下方向的偏移
    private ImageView boardView;    // 棋盘view
    private ImageView diceView;     // 骰子view
    private ImageView arrowView;    // 指示当前回合的箭头
    private TextView tipView;       // 提示view
    private int diceNumber;         // 骰子点数
    private Airplane[] planes;      // 16架飞机
    private int markPlane;          // 被标记的飞机，下次自动走，在迭在别人迭子上时用
    private int winner;             // 胜利者
    private TextView[] playerViews; // 4个玩家信息view
    private SoundPool sp;           // 音效池
    private HashMap<Integer, Integer> soundMap; // 音效类型到音效id的映射
    private int gameType;           // 游戏类型，单机、联网
    private int[] playerType;       // 四个玩家类型，人类、AI
    private int myCamp;             // 自己阵营

    Board(ImageView boardView, ImageView diceView, ImageView arrowView, TextView tipView, float screenWidth, TextView[] playerViews, SoundPool sp, HashMap<Integer, Integer> soundMap){
        this.status = Commdef.GAME_NOT_START;
        this.screenWidth = screenWidth;
        this.boardView = boardView;
        this.diceView = diceView;
        this.arrowView = arrowView;
        this.tipView = tipView;
        this.sp = sp;
        this.soundMap = soundMap;
        boardLength = (int)(screenWidth / 18) * 18;
        gridLength = boardLength / 36;
        // 调整棋盘大小
        ViewGroup.LayoutParams boardParams = boardView.getLayoutParams();
        boardParams.width = (int)boardLength;
        boardParams.height = (int)boardLength;
        boardView.setLayoutParams(boardParams);
        // 调整提示框大小
        ViewGroup.LayoutParams tipParams = tipView.getLayoutParams();
        tipParams.width = (int)boardLength;
        tipParams.height = (int)(boardLength * 0.5);
        tipView.setLayoutParams(tipParams);

        this.playerViews = new TextView[4];
        this.playerViews[0] = playerViews[0];
        this.playerViews[1] = playerViews[1];
        this.playerViews[2] = playerViews[2];
        this.playerViews[3] = playerViews[3];
    }

    // 初始化飞机
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

    // 开始游戏
    public void gameStart(){
        // 禁止点击棋子
        forbidClick();
        // 初始化游戏类型，玩家类型
        gameType = Commdef.LOCAL_GAME;
        playerType = new int[]{Commdef.HUMAN, Commdef.HUMAN, Commdef.HUMAN, Commdef.HUMAN};
        // 如果是联网模式，还要初始化myCamp

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

    // 结束游戏
    public void gameEnd(){
        winner = turn;
        showInfo("恭喜" + Commdef.campName[winner] + "获得胜利!!");
        status = Commdef.GAME_END;

        // 联网模式还要广播获胜消息
        if(gameType == Commdef.ONLINE_GAME){

        }
    }

    // 检查游戏是否结束，但有当前回合阵营四个棋子到达终点则结束
    public boolean checkGameEnd(){
        int finishPlaneNum = 0;
        for(int i : Commdef.COLOR_PLANE[turn]){
            if(planes[i].getStatus() == Commdef.FINISHED) finishPlaneNum++;
        }
        if(finishPlaneNum == 4) return true;
        else return false;
    }

    // 调整index上的飞机坐标，迭子情况下有飞机飞走了就会调整
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

    // 开始回合
    public void beginTurn(){
        // 调整骰子的位置
        if (turn == Commdef.BLUE || turn == Commdef.GREEN) {
            diceView.setX((float)(playerViews[turn].getX() + playerViews[turn].getWidth() * 0.64));
            diceView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
        } else {
            diceView.setX((float)(playerViews[turn].getX() + playerViews[turn].getWidth() * 0.07));
            diceView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
        }
        diceView.setVisibility(View.VISIBLE);

        // 根据游戏类型和玩家类型做判断
        // 当前是单机游戏
        if(gameType == Commdef.LOCAL_GAME){
            // 如果当前回合是AI
            if(playerType[turn] == Commdef.AI){
                // 自动转骰子获取点数diceNumber，如果能走就根据AI策略获取要走的飞机编号，Commdef.COLOR_PLANE[turn]是当前回合四架飞机的编号
                // 通过diceNumber来做转骰子动画，有选择的飞机编号则通过调用飞机的receiveDiceNumber来自动移动
            }
            // 当前回合是玩家
            else{
                // 调整箭头位置
                if (turn == Commdef.BLUE || turn == Commdef.GREEN) {
                    arrowView.setX((float)(playerViews[turn].getX() + playerViews[turn].getWidth() * 1.01));
                    arrowView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
                    arrowView.setRotationY(0);
                } else {
                    arrowView.setX((float)(playerViews[turn].getX() - playerViews[turn].getWidth() * 0.29));
                    arrowView.setY((float)(playerViews[turn].getY() + playerViews[turn].getHeight() * 0.3));
                    arrowView.setRotationY(180);
                }
                arrowView.setVisibility(View.VISIBLE);
                // 做一个动画让箭头不断缩放
                ScaleAnimation arrowAnim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.ABSOLUTE, (float)(arrowView.getX()+arrowView.getWidth() * 0.5), Animation.ABSOLUTE, (float)(arrowView.getY()+arrowView.getHeight() * 0.5));
                arrowAnim.setDuration(500);     //设置动画持续时间
                arrowAnim.setRepeatCount(-1);   //设置重复次数，-1无限循环
                arrowAnim.setRepeatMode(Animation.REVERSE); // 逆序重复
                arrowAnim.setFillAfter(false);              // 不用停在最后一帧
                arrowView.startAnimation(arrowAnim);

                diceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 箭头消失
                        arrowView.clearAnimation();
                        arrowView.setVisibility(View.GONE);
                        // 这回合内禁止再点击骰子
                        diceView.setClickable(false);
                        // 在骰子view载入逐帧动画来达到转骰子效果
                        diceView.setBackgroundResource(R.drawable.diceanim);
                        final AnimationDrawable diceAnim = (AnimationDrawable) diceView.getBackground();
                        diceAnim.start();
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                // 一秒后停止逐帧动画
                                diceAnim.stop();
                                Random rand = new Random();
                                diceNumber = rand.nextInt(6) + 1;
                                // 根据随机的骰子点数来设置要显示的骰子图片
                                if(diceNumber == 1) diceView.setBackgroundResource(R.drawable.dice1);
                                else if(diceNumber == 2) diceView.setBackgroundResource(R.drawable.dice2);
                                else if(diceNumber == 3) diceView.setBackgroundResource(R.drawable.dice3);
                                else if(diceNumber == 4) diceView.setBackgroundResource(R.drawable.dice4);
                                else if(diceNumber == 5) diceView.setBackgroundResource(R.drawable.dice5);
                                else if(diceNumber == 6) diceView.setBackgroundResource(R.drawable.dice6);

                                // 连投奖励
                                if(diceNumber == 6) showInfo("获得一次投骰子机会");

                                ArrayList<Integer> outsidePlanes = new ArrayList<Integer>();
                                // 如果是迭在别人迭子上则自动走那个棋子
                                if(markPlane != -1){
                                    planes[markPlane].receiveDiceNumber(diceNumber);
                                    markPlane = -1;
                                }
                                else {
                                    // 是否全在机场
                                    for (int i : Commdef.COLOR_PLANE[turn]) {
                                        if (!planes[i].isInAirport()) {
                                            // 添加在外面的飞机
                                            outsidePlanes.add(i);
                                        }
                                    }
                                    // 是否是起飞的点数，可以在Commdef.TAKE_OFF_NUMBER进行修改
                                    boolean ableToTakeOff = false;
                                    for (int each : Commdef.TAKE_OFF_NUMBER) {
                                        if (each == diceNumber) {
                                            ableToTakeOff = true;
                                            break;
                                        }
                                    }
                                    if (ableToTakeOff) {
                                        // 是起飞的点数则当前回合的所有飞机都可飞
                                        for (int i : Commdef.COLOR_PLANE[turn]) {
                                            if (planes[i].getStatus() != Commdef.FINISHED)
                                                planes[i].getReadyToFly();
                                        }
                                    } else {
                                        // 不是起飞点数则只有在外面的飞机可以飞
                                        if (outsidePlanes.isEmpty()) {
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
        }
        // 联机模式，在己方看来其他三个是人是AI都一样，在他们回合都是要等待diceNumber和飞机编号
        else{
            if(turn == myCamp){ //己方回合
                if(playerType[myCamp] == Commdef.AI){    // 己方为AI
                    // 自动转骰子获取点数diceNumber，如果能走就根据AI策略获取要走的飞机编号，Commdef.COLOR_PLANE[turn]是当前回合四架飞机的编号
                    // 通过diceNumber来做转骰子动画，有选择的飞机编号则通过调用飞机的receiveDiceNumber来自动移动
                }
                else{   // 己方为人类
                    // 同上等待点击骰子获取diceNumber，能走就等待点击飞机
                    // 从上一步获取骰子点数diceNumber和选择的飞机编号（没有则-1）发送给其他三位玩家
                }
            }
            else{   // 其他玩家回合
                // 等待别人发来的diceNumber和飞机编号，handler消息传递？
                // 通过diceNumber来做转骰子动画，飞机编号不是-1则通过调用飞机的receiveDiceNumber来自动移动
            }
        }


    }

    // 结束回合
    public void endTurn(){
        // 检查游戏是否结束
        if(checkGameEnd()){
            gameEnd();
        }
        else{
            // 游戏没有结束
            if(diceNumber == 6){ // 如果抛到点数为6，下一个回合还是当前阵营
                beginTurn();
            }
            else{               // 否则下一个回合为顺时针下一阵营
                turn = (turn + 1) % Commdef.PLAYER_NUM;
                beginTurn();
            }
        }
    }

    // 横扫其他方的飞机
    public void sweepOthers(int index){
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getCamp() != turn){
                showInfo("撞子啦");
                plane.crackByPlane();
            }
        }
    }

    // 禁止点击任何棋子并清除动画效果（因为可以飞的飞机会跑一个不断缩放的动画来提醒）
    public void forbidClick(){
        for(Airplane plane : planes){
            plane.getPlaneView().setClickable(false);
            plane.getPlaneView().clearAnimation();
        }
    }

    // 判断index上有没有其他方的迭子
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

    // 判断index上有没有其他方的棋子
    public boolean hasOtherPlane(int index){
        for(Airplane plane : planes){
            if(plane.getIndex() == index && plane.getCamp() != turn) return true;
        }
        return false;
    }

    // 获取index上的飞机数目
    public int planeNumOnIndex(int index){
        int planeNum = 0;
        for(Airplane plane : planes){
            if(plane.getIndex() == index) {
                planeNum++;
            }
        }
        return  planeNum;
    }

    // 提示
    public void showInfo(String sentence){
        tipView.setText(sentence);
        tipView.bringToFront();
        tipView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                // 一秒后消失
                tipView.setVisibility(View.GONE);
            }

        }, 1000);   // 等待一秒后执行
    }

    // 播放音效
    public void playSound(int soundId){
        return;
//        int streamID = sp.play(soundMap.get(soundId), 0.8f, 0.8f, 1, 0, 1.0f);
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
        // 调整箭头大小
        ViewGroup.LayoutParams arrowParams = arrowView.getLayoutParams();
        arrowParams.width = (int)(yOffSet * 0.4);
        arrowParams.height = (int)(yOffSet * 0.4);
        arrowView.setLayoutParams(arrowParams);
        // 调整玩家信息框的大小
        for(int i = 0; i < 4; i++){
            ViewGroup.LayoutParams playerParams = playerViews[i].getLayoutParams();
            playerParams.width = (int)(yOffSet * 1.4);
            playerParams.height = (int)(yOffSet);
            playerViews[i].setLayoutParams(playerParams);
        }
    }


    public void setMarkPlane(int number){
        markPlane = number;
    }
}