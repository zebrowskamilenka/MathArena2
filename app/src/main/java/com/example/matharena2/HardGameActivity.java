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

    // ✅ poprawka: osobny timer cooldownu (stabilniejsze niż postDelayed na btn)
    private CountDownTimer attackCooldownTimer;
    private static final int ATTACK_COOLDOWN_MS = 15000; // 15 sekund cooldownu

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

        // Pelementy UI z xml do zmiennychw javie
        imageMonsterHard = findViewById(R.id.imageMonsterHard);
        imageExplosion = findViewById(R.id.imageExplosion);
        textTask = findViewById(R.id.textTask);
        textTimer = findViewById(R.id.textTimer);
        textPoints = findViewById(R.id.textPoints);
        progressHp = findViewById(R.id.progressHp);
        editAnswer = findViewById(R.id.editAnswer);
        btnOk = findViewById(R.id.btnOk);
        btnBack = findViewById(R.id.btnBack);
        btnAttack = findViewById(R.id.btnAttack);

        // obsluga przycisku powrotu - przejscie do activityduficulty
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(HardGameActivity.this, DifficultyActivity.class));
            finish();
        });

        // start gry
        startNewMonster(); // ustawienie hp potyworka na maksa
        generateHardTask(); // losowania działania tu mnożenia i zapisanie do correct answer
        startTimer(); // odliczanie timera od 15 w dol

        // przycisk okej ktory sprawdza czy odpwiedz jest poprawna
        btnOk.setOnClickListener(v -> checkAnswer());

        // 5) atak - wykoananie tatku kosztujace 50 pkt
        btnAttack.setOnClickListener(v -> tryAttack());
    }

    private void startNewMonster() {
        stopAttackCooldown(); // // Reset cooldownu ataku: jeśli poprzedni potwór miał aktywny cooldown,
        // to kasujemy timer, aby nowa walka zaczynała się "czysto".

        monsterHp = MONSTER_MAX_HP; // ustawienie hp na maksimum
        showRandomHardMonster(); // losowanie grafiki potwora

        progressHp.setMax(MONSTER_MAX_HP); // pasek HP
        updateHpUI();
// rteset pola odpowidzi i odblokowanie przycisku okej
        editAnswer.setText("");
        editAnswer.setEnabled(true);
        btnOk.setEnabled(true);
// odblokowanie przycisku atatku i reset jego stanu
        attackOnCooldown = false;
        btnAttack.setEnabled(true);
        btnAttack.setAlpha(1f);
        if (imageExplosion != null) imageExplosion.setVisibility(View.GONE); // ukrycie animacji wybuchu
    }

    private void showRandomHardMonster() {
        Random random = new Random();
        int idx = random.nextInt(hardMonsters.length);
        imageMonsterHard.setImageResource(hardMonsters[idx]);
    }

    // generowanie zadania z mnożeniem
    private void generateHardTask() {
        Random random = new Random(); // losowanie liczb do mnożenia
        int a = random.nextInt(15) + 2; // implemetuje a zeby było  w zakresie od 2 do 16
        int b = random.nextInt(11) + 2; // 2 - 12

        correctAnswer = a * b; //zapis poprawnej odpowiedzi w correctAnswer
        textTask.setText(a + " × " + b + " = ?"); // wyswietlanie zadanie na ekranie
    }

    private void checkAnswer() {
        String txt = editAnswer.getText().toString().trim(); // pobranie wpisu gracza - trim zostal dopisany aby usunac spacje

        if (txt.isEmpty()) { // jesli nic nie wpisano zpstaje przerwane i pokazuje komunikat
            Toast.makeText(this, "Wpisz odpowiedź!", Toast.LENGTH_SHORT).show();
            return;
        }
       //próba zamiany tekstu na liczbę (gdyby ktoś wpisał np. "abc")
        int userAnswer;
        try {
            userAnswer = Integer.parseInt(txt);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Podaj liczbę.", Toast.LENGTH_SHORT).show();
            return;
        }
//sprawdzenie poprawnosci odpowiedzi
        if (userAnswer == correctAnswer) {
            //poprawna odpowiedz dodaje punktgy i zadaje obrazenia
            points += POINTS_CORRECT; // dodaje pkt
            updatePointsUI();

            dealDamageToMonster(DAMAGE_PER_CORRECT); //obrazenia

            Toast.makeText(this, "✅ Dobrze! +" + POINTS_CORRECT + " pkt", Toast.LENGTH_SHORT).show();
        } else {  //bledna odpowiedz
            points -= POINTS_WRONG;
            if (points < 0) points = 0; // odejmowanie 5pkt ale jest zablkowane zeby  nie zeszlo na minusowe
            updatePointsUI();

            Toast.makeText(this, "❌ Źle! −" + POINTS_WRONG + " pkt", Toast.LENGTH_SHORT).show();
        }

        editAnswer.setText(""); //czyszczenie pola odpowiedzi po kazdej probie
// losowanie kolejnego zadania i restart timera
        if (monsterHp > 0) {
            generateHardTask();
            startTimer();
        }
    }

    // jedna metoda na dmg (żeby i OK i Attack używały tego samego)
    private void dealDamageToMonster(int damage) {
        // Odejmowanie HP (z zabezpieczeniem żeby nie spadło poniżej 0)
        monsterHp -= damage;
        if (monsterHp < 0) monsterHp = 0;
        updateHpUI(); // aktualizacja paska hp
// wygrana jesli 0
        if (monsterHp == 0) {
            onWin();
        }
    }

    // ATAK: -50 pkt, -20 HP, animacja, pomija zadanie, cooldown 15s
    private void tryAttack() {
        if (monsterHp <= 0) return; // jesli potwor nie zyje to nie ma ataku
// jesli jest na cooldownie to informujemy uzytkownika
        if (attackOnCooldown) {
            Toast.makeText(this, "Atak dostępny za chwilę!", Toast.LENGTH_SHORT).show();
            return;
        }
// nie ma punktow wystarczajaco to nie ma ataku
        if (points < ATTACK_COST) {
            Toast.makeText(this, "Za mało punktów (potrzeba 50).", Toast.LENGTH_SHORT).show();
            return;
        }

        // koszt ataku -50pkt
        points -= ATTACK_COST;
        updatePointsUI();

        // animacja wybuchu
        playExplosionAnimation();

        // obrażenia zadane potworkowi
        dealDamageToMonster(ATTACK_DAMAGE);

        // pomiń bieżące zadanie i daj nowe + reset timera 15s (jeśli jeszcze żyje)
        if (monsterHp > 0) {
            editAnswer.setText("");
            generateHardTask();
            startTimer();
        }

        // cooldown 15s
        startAttackCooldown();
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

    // ✅ poprawka: cooldown oparty o CountDownTimer (nie gubi się tak łatwo jak postDelayed na przycisku)
    private void startAttackCooldown() {
        // anuluje poprzedni timer cooldownu (jeśli istniał),
        // żeby nie nakładało się kilka timerów naraz
        stopAttackCooldown();
        // Ustaw stan "cooldown aktywny" i zablokuj przycisk ataku
        attackOnCooldown = true;
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);
        // Timer, który po 15 sekundach odblokuje atak
        attackCooldownTimer = new CountDownTimer(ATTACK_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() { // Koniec cooldownu: odblokowanie ataku
                attackOnCooldown = false;
                btnAttack.setEnabled(true);
                btnAttack.setAlpha(1f);
                Toast.makeText(HardGameActivity.this, "Atak znowu dostępny!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void stopAttackCooldown() {
        if (attackCooldownTimer != null) {
            attackCooldownTimer.cancel();
            attackCooldownTimer = null;
        }
        attackOnCooldown = false;  // Ustaw stan jako "atak dostępny
    }

    private void onWin() {
        stopTimer();
        stopAttackCooldown(); // przy wygranej czyścimy cooldown

        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);

        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");

        Toast.makeText(this, "🎉 Pokonałaś potworka!", Toast.LENGTH_SHORT).show();

        //  po chwili start nowego potwora + nowe zadanie + timer
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
        stopTimer();    // Zatrzymanie poprzedniego timera, aby nie było dwóch timerów jednocześnie

        // Odblokowanie OK i pola odpowiedzi na czas nowego zadania
        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
//  Timer 15 sekund — co sekundę aktualizuje tekst, a na końcu blokuje odpowiedź
        countDownTimer = new CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                textTimer.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                // Koniec czasu — blokuje możliwość wpisania i zatwierdzenia odpowiedzi
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
        stopAttackCooldown();
    }
}
