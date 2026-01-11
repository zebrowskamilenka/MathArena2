package com.example.matharena2;

import android.os.Bundle;
import android.os.CountDownTimer; //odliczaniie czasu w dol
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // krotkie komunikaty na ekranie
import android.content.Intent;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Random; // lsowanie

public class EasyGameActivity extends AppCompatActivity {
// elementu ui z layoutu zdafiniowane w xml stringami
    private ImageView imageMonsterEasy; //stworek
    private TextView textTask; //miejsce na zadanie
    private TextView textTimer;     // czas
    private TextView textPoints;    // punkty
    private ProgressBar progressHp; // pasek HP
    private EditText editAnswer; // miejsce do wpisania odpowiedz
    private Button btnOk; // przycisk okej

    // logika zadania
    private int correctAnswer = 0; // poczatkowa wartosc zmiennej przed wygenewoaniem pierwszego zadania
    private int points = 0; // liczba punktow na poczatku

    // HP potworka
    private int monsterHp = 100; // poczatkowe zycie potworka to 100hp
    private final int damagePerCorrect = 20; // za poprawna odpowiedz zadaje sie 20 pkt zycia

    // punkty
    private final int POINTS_CORRECT = 20; // otrzymujesz 20 pkt za poprawna odpowiedz
    private final int POINTS_WRONG = 5; // za zla odpowiedz otrzymujesz minusowe punkty czyli -5

    // timer
    private CountDownTimer countDownTimer; // odliczanie 15 sekund
    private static final int TIME_LIMIT_MS = 15000; // 15 sekund

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
        textTask = findViewById(R.id.textTask); // podpiecie z ui zadania
        textTimer = findViewById(R.id.textTimer); // podpiecie timera
        textPoints = findViewById(R.id.textPoints); // podpiecie miejsca na punkty
        progressHp = findViewById(R.id.progressHp); // podpiecie paska hp potwora
        editAnswer = findViewById(R.id.editAnswer); // podpiecie miejsca gdzie wpisuje sie odpowiedz
        btnOk = findViewById(R.id.btnOk); // podpiecie przycisku okej do zatwierdzania odpowiedzi

        startNewMonster();  // przygotowanie potworka
        generateEasyTask(); // wylosowanie pierwszego zadania
        startTimer(); // start odliczania czasu

        btnOk.setOnClickListener(v -> checkAnswer()); //po kliknieciu sprawdzenie odpowiedzi

        ImageButton btnBack = findViewById(R.id.btnBack); //przycisk cofnij wraca do ekranu wyboru trudnusci
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, DifficultyActivity.class);
            startActivity(intent);
            finish(); // zamkniecie strony latwej gry
        });
    }

    // ====== LOGIKA GRY ======

    private void showRandomEasyMonster() {
        Random random = new Random();
        imageMonsterEasy.setImageResource( //losowanie potworka
                easyMonsters[random.nextInt(easyMonsters.length)] // losowanie indeksu potworka
        );
    }
// reset gry na nowego potworka
    private void startNewMonster() {
        monsterHp = 100; // ustawienie zycia na 100
        showRandomEasyMonster(); // losowanie potworka

        progressHp.setMax(100); // maksymalna wartosc paska na 100
        updateHpUI(); // odswiezenie paska

        points = 0; // reset punktow, jakby punkty jesli zdobyles 100 punktow to w nastepnej grze juz nie bedzie mial tych 100 punktow tylko zaczynasz znowu od 0
        updatePointsUI();

        btnOk.setEnabled(true);
        editAnswer.setEnabled(true);
        editAnswer.setText(""); // czyszczenie pola odpowiedzi
    }
// generowanie zadania
    private void generateEasyTask() {
        Random random = new Random();
        int a = random.nextInt(20) + 1; // losowanie a 1-20
        int b = random.nextInt(20) + 1; // losowanie b 1-20

        correctAnswer = a + b; // zapisanie poprawnej odpowiedzi do correct answer
        textTask.setText(a + " + " + b + " = ?"); // pokazanie tresci zadania na ekraniep
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
