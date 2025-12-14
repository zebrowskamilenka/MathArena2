package com.example.matharena2; // dostosuj do swojego pakietu

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class EasyGameActivity extends AppCompatActivity {

    private ImageView imageMonsterEasy;
    private TextView textLevelTitle;
    private TextView textTask;
    private TextView textTimer;
    private EditText editAnswer;
    private Button btnOk;


    // poprwana odpowiedz i punkty
    private int correctAnswer = 0;
    private int points = 0;


    // BAZA potworków dla ŁATWEGO poziomu
    private final int[] easyMonsters = {
            R.drawable.p1e,
            R.drawable.p2e,
            R.drawable.p5e,
            R.drawable.p8e,
            R.drawable.p10e,
            R.drawable.p21e
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_game);

        imageMonsterEasy = findViewById(R.id.imageMonsterEasy);
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        editAnswer = findViewById( R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);


        // 1) Losujemy potworka z puli ŁATWYCH
        showRandomEasyMonster();

        // 2) Generujemy łatwe zadanie (na razie placeholder)
        generateEasyTask();

        // 3) Ustawiamy mechanizm czasu (na razie też placeholder)
        setupEasyTimer();

        // klikniecie ok -> sprawdza odpowiedz
        btnOk.setOnClickListener(v -> checkAnswer());
    }

    private void showRandomEasyMonster() {
        Random random = new Random();
        int index = random.nextInt(easyMonsters.length);
        int monsterResId = easyMonsters[index];
        imageMonsterEasy.setImageResource(monsterResId);
    }

    private void generateEasyTask() {
        Random random = new Random();

        //losowanie dwoch liczb z zakresu 1-20
        int a = random.nextInt(20)+ 1;
        int b = random.nextInt(20) + 1;

        // obliczanie wyniku
         correctAnswer = a + b;

        String taskText = a + " + " + b + " =  ";

        // wyświetlanie na ekraknie
        textTask.setText(taskText);

    }

    private void checkAnswer(){
        String txt = editAnswer.getText().toString().trim();

        if(txt.isEmpty()){
            Toast.makeText(this, "Wpisz odpowiedź", Toast.LENGTH_SHORT).show();
            return;
        }

        int userAnswer;
        try{
            userAnswer = Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Podaj liczbę.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userAnswer == correctAnswer) {
            points += 1;
            Toast.makeText(this, "Dobrze! Punkty: " + points, Toast.LENGTH_SHORT).show();

            // opcjonalnie: po poprawnej odpowiedzi losuj nowego potworka
            showRandomEasyMonster();
        } else {
            Toast.makeText(this, "Źle! Poprawnie: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        //czyszczenie pola i losowe nowe zadanie
        editAnswer.setText(" ");
        generateEasyTask();
    }

    private void setupEasyTimer() {
        // TU KIEDYŚ: logika timera (np. 10 sekund na odpowiedź)
        // Na razie zostawiamy na sztywno:
        textTimer.setText("Time: 00:10");
    }
}
