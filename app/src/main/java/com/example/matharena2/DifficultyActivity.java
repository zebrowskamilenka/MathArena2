package com.example.matharena2;

import android.content.Intent; // przechodzenie miedzy ekranami
import android.os.Bundle;
import android.widget.ImageButton; // przycisk w formie obrazka

import androidx.appcompat.app.AppCompatActivity;

public class DifficultyActivity extends AppCompatActivity { // elran wyboru trudnosci ktory (extends) od androida

    @Override
    protected void onCreate(Bundle savedInstanceState) { // inicjalizacja calego ekranu
        super.onCreate(savedInstanceState); // przygotowanie aktywnosci
        setContentView(R.layout.activity_difficulty); // podpięcie layoutu

        ImageButton easyButton = findViewById(R.id.btnEasy); //podpiecie przycisku latwy
        ImageButton mediumButton = findViewById(R.id.btnMedium); // podpiecie przycisku sredni
        ImageButton btnHard = findViewById(R.id.btnHard); // podpiecie przycisku trudny
// przyck latwy ktory przechodzi do gry
        easyButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DifficultyActivity.this,
                    EasyGameActivity.class
            );
            startActivity(intent);
        });
// przycisk średni ktory przechodzi do gry
        mediumButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DifficultyActivity.this,
                    MediumGameActivity.class
            );
            startActivity(intent);
        });
// przycisk trudny ktory przechodzi do gry
        btnHard.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DifficultyActivity.this,
                    HardGameActivity.class
            );
            startActivity(intent);
        });
    }
}
