package com.example.pract;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private boolean player1Turn = true;
    private boolean isPlayingWithBot = false;
    private int moveCount = 0;
    private int player1Wins = 0, player2Wins = 0, draws = 0, botWins = 0, playerWinsVsBot = 0, drawsVsBot = 0;
    private SharedPreferences preferences;
    private TextView statsTextView;
    private ImageButton changeThemeButton;
    private boolean isDarkTheme;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        loadTheme();
        setContentView(R.layout.activity_main);

        changeThemeButton = findViewById(R.id.changeThemeButton);
        changeThemeButton.setOnClickListener(v -> toggleTheme());

        loadStats();

        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setOnClickListener(this::onCellClicked);
        }

        statsTextView = findViewById(R.id.statsTextView);
        updateStatsDisplay();

        findViewById(R.id.playWithBotButton).setOnClickListener(v -> toggleBotMode());
        findViewById(R.id.playWithFriendButton).setOnClickListener(v -> startFriendGame());

        updateThemeButton();
    }



    private void toggleTheme() {
        isDarkTheme = !preferences.getBoolean("night_mode", false);
        preferences.edit().putBoolean("night_mode", isDarkTheme).apply();
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();
    }

    private void setCurrentTheme() {
        boolean isNightMode = preferences.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
    private void updateThemeButton() {
        if (preferences.getBoolean("night_mode", false)) {
            changeThemeButton.setImageResource(R.drawable.sun);
        } else {
            changeThemeButton.setImageResource(R.drawable.moon);
        }
    }

    private void saveTheme(boolean isDark) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("darkTheme", isDark);
        editor.apply();
    }

    private void loadTheme() {
        isDarkTheme = preferences.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void startFriendGame() {
        isPlayingWithBot = false;
        resetBoard();
        showMessage("Режим игры с другом включен");
    }

    private void onCellClicked(View view) {
        Button button = (Button) view;
        if (!button.getText().toString().isEmpty()) return;

        button.setText(player1Turn ? "X" : "O");
        moveCount++;

        if (checkWin()) {
            handleWin();
        } else if (moveCount == 9) {
            handleDraw();
        } else {
            player1Turn = !player1Turn;
            if (isPlayingWithBot && !player1Turn) makeBotMove();
        }
        updateStatsDisplay();
    }

    private void handleWin() {
        if (isPlayingWithBot && !player1Turn) {
            botWins++;
            showMessage("Бот победил!");
        } else if (isPlayingWithBot && player1Turn) {
            playerWinsVsBot++;
            showMessage("Игрок победил!");
        } else if (player1Turn) {
            player1Wins++;
            showMessage("Игрок 1 победил!");
        } else {
            player2Wins++;
            showMessage("Игрок 2 победил!");
        }
        saveStats();
        resetBoard();
    }

    private void handleDraw() {
        if (isPlayingWithBot) drawsVsBot++;
        else draws++;
        saveStats();
        showMessage("Ничья!");
        resetBoard();
    }

    private boolean checkWin() {
        Button[][] buttons = new Button[3][3];
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = (Button) gridLayout.getChildAt(i * 3 + j);
            }
        }

        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(buttons[i][1].getText()) &&
                    buttons[i][0].getText().equals(buttons[i][2].getText()) &&
                    !buttons[i][0].getText().toString().isEmpty()) {
                return true;
            }
            if (buttons[0][i].getText().equals(buttons[1][i].getText()) &&
                    buttons[0][i].getText().equals(buttons[2][i].getText()) &&
                    !buttons[0][i].getText().toString().isEmpty()) {
                return true;
            }
        }

        if (buttons[0][0].getText().equals(buttons[1][1].getText()) &&
                buttons[0][0].getText().equals(buttons[2][2].getText()) &&
                !buttons[0][0].getText().toString().isEmpty()) {
            return true;
        }
        if (buttons[0][2].getText().equals(buttons[1][1].getText()) &&
                buttons[0][2].getText().equals(buttons[2][0].getText()) &&
                !buttons[0][2].getText().toString().isEmpty()) {
            return true;
        }
        return false;
    }

    private void resetBoard() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            Button button = (Button) gridLayout.getChildAt(i);
            button.setText("");
        }
        player1Turn = true;
        moveCount = 0;
    }

    private void saveStats() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("player1Wins", player1Wins);
        editor.putInt("player2Wins", player2Wins);
        editor.putInt("draws", draws);
        editor.putInt("botWins", botWins);
        editor.putInt("playerWinsVsBot", playerWinsVsBot);
        editor.putInt("drawsVsBot", drawsVsBot);
        editor.apply();
    }

    private void loadStats() {
        player1Wins = preferences.getInt("player1Wins", 0);
        player2Wins = preferences.getInt("player2Wins", 0);
        draws = preferences.getInt("draws", 0);
        botWins = preferences.getInt("botWins", 0);
        playerWinsVsBot = preferences.getInt("playerWinsVsBot", 0);
        drawsVsBot = preferences.getInt("drawsVsBot", 0);
    }

    private void updateStatsDisplay() {
        String stats = "Игрок 1: " + player1Wins + " побед\n" +
                "Игрок 2: " + player2Wins + " побед\n" +
                "Ничьи: " + draws + "\n" +
                "Игры с ботом: Игрок - " + playerWinsVsBot + ", Бот - " + botWins + ", Ничьи - " + drawsVsBot;
        statsTextView.setText(stats);
    }

    private void toggleBotMode() {
        isPlayingWithBot = !isPlayingWithBot;
        resetBoard();
        showMessage(isPlayingWithBot ? "Режим игры с ботом включен" : "Режим игры с ботом отключен");
    }

    private void makeBotMove() {
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        Random random = new Random();
        int move;
        do {
            move = random.nextInt(9);
        } while (!((Button) gridLayout.getChildAt(move)).getText().toString().isEmpty());

        ((Button) gridLayout.getChildAt(move)).setText("O");
        moveCount++;
        if (checkWin()) {
            botWins++;
            saveStats();
            updateStatsDisplay();
            showMessage("Бот победил!");
            resetBoard();
        } else if (moveCount == 9) {
            drawsVsBot++;
            saveStats();
            updateStatsDisplay();
            showMessage("Ничья!");
            resetBoard();
        } else {
            player1Turn = true;
            updateStatsDisplay();
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
