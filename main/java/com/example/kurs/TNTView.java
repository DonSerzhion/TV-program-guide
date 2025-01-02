package com.example.kurs;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.kurs.databinding.ActivityTntviewBinding;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.Comparator;

public class TNTView extends AppCompatActivity {

        private static final String TAG = "TNTView";
        private static final String BASE_URL_DAY = "https://tvprogram.tnt-online.ru/api/get-tvprogram-by-date?date="; // URL для получения расписания на день
        private static final String CHANNEL_ID = "my_channel_id";
        private TextView tN;
        private LinearLayout layout; // Добавляем LinearLayout для отображения программ
        private List<Channel> favoriteChannels;
        private String savedText; // Для сохранения текста из TextView
        private List<String> savedPrograms; // Для сохранения программ


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_tntview);
            Intent intent = getIntent();
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar); // Установка Toolbar как ActionBar

        // Включаем кнопку "Назад"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

            // Инициализируем TextView
            tN = findViewById(R.id.textViewScheduleT);
        layout = findViewById(R.id.programs_layout);
            createNotificationChannel(); // Инициализация канала уведомлений
        // Загружаем избранные каналы
        loadFavorites();

        // Восстанавливаем состояние, если оно существует
        if (savedInstanceState != null) {
            savedText = savedInstanceState.getString("savedText");
            savedPrograms = savedInstanceState.getStringArrayList("savedPrograms");
            if (savedText != null) {
                tN.setText(savedText);
                tN.setGravity(Gravity.CENTER); // Выравнивание по центру
                tN.setTextSize(18); // Увеличиваем размер шрифта
            }
            if (savedPrograms != null) {
                layout.removeAllViews(); // Очищаем предыдущие программы
                for (String program : savedPrograms) {
                    TextView tvProgram = new TextView(this);
                    tvProgram.setText(program);
                    tvProgram.setGravity(Gravity.CENTER); // Выравнивание по центру
                    tvProgram.setTextSize(18); // Увеличиваем размер шрифта
                    layout.addView(tvProgram);
                }
            }
        }
        }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Сохраняем текст из TextView
        outState.putString("savedText", tN.getText().toString());
        // Сохраняем программы
        ArrayList<String> programs = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) {
                programs.add(((TextView) child).getText().toString());
            }
        }
        outState.putStringArrayList("savedPrograms", programs);
    }

    private void loadFavorites() {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        Set<String> favoriteSet = sharedPreferences.getStringSet("favorite_channels", new HashSet<>());
        favoriteChannels = new ArrayList<>();

        for (String channelName : favoriteSet) {
            if (channelName.equals("ТНТ")) {
                favoriteChannels.add(new Channel("ТНТ", R.drawable.tnt));
            }
            if (channelName.equals("Первый канал")) {
                favoriteChannels.add(new Channel("Первый канал", R.drawable.__5_svg));
            }
            if (channelName.equals("Домашний")) {
                favoriteChannels.add(new Channel("Домашний", R.drawable.logos_d_1));
            }
            if (channelName.equals("НТВ")) {
                favoriteChannels.add(new Channel("НТВ", R.drawable.ntv_logo_2003_svg));
            }

        }
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_tnt_view, menu);
            Log.d(TAG, "Menu created");
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.channel_list) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            if (itemId == R.id.Featured_Channels) {
                Intent intent = new Intent(this, FeaturedChannels.class);
                ArrayList<Channel> favorChannels = new ArrayList<>(favoriteChannels);
                intent.putExtra("favorite_channels", favorChannels); // Передаем список избранных каналов
                startActivity(intent);
            }
            if (itemId==android.R.id.home){
                // Обработка нажатия на кнопку "Назад"
                onBackPressed(); // Возврат к предыдущей активности
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void loadSchedule1DayT(View view) {
            loadScheduleDay();
        }

        private void loadScheduleDay() { //Асинхронный запрос на получение JSON-объекта методом GET
            String date = getCurrentDate();
            String url = BASE_URL_DAY + date;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> { //при успешном ответе от сервера, обработка полученного JSON-объекта
                        List<Program> programs = parseJson(response.toString()); // Парсим JSON
                        displayProgramsByDay(programs); // Отображаем программы
                    },
                    error -> Log.e(TAG, "Volley Error: " + error.toString()
                    ));
            RequestQueue queue = Volley.newRequestQueue(this); //Инициализация очереди запросов
            queue.add(request);
        }

    private void displayProgramsByDay(List<Program> programs) {
        LinearLayout layout = findViewById(R.id.programs_layout);
        layout.removeAllViews(); // Очищаем предыдущие элементы
        String currentDate = getCurrentDate();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Фильтрация программ на текущий день
        List<Program> filteredPrograms = new ArrayList<>();
        for (Program program : programs) {
            if (program.date.equals(currentDate) && program.time.compareTo(currentTime) >= 0) {
                filteredPrograms.add(program);
            }
        }

        // Отображаем программы, начиная с текущего времени
        for (final Program program : filteredPrograms) {
            addProgramToLayout(layout, program);
        }
    }

        private class LoadScheduleTask extends AsyncTask<Void, Void, List<Program>> {
            @Override
            protected List<Program> doInBackground(Void... voids) {
                List<Program> allPrograms = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                // Устанавливаем день недели на понедельник
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                for (int i = 0; i < 7; i++) {
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    String url = BASE_URL_DAY + date;
                    // Выполняем запрос и парсим JSON
                    try {
                        JSONObject response = new JSONObject(getJsonFromUrl(url)); // Предполагается, что вы реализуете этот метод
                        List<Program> programs = parseJson(response.toString());
                        allPrograms.addAll(programs);
                    } catch (Exception e) {
                        Log.e(TAG, "Error fetching data: " + e.getMessage());
                    }
                    // Переходим к следующему дню
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                return allPrograms;
            }
            @Override
            protected void onPostExecute(List<Program> programs) {
                displayProgramsByWeek(programs); // Отображаем все программы
            }
        }

        // Метод для получения JSON-данных из URL
        private String getJsonFromUrl(String urlString) throws IOException {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        // Вызов задачи
        public void loadScheduleWeekT(View view) {
            new LoadScheduleTask().execute();
        }

        private String getCurrentDate() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date());
        }

        private List<Program> parseJson(String json) {
            List<Program> programs = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(json);
                String itemsHtml = jsonObject.getString("items");
                String date = jsonObject.getString("date"); // Предполагаем, что дата есть в корневом объекте JSON
                Document doc = Jsoup.parse(itemsHtml);
                Elements elements = doc.select(".tvprogram_box");
                for (Element element : elements) {
                    String time = element.select(".tv-program_time-slot_time").text();
                    String programTitle = element.select(".tv-program_time-slot_title a").text();
                    String programLink = element.select(".tv-program_time-slot_title a").attr("href");
                    String episodeInfo = element.select(".tv-program_time-slot_number-episode").text();
                    // Передаем дату в конструктор программы
                    Program program = new Program(time, programTitle, programLink, episodeInfo, date);
                    programs.add(program);
                }
            } catch (Exception e) {
                Log.e(TAG, "Parse Error: " + e);
            }
            return programs;
        }

    @SuppressLint("SetTextI18n")
    private void displayProgramsByWeek(List<Program> programs) {
        LinearLayout layout = findViewById(R.id.programs_layout);
        layout.removeAllViews(); // Очищаем предыдущие элементы

        // Сортируем программы по дате
        Collections.sort(programs, (p1, p2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date1 = sdf.parse(p1.date);
                Date date2 = sdf.parse(p2.date);
                return date1.compareTo(date2); // Сравниваем даты
            } catch (ParseException e) {
                e.printStackTrace();
                return 0; // Если произошла ошибка, не меняем порядок
            }
        });

        // Создаем Map для хранения программ по датам
        Map<String, List<Program>> programsByDate = new LinkedHashMap<>();
        // Добавляем программы в Map
        for (Program program : programs) {
            //Проверка, существует ли ключ, соответствующий дате программы
            programsByDate.computeIfAbsent(program.date, k -> new ArrayList<>()).add(program);
            //Если нет, тогда создаётся новый список, ассоциированный с этой датой
        }

        // Отображаем программы по дням недели
        String currentDate = getCurrentDate();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        for (Map.Entry<String, List<Program>> entry : programsByDate.entrySet()) {
            String date = entry.getKey();
            List<Program> dailyPrograms = entry.getValue();

            TextView tvDateLabel = new TextView(this);
            tvDateLabel.setText("Дата: ");
            tvDateLabel.setGravity(Gravity.CENTER); // Выравнивание по центру
            tvDateLabel.setTextSize(18); // Увеличиваем размер шрифта
            tvDateLabel.setTypeface(null, Typeface.BOLD); // Делаем текст жирным
            layout.addView(tvDateLabel);

            // Отображаем дату
            TextView tvDate = new TextView(this);
            tvDate.setText(date  + "\n");
            tvDate.setGravity(Gravity.CENTER); // Выравнивание по центру
            tvDate.setTextSize(18); // Увеличиваем размер шрифта
            tvDate.setTypeface(null, Typeface.BOLD); // Делаем текст жирным
            layout.addView(tvDate);
            // Отображаем программы для этой даты
            for (final Program program : dailyPrograms) {
                // Проверяем, идет ли программа после текущего времени
                if (program.date.equals(currentDate) && program.time.compareTo(currentTime) >= 0) {
                    addProgramToLayout(layout, program);
                } else if (!program.date.equals(currentDate)) {
                    // Если программа на другой день, добавляем её всегда
                    addProgramToLayout(layout, program);
                }
            }
        }
    }

    private void addProgramToLayout(LinearLayout layout, Program program) {
        // Создаем TextView для надписи "Время"
        TextView tvTimeLabel = new TextView(this);
        tvTimeLabel.setText("Время: ");
        tvTimeLabel.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTimeLabel.setTypeface(null, Typeface.BOLD); // Делаем текст жирным
        tvTimeLabel.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTimeLabel);

        TextView tvTime = new TextView(this);
        tvTime.setText(program.time);
        tvTime.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTime.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTime);

        // Создаем TextView для надписи "Название"
        TextView tvTitleLabel = new TextView(this);
        tvTitleLabel.setText("Название:");
        tvTitleLabel.setTypeface(null, Typeface.BOLD); // Делаем текст жирным
        tvTitleLabel.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTitleLabel.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTitleLabel);

        TextView tvTitle = new TextView(this);
        tvTitle.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTitle.setText(program.title + "\n");
        tvTitle.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTitle);

        // Проверяем, идет ли программа после текущего времени
        String currentDate = getCurrentDate();
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        if (program.date.compareTo(currentDate) > 0 ||
                (program.date.equals(currentDate) && program.time.compareTo(currentTime) > 0)) {
            Button btnNotify = new Button(this);
            btnNotify.setText("Уведомить о начале");
            btnNotify.setOnClickListener(v -> {
                createNotification(this, program.date, program.time, program.title, program.link, program.info);
                setReminder(program); // Устанавливаем напоминание
            });
            layout.addView(btnNotify);
        }
        layout.addView(new Space(this)); // Пространство между элементами
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminder(Program program) {
        // Формат для входной даты
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        // Формат для выходной даты
        SimpleDateFormat outputSdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

        try {
            // Парсим дату из формата "год-месяц-день"
            Date programDateTime = inputSdf.parse(program.date + " " + program.time);

            // Преобразуем дату в строку в формате "день-месяц-год"
            String formattedDate = outputSdf.format(programDateTime);

            long triggerTime = 0;
            //long triggerTime = programDateTime.getTime() - 5 * 60 * 1000; // 5 минут до начала

            // Устанавливаем AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("title", program.title);
            intent.putExtra("date", formattedDate.split(" ")[0]); // Передаем только дату в новом формате
            intent.putExtra("time", formattedDate.split(" ")[1]); // Передаем только время

            // Добавляем FLAG_IMMUTABLE
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // Направляем пользователя к настройкам, чтобы дать разрешение
                    Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intentSettings);
                }
            } else {
                // Будильник для версий ниже Android 12
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void createNotification(Context context, String date, String time, String title, String link, String info) {
            Intent intent = new Intent(context, TNTView.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.bell)
                    .setContentTitle(title)
                    .setContentText("Передача начинается " + date + " в " + time)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Сделает так, чтобы уведомление автоматически удалялось после взаимодействия с ним
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            // Получение менеджера уведомлений
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(TNTView.this);
            int notificationId = (int) System.currentTimeMillis(); // Уникальный ID для каждого уведомления
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            notificationManager.notify(notificationId, builder.build());
        }

        public void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

        class Program implements Serializable {
            public String time;
            public String title;
            public String link;
            public String info;
            public String date; // Добавляем поле для хранения даты программы
            public Program(String time, String title, String link, String info, String date) {
                this.time = time;
                this.title = title;
                this.link = link;
                this.info = info;
                this.date = date; // Форматируем дату
            }
        }


}

