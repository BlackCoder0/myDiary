package com.code.mydiary.util;

import android.content.Context;
import android.util.Log; // 保留 Log
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan; // 引入 DotSpan
import java.util.Set;

// 这个类现在用来画黄点，虽然名字还是 YellowDotDecorator
public class YellowDotDecorator implements DayViewDecorator {
    private static final String TAG = "YellowDotDecorator"; // 修改日志标签以反映新功能
    private final Set<CalendarDay> dates;
    private static final int YELLOW_COLOR = 0xFFFFEB3B; // 亮黄色 (Material Yellow 500)

    public YellowDotDecorator(Context context, Set<CalendarDay> dates) {
        // Context 不再需要，因为 DotSpan 不依赖外部资源
        this.dates = dates;
        Log.d(TAG, "创建 YellowDotDecorator，日期数量: " + (dates == null ? 0 : dates.size()));
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        boolean should = dates != null && dates.contains(day);
        if (should) {
            Log.d(TAG, "shouldDecorate: " + day.toString() + " -> true (Yellow Dot)");
        }
        return should;
    }

    @Override
    public void decorate(DayViewFacade view) {
        Log.d(TAG, "decorate: 开始为某个日期添加 Yellow DotSpan");
        try {
            // 添加亮黄色的小圆点，半径为 8 (可以调整)
            view.addSpan(new DotSpan(8, YELLOW_COLOR));
            Log.d(TAG, "decorate: Yellow DotSpan 添加成功");
        } catch (Exception e) {
            Log.e(TAG, "decorate: 添加 Yellow DotSpan 时出错", e);
        }
    }
}