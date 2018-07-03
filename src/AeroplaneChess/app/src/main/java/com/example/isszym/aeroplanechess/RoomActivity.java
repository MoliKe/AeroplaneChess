package com.example.isszym.aeroplanechess;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Administrator on 2018/6/30.
 */

public class RoomActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Button btn_room = (Button) findViewById(R.id.c_room);
        btn_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("创建房间");//设置标题
        View c_view = LayoutInflater.from(this).inflate(R.layout.activity_createroom,null);//获得布局信息
        final EditText room_name = (EditText) c_view.findViewById(R.id.secret);
        final EditText confirmSecret = (EditText) c_view.findViewById(R.id.confirm);
        builder.setView(c_view);//给对话框设置布局
        builder.setPositiveButton("创建", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //点击确定按钮的操作
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }
}
