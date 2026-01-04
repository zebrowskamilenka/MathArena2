package com.example.matharena2;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class HardGameActivity extends AppCompatActivity {

    private ImageView imageMonsterHard;
    private TextView textTask;
    private TextView textTimer;
    private TextView textPoints;
    private ProgressBar progressHp;
    private EditText editAnswer;
    private Button btnOk;
    private ImageButton btnBack;

    private int correctAnswer = 0;
    private int points = 0;

    private int monsterHp = 100;
    private static final int MONSTER_MAX_HP = 100;
    private static final int DAMAGE_PER_CORRECT = 20;

    private static final int POINTS_CORRECT = 20;
    private static final int POINTS_WRONG = 5;

    private CountDownTimer countDownTimer;
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    // ✅ HARD potworki (Twoje nazwy z drawable)
    private final int[] hardMonsters = {
            R.drawable.p7h,
            R.drawable.p18h,
            R.drawable.p23h,
            R.drawable.p24h,
            R.drawable.p33h
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hard_game);

        // 1) bind widoków
        imageMonsterHard = findViewById(R.id.imageMonsterHard);
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        textPoints = findViewById(R.id.textPoints);
        progressHp = findViewById(R.id.progressHp);
        editAnswer = findViewById(R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);

        // 2) back -> Difficulty
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(HardGameActivity.this, DifficultyActivity.class));
            finish();
        });

        // 3) start gry
        startNewMonster();
        generateHardTask();
        startTimer();

        // 4) ok
        btnOk.setOnClickListener(v -> checkAnswer());
    }

    private void startNewMonster() {
        monsterHp = MONSTER_MAX_HP;
        showRandomHardMonster();

        progressHp.setMax(MONSTER_MAX_HP);
        updateHpUI();

        points = 0;
        updatePointsUI();

        editAnswer.setText("");
        editAnswer.setEnabled(true);
        btnOk.setEnabled(true);
    }

    private void showRandomHardMonster() {
        Random random = new Random();
        int idx = random.nextInt(hardMonsters.length);
        imageMonsterHard.setImageResource(hardMonsters[idx]);
    }

    // ✅ HARD = MNOŻENIE
    private void generateHardTask() {
        Random random = new Random();

        int a = random.nextInt(11) + 2; // 2..12
        int b = random.nextInt(11) + 2; // 2..12

        correctAnswer = a * b;
        textTask.setText(a + " × " + b + " = ?");
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
            generateHardTask();
            startTimer();
        }
    }

    private void dealDamageToMonster() {
        monsterHp -= DAMAGE_PER_CORRECT;
        if (monsterHp < 0) monsterHp = 0;
        updateHpUI();

        if (monsterHp == 0) {
            onWin();
        }
    }

    private void onWin() {
        stopTimer();

        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);

        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");

        Toast.makeText(this, "🎉 Pokonałaś potworka!", Toast.LENGTH_LONG).show();
    }

    private void updateHpUI() {
        progressHp.setProgress(monsterHp);
    }

    private void updatePointsUI() {
        textPoints.setText(points + " pkt");
    }

    private void startTimer() {
        stopTimer();

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
                Toast.makeText(HardGameActivity.this, "⏱️ Koniec czasu!", Toast.LENGTH_SHORT).show();

                btnOk.setEnabled(false);
                editAnswer.setEnabled(false);
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
