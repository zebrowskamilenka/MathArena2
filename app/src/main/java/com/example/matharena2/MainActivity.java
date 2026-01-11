package com.example.matharena2;

import android.content.Intent; // przechodzi pomiedzy ekranami
import android.os.Bundle;
import android.view.View; // klasa dla elemntow ui
import android.widget.Button; //przyciski

import androidx.appcompat.app.AppCompatActivity; // zapewnienie zgodnosci z starszymi androidami

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //ustawienie layoutu z ktorego ma pobrac sie pierwsza strona

        Button startButton = findViewById(R.id.startButton); // pobranie przycisku
// gdy gracz klika start wykonuje sie kod ponizej
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =
                        new Intent(MainActivity.this, DifficultyActivity.class); // przechodzenie z strony do strony difficulty
                startActivity(intent); // urochomienie kolejnej strony
            }
        });
    }
}
