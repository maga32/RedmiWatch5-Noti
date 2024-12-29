package org.duckdns.sunga.rw5noti;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.concurrent.ExecutionException;

public class NotificationListener extends NotificationListenerService {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE); // SharedPreferences 초기화
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

                    byte[] byteArrayPng = NotificationUtils.createNotificationImage(appIcon, appName, notificationTitle, notificationText);

                    Log.d("byteArrayPng ::: ", byteArrayPng.length + "kb");

                    ApiUtil apiUtil = new ApiUtil(this);
                    apiUtil.init();
                    apiUtil.sendMessage(byteArrayPng);

                } catch (PackageManager.NameNotFoundException e) {
                    Log.d("error!!", e.toString());
                }
            }
        }
    }

    // 알림이 사라지면 호출되는 메서드
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }
}

