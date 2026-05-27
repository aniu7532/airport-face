package com.arcsoft.arcfacedemo.widget.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.arcsoft.arcfacedemo.R;

import java.util.Calendar;

/**
 * 年月日时分秒选择对话框，基于 {@link NumberPicker}。
 * <pre>
 * DateTimePickerDialogHelper.show(context, null, calendar -> {
 *     long ms = calendar.getTimeInMillis();
 * });
 * </pre>
 */
public final class DateTimePickerDialogHelper {

    private static final int YEAR_MIN = 1970;
    private static final int YEAR_MAX = 2100;

    public interface OnDateTimePickedListener {
        void onPicked(Calendar calendar);
    }

    private DateTimePickerDialogHelper() {
    }

    /**
     * @param context  上下文（建议 Activity）
     * @param initial  初始时间，传 null 表示当前时间
     * @param listener 确定回调，不会为 null
     */
    public static void show(
            Context context,
            @Nullable Calendar initial,
            boolean withoutHMS,
            OnDateTimePickedListener listener) {
        show(context, initial, withoutHMS, false, listener);
    }

    /**
     * @param hourOnly 仅选日期+小时，分秒滚轮隐藏且结果分秒为 0
     */
    public static void show(
            Context context,
            @Nullable Calendar initial,
            boolean withoutHMS,
            boolean hourOnly,
            OnDateTimePickedListener listener) {
        if (listener == null) {
            return;
        }
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_datetime_picker, null);
        NumberPicker npYear = root.findViewById(R.id.np_year);
        NumberPicker npMonth = root.findViewById(R.id.np_month);
        NumberPicker npDay = root.findViewById(R.id.np_day);
        LinearLayout llHour = root.findViewById(R.id.ll_hour);
        NumberPicker npHour = root.findViewById(R.id.np_hour);
        LinearLayout llMinute = root.findViewById(R.id.ll_minute);
        NumberPicker npMinute = root.findViewById(R.id.np_minute);
        LinearLayout llSecond = root.findViewById(R.id.ll_second);
        NumberPicker npSecond = root.findViewById(R.id.np_second);
        Button btnCancel = root.findViewById(R.id.btn_datetime_cancel);
        Button btnOk = root.findViewById(R.id.btn_datetime_ok);

        if (withoutHMS) {
            llHour.setVisibility(View.GONE);
            llMinute.setVisibility(View.GONE);
            llSecond.setVisibility(View.GONE);
        } else if (hourOnly) {
            llHour.setVisibility(View.VISIBLE);
            llMinute.setVisibility(View.GONE);
            llSecond.setVisibility(View.GONE);
        }

        Calendar cal = initial != null ? (Calendar) initial.clone() : Calendar.getInstance();

        npYear.setMinValue(YEAR_MIN);
        npYear.setMaxValue(YEAR_MAX);
        npYear.setWrapSelectorWheel(false);
        npYear.setValue(clamp(cal.get(Calendar.YEAR), YEAR_MIN, YEAR_MAX));

        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setWrapSelectorWheel(true);
        npMonth.setFormatter(i -> String.format("%02d", i));
        npMonth.setValue(cal.get(Calendar.MONTH) + 1);

        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npHour.setWrapSelectorWheel(true);
        npHour.setFormatter(i -> String.format("%02d", i));
        npHour.setValue(cal.get(Calendar.HOUR_OF_DAY));

        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);
        npMinute.setWrapSelectorWheel(true);
        npMinute.setFormatter(i -> String.format("%02d", i));
        npMinute.setValue(cal.get(Calendar.MINUTE));

        npSecond.setMinValue(0);
        npSecond.setMaxValue(59);
        npSecond.setWrapSelectorWheel(true);
        npSecond.setFormatter(i -> String.format("%02d", i));
        npSecond.setValue(cal.get(Calendar.SECOND));

        Runnable refreshDay = () -> {
            int y = npYear.getValue();
            int m = npMonth.getValue();
            int maxDay = maxDayInMonth(y, m);
            int cur = npDay.getValue();
            npDay.setMinValue(1);
            npDay.setMaxValue(maxDay);
            if (cur > maxDay) {
                npDay.setValue(maxDay);
            } else if (cur < 1) {
                npDay.setValue(1);
            }
        };

        npDay.setMinValue(1);
        npDay.setMaxValue(maxDayInMonth(npYear.getValue(), npMonth.getValue()));
        npDay.setWrapSelectorWheel(true);
        npDay.setFormatter(i -> String.format("%02d", i));
        npDay.setValue(clamp(cal.get(Calendar.DAY_OF_MONTH), 1, npDay.getMaxValue()));

        NumberPicker.OnValueChangeListener ymListener = (picker, oldVal, newVal) -> refreshDay.run();
        npYear.setOnValueChangedListener(ymListener);
        npMonth.setOnValueChangedListener(ymListener);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOk.setOnClickListener(v -> {
            Calendar out = Calendar.getInstance();
            out.set(Calendar.YEAR, npYear.getValue());
            out.set(Calendar.MONTH, npMonth.getValue() - 1);
            out.set(Calendar.DAY_OF_MONTH, npDay.getValue());
            out.set(Calendar.HOUR_OF_DAY, npHour.getValue());
            if (hourOnly || withoutHMS) {
                out.set(Calendar.MINUTE, 0);
                out.set(Calendar.SECOND, 0);
            } else {
                out.set(Calendar.MINUTE, npMinute.getValue());
                out.set(Calendar.SECOND, npSecond.getValue());
            }
            out.set(Calendar.MILLISECOND, 0);
            listener.onPicked(out);
            dialog.dismiss();
        });

        dialog.show();
    }

    private static int maxDayInMonth(int year, int month1to12) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month1to12 - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }
}
