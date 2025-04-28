package com.code.mydiary.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.code.mydiary.R;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.HashSet;
import java.util.Set;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

public class BlueDotDecorator implements DayViewDecorator {
    private final Set<CalendarDay> dates;

    public BlueDotDecorator(Context context, Set<CalendarDay> dates) {
        this.dates = dates;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // 只在数字下方显示一个小蓝点
        view.addSpan(new DotSpan(8, 0xFF2196F3)); // 8为半径，颜色为蓝色
    }
}