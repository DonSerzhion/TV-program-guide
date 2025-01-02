package com.example.kurs;

import static com.example.kurs.NTVView.CHANNEL_ID;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    private TextToSpeech tts;

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        // Создаем уведомление
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle("Напоминание")
                .setContentText("Передача " + title + " начинается " + date + " в " + time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Отображаем уведомление
        notificationManager.notify(0, builder.build());

        // Инициализация TextToSpeech
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        String speechText = "Напоминание: передача " + title + " начинается " + date + " в " + time;
                        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }
        });

        // Освобождаем ресурсы, когда они больше не нужны
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                tts.shutdown(); // Освобождаем ресурсы после завершения
            }

            @Override
            public void onError(String utteranceId) {}
        });
    }
}


