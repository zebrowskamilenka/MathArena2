package com.example.matharena2;

import android.content.Intent; // przevhodzenie miedzy ekranami
import android.graphics.drawable.AnimationDrawable; // animacja wybuchu z animation-list
import android.os.Bundle;
import android.os.CountDownTimer; // timer w dol odliczanie
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; //wiadomosci/komunikaty w trakcie gry

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random; // losowanie

public class HardGameActivity extends AppCompatActivity {

    //  rzeczy z layoutu - elementy ui
    private ImageView imageMonsterHard; //obrazek potwora dla trybu trudny
    private ImageView imageExplosion; // animacja wybuchu
    private TextView textTask; // tekst zadania
    private TextView textTimer; // odlicznie (timer)
    private TextView textPoints; //punkty zdobyte przez gracza
    private ProgressBar progressHp; // pasek zycia potworka
    private EditText editAnswer; // pole do wpisania odpowiedz
    private Button btnOk; // przycisk okej ktory zatwierdza odpowiedz ktoa sie wpisuje do editAnswer
    private ImageButton btnBack; //powrot do wyboru poziomu czyli do difficultyAvtivity
    private ImageButton btnAttack;             // przycisk ataku

    // dane gry czyli punkty HP i odpowiedz
    private int correctAnswer = 0;
    private int points = 0; // ile masz punktów, ale zaycznasz od zera

    private int monsterHp = 200; //poczatkowe hp potwora
    private static final int MONSTER_MAX_HP = 200; // maksymalne hp
    private static final int DAMAGE_PER_CORRECT = 20; //ile sie zabiera za poprawna odpowiedz

    private static final int POINTS_CORRECT = 20; // ile sie dostaje za poprawna odpowiedz
    private static final int POINTS_WRONG = 5; //ile sie traci za bledna odpowiedz
    private static final long NEXT_MONSTER_DELAY_MS = 1200; // jak wygrassz to czeka sie 12s zeby pojawil sie kolejny potwor

    // wprowadzenie timera
    private CountDownTimer countDownTimer; //licznik ktory odlicza od 15 do 0
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

    // ATak
    private static final int ATTACK_COST = 50; // koszt ataku
    private static final int ATTACK_DAMAGE = 20; // obrazenia ataku
    private boolean attackOnCooldown = false; // czy atak jest aktualnie zablokowany

    // osobny timer cooldownu ataku
    private CountDownTimer attackCooldownTimer;
    private static final int ATTACK_COOLDOWN_MS = 15000; // 15 sekund cooldownu

    // HARD potworki - losowanie potwora
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
        setContentView(R.layout.activity_hard_game); // start ktory laduje layout do poziomu trudnego

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
            finish(); //zamkniecie HardGameActivity - jkaby gra skonczona i jak klikniesz znowu to nowa runde otwiera
        });

        // start gry
        startNewMonster(); // ustawienie hp potyworka na maksa
        generateHardTask(); // losowania działania tu mnożenia i zapisanie do correct answer
        startTimer(); // odliczanie timera od 15 w dol

        // przycisk okej ktory sprawdza czy odpwiedz jest poprawna
        btnOk.setOnClickListener(v -> checkAnswer());

        // 5) atak - wykoananie ataku kosztujace 50 pkt
        btnAttack.setOnClickListener(v -> tryAttack());
    }

    private void startNewMonster() {
        stopAttackCooldown(); // // Reset cooldownu ataku: jeśli poprzedni potwór miał aktywny cooldown,
        // to kasujemy timer, aby nowa walka zaczynała się "czysto".

        monsterHp = MONSTER_MAX_HP; // ustawienie hp na maksimum
        showRandomHardMonster(); // losowanie grafiki potwora

        progressHp.setMax(MONSTER_MAX_HP); // pasek HP
        updateHpUI(); // zaktualizowanie paska zeby był na maksa
// rteset pola odpowidzi i odblokowanie przycisku okej
        editAnswer.setText("");
        editAnswer.setEnabled(true);
        btnOk.setEnabled(true);
// odblokowanie przycisku atatku i reset jego stanu
        attackOnCooldown = false;
        btnAttack.setEnabled(true);
        btnAttack.setAlpha(1f); // 1f oznacza wpełni widoczny przycisk
        if (imageExplosion != null) imageExplosion.setVisibility(View.GONE); // ukrycie animacji wybuchu
    }
// losowanie i usatwianie obrazku z tablicy hardMosters
    private void showRandomHardMonster() {
        Random random = new Random();
        int idx = random.nextInt(hardMonsters.length); // losowanie potworka
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
// sprawdzenie odpowiedzi po ok
    private void checkAnswer() {
        String txt = editAnswer.getText().toString().trim(); // pobranie wpisu gracza - trim zostal dopisany aby usunac spacje

        if (txt.isEmpty()) { // jesli nic nie wpisano zpstaje przerwane i pokazuje komunikat
            Toast.makeText(this, "Wpisz odpowiedź!", Toast.LENGTH_SHORT).show();
            return;
        }
       //próba zamiany tekstu na liczbę (gdyby ktoś wpisał np. abc)
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
            updatePointsUI(); //aktualizacja punktow

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

    //  metoda na dmg
    private void dealDamageToMonster(int damage) {
        // Odejmowanie HP (z zabezpieczeniem żeby nie spadło poniżej 0)
        monsterHp -= damage;
        if (monsterHp < 0) monsterHp = 0;
        updateHpUI(); // aktualizacja paska hp
// wygrana jesli potwor ma 0hp
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
            editAnswer.setText("");// czyszczenie
            generateHardTask(); //nowe zadanie
            startTimer(); //nowy timer
        }
        // cooldown 15s
        startAttackCooldown();
    }
//animacja wybuchu po uzyciu przycisku ataku
private void playExplosionAnimation() {
    imageExplosion.setVisibility(View.VISIBLE);

    AnimationDrawable anim = (AnimationDrawable) imageExplosion.getDrawable();
    anim.start();

    // chowamy wybuch po krótkiej chwili
    imageExplosion.postDelayed(() ->
            imageExplosion.setVisibility(View.GONE), 800);
}
// blokada ataku na 15s
    private void startAttackCooldown() {
        stopAttackCooldown(); // jesli był cooldown to go zatrzymaj

        attackOnCooldown = true; //zablokowanie ataku - ikona jest widoczna ale wygasnieta
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f); // wyblakniety przycisk
        // Timer, który po 15 sekundach odblokuje atak
        attackCooldownTimer = new CountDownTimer(ATTACK_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() { // Koniec cooldownu: odblokowanie ataku
                attackOnCooldown = false;
                btnAttack.setEnabled(true); // przycisk ataku jest dostepny
                btnAttack.setAlpha(1f); // przycisk ma pelna widocznosc
                Toast.makeText(HardGameActivity.this, "Atak znowu dostępny!", Toast.LENGTH_SHORT).show();
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
// wygrana - czyli pokanienie potwora i ma 0hp
    private void onWin() {
        stopTimer(); //zatrzymanie timera zadania
        stopAttackCooldown(); // przy wygranej czyszczenie cooldown
// blokowanie przyciskow bo sa juz niepotrzebne
        btnOk.setEnabled(false);
        editAnswer.setEnabled(false);
        btnAttack.setEnabled(false);
        btnAttack.setAlpha(0.5f);
//komuniekat w miejscu gdzie wyswietka sie zadanie matematyczne
        textTask.setText("WYGRANA! 🎉");
        textTimer.setText("0s");
// komnikat na dole ekranu
        Toast.makeText(this, "🎉 Pokonałaś potworka!", Toast.LENGTH_SHORT).show();

        //  po chwili start nowego potwora + nowe zadanie + timer
        textTask.postDelayed(() -> {
            startNewMonster();      // reset stworka
            generateHardTask();     // nowe działanie
            startTimer();           // nowe 15s
        }, NEXT_MONSTER_DELAY_MS);
    }
// aktualizacja paska zycia
    private void updateHpUI() {
        progressHp.setProgress(monsterHp);
    }
// aktualizacja punktow na ekranie
    private void updatePointsUI() {
        textPoints.setText(points + " pkt");
    }
// Czas na odpowiedz
    private void startTimer() {
        stopTimer();    // timer z poprzedniej rundy jest zatrzymywany aby nie bylo dwoch timerow jednoczesnie

        // Odblokowanie OK i pola odpowiedzi na czas nowego zadania
        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
//  Timer 15 sekund — co sekundę aktualizuje tekst, a na końcu blokuje odpowiedź
        countDownTimer = new CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                textTimer.setText(seconds + "s"); // pokazywanie ile czasu zostało
            }

            @Override
            public void onFinish() {
                // Koniec czasu — blokuje możliwość wpisania i zatwierdzenia odpowiedzi
                textTimer.setText("0s");
                Toast.makeText(HardGameActivity.this, "Koniec czasu!", Toast.LENGTH_SHORT).show();

                btnOk.setEnabled(false);
                editAnswer.setEnabled(false);

            }
        }.start();
    }
// zatrzymanie timera zadnia
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
// po wyjsciu z rozgrywki badz gry wszytsko jest zatrzymane
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopAttackCooldown();
    }
}
