package com.example.mobilalk2025.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.mobilalk2025.AppointmentListActivity;
import com.example.mobilalk2025.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppointmentReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "AppointmentReminder";
    private static final String CHANNEL_ID = "appointment_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Reminder received!");
        

        String appointmentId = intent.getStringExtra("appointmentId");
        String serviceName = intent.getStringExtra("serviceName");
        long appointmentTime = intent.getLongExtra("appointmentTime", 0);
        

        Date date = new Date(appointmentTime);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());
        String formattedDate = formatter.format(date);
        

        createNotificationChannel(context);
        

        Intent notificationIntent = new Intent(context, AppointmentListActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0,
                notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Időpontfoglalási emlékeztető")
                .setContentText("Holnap " + serviceName + " időpontod van! (" + formattedDate + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        

        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    private void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Appointment Reminders";
            String description = "Notifications for upcoming nail appointments";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
