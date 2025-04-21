package com.code.mydiary;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import com.code.mydiary.R;

public class ImfDiary {
    public void showDiaryDialog(Context context) {
        Dialog dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.imf_diary, null);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9),
                    (int)(context.getResources().getDisplayMetrics().heightPixels * 0.9)
            );
        }
        dialog.show();
    }
}