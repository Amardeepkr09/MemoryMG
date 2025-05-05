package com.example.memorymg;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    GridLayout gridLayout;
    TextView scoreText, timerText;
    Button restartBtn;

    int[] images = {
            R.drawable.apple, R.drawable.apple,
            R.drawable.bluef, R.drawable.bluef,
            R.drawable.org, R.drawable.org,
            R.drawable.sberry, R.drawable.sberry,
            R.drawable.star, R.drawable.star,
            R.drawable.sun, R.drawable.sun,
            R.drawable.tool, R.drawable.tool,
            R.drawable.van, R.drawable.van
    };

    ArrayList<Integer> imageList = new ArrayList<>();
    ImageView firstCard = null;
    ImageView secondCard = null;
    int firstImageId = 0;
    int secondImageId = 0;
    boolean isBusy = false;
    final int cardBackId = R.drawable.card_back;

    int score = 0;
    int seconds = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = findViewById(R.id.gridLayout);
        scoreText = findViewById(R.id.scoreText);
        timerText = findViewById(R.id.timerText);
        restartBtn = findViewById(R.id.restartBtn);

        restartBtn.setOnClickListener(v -> resetGame());

        startNewGame();
    }

    @SuppressLint("SetTextI18n")
    private void startNewGame() {
        score = 0;
        seconds = 0;
        scoreText.setText("Score: 0");
        timerText.setText("Time: 0s");

        imageList.clear();
        for (int img : images) imageList.add(img);
        Collections.shuffle(imageList);

        gridLayout.removeAllViews();
        firstCard = null;
        secondCard = null;
        firstImageId = 0;
        secondImageId = 0;
        isBusy = false;

        loadCards();

        timerHandler.removeCallbacks(timerRunnable);
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                timerText.setText("Time: " + seconds + "s");
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void loadCards() {
        for (int i = 0; i < 16; i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.card_item, gridLayout, false);
            final ImageView cardImage = cardView.findViewById(R.id.cardImage);

            int imageRes = imageList.get(i);
            cardImage.setTag(imageRes);
            cardImage.setContentDescription(String.valueOf(i)); // Store the original index
            cardImage.setImageResource(cardBackId);

            final int finalI = i;

            cardView.setOnClickListener(v -> {
                Log.d("CardClick", "Card " + finalI + " clicked. isBusy: " + isBusy + ", tag: " + cardImage.getTag());

                if (isBusy || (int) cardImage.getTag() == 0) {
                    Log.d("CardClick", "Click ignored for card " + finalI + " (busy or already matched)");
                    return;
                }

                int currentTag = (int) cardImage.getTag();

                cardImage.setImageResource(currentTag);
                cardImage.setTag(cardBackId);

                Log.d("CardClick", "Card " + finalI + " flipped to: " + currentTag);

                if (firstCard == null) {
                    firstCard = cardImage;
                    firstImageId = currentTag;
                    Log.d("CardClick", "First card selected: " + firstImageId);
                } else {
                    secondCard = cardImage;
                    secondImageId = currentTag;
                    isBusy = true;
                    Log.d("CardClick", "Second card selected: " + secondImageId + ", isBusy set to true");

                    new Handler().postDelayed(() -> {
                        int firstCardTag = (int) firstCard.getTag();
                        int secondCardTag = (int) secondCard.getTag();

                        if (firstImageId != secondImageId) {
                            Log.d("CardMatch", "No match. Flipping back.");
                            firstCard.setImageResource(cardBackId);
                            secondCard.setImageResource(cardBackId);

                            int firstCardIndex = Integer.parseInt((String) firstCard.getContentDescription());
                            int secondCardIndex = Integer.parseInt((String) secondCard.getContentDescription());

                            firstCard.setTag(imageList.get(firstCardIndex));
                            secondCard.setTag(imageList.get(secondCardIndex));
                        } else {
                            Log.d("CardMatch", "Match found!");
                            firstCard.setTag(0);
                            secondCard.setTag(0);
                            score++;
                            scoreText.setText("Score: " + score);

                            if (score == 8) {
                                timerHandler.removeCallbacks(timerRunnable);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("You Win!")
                                        .setMessage("All matched in " + seconds + " seconds.")
                                        .setPositiveButton("Restart", (dialog, which) -> startNewGame())
                                        .setCancelable(false)
                                        .show();
                            }
                        }

                        firstCard = null;
                        secondCard = null;
                        isBusy = false;
                        Log.d("CardMatch", "Resetting firstCard, secondCard, and isBusy to false");
                    }, 800);
                }
            });

            gridLayout.addView(cardView);
        }
    }

    private void resetGame() {
        timerHandler.removeCallbacks(timerRunnable);
        startNewGame();
    }
}
