package com.example.kurs;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.kurs.databinding.ActivityFeaturedChannelsBinding;

import java.util.ArrayList;
import java.util.List;

public class FeaturedChannels extends AppCompatActivity {

    private LinearLayout channelList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_featured_channels, menu);
        Log.d("Menu", "Menu created");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.channel_list) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        if (itemId == R.id.Featured_Channels) {
            return true;
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
        setContentView(R.layout.activity_featured_channels);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Установка Toolbar как ActionBar

        // Включаем кнопку "Назад"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Получаем список избранных каналов
        ArrayList<Channel> favoriteChannels = (ArrayList<Channel>) getIntent().getSerializableExtra("favorite_channels");
        channelList = findViewById(R.id.channel_list);

        if (favoriteChannels != null) {
            for (Channel channel : favoriteChannels) {
                addChannelToList(channel);
            }
        }
    }

    private void addChannelToList(Channel channel) {
        // Создаем новый элемент для канала
        View channelView = LayoutInflater.from(this).inflate(R.layout.item_channel, null);
        TextView channelTextView = channelView.findViewById(R.id.textView2);
        ImageView channelImageView = channelView.findViewById(R.id.channel_image);

        // Устанавливаем текст и изображение
        channelTextView.setText(channel.getName());
        channelImageView.setImageResource(channel.getImageResource());

        // Устанавливаем обработчик нажатия
        channelTextView.setOnClickListener(v -> {
            switch (channel.getName()) {
                case "Первый канал":
                    startActivity(new Intent(this, PervyView.class));
                    break;
                case "НТВ":
                    startActivity(new Intent(this, NTVView.class));
                    break;
                case "ТНТ":
                    startActivity(new Intent(this, TNTView.class));
                    break;
                case "Домашний":
                    startActivity(new Intent(this, STSView.class));
                    break;
                // Добавьте другие каналы по мере необходимости
            }
        });

        // Добавляем элемент в список
        channelList.addView(channelView);
    }
}