package com.example.mobilalk2025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilalk2025.adapter.ServiceAdapter;
import com.example.mobilalk2025.model.NailService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {

    private static final String TAG = "ServiceListActivity";
    
    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<NailService> serviceList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_list);
        

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
 
            Toast.makeText(this, "Nem vagy bejelentkezve!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ServiceListActivity.this, MainActivity.class));
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        

        recyclerView = findViewById(R.id.servicesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        

        serviceList = new ArrayList<>();
        adapter = new ServiceAdapter(this, serviceList);
        

        int spanCount = getResources().getBoolean(R.bool.is_tablet) ? 2 : 1;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerView.setAdapter(adapter);
        

        loadServices();
    }
    
    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);
        serviceList.clear();
        

        db.collection("services")
            .limit(50) 
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NailService service = document.toObject(NailService.class);
                            serviceList.add(service);
                        }
                        
                        adapter.notifyDataSetChanged();
                        

                        if (serviceList.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.w(TAG, "Error getting services", task.getException());
                        Toast.makeText(ServiceListActivity.this, 
                                "Hiba történt a szolgáltatások betöltésekor", 
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
