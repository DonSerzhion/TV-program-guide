package com.example.kurs;

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
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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

import com.example.kurs.databinding.ActivityNtvviewBinding;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class NTVView extends AppCompatActivity {
    private static final String BASE_URL_DAY = "https://www.ntv.ru/api/new/widgets/air/schedule/index.jsp?channel=ntv";
    private static final String BASE_URL_WEEK = "https://www.ntv.ru/api/new/widgets/air/schedule/index.jsp?channel=ntv&dt=";
    static final String CHANNEL_ID = "program_notifications"; // Идентификатор канала уведомлений
    private TextView tN;
    private LinearLayout layout; // LinearLayout для отображения программ
    private List<Channel> favoriteChannels;
    private String savedText; // Для сохранения текста из TextView
    private List<String> savedPrograms; // Для сохранения программ


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ntv_view, menu);
        Log.d("Menu", "Menu created");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntvview);
        Intent intent = getIntent();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Установка Toolbar как ActionBar

        // Включаем кнопку "Назад"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Инициализируем TextView и LinearLayout
        tN = findViewById(R.id.textViewScheduleN);
        layout = findViewById(R.id.programs_layout);
        createNotificationChannel(); // Создаем канал уведомлений
        // Загружаем избранные каналы
        loadFavorites();

        // Восстанавливаем состояние, если оно существует
        if (savedInstanceState != null) {
            savedText = savedInstanceState.getString("savedText");
            savedPrograms = savedInstanceState.getStringArrayList("savedPrograms");
            if (savedText != null) {
                tN.setText(savedText);
                tN.setTextSize(18); // Увеличиваем размер шрифта
                tN.setGravity(Gravity.CENTER); // Выравнивание по центру
            }
            if (savedPrograms != null) {
                layout.removeAllViews(); // Очищаем предыдущие программы
                for (String program : savedPrograms) {
                    TextView tvProgram = new TextView(this);
                    tvProgram.setText(program);
                    tvProgram.setTextSize(18); // Увеличиваем размер шрифта
                    tvProgram.setGravity(Gravity.CENTER); // Выравнивание по центру
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

    public void loadSchedule1DayN(View view) {
        loadSchedule(BASE_URL_DAY, true);
    }

    public void loadScheduleWeekN(View view) {
        String urlString = BASE_URL_WEEK + getCurrentDate() + "&week=1";
        loadSchedule(urlString, false);
    }

    @SuppressLint("SetTextI18n")
    private void loadSchedule(String urlString, boolean filterToday) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                // Читаем строки из потока и добавляем их в StringBuilder
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                // Парсинг JSON-ответа
                String jsonResponse = parseJson(result.toString(), filterToday);
                // Обновляем TextView в главном потоке
                runOnUiThread(() -> {
                    tN.setText(jsonResponse);
                    layout.removeAllViews(); // Очищаем предыдущие программы
                    addProgramsToLayout(result.toString(), filterToday); // Добавляем новые программы
                });
            } catch (Exception e) {
                runOnUiThread(() -> tN.setText("Error: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private String parseJson(String json, boolean filterByToday) {
        StringBuilder result = new StringBuilder();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray schedule = jsonObject.getJSONObject("data").getJSONArray("schedule");

            // Формат для отображения времени
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

            // Получаем сегодняшнюю дату
            Calendar calendar = Calendar.getInstance();
            long todayStartMillis = calendar.getTimeInMillis(); // Начало сегодняшнего дня в миллисекундах

            // Устанавливаем конец сегодняшнего дня
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long todayEndMillis = calendar.getTimeInMillis(); // Конец сегодняшнего дня в миллисекундах

            for (int i = 0; i < schedule.length(); i++) {
                JSONObject program = schedule.getJSONObject(i);
                String title = program.getString("title");
                title = StringEscapeUtils.unescapeHtml4(title); // Убираем HTML-сущности
                String description = program.optString("description", "Нет описания");
                String dateStartString = program.getString("date_start");
                String dateStopString = program.getString("date_stop");

                // Парсим строки в даты
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                Date beginDate = inputFormat.parse(dateStartString);
                Date endDate = inputFormat.parse(dateStopString);

                // Конвертация времени в миллисекунды
                long beginTimeMillis = beginDate.getTime();

                // Проверяем, попадает ли программа в диапазон сегодняшнего дня, если это дневное расписание
                if (filterByToday) {
                    if (beginTimeMillis >= todayStartMillis && beginTimeMillis <= todayEndMillis) {
                        // Конвертация времени в человеко-читаемый формат
                        String formattedBeginTime = sdf.format(beginDate);
                        String formattedEndTime = sdf.format(endDate);

                        result.append("Название: ").append(title).append("\n");
                        result.append("Начало: ").append(formattedBeginTime).append("\n");
                        result.append("Конец: ").append(formattedEndTime).append("\n");
                        result.append("Возрастное ограничение: ").append(program.optString("age_restriction", "Нет данных")).append("\n");
                        result.append("Ссылка на программу: ").append(program.optString("program_link", "Нет ссылки")).append("\n\n");
                    }
                } else {
                    // Если не фильтруем по сегодняшней дате, добавляем все программы
                    String formattedBeginTime = sdf.format(beginDate);
                    String formattedEndTime = sdf.format(endDate);

                    result.append("Название: ").append(title).append("\n");
                    result.append("Начало: ").append(formattedBeginTime).append("\n");
                    result.append("Конец: ").append(formattedEndTime).append("\n");
                    result.append("Возрастное ограничение: ").append(program.optString("age_restriction", "Нет данных")).append("\n");
                    result.append("Ссылка на программу: ").append(program.optString("program_link", "Нет ссылки")).append("\n\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            Log.e("JSON_ERROR", e.toString());
            return "Failed to parse JSON";
        }
    }

    //Функция для отображения программ на неделю
    private void addProgramsToLayout(String json, boolean filterByToday) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray schedule = jsonObject.getJSONObject("data").getJSONArray("schedule");

            // Установка временной зоны для форматирования
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault()); // Устанавливаем временную зону

            long currentTimeMillis = System.currentTimeMillis(); // Получаем текущее время в миллисекундах

            for (int i = 0; i < schedule.length(); i++) {
                JSONObject program = schedule.getJSONObject(i);
                String title = Html.fromHtml(program.getString("title")).toString(); // Удаляем HTML-сущности
                String dateStartString = program.getString("date_start");

                // Парсим строки в даты
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                // Убираем установку временной зоны. Теперь inputFormat будет учитывать временную зону в строке даты
                Date beginDate = inputFormat.parse(dateStartString);
                long beginTimeMillis = beginDate.getTime();

                // Добавляем 3 часа к времени начала
                beginTimeMillis += 3 * 60 * 60 * 1000; // 3 часа в миллисекундах

                // Форматируем дату и время
                String formattedDate = outputFormat.format(new Date(beginTimeMillis)); // Используем новое время

                // Проверяем, попадает ли программа в диапазон сегодняшнего дня, если это дневное расписание
                if (filterByToday) {
                    long todayStartMillis = Calendar.getInstance().getTimeInMillis();
                    if (beginTimeMillis >= todayStartMillis) {
                        addProgramToLayout(layout, title, formattedDate, program.optString("program_link", "Нет ссылки"), program.optString("description", "Нет описания"), beginTimeMillis, currentTimeMillis);
                    }
                } else {
                    addProgramToLayout(layout, title, formattedDate, program.optString("program_link", "Нет ссылки"), program.optString("description", "Нет описания"), beginTimeMillis, currentTimeMillis);
                }
            }
        } catch (Exception e) {
            Log.e("JSON_ERROR", e.toString());
        }
    }

    //Метод для дневного расписания
    private void addProgramToLayout(LinearLayout layout, String title, String formattedDate, String link, String info, long beginTimeMillis, long currentTimeMillis) {
        // Создаем TextView для времени
        TextView tvTime = new TextView(this);
        SpannableString spannableTime = new SpannableString("Дата и время начала: " + formattedDate + "\n");
        spannableTime.setSpan(new StyleSpan(Typeface.BOLD), 0, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Время:"
        tvTime.setText(spannableTime);
        tvTime.setTextSize(18); // Увеличиваем размер шрифта
        tvTime.setGravity(Gravity.CENTER); // Выравнивание по центру
        layout.addView(tvTime);

        // Создаем TextView для названия
        TextView tvTitle = new TextView(this);
        SpannableString spannableTitle = new SpannableString("Название: " + title + "\n");
        spannableTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // Жирный шрифт для "Название:"
        tvTitle.setText(spannableTitle);
        tvTitle.setTextSize(18); // Увеличиваем размер шрифта
        tvTitle.setGravity(Gravity.CENTER); // Выравнивание по центру
        layout.addView(tvTitle);

        Program program = new Program("program_id", title, "category_name", link, "episode_image_url", formattedDate.split(" ")[0], formattedDate.split(" ")[1], 0, 0);

        // Проверяем, нужно ли показывать кнопку уведомления
        if (beginTimeMillis > currentTimeMillis) {
            Button btnNotify = new Button(this);
            btnNotify.setText("Уведомить о начале");
            btnNotify.setOnClickListener(v -> {
                createNotification(formattedDate, title, link, info); // Используем отформатированное время
                setReminder(program); // Устанавливаем напоминание
            });
            layout.addView(btnNotify);
        }
        layout.addView(new Space(this)); // Пространство между элементами
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminder(NTVView.Program program) {
        // Получаем текущее время
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date programDateTime = sdf.parse(program.date + " " + program.time);
            long triggerTime = programDateTime.getTime() - 5 * 60 * 1000; // 5 минут до начала

            // Устанавливаем AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("title", program.title);
            intent.putExtra("date", program.date);
            intent.putExtra("time", program.time);

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

    public void createNotification(String formattedDate, String title, String link, String info) {
        Intent intent = new Intent(this, NTVView.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell) // Иконка уведомления
                .setContentTitle(title)
                .setContentText("Передача начинается " + formattedDate) // Используем отформатированное время
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        // Получение менеджера уведомлений
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
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
        // Устанавливаем день недели на понедельник
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // Форматируем дату в нужный формат
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    class Program implements Serializable {
        public String id;
        public String title;
        public String category;
        public String episodeUrl;
        public String episodeImage;
        public String date; // Поле для даты
        public String time; // Поле для времени
        public int seasonNumber; // Номер сезона
        public int episodeNumber; // Номер эпизода

        public Program(String id, String title, String category, String episodeUrl, String episodeImage, String date, String time, int seasonNumber, int episodeNumber) {
            this.id = id;
            this.title = title; // Инициализация заголовка
            this.category = category; // Инициализация категории
            this.episodeUrl = episodeUrl; // Инициализация URL эпизода
            this.episodeImage = episodeImage; // Инициализация изображения эпизода
            this.date = date; // Инициализация даты
            this.time = time; // Инициализация времени
            this.seasonNumber = seasonNumber; // Инициализация номера сезона
            this.episodeNumber = episodeNumber; // Инициализация номера эпизода
        }
    }


}