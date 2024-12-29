package org.duckdns.sunga.rw5noti;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.ByteArrayOutputStream;

public class NotificationUtils {

    public static byte[] createNotificationImage(Drawable appIcon, String appName, String title, String text) {
        int width = 350;
        int height = 500;

        // 빈 캔버스 생성
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 배경 색상 설정 (투명)
        canvas.drawColor(Color.TRANSPARENT);

        // 텍스트 색상 설정 (흰색)
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);

        // 앱 아이콘 크기 설정
        int iconSize = 80;
        Bitmap iconBitmap = ((BitmapDrawable) appIcon).getBitmap();
        Bitmap iconScaled = Bitmap.createScaledBitmap(iconBitmap, iconSize, iconSize, false);

        // 앱 아이콘을 캔버스에 그리기
        Rect iconRect = new Rect(20, 20, 20 + iconSize, 20 + iconSize);
        canvas.drawBitmap(iconScaled, null, iconRect, paint);

        // 앱 이름 텍스트 그리기
        canvas.drawText(appName, 120f, 80f, paint);

        paint.setTextSize(35f);
        drawTextWithCharacterBreak(canvas, title, 20f, 150f, paint); // 알림 제목 그리기 (줄바꿈 처리)
        paint.setTextSize(30f);
        drawTextWithCharacterBreak(canvas, text, 20f, 220f, paint); // 알림 내용 그리기 (줄바꿈 처리)

        return bitmapToByteArray(bitmap);
    }

    // 글자 단위로 줄바꿈하여 텍스트 그리기
    private static void drawTextWithCharacterBreak(Canvas canvas, String text, float x, float y, Paint paint) {
        float maxWidth = 300f; // 텍스트 영역 너비
        float currentX = x;
        float currentY = y;

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
            } else {
                line += character;
            }
        }

        // 마지막 남은 텍스트 그리기
        if (!line.isEmpty()) {
            canvas.drawText(line, currentX, currentY, paint);
        }
    }

    // Bitmap을 PNG 형식으로 압축하여 OutputStream에 저장
    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return outputStream.toByteArray();
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
