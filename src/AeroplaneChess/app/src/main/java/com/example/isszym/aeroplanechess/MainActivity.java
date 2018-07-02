package com.example.isszym.aeroplanechess;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Board chessboard;
    private ImageView boardView;
    private ImageView[] planeViews;
    private ImageView diceView;
    private ImageView arrowView;
    private TextView tipView;
    private TextView[] playerViews;
    private float screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载资源
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardView = (ImageView)findViewById(R.id.board);
        diceView = (ImageView)findViewById(R.id.dice);
        arrowView = (ImageView)findViewById(R.id.arrow);
        tipView = (TextView)findViewById(R.id.tip);
        playerViews = new TextView[4];
        playerViews[0] = (TextView)findViewById(R.id.bluePlayer);
        playerViews[1] = (TextView)findViewById(R.id.greenPlayer);
        playerViews[2] = (TextView)findViewById(R.id.redPlayer);
        playerViews[3] = (TextView)findViewById(R.id.yellowPlayer);

        screenWidth = getScreenW(getApplicationContext());

        chessboard = new Board(boardView, diceView, arrowView, tipView, screenWidth, playerViews);
        planeViews = new ImageView[16];
        planeViews[0] = (ImageView)findViewById(R.id.bluePlane1);
        planeViews[1] = (ImageView)findViewById(R.id.bluePlane2);
        planeViews[2] = (ImageView)findViewById(R.id.bluePlane3);
        planeViews[3] = (ImageView)findViewById(R.id.bluePlane4);
        planeViews[4] = (ImageView)findViewById(R.id.greenPlane1);
        planeViews[5] = (ImageView)findViewById(R.id.greenPlane2);
        planeViews[6] = (ImageView)findViewById(R.id.greenPlane3);
        planeViews[7] = (ImageView)findViewById(R.id.greenPlane4);
        planeViews[8] = (ImageView)findViewById(R.id.redPlane1);
        planeViews[9] = (ImageView)findViewById(R.id.redPlane2);
        planeViews[10] = (ImageView)findViewById(R.id.redPlane3);
        planeViews[11] = (ImageView)findViewById(R.id.redPlane4);
        planeViews[12] = (ImageView)findViewById(R.id.yellowPlane1);
        planeViews[13] = (ImageView)findViewById(R.id.yellowPlane2);
        planeViews[14] = (ImageView)findViewById(R.id.yellowPlane3);
        planeViews[15] = (ImageView)findViewById(R.id.yellowPlane4);

        //增加整体布局监听,获取棋盘在屏幕中的偏移量,因为在onCreate中board未完成渲染
        ViewTreeObserver vto = chessboard.getBoardView().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                float xOffSet = chessboard.getBoardView().getLeft();
                float yOffSet = chessboard.getBoardView().getTop();
                chessboard.setXOffSet(xOffSet);
                chessboard.setYOffSet(yOffSet);

                // 初始化飞机
                chessboard.initPlanes(planeViews);

                chessboard.getBoardView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    // 菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(1, 1, 1, "开始游戏");
        menu.add(1, 2, 2, "结束游戏");
        menu.setGroupCheckable(2, false, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 1:
                chessboard.gameStart();
                break;
            case 2:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {//MotionEvent.ACTION_MOVE
            return false;
        }
        return true;
    }



    // 获取屏幕尺寸
    public static int[] getScreenHW(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int[] HW = new int[] { width, height };
        return HW;
    }
    public static int getScreenW(Context context) { return getScreenHW(context)[0]; }
    public static int getScreenH(Context context) { return getScreenHW(context)[1]; }
}
