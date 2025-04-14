package com.example.mobilalk2025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView errorTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Auth inicializálása
        mAuth = FirebaseAuth.getInstance();

        // View elemek inicializálása
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        errorTextView = findViewById(R.id.errorTextView);

        // Regisztráció gomb eseményfigyelője
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validáció
        if (email.isEmpty()) {
            showError("Kérjük, add meg az email címed!");
            return;
        }

        if (password.isEmpty()) {
            showError("Kérjük, add meg a jelszavad!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("A jelszavak nem egyeznek!");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Érvénytelen email formátum!");
            return;
        }

        if (password.length() < 6) {
            showError("A jelszónak legalább 6 karakter hosszúnak kell lennie!");
            return;
        }

        // Firebase regisztráció
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sikeres regisztráció
                            Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!",
                                    Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        } else {
                            // Sikertelen regisztráció
                            showError("A regisztráció sikertelen: " +
                                    task.getException().getMessage());
                        }
                    }
                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private void showError(String errorMessage) {
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
    }
}