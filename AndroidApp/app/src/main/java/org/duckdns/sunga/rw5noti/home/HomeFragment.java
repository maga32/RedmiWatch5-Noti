package org.duckdns.sunga.rw5noti.home;

import static org.duckdns.sunga.rw5noti.service.NotificationUtils.createNotificationImage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.AuthApi;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.message.MessageApi;
import com.xiaomi.xms.wearable.node.Node;
import com.xiaomi.xms.wearable.node.NodeApi;

import org.duckdns.sunga.rw5noti.databinding.FragmentMessageBinding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private MessageViewModel dashboardViewModel;
    private SharedPreferences sharedPreferences;
    private FragmentMessageBinding binding;
    private NodeApi nodeApi;
    private AuthApi authApi;
    private MessageApi messageApi;
    private Node curNode;
    private Drawable testAppIcon;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        nodeApi = Wearable.getNodeApi(context.getApplicationContext());
        messageApi = Wearable.getMessageApi(context.getApplicationContext());
        authApi = Wearable.getAuthApi(context.getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = requireContext();

        dashboardViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        binding = FragmentMessageBinding.inflate(inflater, container, false);

        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE); // SharedPreferences 초기화

        // RecyclerView 설정
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 앱 리스트 가져오기
        List<AppInfo> apps = getInstalledApps();

        // Adapter 설정
        recyclerView.setAdapter(new AppAdapter(apps, sharedPreferences, (appInfo, isChecked) -> {
            // 체크박스 상태 변경 시 처리
            saveAppCheckedState(appInfo.getPackageName(), isChecked); // 체크 상태 저장
        }));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getConnectedNodes();

        binding.sendMessage.setOnClickListener(v -> {
            if (curNode != null) {
                // 테스트용 알림 만들기
                String imgString = createNotificationImage(testAppIcon , "테스트앱", "타이틀", "알림 테스트입니다.");
                System.out.println(imgString);

                messageApi.sendMessage(curNode.id, imgString.getBytes(StandardCharsets.UTF_8))
                        .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "알림전송 성공", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "알림전송 실패 : " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        binding.requestPermissions.setOnClickListener(v -> {
            if (curNode != null) {
                authApi.requestPermission(curNode.id, Permission.DEVICE_MANAGER)
                        .addOnSuccessListener(permissions -> {
                            StringBuilder permissionGrantedList = new StringBuilder();
                            for (Permission permission : permissions) {
                                permissionGrantedList.append(permission.getName()).append(" ");
                            }
                            Toast.makeText(getActivity(), "권한획득 : " + permissionGrantedList.toString(), Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "권한요청 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void getConnectedNodes() {
        nodeApi.getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (nodes.size() > 0) {
                        curNode = nodes.get(0);
                        binding.status.setText("연결된 워치 : " + curNode.toString());
                    }
                })
                .addOnFailureListener(e -> binding.status.setText("워치 연결 실패 : " + e.getMessage()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // NotificationListenerService가 활성화되어 있는지 확인
    private boolean permissionGranted() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(requireContext());
        return sets != null && sets.contains(requireContext().getPackageName());
    }

    private void saveAppCheckedState(String packageName, boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(packageName, isChecked);  // 패키지 이름을 키로, 체크 상태를 값으로 저장
        editor.apply();
    }

    private List<AppInfo> getInstalledApps() {
        PackageManager pm = requireContext().getPackageManager();
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

        testAppIcon = apps.get(0).getIcon();

        return apps;
    }

}
