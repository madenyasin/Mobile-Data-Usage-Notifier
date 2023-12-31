package com.example.mobile_data_usage_notifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobile_data_usage_notifier.databinding.ActivityMainBinding;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 155;
    private static final int REQUEST_SMS_PERMISSION = 110;
    private static final String CHANNEL_ID = "data_usage";
    private static final int NOTIFICATION_ID = 112;
    private String number = "";

    WorkRequest workRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        binding.startWorkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWorker();
            }
        });
        binding.stopWorkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopWorker();
            }
        });
        binding.setPhoneNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = binding.phoneNumberText.getText().toString();
            }
        });

    }
    public void getSmsPermission(View view) {
        // Check if SEND_SMS permission is granted
        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
        } else {
            // Permission is not granted, request it
            requestSmsPermission();
        }

    }
    public void getNotificationPermission(View view) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // izin verilmiş
        } else {
            // izin veilmemiş, izin isteniyor
            requestNotificationPermission();
        }
    }

    private void requestSmsPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
            // Provide an explanation for why the permission is needed
            // This is optional and can be omitted if not needed.
        }

        // Request the SEND_SMS permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
    }

    private void requestNotificationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // izin isteği için açıklama gerekliyse doldurulur. Gerekli değilse isteğe bağlı.
        }
        // izin isteği gönderiliyor
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izin verildi
            } else {
                // izin reddedildi
                Toast.makeText(this, "Permission denied. You cannot send notifications.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // izin verildi
            } else {
                // izin reddedildi
                Toast.makeText(this, "Permission denied. You cannot send SMS.", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private NotificationChannel createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Mobile data usage alert notifications";
            String description = "It comes into play when mobile data is used effectively.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            return channel;
        }

        return null;
    }
    @SuppressLint("MissingPermission")
    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Mobile Data Usage Warning!")
                .setContentText("Mobile data is used effectively. Please turn off mobile data and connect to a WI-FI point.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void startWorker() {
        Data data = new Data.Builder().putString("phoneNumber", number).build();
        Constraints constraints = new Constraints.Builder()
                //.setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(false)
                .build();

        workRequest = new PeriodicWorkRequest.Builder(MonitoringWorker.class,15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void stopWorker() {
        WorkManager.getInstance(this).cancelWorkById(workRequest.getId());
    }
    public void sendSms(String number, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, message, null, null);
            Toast.makeText(getApplicationContext(),"Message Sent",Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Message could not be sent",Toast.LENGTH_LONG).show();
        }
    }

}