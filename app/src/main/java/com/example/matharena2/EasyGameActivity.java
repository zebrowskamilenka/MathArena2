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

public class EasyGameActivity extends AppCompatActivity {

    private ImageView imageMonsterEasy;
    private TextView textTask;
    private TextView textTimer;     // czas
    private TextView textPoints;    // punkty
    private ProgressBar progressHp; // pasek HP
    private EditText editAnswer;
    private Button btnOk;

    // logika zadania
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

    // potworki
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
        textPoints = findViewById(R.id.textPoints);
        progressHp = findViewById(R.id.progressHp);
        editAnswer = findViewById(R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);

        startNewMonster();
        generateEasyTask();
        startTimer();

        btnOk.setOnClickListener(v -> checkAnswer());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ====== LOGIKA GRY ======

    private void showRandomEasyMonster() {
        Random random = new Random();
        imageMonsterEasy.setImageResource(
                easyMonsters[random.nextInt(easyMonsters.length)]
        );
    }

    private void startNewMonster() {
        monsterHp = 100;
        showRandomEasyMonster();

        progressHp.setMax(100);
        updateHpUI();

        points = 0;
        updatePointsUI();

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
        editAnswer.setText("");
    }

    private void generateEasyTask() {
        Random random = new Random();
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;

        correctAnswer = a + b;
        textTask.setText(a + " + " + b + " = ?");
    }

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
            // ✅ DOBRA ODPOWIEDŹ
            points += POINTS_CORRECT;
            updatePointsUI();

            dealDamageToMonster();

            Toast.makeText(this, "✅ Dobrze! +" + POINTS_CORRECT + " pkt", Toast.LENGTH_SHORT).show();
        } else {
            // ❌ ZŁA ODPOWIEDŹ
            points -= POINTS_WRONG;
            if (points < 0) points = 0;
            updatePointsUI();

            Toast.makeText(this, "❌ Źle! −" + POINTS_WRONG + " pkt", Toast.LENGTH_SHORT).show();
        }

        editAnswer.setText("");

        if (monsterHp > 0) {
            generateEasyTask();
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
                Toast.makeText(EasyGameActivity.this, "⏱️ Koniec czasu!", Toast.LENGTH_SHORT).show();

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
