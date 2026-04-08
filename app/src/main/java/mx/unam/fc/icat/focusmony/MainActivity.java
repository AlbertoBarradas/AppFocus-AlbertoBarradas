package mx.unam.fc.icat.focusmony;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mx.unam.fc.icat.focusmony.model.Session;
import mx.unam.fc.icat.focusmony.model.SessionManager;
import mx.unam.fc.icat.focusmony.view.PreferencesActivity;
import mx.unam.fc.icat.focusmony.view.SessionHistoryActivity;

public class MainActivity extends AppCompatActivity {

    enum TimerState { IDLE, RUNNING, PAUSED }
    enum SessionMode { FOCUS, BREAK, REST }

    private static final long FOCUS_DURATION_MS   = 25 * 60 * 1000L;
    private static final long BREAK_DURATION_MS   =  5 * 60 * 1000L;
    private static final long REST_DURATION_MS    = 15 * 60 * 1000L;
    private static final int SESSIONS_BEFORE_REST = 4;

    private Toolbar toolbar;
    private ChipGroup chipGroupMode;
    private Chip chipFocus, chipBreak, chipRest;
    private TextView tvTimerDisplay, tvSessionStatus, tvSessionCounter;
    private MaterialButton btnStartStop;
    private ImageButton btnReset, btnSkip;
    private LinearLayout sessionDotsContainer;

    private CountDownTimer countDownTimer;
    private TimerState timerState = TimerState.IDLE;
    private SessionMode currentMode = SessionMode.FOCUS;
    private long timeLeftMillis = FOCUS_DURATION_MS;
    private int focusSessionsCompleted = 0;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bindViews();
        setSupportActionBar(toolbar);
        setupClickListeners();

        sessionManager = new SessionManager(this);

        updateTimerDisplay(timeLeftMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.tbMenu);
        chipGroupMode = findViewById(R.id.chipGroupMode);
        chipFocus = findViewById(R.id.chipFocus);
        chipBreak = findViewById(R.id.chipBreak);
        chipRest = findViewById(R.id.chipRest);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        btnStartStop = findViewById(R.id.btnStartStop);
        sessionDotsContainer = findViewById(R.id.sessionDotsContainer);

        tvSessionStatus = findViewById(R.id.tvSessionStatus);
        tvSessionCounter = findViewById(R.id.tvSessionCounter);
        btnReset = findViewById(R.id.btnReset);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> {
            if (timerState == TimerState.RUNNING) pauseTimer();
            else startTimer();
        });

        btnReset.setOnClickListener(v -> resetTimer());
        btnSkip.setOnClickListener(v -> skipToNextSession());
    }

    private void startTimer() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.RUNNING;
        btnStartStop.setText(R.string.btn_pause);

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onSessionFinished();
            }
        }.start();
    }

    private void pauseTimer() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (countDownTimer != null) countDownTimer.cancel();
        timerState = TimerState.PAUSED;
        btnStartStop.setText(R.string.btn_resume);
    }

    private void onSessionFinished() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.IDLE;

        // 🔥 Guardar sesión
        saveSession();

        if (currentMode == SessionMode.FOCUS) {
            focusSessionsCompleted++;
            addDot();

            if (focusSessionsCompleted >= SESSIONS_BEFORE_REST) {
                focusSessionsCompleted = 0;
                currentMode = SessionMode.REST;
            } else {
                currentMode = SessionMode.BREAK;
            }
        } else {
            currentMode = SessionMode.FOCUS;
        }

        tvSessionCounter.setText(focusSessionsCompleted + " / " + SESSIONS_BEFORE_REST);

        Toast.makeText(this, "¡Sesión terminada!", Toast.LENGTH_SHORT).show();

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        resetModeTime();
        btnStartStop.setText(R.string.btn_start);
    }

    private void saveSession() {
        new Thread(() -> {

            String type = currentMode.toString();

            String date = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                    .format(new Date());

            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date());

            int duration;
            switch (currentMode) {
                case FOCUS: duration = 25; break;
                case BREAK: duration = 5; break;
                case REST: duration = 15; break;
                default: duration = 0;
            }

            Session session = new Session(type, date, time, duration, true);
            new Thread(() -> sessionManager.insertSession(session)).start();

        }).start();
    }
    private void addDot() {
        View dot = new View(this);
        int size = (int) (10 * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));

        dot.setLayoutParams(params);
        dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_session_completed));
        sessionDotsContainer.addView(dot);
    }

    private void resetModeTime() {
        switch (currentMode) {
            case FOCUS: timeLeftMillis = FOCUS_DURATION_MS; break;
            case BREAK: timeLeftMillis = BREAK_DURATION_MS; break;
            case REST: timeLeftMillis = REST_DURATION_MS; break;
        }
        updateTimerDisplay(timeLeftMillis);
    }

    private void cancelTimer() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void resetTimer() {
        cancelTimer();
        timerState = TimerState.IDLE;
        resetModeTime();
        btnStartStop.setText(R.string.btn_start);
    }

    private void skipToNextSession() {
        cancelTimer();
        onSessionFinished();
    }

    private void updateTimerDisplay(long millis) {
        selectChipForMode(currentMode);

        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;

        tvTimerDisplay.setText(String.format("%02d:%02d", minutes, seconds));

        switch (currentMode) {
            case FOCUS:
                tvSessionStatus.setText(R.string.status_focus);
                break;
            case BREAK:
                tvSessionStatus.setText(R.string.status_break);
                break;
            case REST:
                tvSessionStatus.setText(R.string.status_rest);
                break;
        }
    }

    private void selectChipForMode(SessionMode mode) {
        int chipId;
        switch (mode) {
            case BREAK:
                chipId = R.id.chipBreak;
                highlightChip(chipBreak); break;
            case REST:
                chipId = R.id.chipRest;
                highlightChip(chipRest); break;
            default:
                chipId = R.id.chipFocus;
                highlightChip(chipFocus); break;
        }
        chipGroupMode.check(chipId);
    }

    private void highlightChip(Chip activeChip) {
        float density = getResources().getDisplayMetrics().density;
        Chip[] allChips = {chipFocus, chipBreak, chipRest};

        for (Chip chip : allChips) {
            chip.setChipStrokeWidth(0);
        }

        activeChip.setChipStrokeWidth(2 * density);
        int color = ContextCompat.getColor(this, R.color.color_border_accent);
        activeChip.setChipStrokeColor(ColorStateList.valueOf(color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            startActivity(new Intent(this, SessionHistoryActivity.class));
        }
        if (item.getItemId() == R.id.action_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}