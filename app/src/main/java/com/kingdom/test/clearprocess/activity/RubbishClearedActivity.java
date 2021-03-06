package com.kingdom.test.clearprocess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kingdom.test.clearprocess.R;

public class RubbishClearedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rubbish_cleared);

        ImageView ivBack = (ImageView) findViewById(R.id.btn_back);
        TextView textView = (TextView) findViewById(R.id.tv_clear_memory);
        Intent intent =getIntent();


        long clearSize  = intent.getIntExtra("clearSize", 0);
        textView.setText("清理垃圾"+ Formatter.formatFileSize(this,clearSize));
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
