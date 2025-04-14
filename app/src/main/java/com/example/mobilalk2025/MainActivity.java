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
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView errorTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Auth inicializálása
        mAuth = FirebaseAuth.getInstance();

        // View elemek inicializálása
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);
        Button registerButton = findViewById(R.id.registerButton);

        // Bejelentkezés gomb eseményfigyelője
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent); // Activity indítása
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Animáció
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Ellenőrizzük, hogy van-e bejelentkezett felhasználó
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // Ha van bejelentkezett felhasználó, navigálj a főoldalra
            navigateToHome();
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validáció
        if (email.isEmpty()) {
            showError("Kérjük, add meg az email címed!");
            return;
        }

        if (password.isEmpty()) {
            showError("Kérjük, add meg a jelszavad!");
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

        // Firebase hitelesítés
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sikeres bejelentkezés
                            hideError();
                            Toast.makeText(MainActivity.this, "Sikeres bejelentkezés!",
                                    Toast.LENGTH_SHORT).show();
                            navigateToHome();
                        } else {
                            // Sikertelen bejelentkezés
                            showError("Hibás email cím vagy jelszó!");
                        }
                    }
                });
    }

    private void navigateToHome() {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();

    }

    private void showError(String errorMessage) {
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorTextView.setVisibility(View.GONE);
    }
}