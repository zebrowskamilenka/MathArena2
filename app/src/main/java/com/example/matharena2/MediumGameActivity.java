package com.example.matharena2;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MediumGameActivity extends AppCompatActivity {

    private ImageView imageMonsterMedium;
    private TextView textTask;
    private TextView textTimer;     // czas
    private TextView textPoints;    // punkty
    private ProgressBar progressHp; // pasek HP
    private EditText editAnswer;
    private Button btnOk;

    private int correctAnswer = 0;
    private int points = 0;

    // HP potworka
    private int monsterHp = 100;
    private final int damagePerCorrect = 20;

    // punkty
    private final int POINTS_CORRECT = 20;
    private final int POINTS_WRONG = 5;

    // timer
    private CountDownTimer countDownTimer;
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    // ✅ POTWORKI dla MEDIUM — tutaj podmieniasz na swoje drawables!
    private final int[] mediumMonsters = {
            R.drawable.p36m,
            R.drawable.p28m,
            R.drawable.p27m,
            R.drawable.p12m,
            R.drawable.p4m
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medium_game);

        imageMonsterMedium = findViewById(R.id.imageMonsterMedium);
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        textPoints = findViewById(R.id.textPoints);
        progressHp = findViewById(R.id.progressHp);
        editAnswer = findViewById(R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);

        startNewMonster();
        generateMediumTask();
        startTimer();

        btnOk.setOnClickListener(v -> checkAnswer());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ====== POTWOREK ======

    private void showRandomMediumMonster() {
        Random random = new Random();
        imageMonsterMedium.setImageResource(
                mediumMonsters[random.nextInt(mediumMonsters.length)]
        );
    }

    private void startNewMonster() {
        monsterHp = 100;
        showRandomMediumMonster();

        progressHp.setMax(100);
        updateHpUI();

        points = 0;
        updatePointsUI();

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
        editAnswer.setText("");
    }

    // ====== ZADANIE (na razie takie jak easy: dodawanie) ======

    private void generateMediumTask() {
        Random random = new Random();

        int a = random.nextInt(50) + 1;
        int b = random.nextInt(50) + 1;

        boolean subtraction = random.nextBoolean();

        if (subtraction) {
            int max = Math.max(a, b);
            int min = Math.min(a, b);
            correctAnswer = max - min;
            textTask.setText(max + " - " + min + " = ?");
        } else {
            correctAnswer = a + b;
            textTask.setText(a + " + " + b + " = ?");
        }
    }



    // ====== ODPOWIEDŹ ======

    private void checkAnswer() {
        String txt = editAnswer.getText().toString().trim();

        if (txt.isEmpty()) {
            Toast.makeText(this, "Wpisz odpowiedź!", Toast.LENGTH_SHORT).show();
            return;
        }

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Podaj liczbę.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userAnswer == correctAnswer) {
            points += POINTS_CORRECT;
            updatePointsUI();

            dealDamageToMonster();

            Toast.makeText(this, "✅ Dobrze! +" + POINTS_CORRECT + " pkt", Toast.LENGTH_SHORT).show();
        } else {
            points -= POINTS_WRONG;
            if (points < 0) points = 0;
            updatePointsUI();

            Toast.makeText(this, "❌ Źle! −" + POINTS_WRONG + " pkt", Toast.LENGTH_SHORT).show();
        }

        editAnswer.setText("");

        if (monsterHp > 0) {
            generateMediumTask();
            startTimer();
        }
    }

    private void dealDamageToMonster() {
        monsterHp -= damagePerCorrect;
        if (monsterHp < 0) monsterHp = 0;

        updateHpUI();

        if (monsterHp == 0) {
            monsterDefeated();
        }
    }

    private void monsterDefeated() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);

        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");

        Toast.makeText(this, "🎉 Pokonałaś potworka!", Toast.LENGTH_LONG).show();
    }

    // ====== UI ======

    private void updateHpUI() {
        progressHp.setProgress(monsterHp);
    }

    private void updatePointsUI() {
        textPoints.setText(points + " pkt");
    }

    // ====== TIMER ======

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);

        countDownTimer = new CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                textTimer.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                textTimer.setText("0s");
                Toast.makeText(MediumGameActivity.this, "⏱️ Koniec czasu!", Toast.LENGTH_SHORT).show();

                btnOk.setEnabled(false);
                editAnswer.setEnabled(false);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
