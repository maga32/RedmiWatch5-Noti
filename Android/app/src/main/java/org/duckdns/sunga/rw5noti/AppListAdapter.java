package org.duckdns.sunga.rw5noti;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private List<ApplicationInfo> appList;
    private OnAppSelected onAppSelected;
    private Set<String> selectedApps;

    public interface OnAppSelected {
        void onAppSelected(ApplicationInfo appInfo, boolean isSelected);
    }

    public AppListAdapter(List<ApplicationInfo> appList, OnAppSelected onAppSelected) {
        this.appList = appList;
        this.onAppSelected = onAppSelected;
        this.selectedApps = new HashSet<>();
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        ApplicationInfo app = appList.get(position);
        holder.appName.setText(app.loadLabel(holder.itemView.getContext().getPackageManager()));
        holder.checkBox.setChecked(selectedApps.contains(app.packageName));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedApps.add(app.packageName);
            } else {
                selectedApps.remove(app.packageName);
            }
            onAppSelected.onAppSelected(app, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        TextView appName;
        CheckBox checkBox;

        public AppViewHolder(View view) {
            super(view);
            appName = view.findViewById(R.id.appName);
            checkBox = view.findViewById(R.id.checkBox);
        }
    }
}
