package org.duckdns.sunga.rw5noti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 알림 리스너 서비스 권한 요청
        if (!permissionGranted()) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE); // SharedPreferences 초기화

        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 앱 리스트 가져오기
        List<AppInfo> apps = getInstalledApps();

        // Adapter 설정
        recyclerView.setAdapter(new AppAdapter(apps, sharedPreferences, (appInfo, isChecked) -> {
            // 체크박스 상태 변경 시 처리
            saveAppCheckedState(appInfo.getPackageName(), isChecked); // 체크 상태 저장
        }));
    }

    private List<AppInfo> getInstalledApps() {
        PackageManager pm = getPackageManager();
        List<AppInfo> apps = new ArrayList<>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String appName = packageInfo.loadLabel(pm).toString();
                String packageName = packageInfo.packageName;
                android.graphics.drawable.Drawable icon = packageInfo.loadIcon(pm);
                apps.add(new AppInfo(appName, packageName, icon));
            }
        }
        // 앱 이름 기준으로 오름차순 정렬
        Collections.sort(apps, (app1, app2) -> app1.getAppName().compareTo(app2.getAppName()));
        return apps;
    }

    private void saveAppCheckedState(String packageName, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(packageName, isChecked);  // 패키지 이름을 키로, 체크 상태를 값으로 저장
        editor.apply();
    }

    // NotificationListenerService가 활성화되어 있는지 확인
    private boolean permissionGranted() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(this);
        return sets != null && sets.contains(getPackageName());
    }
}
