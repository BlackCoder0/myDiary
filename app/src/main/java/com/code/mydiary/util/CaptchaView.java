package com.code.mydiary.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class CaptchaView extends View {
    private String captchaCode = "";
    private Paint paint = new Paint();
    private Random random = new Random();

    public CaptchaView(Context context) {
        super(context);
        generateCode();
    }

    public CaptchaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        generateCode();
    }

    public void generateCode() {
        captchaCode = "";
        for (int i = 0; i < 4; i++) {
            captchaCode += (char) (random.nextInt(26) + 65); // 生成大写字母
        }
        invalidate();
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.BLACK);
        paint.setTextSize(60);
        canvas.drawColor(Color.LTGRAY);
        canvas.drawText(captchaCode, 40, 80, paint);

        // 干扰线
        for (int i = 0; i < 6; i++) {
            paint.setColor(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            int startX = random.nextInt(getWidth());
            int startY = random.nextInt(getHeight());
            int stopX = random.nextInt(getWidth());
            int stopY = random.nextInt(getHeight());
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }
}