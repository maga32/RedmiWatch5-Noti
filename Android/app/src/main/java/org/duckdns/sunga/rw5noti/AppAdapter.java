package org.duckdns.sunga.rw5noti;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

    private List<AppInfo> apps;
    private SharedPreferences sharedPreferences;
    private OnAppCheckedChange onAppCheckedChange;

    public interface OnAppCheckedChange {
        void onAppCheckedChange(AppInfo appInfo, boolean isChecked);
    }

    public AppAdapter(List<AppInfo> apps, SharedPreferences sharedPreferences, OnAppCheckedChange onAppCheckedChange) {
        this.apps = apps;
        this.sharedPreferences = sharedPreferences;
        this.onAppCheckedChange = onAppCheckedChange;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        AppInfo app = apps.get(position);
        holder.appName.setText(app.getAppName());
        holder.appIcon.setImageDrawable(app.getIcon());

        // 체크박스 상태 불러오기
        holder.checkbox.setChecked(loadAppCheckedState(app.getPackageName()));

        // 체크박스 상태 변경 처리
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onAppCheckedChange.onAppCheckedChange(app, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    // SharedPreferences에서 체크박스 상태를 불러오는 함수
    private boolean loadAppCheckedState(String packageName) {
        return sharedPreferences.getBoolean(packageName, false); // 기본값은 false
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;
        CheckBox checkbox;

        public AppViewHolder(View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            checkbox = itemView.findViewById(R.id.checkBox);
        }
    }
}
