package com.example.matharena2;

import android.os.Bundle;
import android.os.CountDownTimer; // odliczaniie czasu w dol
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // krotkie komunikaty na ekranie
import android.content.Intent;
import android.widget.ImageButton;
import android.graphics.drawable.AnimationDrawable; // animacja wybuchu z animation-list
import android.view.View; // View.VISIBLE / View.GONE

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random; // losowanie

public class MediumGameActivity extends AppCompatActivity {

    // elementu ui z layoutu zdafiniowane w xml stringami
    private ImageView imageMonsterMedium; //stworek
    private ImageView imageExplosion; // animacja wybuchu (overlay)
    private TextView textTask; //miejsce na zadanie
    private TextView textTimer;     // czas
    private TextView textPoints;    // punkty
    private ProgressBar progressHp; // pasek HP
    private EditText editAnswer; // miejsce do wpisania odpowiedz
    private Button btnOk; // przycisk okej
    private ImageButton btnAttack; // przycisk ataku

    // logika zadania
    private int correctAnswer = 0; // poczatkowa wartosc zmiennej przed wygenewoaniem pierwszego zadania
    private int points = 0; // liczba punktow na poczatku

    // HP potworka
    private int monsterHp = 160; // poczatkowe zycie potworka w medium
    private static final int MONSTER_MAX_HP = 160; // maksymalne hp potwora
    private final int damagePerCorrect = 20; // za poprawna odpowiedz zadaje sie 20 pkt zycia

    // punkty
    private final int POINTS_CORRECT = 20; // otrzymujesz 20 pkt za poprawna odpowiedz
    private final int POINTS_WRONG = 5; // za zla odpowiedz tracisz 5 punktow

    // ATAK (jak w hard)
    private static final int ATTACK_COST = 50; // koszt ataku
    private static final int ATTACK_DAMAGE = 20; // obrazenia ataku

    // timer
    private CountDownTimer countDownTimer; // odliczanie 15 sekund
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    // potworki - lista potworkow pobieranych z drawable
    // R - oznacza zeby pobrał z folderu res (skrot od resorources - zasoby)
    // drawable - ozancza gdzie dokkladnie w ktorym folderze sie znajduje potworek
    private final int[] mediumMonsters = {
            R.drawable.p36m,
            R.drawable.p28m,
            R.drawable.p27m,
            R.drawable.p12m,
            R.drawable.p4m
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) { // co siedzieje po wejsciu na ekran
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medium_game); // wczytanie layoutu

        // podpiecie elementow z xml do zmiennych w javie
        imageMonsterMedium = findViewById(R.id.imageMonsterMedium);  // podpiecei z ui zdjec potworkow
        imageExplosion = findViewById(R.id.imageExplosion); // podpiecie animacji wybuchu
        textTask = findViewById(R.id.textTask); // podpiecie z ui zadania
        textTimer = findViewById(R.id.textTimer); // podpiecie timera
        textPoints = findViewById(R.id.textPoints); // podpiecie miejsca na punkty
        progressHp = findViewById(R.id.progressHp); // podpiecie paska hp potwora
        editAnswer = findViewById(R.id.editAnswer); // podpiecie miejsca gdzie wpisuje sie odpowiedz
        btnOk = findViewById(R.id.btnOk); // podpiecie przycisku okej do zatwierdzania odpowiedzi
        btnAttack = findViewById(R.id.btnAttack); // podpiecie przycisku ataku

        startNewMonster();  // przygotowanie potworka
        generateMediumTask(); // wylosowanie pierwszego zadania
        startTimer(); // start odliczania czasu

        btnOk.setOnClickListener(v -> checkAnswer()); //po kliknieciu sprawdzenie odpowiedzi

        // przycisk ataku - zabiera punkty i zadaje obrazenia potworkowi + animacja wybuchu
        btnAttack.setOnClickListener(v -> tryAttack());

        ImageButton btnBack = findViewById(R.id.btnBack); //przycisk cofnij wraca do ekranu wyboru trudnusci
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            startActivity(intent);
            finish(); // zamkniecie strony medium gry
        });
    }

    // ====== LOGIKA POTWORKA ======

    private void showRandomMediumMonster() {
        Random random = new Random();
        imageMonsterMedium.setImageResource( //losowanie potworka
                mediumMonsters[random.nextInt(mediumMonsters.length)] // losowanie indeksu potworka
        );
    }

    // reset gry na nowego potworka
    private void startNewMonster() {
        monsterHp = MONSTER_MAX_HP; // ustawienie zycia na max
        showRandomMediumMonster(); // losowanie potworka

        progressHp.setMax(MONSTER_MAX_HP); // maksymalna wartosc paska na max hp
        updateHpUI(); // odswiezenie paska
        updatePointsUI(); // pokazanie punktow

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
        editAnswer.setText(""); // czyszczenie pola odpowiedzi

        // odblokowanie ataku na start rundy
        btnAttack.setEnabled(true);
        btnAttack.setAlpha(1f);

        // ukrycie wybuchu na start rundy
        if (imageExplosion != null) imageExplosion.setVisibility(View.GONE);
    }

    // ====== GENEROWANIE ZADANIA ======

    private void generateMediumTask() {
        Random random = new Random();

        int a = random.nextInt(50) + 1; // losowanie a 1-50
        int b = random.nextInt(50) + 1; // losowanie b 1-50

        boolean subtraction = random.nextBoolean(); // losowanie czy dodawanie czy odejmowanie

        if (subtraction) {
            // zabezpieczenie aby nie bylo ujemnych wynikow
            int max = Math.max(a, b);
            int min = Math.min(a, b);

            correctAnswer = max - min; // zapisanie poprawnej odpowiedzi
            textTask.setText(max + " - " + min + " = ?"); // pokazanie tresci zadania na ekranie
        } else {
            correctAnswer = a + b; // zapisanie poprawnej odpowiedzi
            textTask.setText(a + " + " + b + " = ?"); // pokazanie tresci zadania na ekranie
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
            // ✅ DOBRA ODPOWIEDŹ
            points += POINTS_CORRECT;
            updatePointsUI();

            dealDamageToMonster(damagePerCorrect); // odejmowanie hp potworkowi

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
            generateMediumTask();
            startTimer();
        }
    }

    // jedna metoda na dmg (przyjmuje ile damage ma zadac)
    private void dealDamageToMonster(int damage) {
        monsterHp -= damage;
        if (monsterHp < 0) monsterHp = 0;

        updateHpUI();

        if (monsterHp == 0) {
            monsterDefeated();
        }
    }

    // ====== ATAK ======
    // atak: kosztuje 50 pkt i zabiera 20 hp, pomija zadanie i daje nowe (jak w hard)
    private void tryAttack() {
        if (monsterHp <= 0) return; // jesli potwor nie zyje to nie ma ataku

        // brak punktow = brak ataku
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

        // pomijamy biezace zadanie i dajemy nowe + reset timera (jesli potwor jeszcze zyje)
        if (monsterHp > 0) {
            editAnswer.setText("");
            generateMediumTask();
            startTimer();
        }
    }

    // animacja wybuchu po uzyciu przycisku ataku
    private void playExplosionAnimation() {
        if (imageExplosion == null) return;

        imageExplosion.setVisibility(View.VISIBLE);

        AnimationDrawable anim = (AnimationDrawable) imageExplosion.getDrawable();
        anim.start();

        // chowamy wybuch po krótkiej chwili
        imageExplosion.postDelayed(() ->
                imageExplosion.setVisibility(View.GONE), 800);
    }

    // ====== WYGRANA ======

    private void monsterDefeated() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);

        // po wygranej blokujemy tez atak
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);

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

                // po koncu czasu blokujemy tez atak (zeby bylo konsekwentnie)
                btnAttack.setEnabled(false);
                btnAttack.setAlpha(0.5f);
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
