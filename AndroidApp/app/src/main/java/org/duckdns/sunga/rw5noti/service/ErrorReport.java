package org.duckdns.sunga.rw5noti.service;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.duckdns.sunga.rw5noti.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ErrorReport implements Thread.UncaughtExceptionHandler {
    private Context context;

    public ErrorReport(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // 로그를 파일로 저장
        saveCrashLogToFile(e);

        // 앱 재시작 (선택 사항)
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        // 프로세스 종료
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private void saveCrashLogToFile(Throwable e) {
        String timestamp = new SimpleDateFormat("yyMMdd_HHmmss", java.util.Locale.getDefault()).format(new Date());
        String fileName ="crash_log_"+timestamp+".txt";

        // RW5Noti 폴더 경로 설정
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File rw5NotiFolder = new File(downloadsFolder, "RW5Noti");

        // RW5Noti 폴더가 없으면 생성
        if (!rw5NotiFolder.exists()) {
            if (!rw5NotiFolder.mkdirs()) {
                // 폴더 생성 실패 처리
                Log.e("CrashHandler", "Failed to create directory: " + rw5NotiFolder.getAbsolutePath());
                return;
            }
        }

        // 파일 저장
        File crashLogFile = new File(rw5NotiFolder, fileName);
        try (FileWriter writer = new FileWriter(crashLogFile)) {
            writer.write(Log.getStackTraceString(e));
            Toast.makeText(context, "Error!!: Log saved in Download/RW5Noti", Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
