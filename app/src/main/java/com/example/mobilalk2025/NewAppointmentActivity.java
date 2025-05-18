package com.example.mobilalk2025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobilalk2025.model.Appointment;
import com.example.mobilalk2025.model.NailService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "NewAppointmentActivity";
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 100;
    
    private TextView serviceNameTextView;
    private TextView dateTimeTextView;
    private Button selectDateTimeButton;
    private EditText notesEditText;
    private Button bookAppointmentButton;
    private Button addToCalendarButton;
    private ProgressBar progressBar;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    

    private String serviceId;
    private String serviceName;
    private NailService selectedService;
    

    private Calendar selectedDateTime;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_appointment);
        

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        
        if (currentUser == null) {

            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(NewAppointmentActivity.this, MainActivity.class));
            finish();
            return;
        }
        

        serviceNameTextView = findViewById(R.id.serviceNameTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        selectDateTimeButton = findViewById(R.id.selectDateTimeButton);
        notesEditText = findViewById(R.id.notesEditText);
        bookAppointmentButton = findViewById(R.id.bookAppointmentButton);
        addToCalendarButton = findViewById(R.id.addToCalendarButton);
        progressBar = findViewById(R.id.progressBar);
        

        selectedDateTime = Calendar.getInstance();
 
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);

        selectedDateTime.set(Calendar.MINUTE, 0);
        selectedDateTime.set(Calendar.SECOND, 0);
        

        serviceId = getIntent().getStringExtra("serviceId");
        serviceName = getIntent().getStringExtra("serviceName");
        
        if (serviceId != null && serviceName != null) {

            serviceNameTextView.setText(serviceName);
            loadServiceDetails();
        } else {

            serviceNameTextView.setText("Válassz szolgáltatást");
            serviceNameTextView.setOnClickListener(v -> {
                startActivity(new Intent(NewAppointmentActivity.this, ServiceListActivity.class));
                finish();
            });
        }
        
        updateDateTimeDisplay();
        

        selectDateTimeButton.setOnClickListener(v -> showDateTimePicker());
        bookAppointmentButton.setOnClickListener(v -> bookAppointment());
        addToCalendarButton.setOnClickListener(v -> addToCalendar());
    }
    
    private void loadServiceDetails() {
        db.collection("services").document(serviceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            selectedService = document.toObject(NailService.class);
                        }
                    } else {
                        Log.w(TAG, "Error getting service details", task.getException());
                    }
                });
    }
    
    private void updateDateTimeDisplay() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
        dateTimeTextView.setText(formatter.format(selectedDateTime.getTime()));
    }
    
    private void showDateTimePicker() {

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDateTime.set(Calendar.YEAR, year);
                        selectedDateTime.set(Calendar.MONTH, month);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        

                        showTimePicker();
                    }
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        updateDateTimeDisplay();
                    }
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
    
    private void bookAppointment() {
        if (serviceId == null || serviceName == null) {
            Toast.makeText(this, "Kérjük, válassz szolgáltatást!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Kérjük, válassz jövőbeli időpontot!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String notes = notesEditText.getText().toString().trim();
        

        progressBar.setVisibility(View.VISIBLE);
        bookAppointmentButton.setEnabled(false);
        

        Appointment appointment = new Appointment(
                currentUser.getUid(),
                currentUser.getEmail(),
                serviceId,
                serviceName,
                selectedDateTime.getTime(),
                notes
        );
        

        db.collection("appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    

                    String id = documentReference.getId();
                    documentReference.update("id", id);
                    
                    Toast.makeText(NewAppointmentActivity.this, 
                            "Időpontfoglalás sikeres!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(NewAppointmentActivity.this, AppointmentListActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    bookAppointmentButton.setEnabled(true);
                    
                    Toast.makeText(NewAppointmentActivity.this,
                            "Hiba történt az időpontfoglalás során: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    
                    Log.e(TAG, "Error adding appointment", e);
                });
    }
    
    private void addToCalendar() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR},
                    CALENDAR_PERMISSION_REQUEST_CODE);
            return;
        }
        

        addAppointmentToCalendar();
    }
    
    private void addAppointmentToCalendar() {
        if (serviceId == null || serviceName == null) {
            Toast.makeText(this, "Kérjük, válassz szolgáltatást előbb!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        

        values.put(CalendarContract.Events.TITLE, "Műköröm: " + serviceName);
        values.put(CalendarContract.Events.DESCRIPTION, notesEditText.getText().toString());
        values.put(CalendarContract.Events.DTSTART, selectedDateTime.getTimeInMillis());
        

        long endTime;
        if (selectedService != null && selectedService.getDurationMinutes() > 0) {
            endTime = selectedDateTime.getTimeInMillis() + (selectedService.getDurationMinutes() * 60 * 1000);
        } else {
            endTime = selectedDateTime.getTimeInMillis() + (60 * 60 * 1000); // Add 1 hour as default
        }
        values.put(CalendarContract.Events.DTEND, endTime);
        

        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Budapest");
        values.put(CalendarContract.Events.ALL_DAY, 0);
        

        values.put(CalendarContract.Events.HAS_ALARM, 1);
        

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        
        if (uri != null) {

            long eventID = Long.parseLong(uri.getLastPathSegment());
            

            ContentValues reminderValues = new ContentValues();
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
            reminderValues.put(CalendarContract.Reminders.MINUTES, 30);
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            Uri reminderUri = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
            
            Toast.makeText(this, "Esemény hozzáadva a naptárhoz!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Hiba történt a naptárbejegyzés létrehozásakor!", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                addAppointmentToCalendar();
            } else {

                Toast.makeText(this, "Naptár hozzáférés engedélye nélkül nem lehet eseményt hozzáadni!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
