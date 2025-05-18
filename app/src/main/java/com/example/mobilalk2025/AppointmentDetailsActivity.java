package com.example.mobilalk2025;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilalk2025.model.Appointment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppointmentDetailsActivity extends AppCompatActivity {
    
    private static final String TAG = "ApptDetailsActivity";
    
    private TextView serviceNameTextView;
    private TextView dateTimeTextView;
    private TextView statusTextView;
    private TextView notesTextView;
    private Button cancelButton;
    private Button callSalonButton;
    private ProgressBar progressBar;
    
    private String appointmentId;
    private Appointment appointment;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);
        

        appointmentId = getIntent().getStringExtra("appointmentId");
        if (appointmentId == null) {
            Toast.makeText(this, "Hiba történt az időpont betöltésekor", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        

        db = FirebaseFirestore.getInstance();
        

        serviceNameTextView = findViewById(R.id.serviceNameTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        notesTextView = findViewById(R.id.notesTextView);
        cancelButton = findViewById(R.id.cancelButton);
        callSalonButton = findViewById(R.id.callSalonButton);
        progressBar = findViewById(R.id.progressBar);
        

        loadAppointmentDetails();
        

        cancelButton.setOnClickListener(v -> cancelAppointment());
        
        callSalonButton.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+36201234567"));
            startActivity(intent);
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        loadAppointmentDetails();
    }
    
    private void loadAppointmentDetails() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("appointments").document(appointmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (documentSnapshot.exists()) {
                        appointment = documentSnapshot.toObject(Appointment.class);
                        displayAppointmentDetails();
                    } else {
                        Toast.makeText(this, "Az időpont nem található", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Hiba történt az időpont betöltésekor: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading appointment", e);
                    finish();
                });
    }
    
    private void displayAppointmentDetails() {
        if (appointment == null) return;
        
        serviceNameTextView.setText(appointment.getServiceName());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
        dateTimeTextView.setText(dateFormat.format(appointment.getAppointmentDate()));
        
        statusTextView.setText(appointment.getStatus());
        

        switch (appointment.getStatus()) {
            case "PENDING":
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "CONFIRMED":
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "COMPLETED":
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "CANCELLED":
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        
        notesTextView.setText(appointment.getNotes());
        

        if ("CANCELLED".equals(appointment.getStatus()) || "COMPLETED".equals(appointment.getStatus())) {
            cancelButton.setVisibility(View.GONE);
        } else {
            cancelButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void cancelAppointment() {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection("appointments").document(appointmentId)
                .update("status", "CANCELLED")
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AppointmentDetailsActivity.this, "Időpont lemondva!",
                            Toast.LENGTH_SHORT).show();
                    
                    // Refresh the details display
                    appointment.setStatus("CANCELLED");
                    displayAppointmentDetails();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AppointmentDetailsActivity.this,
                            "Hiba történt az időpont lemondásakor: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error cancelling appointment", e);
                });
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
