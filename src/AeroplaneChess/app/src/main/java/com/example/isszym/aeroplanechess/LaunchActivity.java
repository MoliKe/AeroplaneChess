package com.example.isszym.aeroplanechess;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;



public class LaunchActivity extends Activity {

    public MediaPlayer startplayer = null;
    public static boolean hasPermission = true;
    private static String[] ps;
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        if (ContextCompat.checkSelfPermission(LaunchActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LaunchActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {

            try {
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        //LaunchActivity.this.startplayer.start();
        Integer time = 2000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchActivity.this, MainActivity.class));
          //      LaunchActivity.this.startplayer.stop();
                LaunchActivity.this.finish();
            }
        }, time);

        ps = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        verifyStrongPermissions(LaunchActivity.this);
    }

    public static void verifyStrongPermissions(Activity activity)
    {
        try
        {
            int permission = ActivityCompat.checkSelfPermission(activity,"android.permission.READ_EXTERNAL_STORAGE");
            if(permission != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(activity, ps ,1);
            }
            else
            {
                hasPermission = true;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults)
    {
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            //do what you want
        }
        else
        {
            System.exit(0);
        }
        return;
    }

    /*private void showGIFAnimation(){
        ImageView iv_gif = (ImageView) findViewById(R.id.gif);
        InputStream is = getResources().openRawResource(R.raw.gif);
        GifImage gifImage = new GifImage();

    }*/
}
