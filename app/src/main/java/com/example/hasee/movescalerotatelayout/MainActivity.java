package com.example.hasee.movescalerotatelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.hasee.movescalerotatelayout.weight.MoveScaleRotateLayout;



public class MainActivity extends AppCompatActivity {

    MoveScaleRotateLayout view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        Button button = findViewById(R.id.btn_test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "test", Toast.LENGTH_SHORT).show();
            }
        });
        

    }
}
