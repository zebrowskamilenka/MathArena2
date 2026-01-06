package com.example.matharena2;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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


    // kontenery rzeczy z layoutu
    private ImageView imageMonsterHard;
    private ImageView imageExplosion;          // ✅ overlay animacji
    private TextView textTask;
    private TextView textTimer;
    private TextView textPoints;
    private ProgressBar progressHp;
    private EditText editAnswer;
    private Button btnOk;
    private ImageButton btnBack;
    private ImageButton btnAttack;             // ✅ przycisk ataku

    // dane gry czyli punkty HP i odpowiedz
    private int correctAnswer = 0;
    private int points = 0; // ile masz punktów

    private int monsterHp = 200; //aktualne hp potwora
    private static final int MONSTER_MAX_HP = 200; // maksymalne hp
    private static final int DAMAGE_PER_CORRECT = 20; //ile sie zabiera za poprawna odpowiedz

    private static final int POINTS_CORRECT = 20; // ile sie dostaje za poprawna odpowiedz
    private static final int POINTS_WRONG = 5; //ile sie traci za bledna odpowiedz
    private static final long NEXT_MONSTER_DELAY_MS = 1200;


    // wprowadzenie timera
    private CountDownTimer countDownTimer; //licznik ktory odlicza od 15 do 0
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    // ATak
    private static final int ATTACK_COST = 50; // koszt ataku
    private static final int ATTACK_DAMAGE = 20; //
    private boolean attackOnCooldown = false;

    // ✅ HARD potworki - losowanie potwora
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
        setContentView(R.layout.activity_hard_game); // start ktory laduje layout

        // 1) bind widoków
        imageMonsterHard = findViewById(R.id.imageMonsterHard);
        imageExplosion = findViewById(R.id.imageExplosion);   // ✅
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        textPoints = findViewById(R.id.textPoints);
        progressHp = findViewById(R.id.progressHp);
        editAnswer = findViewById(R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);
        btnBack = findViewById(R.id.btnBack);
        btnAttack = findViewById(R.id.btnAttack);             // ✅

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

        // 5) atak
        btnAttack.setOnClickListener(v -> tryAttack());
    }

    private void startNewMonster() {
        monsterHp = MONSTER_MAX_HP;
        showRandomHardMonster();

        progressHp.setMax(MONSTER_MAX_HP);
        updateHpUI();

        editAnswer.setText("");
        editAnswer.setEnabled(true);
        btnOk.setEnabled(true);

        attackOnCooldown = false;
        btnAttack.setEnabled(true);
        btnAttack.setAlpha(1f);
        if (imageExplosion != null) imageExplosion.setVisibility(View.GONE);
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

            dealDamageToMonster(DAMAGE_PER_CORRECT);

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

    // ✅ jedna metoda na dmg (żeby i OK i Attack używały tego samego)
    private void dealDamageToMonster(int damage) {
        monsterHp -= damage;
        if (monsterHp < 0) monsterHp = 0;
        updateHpUI();

        if (monsterHp == 0) {
            onWin();
        }
    }

    // ✅ ATAK: -50 pkt, -20 HP, animacja, pomija zadanie, cooldown 15s
    private void tryAttack() {
        if (monsterHp <= 0) return; // już wygrane

        if (attackOnCooldown) {
            Toast.makeText(this, "Atak dostępny za chwilę!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (points < ATTACK_COST) {
            Toast.makeText(this, "Za mało punktów (potrzeba 50).", Toast.LENGTH_SHORT).show();
            return;
        }

        // koszt
        points -= ATTACK_COST;
        updatePointsUI();

        // animacja
        playExplosionAnimation();

        // obrażenia
        dealDamageToMonster(ATTACK_DAMAGE);

        // pomiń bieżące zadanie i daj nowe + reset timera 15s (jeśli jeszcze żyje)
        if (monsterHp > 0) {
            editAnswer.setText("");
            generateHardTask();
            startTimer();
        }

        // cooldown 15s
        startAttackCooldown(15);
    }

    private void playExplosionAnimation() {
        if (imageExplosion == null) return;

        imageExplosion.setVisibility(View.VISIBLE);

        // explosion_anim musi być animation-list
        if (imageExplosion.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable anim = (AnimationDrawable) imageExplosion.getDrawable();
            anim.stop();
            anim.start();

            int durationMs = 0;
            for (int i = 0; i < anim.getNumberOfFrames(); i++) {
                durationMs += anim.getDuration(i);
            }
            imageExplosion.postDelayed(() -> imageExplosion.setVisibility(View.GONE), durationMs);
        } else {
            // fallback: schowaj po chwili
            imageExplosion.postDelayed(() -> imageExplosion.setVisibility(View.GONE), 500);
        }
    }

    private void startAttackCooldown(int seconds) {
        attackOnCooldown = true;
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);

        btnAttack.postDelayed(() -> {
            attackOnCooldown = false;
            btnAttack.setEnabled(true);
            btnAttack.setAlpha(1f);
            Toast.makeText(this, "Atak znowu dostępny!", Toast.LENGTH_SHORT).show();
        }, seconds * 1000L);
    }

    private void onWin() {
        stopTimer();

        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);

        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");

        Toast.makeText(this, "🎉 Pokonałaś potworka!", Toast.LENGTH_SHORT).show();

        // ✅ po chwili start nowego potwora + nowe zadanie + timer
        textTask.postDelayed(() -> {
            startNewMonster();      // reset HP + losuje potwora + resetuje UI (u Ciebie też punkty)
            generateHardTask();     // nowe działanie
            startTimer();           // nowe 15s
        }, NEXT_MONSTER_DELAY_MS);
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

                // opcjonalnie: pozwól nadal użyć ataku mimo końca czasu
                // (jeśli chcesz zablokować atak po czasie, to odkomentuj):
                // btnAttack.setEnabled(false);
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
