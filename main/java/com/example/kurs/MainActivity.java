package com.example.kurs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.kurs.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private List<Channel> channels;
    private List<Channel> favoriteChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация списка каналов
        channels = new ArrayList<>();
        channels.add(new Channel("Первый канал", R.drawable.__5_svg));
        channels.add(new Channel("Домашний", R.drawable.logos_d_1));
        channels.add(new Channel("НТВ", R.drawable.ntv_logo_2003_svg));
        channels.add(new Channel("ТНТ", R.drawable.tnt));

        favoriteChannels = new ArrayList<>();

//        // Очищаем список избранных каналов при запуске приложения
//        clearFavorites();

        // Загрузка избранных каналов
        loadFavorites();

        Intent intent = getIntent();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Установка Toolbar как ActionBar
    }

//    private void clearFavorites() {
//        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear(); // Очищаем все данные
//        editor.apply(); // Применяем изменения
//    }

    private void loadFavorites() {
        //Доступ к области с ключом "favorites"
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        //Получаем набор строк "favorite_cannels"
        Set<String> favoriteSet = sharedPreferences.getStringSet("favorite_channels", new HashSet<>());
        for (Channel channel : channels) { //Добавление избранных каналов в favoriteChannels для отправки в Избранные
            if (favoriteSet.contains(channel.getName())) {
                channel.setFavorite(true);
                favoriteChannels.add(channel);
            }
        }
    }

    private void saveFavorites() {
        SharedPreferences sharedPreferences = getSharedPreferences("favorites", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();// Редактор для изменения области
        Set<String> favoriteSet = new HashSet<>();
        for (Channel channel : favoriteChannels) {
            if (channel.isFavorite()) {
                favoriteSet.add(channel.getName());
            }
        }
        editor.putStringSet("favorite_channels", favoriteSet); //сохранение набора строк в области
        editor.apply(); //асинхронные изменения
    }

    // Метод для добавления канала в избранное
    private void toggleFavorite(Channel channel) {
        if (channel.isFavorite()) {
            channel.setFavorite(false);
            favoriteChannels.remove(channel);
        } else {
            channel.setFavorite(true);
            favoriteChannels.add(channel);
        }
        saveFavorites(); // Сохраняем изменения
    }

    // Метод для получения избранных каналов
    public List<Channel> getFavoriteChannels() {
        return favoriteChannels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d("Menu", "Menu created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.channel_list) {
            return true;
        }
        if (itemId == R.id.Featured_Channels) {
            Intent intent = new Intent(this, FeaturedChannels.class);
            ArrayList<Channel> favoriteChannels = new ArrayList<>(getFavoriteChannels());
            intent.putExtra("favorite_channels", favoriteChannels);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addChannelToFavorites(View view) {
        //родительский LinearLayout, в котором находится кнопка
        LinearLayout parentLayout = (LinearLayout) view.getParent();

        //id кнопки, чтобы определить, какой канал добавляется в избранное
        int buttonId = view.getId();
        TextView channelTextView = null;

        //if-else для определения соответствующего TextView
        if (buttonId == R.id.button_favorite_1) {
            channelTextView = parentLayout.findViewById(R.id.textView2);
        } else if (buttonId == R.id.button_favorite_2) {
            channelTextView = parentLayout.findViewById(R.id.textView3);
        } else if (buttonId == R.id.button_favorite_3) {
            channelTextView = parentLayout.findViewById(R.id.textView4);
        } else if (buttonId == R.id.button_favorite_4) {
            channelTextView = parentLayout.findViewById(R.id.textView5);
        }

        if (channelTextView != null) {
            String channelName = channelTextView.getText().toString();

            // Логика добавления в избранное
            for (Channel channel : channels) {
                if (channel.getName().equals(channelName)) {
                    toggleFavorite(channel);
                    Toast.makeText(this, channel.isFavorite() ? "Добавлено в избранное" : "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    public void PervyView(View view) {
        Intent intent1 = new Intent(this, PervyView.class);
        startActivity(intent1);
    }

    public void NTVView(View view) {
        Intent intent2 = new Intent(this, NTVView.class);
        startActivity(intent2);
    }

    public void TNTView(View view) {
        Intent intent3 = new Intent(this, TNTView.class);
        startActivity(intent3);
    }

    public void STSView(View view) {
        Intent intent4 = new Intent(this, STSView.class);
        startActivity(intent4);
    }
}

