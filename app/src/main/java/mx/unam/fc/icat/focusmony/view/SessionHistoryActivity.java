package mx.unam.fc.icat.focusmony.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import mx.unam.fc.icat.focusmony.R;
import mx.unam.fc.icat.focusmony.model.Session;
import mx.unam.fc.icat.focusmony.model.SessionManager;

public class SessionHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvResultCount;
    private ConstraintLayout layoutEmpty;
    private RecyclerView recyclerView;

    private SessionHistoryAdapter adapter;
    private SessionManager sessionManager;

    private ChipGroup chipGroupFilter;
    private Chip chipToday, chipWeek, chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_session);

        bindViews();
        setupToolbar();

        // 🔥 Cargar datos iniciales
        List<Session> history = sessionManager.getAllSessions();

        adapter = new SessionHistoryAdapter(history, getResources());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateHistoryDisplay(history);

        setupFilterLogic();
    }

    private void bindViews() {
        toolbar = findViewById(R.id.history_toolbar);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerView = findViewById(R.id.recyclerViewHistory);

        // 🔥 IMPORTANTE: pasar contexto
        sessionManager = new SessionManager(this);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipToday = findViewById(R.id.chipFilterToday);
        chipWeek = findViewById(R.id.chipFilterWeek);
        chipAll = findViewById(R.id.chipFilterAll);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_history);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔥 SQLite ahora
        List<Session> history = sessionManager.getAllSessions();

        adapter = new SessionHistoryAdapter(history, getResources());
        recyclerView.setAdapter(adapter);
    }

    private void updateHistoryDisplay(List<Session> sessions) {

        boolean isEmpty = sessions == null || sessions.isEmpty();

        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        int count = (sessions != null ? sessions.size() : 0);

        tvResultCount.setText(
                getResources().getQuantityString(R.plurals.session_count, count, count)
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupFilterLogic() {

        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {

            List<Session> filtered;

            if (checkedId == R.id.chipFilterToday) {
                filtered = sessionManager.getSessionsToday();

            } else if (checkedId == R.id.chipFilterWeek) {
                filtered = sessionManager.getSessionsThisWeek();

            } else {
                filtered = sessionManager.getAllSessions();
            }

            adapter = new SessionHistoryAdapter(filtered, getResources());
            recyclerView.setAdapter(adapter);

            updateHistoryDisplay(filtered);
        });
    }


}