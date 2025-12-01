package com.example.matharena2; // dostosuj do swojego pakietu

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class EasyGameActivity extends AppCompatActivity {

    private ImageView imageMonsterEasy;
    private TextView textLevelTitle;
    private TextView textTask;
    private TextView textTimer;
    private TextView textScore;

    // BAZA potworków dla ŁATWEGO poziomu
    private final int[] easyMonsters = {
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p7,
            R.drawable.p4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_game);

        imageMonsterEasy = findViewById(R.id.imageMonsterEasy);
        textLevelTitle = findViewById(R.id.textLevelTitle);
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        textScore = findViewById(R.id.textScore);

        // Ustaw nagłówek - jakby kiedyś było więcej info
        textLevelTitle.setText("Poziom: Łatwy");

        // 1) Losujemy potworka z puli ŁATWYCH
        showRandomEasyMonster();

        // 2) Generujemy łatwe zadanie (na razie placeholder)
        generateEasyTask();

        // 3) Ustawiamy mechanizm czasu (na razie też placeholder)
        setupEasyTimer();
    }

    private void showRandomEasyMonster() {
        Random random = new Random();
        int index = random.nextInt(easyMonsters.length);
        int monsterResId = easyMonsters[index];
        imageMonsterEasy.setImageResource(monsterResId);
    }

    private void generateEasyTask() {
        Random random = new Random();

        //losowanie dwoch liczb z zakresu 1-10
        int a = random.nextInt(10)+ 1;
        int b = random.nextInt(10)+1;

        // obliczanie wyniku
        int result = a + b;

        String taskText = a + " + " + b + " =? ";

        // wyświetlanie na ekraknie
        textTask.setText(taskText);

    }

    private void setupEasyTimer() {
        // TU KIEDYŚ: logika timera (np. 10 sekund na odpowiedź)
        // Na razie zostawiamy na sztywno:
        textTimer.setText("Czas: 00:10");
    }
}
