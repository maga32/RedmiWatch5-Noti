package org.duckdns.sunga.rw5noti.service;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationUtils {

    public static String createNotificationImage(Drawable appIcon, String appName, String title, String text) {
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd E HH:mm");
        String nowTime = sdf.format(currentDate);

        int width = 350;
        int height = 500;

        // 빈 캔버스 생성
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 배경 색상 설정 (투명)
        canvas.drawColor(Color.TRANSPARENT);

        // 텍스트 색상 설정 (흰색)
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40f);

        // 앱 아이콘 크기 설정
        int iconSize = 80;

        // 아이콘 처리
        Bitmap iconBitmap = null;

        // 기종에 따른 아이콘 처리 분류 BitmapDrawable / AdaptiveIconDrawable 처리
        if (appIcon instanceof BitmapDrawable) {
            iconBitmap = ((BitmapDrawable) appIcon).getBitmap();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appIcon instanceof AdaptiveIconDrawable) {
            Bitmap tmpBitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
            Canvas tmpCanvas = new Canvas(tmpBitmap);
            appIcon.setBounds(0, 0, tmpCanvas.getWidth(), tmpCanvas.getHeight());
            appIcon.draw(tmpCanvas);
            iconBitmap = tmpBitmap;
        }

        // 앱 아이콘을 캔버스에 그리기
        if (iconBitmap != null) {
            Bitmap iconScaled = Bitmap.createScaledBitmap(iconBitmap, iconSize, iconSize, false);

            Rect iconRect = new Rect(20, 20, 20 + iconSize, 20 + iconSize);
            canvas.drawBitmap(iconScaled, null, iconRect, paint);
        }

        // 앱 이름 텍스트 그리기
        canvas.drawText(appName, 120f, 50f, paint);

        paint.setTextSize(35f);
        canvas.drawText(nowTime, 120f, 90f, paint); // 시간 그리기
        drawTextWithCharacterBreak(canvas, title, 20f, 150f, paint, 2); // 알림 제목 그리기 (줄바꿈 처리)
        paint.setTextSize(30f);
        drawTextWithCharacterBreak(canvas, text, 20f, 220f, paint, 0); // 알림 내용 그리기 (줄바꿈 처리)

        return bitmapToBase64(bitmap);
    }

    // 글자 단위로 줄바꿈하여 텍스트 그리기
    private static void drawTextWithCharacterBreak(Canvas canvas, String text, float x, float y, Paint paint, int maxLine) {
        float maxWidth = 300f; // 텍스트 영역 너비
        float currentX = x;
        float currentY = y;
        int lineCount = 0; // 그린 라인 수

        // 한 글자씩 그리기
        String line = "";
        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));
            float lineWidth = paint.measureText(line + character);

            // 텍스트가 영역을 초과하면 줄바꿈
            if (lineWidth > maxWidth) {
                canvas.drawText(line, currentX, currentY, paint);
                currentY += paint.getTextSize(); // 줄바꿈 처리
                line = character; // 새로운 줄 시작
                lineCount++; // 라인 카운트 증가

                // 라인 수가 maxLine 초과시 그리기 종료
                if (maxLine != 0 && lineCount >= maxLine) break;
            } else {
                line += character;
            }
        }

        // maxLine 이하인경우 마지막 남은 텍스트 그리기
        if (!line.isEmpty() && (maxLine == 0 || lineCount < maxLine)) {
            canvas.drawText(line, currentX, currentY, paint);
        }
    }

    // Bitmap을 PNG 형식으로 압축하여 Base64로 변환
    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();

            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
