package com.example.matharena2;

import android.os.Bundle;
import android.os.CountDownTimer; //odliczaniie czasu w dol
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // krotkie komunikaty na ekranie (na dole ekranu)
import android.content.Intent;
import android.widget.ImageButton;

import android.graphics.drawable.AnimationDrawable; // animacja wybuchu z animation-list
import android.view.View; // View.VISIBLE / View.GONE

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random; // lsowanie liczb i potworkow

public class EasyGameActivity extends AppCompatActivity {
    // elementu ui z layoutu zdafiniowane w xml stringami
    private ImageView imageMonsterEasy; //stworek
    private ImageView imageExplosion; // animacja wybuchu
    private TextView textTask; //miejsce na zadanie
    private TextView textTimer;     // czas
    private TextView textPoints;    // punkty
    private ProgressBar progressHp; // pasek HP
    private EditText editAnswer; // miejsce do wpisania odpowiedz
    private Button btnOk; // przycisk okej
    private ImageButton btnAttack; // przycisk ataku

    // logika gry
    private int correctAnswer = 0; // poczatkowa wartosc zmiennej przed wygenewoaniem pierwszego zadania
    private int points = 0; // liczba punktow na poczatku

    // HP potworka
    private int monsterHp = 100; // poczatkowe zycie potworka to 100hp
    private final int damagePerCorrect = 20; // za poprawna odpowiedz zadaje sie 20 pkt zycia

    // punkty
    private final int POINTS_CORRECT = 20; // gracz dostaje 20 pkt za poprawna odpowiedz
    private final int POINTS_WRONG = 5; // zla odp minusowe punkty czyli -5

    // ATak
    private static final int ATTACK_COST = 50;       // koszt ataku
    private static final int ATTACK_DAMAGE = 20;     // obrazenia ataku
    private boolean attackOnCooldown = false;        // czy atak jest aktualnie zablokowany

    // osobny timer cooldownu ataku
    private CountDownTimer attackCooldownTimer;
    private static final int ATTACK_COOLDOWN_MS = 15000; // 15 sekund na blokade ataku

    // timer zadania
    private CountDownTimer countDownTimer; // odliczanie 15 sekund
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    private static final long NEXT_MONSTER_DELAY_MS = 1500; // po wygranej przerwa zanim pojawi sie kolejny potwor

    // potworki - lista potworkow pobieranych z drawable
    // R - oznacza zeby pobrał z folderu res (skrot od resorources - zasoby)
    // drawable - ozancza gdzie dokkladnie w ktorym folderze sie znajduje potworek
    private final int[] easyMonsters = {
            R.drawable.p1e,
            R.drawable.p2e,
            R.drawable.p5e,
            R.drawable.p8e,
            R.drawable.p10e,
            R.drawable.p21e
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) { // co siedzieje po wejsciu na ekran
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_game); // wczytanie layoutu

        imageMonsterEasy = findViewById(R.id.imageMonsterEasy);  // podpiecei z ui zdjec potworkow
        imageExplosion = findViewById(R.id.imageExplosion); // podpiecie animacji wybuchu
        textTask = findViewById(R.id.textTask); // podpiecie z ui zadania
        textTimer = findViewById(R.id.textTimer); // podpiecie timera
        textPoints = findViewById(R.id.textPoints); // podpiecie miejsca na punkty
        progressHp = findViewById(R.id.progressHp); // podpiecie paska hp potwora
        editAnswer = findViewById(R.id.editAnswer); // podpiecie miejsca gdzie wpisuje sie odpowiedz
        btnOk = findViewById(R.id.btnOk); // podpiecie przycisku okej do zatwierdzania odpowiedzi
        btnAttack = findViewById(R.id.btnAttack); // podpiecie przycisku ataku

        startNewMonster();  // przygotowanie potworka
        generateEasyTask(); // wylosowanie pierwszego zadania
        startTimer(); // start odliczania czasu

        btnOk.setOnClickListener(v -> checkAnswer()); //po kliknieciu sprawdzenie odpowiedzi
        btnAttack.setOnClickListener(v -> tryAttack()); // po kliknieciu atak (kosztuje punkty)

        ImageButton btnBack = findViewById(R.id.btnBack); //przycisk cofnij wraca do ekranu wyboru trudnusci
        btnBack.setOnClickListener(v -> { //wywolanie przycisku cofnij
            Intent intent = new Intent(this, DifficultyActivity.class);
            startActivity(intent);
            finish(); // zamkniecie strony latwej gry
        });
    }
// logika gry
    // zaczynamy od wylosowania potwora
    private void showRandomEasyMonster() {
        Random random = new Random();
        imageMonsterEasy.setImageResource( //losowanie potworka
                easyMonsters[random.nextInt(easyMonsters.length)] // losowanie indeksu potworka
        );
    }

    // reset gry na nowego potworka
    private void startNewMonster() {
        stopAttackCooldown(); // reset cooldownu ataku
        monsterHp = 100; // ustawienie zycia na 100hp
        showRandomEasyMonster(); // losowanie potworka

        progressHp.setMax(100); // maksymalna wartosc paska na 100
        updateHpUI(); // odswiezenie paska
        updatePointsUI(); //odswiezenie punktow

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
        editAnswer.setText(""); // czyszczenie pola odpowiedzi

        // odblokowanie przycisku ataku i reset jego stanu (jak w Hard)
        attackOnCooldown = false;
        btnAttack.setEnabled(true);
        btnAttack.setAlpha(1f); // 1f oznacza wpelni widoczny przycisk

        // ukrycie wybuchu na start rundy
        if (imageExplosion != null) imageExplosion.setVisibility(View.GONE);
    }

    // generowanie zadania
    private void generateEasyTask() {
        Random random = new Random();
        int a = random.nextInt(20) + 1; // losowanie a 1-20
        int b = random.nextInt(20) + 1; // losowanie b 1-20

        correctAnswer = a + b; // zapisanie poprawnej odpowiedzi do correct answer
        textTask.setText(a + " + " + b + " = ?"); // pokazanie tresci zadania na ekraniep
    }

    private void startNextRound() {
        startNewMonster();     // nowy potwór
        generateEasyTask();    // nowe zadanie
        startTimer();          // start czasu
    }

    private void checkAnswer() {
        String txt = editAnswer.getText().toString().trim();
        if (txt.isEmpty()) { //jesli gracz nie kliknie pola wyswietla sie zeby cos wpisał
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
// porowananie z poprawnym wynikiem
        if (userAnswer == correctAnswer) {
            // DOBRA ODPOWIEDŹ
            points += POINTS_CORRECT;
            updatePointsUI();

            dealDamageToMonster(damagePerCorrect); // obrazenia za dobra odpowiedz

            Toast.makeText(this, "✅ Dobrze! +" + POINTS_CORRECT + " pkt", Toast.LENGTH_SHORT).show(); // wyswieyla sie jako komunikat za dobra odpowiedz
        } else {
            // ZŁA ODPOWIEDŹ
            points -= POINTS_WRONG;
            if (points < 0) points = 0; //jesli jest to pierwsza rozgrywka lub graczowi nie idzie jest to zabezpieczenie zeby nie mial punktow minusowych
            updatePointsUI(); // update punktow

            Toast.makeText(this, "❌ Źle! −" + POINTS_WRONG + " pkt", Toast.LENGTH_SHORT).show(); //wyswietla sie jako komunikat za zla odpowiedz
        }

        editAnswer.setText(""); //wyczyszczenie pola odpowiedzi do kolejnego zadania

        if (monsterHp > 0) {
            generateEasyTask(); // generowanie zadania jesli potwor ma wiecej niz 0 hp
            startTimer(); // odliczanie czasu
        }
    }

    private void dealDamageToMonster(int damage) {
        monsterHp -= damage;
        if (monsterHp < 0) monsterHp = 0; //zycie nie spada ponizej 0

        updateHpUI(); // aktualizacja paska hp

        if (monsterHp == 0) { //jesli potowrek ma 0 zycia to sie wygrywa
            onWin(); // wygrana
        }
    }

    // ATAK: -50 pkt, -20 HP, animacja, pomija zadanie, cooldown 15s
    private void tryAttack() {
        if (monsterHp <= 0) return; // jesli potwor nie zyje to nie ma ataku

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

        // pomiń bieżące zadanie i daj nowe + reset timera 15s jeśli jeszcze żyje
        if (monsterHp > 0) {
            editAnswer.setText(""); // czyszczeni pola
            generateEasyTask();// nowe zadanie
            startTimer(); // restart timera
        }

        startAttackCooldown();// cooldown 15s na atak potwora
    }

    // animacja wybuchu po uzyciu przycisku ataku
    private void playExplosionAnimation() {
        if (imageExplosion == null) return;

        imageExplosion.setVisibility(View.VISIBLE);

        AnimationDrawable anim = (AnimationDrawable) imageExplosion.getDrawable(); // uruchomienie animacji z drawable
        anim.start(); // start animacji

        //  wybuch chowany po  chwili(800ms)
        imageExplosion.postDelayed(() ->
                imageExplosion.setVisibility(View.GONE), 800); //nie moze byc dluzej bo brzydko wyglada
    }

    // blokada ataku na 15s
    private void startAttackCooldown() {
        stopAttackCooldown(); // jesli był cooldown to go zatrzymaj

        attackOnCooldown = true; //zablokowanie ataku - ikona jest widoczna ale wygasnieta
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f); // wyblakniety przycisk

        // Timer, który odblokuje atak
        attackCooldownTimer = new CountDownTimer(ATTACK_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() { // Koniec cooldownu: odblokowanie ataku
                attackOnCooldown = false;
                btnAttack.setEnabled(true);
                btnAttack.setAlpha(1f);
                Toast.makeText(EasyGameActivity.this, "Atak znowu dostępny!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    // jesli timer istnieje to go zatrzymaj
    private void stopAttackCooldown() {
        if (attackCooldownTimer != null) {
            attackCooldownTimer.cancel();
            attackCooldownTimer = null;
        }
        attackOnCooldown = false;  // ustawienie stanu jako atak dostępny
    }

    // wygrana - czyli pokanienie potwora i ma 0hp (jak w Hard)
    private void onWin() {
        // zatrzymanie timera zadania
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        stopAttackCooldown(); // przy wygranej czyszczenie cooldown

        // blokowanie przyciskow bo sa juz niepotrzebne -gracz wygral
        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);

        // komunikat w miejscu gdzie wyswietla sie zadanie matematyczne
        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");

        // komunikat na dole ekranu
        Toast.makeText(this, "🎉BRAWO! Pokonałaś potworka!", Toast.LENGTH_SHORT).show();

        //  po chwili start nowego potwora + nowe zadanie + timer
        textTask.postDelayed(() -> {
            startNewMonster();      // reset stworka
            generateEasyTask();     // nowe działanie
            startTimer();           // nowe 15s
        }, NEXT_MONSTER_DELAY_MS);
    }

    // ====== UI ======

    private void updateHpUI() {
        progressHp.setProgress(monsterHp);
    }

    private void updatePointsUI() {
        textPoints.setText(points + " pkt");
    }

    // timer

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
// nowe zadanie odblokowuje wpisywanie i przycisk okej
        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);

        // jesli atak nie jest na cooldownie to przy nowym zadaniu moze be byc dostepny
        if (!attackOnCooldown) {
            btnAttack.setEnabled(true);
            btnAttack.setAlpha(1f);
        }

        countDownTimer = new CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                textTimer.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                textTimer.setText("0s");
                Toast.makeText(EasyGameActivity.this, " Koniec czasu!", Toast.LENGTH_SHORT).show();
// koniec czasu wiec odpowiedz nie jest mozliwa i nie mozna jej wpisac
                btnOk.setEnabled(false);
                editAnswer.setEnabled(false);

                // po koncu czasu blokujemy tez atak
                btnAttack.setEnabled(false);
                btnAttack.setAlpha(0.5f);
            }
        }.start();
    }
// jka wychodzimy z gry lub wylaczamy apliacje timer nie liczy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAttackCooldown(); // po wyjsciu z gry czyscimy cooldown ataku
    }
}
