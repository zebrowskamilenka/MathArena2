package com.example.matharena2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        ImageButton easyButton = findViewById(R.id.btnEasy);
        ImageButton mediumButton = findViewById(R.id.btnMedium);

        easyButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DifficultyActivity.this,
                    EasyGameActivity.class
            );
            startActivity(intent);
        });

        mediumButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DifficultyActivity.this,
                    MediumGameActivity.class
            );
            startActivity(intent);
        });
    }
}
