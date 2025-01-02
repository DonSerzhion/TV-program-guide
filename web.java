package com.example.kurs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;

import com.example.kurs.databinding.ActivityPervyViewBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PervyView extends AppCompatActivity {
   private ActivityPervyViewBinding binding;
      @SuppressLint("SetJavaScriptEnabled")
      @Override
      protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             setContentView(R.layout.activity_pervy_view);

             WebView browser = findViewById(R.id.Pervy);
             WebSettings webSettings = browser.getSettings();
             webSettings.setJavaScriptEnabled(true);
             // Текущее время
             Date currentDate = new Date();
             // Форматирование времени как "гггг-мм-дд"
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
             String dateText = dateFormat.format(currentDate);

             String url = "https://www.1tv.ru/schedule/" + dateText;
             // Установка WebViewClient для обработки страниц и ошибок
             browser.setWebViewClient(new WebViewClient() {
             @Override
             public void onPageFinished(WebView view, String url) {
             super.onPageFinished(view, url);
             }
             @Override
             public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
             super.onReceivedError(view, request, error);
             Snackbar.make(view, "Ошибка загрузки страницы", Snackbar.LENGTH_LONG).show();
             }
             });
             // Загрузка URL
             browser.loadUrl(url);
             }
             }
             