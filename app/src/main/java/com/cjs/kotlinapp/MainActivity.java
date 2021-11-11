package com.cjs.kotlinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cjs.threadpool.TPFactory;
import com.cjs.widgets.dialog.MsgDialog;

public class MainActivity extends AppCompatActivity {
    private Button btn1, btn2, ed_keyboard,btn_tp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        ed_keyboard = findViewById(R.id.ed_keyboard);
        btn_tp=findViewById(R.id.btn_tp);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MsgDialog.Builder()
                        .setMsg("这是一个测试的单按钮模态窗口")
                        .setCancelable(false)
                        .setOutsideCancelable(false)
                        .build().show(getSupportFragmentManager());
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MsgDialog.Builder()
                        .setMsg("这是一个测试的双按钮模态窗口")
                        .setDialogStyle(MsgDialog.Style.TWO_BUTTON)
                        .setCancelable(false)
                        .setOutsideCancelable(false)
                        .build().show(getSupportFragmentManager());
            }
        });

        btn_tp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TPFactory.main(null);
            }
        });

        ed_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, EditTestActivity.class);
                startActivity(i);
            }
        });

    }
}