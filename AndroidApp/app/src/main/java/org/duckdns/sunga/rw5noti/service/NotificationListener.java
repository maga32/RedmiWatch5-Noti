package org.duckdns.sunga.rw5noti.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.node.NodeApi;

import org.duckdns.sunga.rw5noti.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class NotificationListener extends NotificationListenerService {

    private SharedPreferences sharedPreferences;
    private NodeApi nodeApi;
    private MessageApi messageApi;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE); // SharedPreferences 초기화

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "CHANNEL_ID",
                "Foreground Service",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "CHANNEL_ID")
                    .setContentTitle("Notification Listener")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .build();
        }

        startForeground(1, notification);
    }

    // 알림이 오면 호출되는 메서드
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null) {
            String packageName = sbn.getPackageName();
            boolean isChecked = sharedPreferences.getBoolean(packageName, false); // 체크된 앱 확인

            // 체크된 앱의 알림만 처리
            if (isChecked) {
                String notificationTitle = sbn.getNotification().extras.getString("android.title", "");
                String notificationText = sbn.getNotification().extras.getString("android.text", "");

                if (notificationText.isEmpty()) return;

                PackageManager pm = getPackageManager();
                try {
                    android.content.pm.ApplicationInfo app = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                    String appName = pm.getApplicationLabel(app).toString();
                    android.graphics.drawable.Drawable appIcon = app.loadIcon(pm);

                    String pngString = NotificationUtils.createNotificationImage(appIcon, appName, notificationTitle, notificationText);
                    ArrayList<String> chunkList = new ArrayList<>();
                    int chunkSize = 20000;
                    String timeStamp = String.valueOf(System.currentTimeMillis());

                    for (int start = 0; start < pngString.length(); start += chunkSize) {
                        int end = Math.min(start + chunkSize, pngString.length());
                        // 데이터구분(1) 일반은 d 끝은 e / timeStamp파일명(13) / 데이터(최대20000)
                        String chunk = (end == pngString.length() ? "e" : "d") + timeStamp + pngString.substring(start, end);
                        chunkList.add(chunk);
                    }

                    nodeApi = Wearable.getNodeApi(getApplicationContext());
                    messageApi = Wearable.getMessageApi(getApplicationContext());

                    nodeApi.getConnectedNodes()
                        .addOnSuccessListener(nodes -> {
                            if (!nodes.isEmpty()) {
                                nodeApi.launchWearApp(nodes.get(0).id, "/index")
                                    .addOnSuccessListener(data -> {

                                        for(String chuck : chunkList) {
                                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                messageApi.sendMessage(nodes.get(0).id, chuck.getBytes(StandardCharsets.UTF_8))
                                                    .addOnSuccessListener(aVoid -> Log.d("realSend", "알림전송 성공"))
                                                    .addOnFailureListener(e -> Log.d("realSend", "알림전송 실패"));
                                            }, 1500);
                                        }

                                    });

                            }
                        });

                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("error!!", e.toString());
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // 서비스가 종료된 경우 자동으로 다시 시작
    }

    // 알림이 사라지면 호출되는 메서드
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }
}

