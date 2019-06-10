package com.mimi.cachecache;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button hideBtn = findViewById(R.id.hide);
        Button findBtn = findViewById(R.id.find);

        hideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activity2Intent = new Intent(getApplicationContext(), HideActivity.class);
                startActivity(activity2Intent);
            }
        });

        findBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent activity2Intent = new Intent(getApplicationContext(), FindActivity.class);
                startActivity(activity2Intent);
            }
        });
    }
}

