package com.example.mobilalk2025.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilalk2025.NewAppointmentActivity;
import com.example.mobilalk2025.R;
import com.example.mobilalk2025.model.NailService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private final Context context;
    private final List<NailService> serviceList;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("hu", "HU"));
    private int lastPosition = -1;

    public ServiceAdapter(Context context, List<NailService> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        NailService service = serviceList.get(position);
        
        holder.nameTextView.setText(service.getName());
        holder.descriptionTextView.setText(service.getDescription());
        holder.priceTextView.setText(currencyFormat.format(service.getPrice()));
        holder.durationTextView.setText(service.getDurationMinutes() + " perc");
        
        holder.bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewAppointmentActivity.class);
            intent.putExtra("serviceId", service.getId());
            intent.putExtra("serviceName", service.getName());
            context.startActivity(intent);
        });
        
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
            holder.itemView.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView descriptionTextView;
        TextView priceTextView;
        TextView durationTextView;
        Button bookButton;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            bookButton = itemView.findViewById(R.id.bookButton);
        }
    }
}
