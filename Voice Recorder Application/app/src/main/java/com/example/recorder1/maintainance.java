package com.example.recorder1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class maintainance extends AppCompatActivity {
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintainance);

        button = (Button) findViewById(R.id.button_back_maintainance);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPage2();
            }
        });
    }
    public void openPage2() {
        Intent intent = new Intent(this, Page2.class);
        startActivity(intent);
    }
}