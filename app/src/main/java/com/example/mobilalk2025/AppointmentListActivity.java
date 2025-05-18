package com.example.mobilalk2025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobilalk2025.adapter.AppointmentAdapter;
import com.example.mobilalk2025.model.Appointment;
import com.example.mobilalk2025.notification.AppointmentReminderReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppointmentListActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentListActivity";
    
    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button newAppointmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);
        

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        

        recyclerView = findViewById(R.id.appointmentsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        newAppointmentButton = findViewById(R.id.newAppointmentButton);
        

        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(this, appointmentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        

        swipeRefreshLayout.setOnRefreshListener(this::loadAppointments);
        

        newAppointmentButton.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentListActivity.this, NewAppointmentActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        

        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        newAppointmentButton.startAnimation(bounce);
        

        loadAppointments();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    private void loadAppointments() {
        if (currentUser == null) {
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        swipeRefreshLayout.setRefreshing(true);
        appointmentList.clear();
        

        db.collection("appointments")
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Appointment appointment = document.toObject(Appointment.class);
                            appointmentList.add(appointment);
                            
                            // Schedule notification reminder for this appointment
                            scheduleAppointmentReminder(appointment);
                        }
                        

                        
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        Log.w(TAG, "Error getting appointments", task.getException());
                        Toast.makeText(AppointmentListActivity.this, 
                                "Hiba történt az időpontok betöltésekor", 
                                Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
    }
    

    private void loadHighPriceAppointments() {
        if (currentUser == null) return;
        

        db.collection("appointments")
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Appointment> userAppointments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Appointment appointment = document.toObject(Appointment.class);
                        userAppointments.add(appointment);
                    }
                    
                    Log.d(TAG, "Felhasználói időpontok száma: " + userAppointments.size());
                } else {
                    Log.w(TAG, "Hiba az időpontok lekérdezésekor", task.getException());
                }
            });
    }
    
    private void scheduleAppointmentReminder(Appointment appointment) {

        Intent intent = new Intent(this, AppointmentReminderReceiver.class);
        intent.putExtra("appointmentId", appointment.getId());
        intent.putExtra("serviceName", appointment.getServiceName());
        intent.putExtra("appointmentTime", appointment.getAppointmentDate().getTime());
        

        int requestCode = appointment.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointment.getAppointmentDate());
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        

        if (calendar.getTimeInMillis() > System.currentTimeMillis()) {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
            
            Log.d(TAG, "Reminder scheduled for appointment: " + appointment.getId());
        }
    }
}
