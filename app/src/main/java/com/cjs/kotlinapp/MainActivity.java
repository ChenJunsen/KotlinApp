package com.cjs.kotlinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.cjs.widgets.dialog.MsgDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_popWindow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MsgDialog.Builder()
                        .setMsg("这是一个测试的模态窗口")
                        .setCancelable(false)
                        .setOutsideCancelable(false)
                        .build().show(getSupportFragmentManager());
            }
        });

    }
}