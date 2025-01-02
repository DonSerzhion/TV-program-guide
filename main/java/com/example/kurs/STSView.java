package com.example.kurs;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;


public class STSView extends AppCompatActivity {
    private static final String TAG = "DomashniyView";
    private static final String BASE_URL_DAY = "https://domashniy.ru/api/schedule/"; // URL для получения расписания
    private TextView tN;
    private LinearLayout programsLayout; // Для отображения программ
    private static final String CHANNEL_ID = "my_channel_id";
    private List<Channel> favoriteChannels;
    private String savedText; // Для сохранения текста из TextView
    private List<String> savedPrograms; // Для сохранения программ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sts_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Установка Toolbar как ActionBar

        // Включаем кнопку "Назад"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tN = findViewById(R.id.textViewScheduleS);
        programsLayout = findViewById(R.id.programs_layout); // Инициализация LinearLayout
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
                programsLayout.removeAllViews(); // Очищаем предыдущие программы
                for (String program : savedPrograms) {
                    TextView tvProgram = new TextView(this);
                    tvProgram.setText(program);
                    tvProgram.setGravity(Gravity.CENTER); // Выравнивание по центру
                    tvProgram.setTextSize(18); // Увеличиваем размер шрифта
                    programsLayout.addView(tvProgram);
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
        for (int i = 0; i < programsLayout.getChildCount(); i++) {
            View child = programsLayout.getChildAt(i);
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
        getMenuInflater().inflate(R.menu.menu_sts_view, menu);
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

    public void loadSchedule1DayS(View view) {
        String date = getCurrentDate(); // Получаем только день
        String url = BASE_URL_DAY + date; // Формируем URL
        List<Program> allPrograms = new ArrayList<>(); // Создаем список для программ
        CountDownLatch latch = new CountDownLatch(1); // Счетчик для одного запроса
        fetchSchedule(url, allPrograms, date, latch); // Передаем дату
        new Thread(() -> {
            try {
                latch.await(); // Ждем завершения запроса
                runOnUiThread(() -> displayProgramsByDay(allPrograms)); // Обновляем UI
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for latch: " + e.getMessage());
            }
        }).start();
    }

    public void loadScheduleWeekS(View view) {

        List<Program> allPrograms = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(7); // Для 7 дней
        for (int i = 0; i < 7; i++) {
            String date = getDateFromToday(i); // Получаем только день
            String url = BASE_URL_DAY + date; // Формируем URL
            fetchSchedule(url, allPrograms, date, latch); // Передаем дату
        }
        new Thread(() -> {
            try {
                latch.await(); // Ждем завершения всех запросов
                runOnUiThread(() -> displayProgramsByWeek(allPrograms)); // Обновляем UI
            } catch (InterruptedException e) {
                Log.e(TAG, "Error waiting for latch: " + e.getMessage());
            }
        }).start();
    }

    private void fetchSchedule(String url, List<Program> allPrograms, String date, CountDownLatch latch) {
        //создается экземпляр Retrofit с базовым URL и конвертером JSON (Gson),
        //который будет использоваться для преобразования JSON-ответов в Java-объекты
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_DAY)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //Экземпляр интерфейса JsonPlaceHolderApi, который содержит методы для выполнения запросов к API
        JsonPlaceHolderApi api = retrofit.create(JsonPlaceHolderApi.class);
        //объект Call - асинхронный запрос к API
        Call<JsonObject> call = api.getSchedule(url);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Error Code: " + response.code());
                    if (latch != null) latch.countDown();
                    return;
                }
                JsonObject body = response.body();
                if (body != null) {
                    Log.d(TAG, "Response body: " + body.toString()); // Логирование ответа
                    if (body.has("status") && body.get("status").getAsString().equals("success")) {
                        JsonArray dataArray = body.getAsJsonArray("data");
                        List<Program> programs = parseJson(dataArray, date); // Передаем дату в parseJson
                        Log.d("ProgramsList", programs.toString());
                        if (latch != null) {
                            allPrograms.addAll(programs);
                            latch.countDown(); // Уменьшаем счетчик
                        } else {
                            displayProgramsByDay(programs); // Отображаем программы
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                if (latch != null) latch.countDown(); // Уменьшаем счетчик даже в случае ошибки
            }
        });
    }

    private List<Program> parseJson(JsonArray dataArray, String date) {
        List<Program> programs = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault()); // Формат времени

        for (JsonElement element : dataArray) {
            JsonObject programObject = element.getAsJsonObject();

            String id = getStringFromJson(programObject, "id");
            String name = getStringFromJson(programObject, "name");
            String category = getStringFromJson(programObject, "category");

            JsonArray episodesArray = programObject.getAsJsonArray("episodes");
            Log.d("episodesArray", episodesArray.toString());

            boolean hasValidEpisodes = false;

            for (JsonElement episodeElement : episodesArray) {
                JsonObject episodeObject = episodeElement.getAsJsonObject();
                if (episodeObject.has("time") && !episodeObject.get("time").isJsonNull()) {
                    hasValidEpisodes = true; // Есть хотя бы один действительный эпизод
                    break;
                }
            }

            // Если нет действительных эпизодов, создаем программу с временем начала
            if (!hasValidEpisodes) {
                long programTimeInMillis = programObject.has("time") ? programObject.get("time").getAsLong() : 0;
                Log.d("ProgramTime", sdf.format(new Date(programTimeInMillis)));
                // Добавляем 3 часа (3 * 60 * 60 * 1000 миллисекунд)
                programTimeInMillis += 3 * 60 * 60 * 1000;

                String time = sdf.format(new Date(programTimeInMillis)); // Преобразуем в строку формата "HH:mm"

                // Создаем новый объект Program без действительных эпизодов
                Program program = new Program(id, name, category, null, null, date, time, 0, 0);
                programs.add(program);
            } else {
                for (JsonElement episodeElement : episodesArray) {
                    JsonObject episodeObject = episodeElement.getAsJsonObject();
                    String episodeUrl = getStringFromJson(episodeObject, "url");
                    String episodeImage = getStringFromJson(episodeObject, "image");

                    // Извлекаем информацию о сезоне и серии с проверками на null
                    int seasonNumber = 0; // Значение по умолчанию
                    int episodeNumber = 0; // Значение по умолчанию

                    if (episodeObject.has("info") && !episodeObject.get("info").isJsonNull()) {
                        JsonObject infoObject = episodeObject.getAsJsonObject("info");
                        seasonNumber = infoObject.has("season") && !infoObject.get("season").isJsonNull()
                                ? infoObject.get("season").getAsInt()
                                : 0; // Значение по умолчанию, если нет информации о сезоне

                        // Проверяем значение number
                        String numberStr = infoObject.has("number") && !infoObject.get("number").isJsonNull()
                                ? infoObject.get("number").getAsString()
                                : ""; // Получаем значение как строку

                        episodeNumber = !numberStr.isEmpty() ? Integer.parseInt(numberStr) : 0; // Преобразуем в int или устанавливаем 0
                    }

                    // Получаем время в миллисекундах для каждой серии
                    long episodeTimeInMillis = episodeObject.has("time") && !episodeObject.get("time").isJsonNull()
                            ? episodeObject.get("time").getAsLong() * 1000 // Умножаем на 1000, если время в секундах
                            : 0; // Устанавливаем значение по умолчанию, если поле отсутствует

                    // Добавляем 3 часа к времени
                    episodeTimeInMillis += 3 * 60 * 60 * 1000;

                    String time = sdf.format(new Date(episodeTimeInMillis)); // Преобразуем в строку формата "HH:mm"

                    // Создаем новый объект Program с информацией о сезоне, серии и времени
                    Program program = new Program(id, name, category, episodeUrl, episodeImage, date, time, seasonNumber, episodeNumber);
                    programs.add(program);
                }
            }
        }
        return programs;
    }


    private String getStringFromJson(JsonObject jsonObject, String key) {
        return jsonObject.has(key) && !jsonObject.get(key).isJsonNull()
                ? jsonObject.get(key).getAsString()
                : "unknown_" + key; // Значение по умолчанию
    }

    private void displayProgramsByDay(List<Program> programs) {
        programsLayout.removeAllViews(); // Очищаем предыдущие элементы
        String currentDay = getCurrentDate(); // Получаем текущий день месяца
        String currentMonth = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1); // Получаем текущий месяц
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)); // Получаем текущий год

        @SuppressLint("DefaultLocale") String currentDate = String.format("%02d-%02d-%04d", Integer.parseInt(currentDay), Integer.parseInt(currentMonth), Integer.parseInt(currentYear));

        // Получаем текущее время в формате "HH:mm"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(Calendar.getInstance().getTime());

        // Фильтрация программ на текущий день
        List<Program> filteredPrograms = new ArrayList<>();
        for (Program program : programs) {
            // Сравниваем полные даты
            if (program.date.equals(currentDate) && program.time.compareTo(currentTime) > 0) {
                filteredPrograms.add(program);
            }
        }

        // Логирование количества отфильтрованных программ
        Log.d(TAG, "Filtered programs count: " + filteredPrograms.size());

        // Добавляем отфильтрованные программы в пользовательский интерфейс
        for (Program program : filteredPrograms) {
            addProgramToLayout(program);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayProgramsByWeek(List<Program> programs) {
        programsLayout.removeAllViews(); // Очищаем предыдущие элементы
        // Сортируем программы по дате с помощью TreeMap
        Map<String, List<Program>> programsByDate = new TreeMap<>();
        for (Program program : programs) {
            programsByDate.putIfAbsent(program.date, new ArrayList<>());
            programsByDate.get(program.date).add(program);
        }

        // Получаем текущее время в формате "HH:mm"
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(Calendar.getInstance().getTime());

        // Отображаем программы по датам
        for (Map.Entry<String, List<Program>> entry : programsByDate.entrySet()) {
            String date = entry.getKey();
            List<Program> dailyPrograms = entry.getValue();

            // Заголовок с датой
            TextView dateHeader = new TextView(this);
            dateHeader.setText("Расписание программы передач на: " + date); // Форматируйте дату по желанию
            dateHeader.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            dateHeader.setTypeface(null, Typeface.BOLD); // Сделаем заголовок жирным
            dateHeader.setGravity(Gravity.CENTER); // Выравнивание по центру
            dateHeader.setTextSize(18); // Увеличиваем размер шрифта
            programsLayout.addView(dateHeader);

            // Программы текущего дня
            for (Program program : dailyPrograms) {
                // Сравниваем время программы с текущим временем
                if (program.time.compareTo(currentTime) > 0) { // Если время программы позже текущего
                    addProgramToLayout(program); // Используем метод добавления программы
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void addProgramToLayout(Program program) {
        // Создаем текстовое представление для даты
        TextView tvData = new TextView(this);
        SpannableString spannableData = new SpannableString(String.format("Дата: %s\n", program.date));
        spannableData.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Дата: "
        tvData.setText(spannableData);
        tvData.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvData.setTextSize(18); // Увеличиваем размер шрифта
        programsLayout.addView(tvData);

        // Создаем текстовое представление для названия
        TextView tvName = new TextView(this);
        SpannableString spannableName = new SpannableString(String.format("Название: %s\n", program.name));
        spannableName.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Название: "
        tvName.setText(spannableName);
        tvName.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tvName.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvName.setTextSize(18); // Увеличиваем размер шрифта
        programsLayout.addView(tvName);

        // Создаем текстовое представление для сезона и серии
        TextView tvSeason = new TextView(this);
        SpannableString spannableSeason = new SpannableString(String.format("Сезон: %d, Серия: %d\n", program.seasonNumber, program.episodeNumber));
        spannableSeason.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Сезон: "
        tvSeason.setText(spannableSeason);
        tvSeason.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tvSeason.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvSeason.setTextSize(18); // Увеличиваем размер шрифта
        programsLayout.addView(tvSeason);

        // Создаем текстовое представление для времени
        TextView tvTime = new TextView(this);
        SpannableString spannableTime = new SpannableString(String.format("Время: %s", program.time));
        spannableTime.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Время: "
        tvTime.setText(spannableTime);
        tvTime.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        tvTime.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTime.setTextSize(18); // Увеличиваем размер шрифта
        programsLayout.addView(tvTime);

        // Добавляем кнопку для уведомления
        Button btnNotify = new Button(this);
        btnNotify.setText("Уведомить о начале");
        btnNotify.setOnClickListener(v -> {
                createNotification(this, program.date, program.time, program.name, program.episodeUrl, "Информация о программе: " + program.name);
                setReminder(program); // Устанавливаем напоминание
        });
        programsLayout.addView(btnNotify); // Добавляем кнопку в layout

        // Добавляем пространство между элементами
        programsLayout.addView(new Space(this));
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminder(STSView.Program program) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date programDateTime = sdf.parse(program.date + " " + program.time);
            long triggerTime = programDateTime.getTime() - 5 * 60 * 1000; // 5 минут до начала

            // Проверяем, что triggerTime в будущем
            if (triggerTime > System.currentTimeMillis()) {
                // Устанавливаем AlarmManager
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("title", program.name);
                intent.putExtra("date", program.date);
                intent.putExtra("time", program.time);

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
            } else {
                Log.d(TAG, "Reminder time is in the past, not setting alarm.");
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
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(STSView.this);
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

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)); // Возвращаем только день месяца
    }

    private String getDateFromToday(int daysFromToday) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysFromToday);
        return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)); // Возвращаем только день месяца
    }

    interface JsonPlaceHolderApi {
        @GET
        Call<JsonObject> getSchedule(@Url String url);
    }

    class Program implements Serializable {
        public String id;
        public String name;
        public String category;
        public String episodeUrl;
        public String episodeImage;
        public String date; // Добавлено поле для даты
        public String time; // Добавлено поле для времени
        public int seasonNumber; // Номер сезона
        public int episodeNumber; // Номер серии

        public Program(String id, String name, String category, String episodeUrl, String episodeImage, String day, String time, int seasonNumber, int episodeNumber) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.episodeUrl = episodeUrl;
            this.episodeImage = episodeImage;
            this.date = formatDate(day); // Форматируем дату
            this.time = time; // Инициализация времени
            this.seasonNumber = seasonNumber; // Инициализация номера сезона
            this.episodeNumber = episodeNumber; // Инициализация номера серии
        }

        private String formatDate(String day) {
            try {
                // Получаем текущую дату
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1; // Месяцы начинаются с 0
                int year = calendar.get(Calendar.YEAR);

                // Формируем строку даты в формате "день-месяц-год"
                String formattedDate = String.format("%02d-%02d-%04d", Integer.parseInt(day), month, year);
                return formattedDate;
            } catch (Exception e) {
                e.printStackTrace();
                return day; // Возвращаем исходный день в случае ошибки
            }
        }
    }

}

