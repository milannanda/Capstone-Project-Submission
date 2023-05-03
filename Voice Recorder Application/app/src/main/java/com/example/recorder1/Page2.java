package com.example.recorder1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Page2 extends AppCompatActivity {
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        button = (Button) findViewById(R.id.button_record);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRecord();
            }
        });

        button = (Button) findViewById(R.id.button_upload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUpload();
            }
        });

    }
    public void openRecord() {
        Intent intent = new Intent(this, Record.class);
            startActivity(intent);
        }





    public void openUpload() {
        Intent intent = new Intent(this, Upload.class);
            startActivity(intent);
    }

}
