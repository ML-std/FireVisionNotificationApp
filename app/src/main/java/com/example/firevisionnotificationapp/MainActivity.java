package com.example.firevisionnotificationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    Button notifyButton;
    TextView dataText;
    ImageView dataImage;
    private OkHttpClient client;
    final class EchoWebSocketListener extends WebSocketListener {
        private  static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull okhttp3.Response response) {
            //webSocket.send("ssls");
            //webSocket.close(NORMAL_CLOSURE_STATUS,"sleep");
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            //onTextChange("receiving" + text);
            createNotification(text);
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            byte[] byteArray = bytes.toByteArray();
            onImageChange();

        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            onTextChange("closing" + code + "/" + reason);
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable okhttp3.Response response) {
            onTextChange("error: " + t.getMessage());
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notifyButton = findViewById(R.id.notifyButton);
        dataText = findViewById(R.id.dataText);
        dataImage = findViewById(R.id.dataImage);
        client = new OkHttpClient();
        start();
        onImageChange();


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("New Fire", "Fire Alerrt", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager =  getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    public void onNotified(View view){
        createNotification("empty");
    }

    public void  createNotification(String text){

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        System.out.println("new notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "New Fire");
        builder.setContentTitle("Yeni Yangın Var!");

        builder.setContentText(text);
        onTextChange(text);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
        managerCompat.notify(1, builder.build());

    }
    private void onTextChange(String text) {runOnUiThread(new Runnable() {
        @Override
        public void run() {
            dataText.setText(text);
        }
    });
    }

    private void onImageChange(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                dataImage.setImageResource(R.mipmap.fire_fision_logo_round);
            }
        });
    }
    private void start(){
        okhttp3.Request request = new okhttp3.Request.Builder().url("wss://demo.piesocket.com/v3/channel_1?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws =  client.newWebSocket(request,listener);
    }
    public void onClose(View view){
        finish();
    }
}