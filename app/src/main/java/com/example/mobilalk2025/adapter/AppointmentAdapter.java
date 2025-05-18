package com.example.mobilalk2025.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilalk2025.AppointmentDetailsActivity;
import com.example.mobilalk2025.R;
import com.example.mobilalk2025.model.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final Context context;
    private final List<Appointment> appointmentList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());

    public AppointmentAdapter(Context context, List<Appointment> appointmentList) {
        this.context = context;
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        
        // Set appointment details
        holder.serviceNameTextView.setText(appointment.getServiceName());
        holder.dateTextView.setText(DATE_FORMAT.format(appointment.getAppointmentDate()));
        holder.statusTextView.setText(appointment.getStatus());
        
        // Set status color based on appointment status
        switch (appointment.getStatus()) {
            case "PENDING":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "CONFIRMED":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "COMPLETED":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case "CANCELLED":
                holder.statusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppointmentDetailsActivity.class);
            intent.putExtra("appointmentId", appointment.getId());
            context.startActivity(intent);
        });
        
        holder.cancelButton.setOnClickListener(v -> cancelAppointment(appointment, position));
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    private void cancelAppointment(Appointment appointment, int position) {
        db.collection("appointments").document(appointment.getId())
                .update("status", "CANCELLED")
                .addOnSuccessListener(aVoid -> {
                    appointment.setStatus("CANCELLED");
                    notifyItemChanged(position);
                    Toast.makeText(context, "Időpont lemondva!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                        Toast.makeText(context, "Hiba történt az időpont lemondásakor!", Toast.LENGTH_SHORT).show());
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView serviceNameTextView;
        TextView dateTextView;
        TextView statusTextView;
        Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameTextView = itemView.findViewById(R.id.serviceNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}
