package com.example.mobilalk2025;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "ProfileActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    
    private ImageView profileImageView;
    private EditText displayNameEditText;
    private EditText phoneEditText;
    private TextView emailTextView;
    private TextView locationTextView;
    private Button updateProfileButton;
    private Button takePhotoButton;
    private Button getLocationButton;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    
    private LocationManager locationManager;
    private String currentPhotoPath;
    private Uri photoUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        
        if (currentUser == null) {
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        profileImageView = findViewById(R.id.profileImageView);
        displayNameEditText = findViewById(R.id.displayNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailTextView = findViewById(R.id.emailTextView);
        locationTextView = findViewById(R.id.locationTextView);
        updateProfileButton = findViewById(R.id.updateProfileButton);
        takePhotoButton = findViewById(R.id.takePhotoButton);
        getLocationButton = findViewById(R.id.getLocationButton);
        progressBar = findViewById(R.id.progressBar);
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        updateProfileButton.setOnClickListener(v -> updateProfile());
        takePhotoButton.setOnClickListener(v -> takePicture());
        getLocationButton.setOnClickListener(v -> getLocation());
        
        loadUserProfile();
    }
    
    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        
        emailTextView.setText(currentUser.getEmail());
        
        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String displayName = document.getString("displayName");
                    String phone = document.getString("phone");
                    String location = document.getString("location");
                    String photoUrl = document.getString("photoUrl");
                    
                    if (displayName != null && !displayName.isEmpty()) {
                        displayNameEditText.setText(displayName);
                    }
                    
                    if (phone != null && !phone.isEmpty()) {
                        phoneEditText.setText(phone);
                    }
                    
                    if (location != null && !location.isEmpty()) {
                        locationTextView.setText(location);
                    }
                    
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Log.d(TAG, "Profile photo URL: " + photoUrl);
                    }
                } else {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", currentUser.getEmail());
                    userRef.set(userData);
                }
            } else {
                Toast.makeText(ProfileActivity.this, 
                        "Hiba történt a profil betöltésekor: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading user profile", task.getException());
            }
        });
    }
    
    private void updateProfile() {
        String displayName = displayNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        
        if (displayName.isEmpty()) {
            displayNameEditText.setError("Kérjük, add meg a neved!");
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        updateProfileButton.setEnabled(false);
        
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("displayName", displayName);
                        userData.put("phone", phone);
                        
                        db.collection("users").document(currentUser.getUid())
                                .update(userData)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    updateProfileButton.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, 
                                            "Profil sikeresen frissítve!", 
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    updateProfileButton.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, 
                                            "Hiba történt a profil frissítésekor: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error updating user data in Firestore", e);
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        updateProfileButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, 
                                "Hiba történt a profil frissítésekor: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error updating user profile in Auth", task.getException());
                    }
                });
    }
    
    private void takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }
        
        dispatchTakePictureIntent();
    }
    
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "Hiba történt a kép létrehozásakor!", Toast.LENGTH_SHORT).show();
            }
            
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.example.mobilalk2025.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "Nem található kamera alkalmazás!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        try {
            progressBar.setVisibility(View.VISIBLE);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10,
                    this);
            
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                onLocationChanged(lastKnownLocation);
            } else {
                Toast.makeText(this, "Helymeghatározás folyamatban...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Hiba történt a helymeghatározás során: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error getting location", e);
        }
    }
    
    @Override
    public void onLocationChanged(@NonNull Location location) {
        progressBar.setVisibility(View.GONE);
        
        String locationString = String.format(Locale.getDefault(),
                "Szélesség: %.6f, Hosszúság: %.6f",
                location.getLatitude(), location.getLongitude());
        
        locationTextView.setText(locationString);
        
        db.collection("users").document(currentUser.getUid())
                .update("location", locationString)
                .addOnSuccessListener(aVoid -> 
                        Toast.makeText(ProfileActivity.this, "Helyadat mentve!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> 
                        Log.e(TAG, "Error saving location data", e));
        
        locationManager.removeUpdates(this);
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }
    
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "Kérjük, engedélyezze a helymeghatározást!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Profilkép frissítve!", Toast.LENGTH_SHORT).show();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kép feltöltése")
                    .setMessage("A profilkép sikeresen elkészült. Szeretnéd feltölteni?")
                    .setPositiveButton("Igen", (dialog, which) -> {
                        Toast.makeText(ProfileActivity.this, 
                                "A kép feltöltése folyamatban...", 
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Nem", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Kamera engedély nélkül nem lehet profilképet készíteni!",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Helymeghatározás engedély nélkül nem lehet helyadatot menteni!",
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
