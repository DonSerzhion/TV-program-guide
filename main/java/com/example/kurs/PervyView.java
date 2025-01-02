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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.widget.Button;
import android.widget.Space;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.kurs.databinding.ActivityPervyViewBinding;
import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;


public class PervyView extends AppCompatActivity {
    private static final String BASE_URL_DAY = "https://stream.1tv.ru/api/schedule.json";
    private static final String BASE_URL_WEEK = "https://api.1tv.ru/www/v1/schedule/air_positions?orbit=1&date=";
    private static final String CHANNEL_ID = "my_channel_id";
    private TextView t1;
    private LinearLayout layout; // Добавляем LinearLayout для отображения программ
    private List<Channel> favoriteChannels;
    private String savedText; // Для сохранения текста из TextView
    private List<String> savedPrograms; // Для сохранения программ
    private List<Button> savedButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pervy_view);
        Intent intent = getIntent();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Установка Toolbar как ActionBar

        // Включаем кнопку "Назад"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Инициализируем TextView и LinearLayout
        t1 = findViewById(R.id.textViewSchedule1);
        layout = findViewById(R.id.programs_layout);
        // Загружаем расписание на день по умолчанию
        createNotificationChannel(); // Инициализация канала уведомлений
        // Загружаем избранные каналы
        loadFavorites();

        // Восстанавливаем состояние, если оно существует
        if (savedInstanceState != null) {
            savedText = savedInstanceState.getString("savedText");
            savedPrograms = savedInstanceState.getStringArrayList("savedPrograms");
            if (savedText != null) {
                t1.setText(savedText);
                t1.setGravity(Gravity.CENTER); // Выравнивание по центру
                t1.setTextSize(18); // Увеличиваем размер шрифта
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
        outState.putString("savedText", t1.getText().toString());
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
        getMenuInflater().inflate(R.menu.menu_pervy_view, menu);
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

    public void loadSchedule1DayP(View view) {
        loadSchedule(BASE_URL_DAY, true);
    }

    public void loadScheduleWeekP(View view) {
        String urlString = BASE_URL_WEEK + getCurrentWeekStartDate() + "&view=7";
        loadSchedule(urlString, false);
    }

    private void loadSchedule(String urlString, boolean isDaySchedule) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                List<Program> programs = isDaySchedule ? parseJsonDay(result.toString()) : parseJsonWeek(result.toString());
                runOnUiThread(() -> {
                    if (isDaySchedule) {
                        displayProgramsByDay(programs);
                    } else {
                        try {
                            displayProgramsByWeek(programs);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> t1.setText("Error: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void displayProgramsByDay(List<Program> programs) {
        layout.removeAllViews(); // Очищаем предыдущие элементы
        // Получаем текущую дату
        String currentDate = getCurrentDate();
        TextView dateTextView = new TextView(this);
        dateTextView.setText("Дата: " + currentDate);
        dateTextView.setGravity(Gravity.CENTER); // Выравнивание по центру
        dateTextView.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(dateTextView); // Добавляем TextView с датой
        for (Program program : programs) {
            addProgramToLayout(layout, program);
        }
    }

    private void displayProgramsByWeek(List<Program> programs) throws ParseException {
        layout.removeAllViews(); // Очищаем предыдущие элементы
        // Заголовок для недели
        TextView weekTitleTextView = new TextView(this);
        weekTitleTextView.setText("Расписание на неделю:");
        weekTitleTextView.setGravity(Gravity.CENTER); // Выравнивание по центру
        weekTitleTextView.setTextSize(18); // Увеличиваем размер шрифта
        weekTitleTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        layout.addView(weekTitleTextView); // Добавляем заголовок для недели

        // Получаем дату начала недели
        String weekStartDate = getCurrentWeekStartDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(weekStartDate));

        // Добавляем даты для каждого дня недели
        for (int i = 0; i < 7; i++) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            TextView dateTextLabel = new TextView(this);
            dateTextLabel.setText("Дата: ");
            dateTextLabel.setGravity(Gravity.CENTER); // Выравнивание по центру
            dateTextLabel.setTextSize(18); // Увеличиваем размер шрифта
            dateTextLabel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            layout.addView(dateTextLabel);

            TextView dateTextView = new TextView(this);
            dateTextView.setText(currentDate);
            dateTextView.setGravity(Gravity.CENTER); // Выравнивание по центру
            dateTextView.setTextSize(18); // Увеличиваем размер шрифта
            layout.addView(dateTextView); // Добавляем TextView с датой

            // Фильтруем и отображаем программы для текущего дня
            List<Program> dailyPrograms = getProgramsForDate(programs, currentDate);
            for (Program program : dailyPrograms) {
                addProgramToLayout(layout, program);
            }
            // Переходим к следующему дню
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    // Метод для получения программ для определенной даты
    private List<Program> getProgramsForDate(List<Program> programs, String date) {
        List<Program> dailyPrograms = new ArrayList<>();
        for (Program program : programs) {
            if (program.getDate().equals(date)) { // Предполагается, что у класса Program есть метод getDate()
                dailyPrograms.add(program);
            }
        }
        return dailyPrograms;
    }

    private void addProgramToLayout(LinearLayout layout, Program program) {
        // Создаем TextView для надписи "Время"
        TextView tvTimeLabel = new TextView(this);
        tvTimeLabel.setText("Время:");
        tvTimeLabel.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTimeLabel.setTypeface(null, Typeface.BOLD); // Делаем текст жирным
        tvTimeLabel.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTimeLabel);

        // Создаем TextView для времени программы
        TextView tvTime = new TextView(this);
        tvTime.setText(program.time); // Только время
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

        // Создаем TextView для названия программы
        TextView tvTitle = new TextView(this);
        tvTitle.setText(program.title); // Только название
        tvTitle.setGravity(Gravity.CENTER); // Выравнивание по центру
        tvTitle.setTextSize(18); // Увеличиваем размер шрифта
        layout.addView(tvTitle);

        // Проверяем, начинается ли программа с текущего времени
        long currentTimeMillis = System.currentTimeMillis() / 1000; // Время в секундах
        long beginTime = program.getBeginTime();

        if (beginTime >= currentTimeMillis) {
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
    private void setReminder(PervyView.Program program) {
        // Получаем текущее время
        SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat outputSdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()); // Формат для вывода
        try {
            // Парсим дату из формата "год-месяц-день"
            Date programDateTime = inputSdf.parse(program.date + " " + program.time);

            // Преобразуем дату в строку в формате "день-месяц-год"
            String formattedDate = outputSdf.format(programDateTime);

            long triggerTime = programDateTime.getTime() - 5 * 60 * 1000; // 5 минут до начала

            // Устанавливаем AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("title", program.title);
            intent.putExtra("date", formattedDate.split(" ")[0]); // Передаем только дату в новом формате
            intent.putExtra("time", formattedDate.split(" ")[1]); // Передаем только время

            // Создается PendingIntent, который будет отправлен AlarmManager
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) { //Можно ли поставить точный будильник
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent); //Установка
                } else {
                    // Иначе направляем пользователя к настройкам, чтобы дать разрешение
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

    //Создание и отображение уведомления
    public void createNotification(Context context, String date, String time, String title, String link, String info) {
        Intent openIntent = new Intent(context, PervyView.class);
        PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bell)
                .setContentTitle(title)
                .setContentText("Передача начинается " + date + " в " + time)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = (int) System.currentTimeMillis();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }

    //Создание канала уведомлений, который необходим для отправки уведомлений на устройствах с Android 8.0 и выше
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Настройка канала
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //Создание канала с описанием
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            //Регистрация канала уведомлений
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getCurrentWeekStartDate() {
        Calendar calendar = Calendar.getInstance();
        // Устанавливаем день недели на понедельник
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private List<Program> parseJsonDay(String json) {
        List<Program> programs = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject channel = jsonObject.getJSONObject("channel");
            JSONArray programArray = channel.getJSONObject("schedule").getJSONArray("program");

            // Получаем текущее время и дату
            long currentTimeMillis = System.currentTimeMillis() / 1000; // Время в секундах
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Получаем дату завтрашнего дня
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            for (int i = 0; i < programArray.length(); i++) {
                JSONObject program = programArray.getJSONObject(i);
                String title = program.getString("title");

                if (program.has("begin") && program.has("end")) {
                    long beginTime = program.getLong("begin");
                    String programDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(beginTime * 1000));

                    // Проверяем, попадает ли программа в текущий день и начинается ли она с текущего времени, и не является ли она программой завтрашнего дня
                    if (programDate.equals(currentDate) && beginTime >= currentTimeMillis) {
                        programs.add(new Program(
                                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(beginTime * 1000)),
                                title,
                                "",
                                program.optString("lead", "Нет описания"),
                                currentDate
                        ));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return programs; // Возвращаем список программ
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private List<Program> parseJsonWeek(String json) {
        List<Program> programs = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject data = jsonObject.getJSONObject("data");
            // Перебираем дни недели
            Iterator<String> keys = data.keys(); // Получаем итератор ключей
            while (keys.hasNext()) {
                String date = keys.next(); // Получаем следующий ключ (дату)
                JSONArray programArray = data.getJSONArray(date);
                // Перебираем программы
                for (int i = 0; i < programArray.length(); i++) {
                    JSONObject program = programArray.getJSONObject(i);
                    String title = program.getString("title");
                    String description = program.optString("lead", "Нет описания");
                    long beginTime = program.getLong("datetime_start"); // Время начала в UNIX формате
                    // Добавляем программу в список
                    programs.add(new Program(
                            new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(beginTime * 1000)),
                            title,
                            "",
                            description,
                            date // Добавляем дату программы
                    ));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return programs; // Возвращаем список программ
    }

    class Program {
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
            this.date = date; // Инициализируем поле даты
        }
        public String getDate(){
            return this.date;
        }
        //Получение времени начала передачи
        public long getBeginTime() {
            // Преобразование времени в формате HH:mm обратно в UNIX timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date date = sdf.parse(this.time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.YEAR, Integer.parseInt(this.date.split("-")[0]));
                calendar.set(Calendar.MONTH, Integer.parseInt(this.date.split("-")[1]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(this.date.split("-")[2]));
                return calendar.getTimeInMillis() / 1000; // Время в секундах
            } catch (ParseException e) {
                e.printStackTrace();
                return 0; // Возвращаем 0 в случае ошибки
            }
        }
    }
}
