package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.TimeControl;
import com.arcsoft.arcfacedemo.util.log.ALog;

import java.util.Calendar;

/**
 * 时间控制工具类
 * 用于检查通行证是否在允许的通行时间段内
 */
public class TimeControlUtil {

    /**
     * 检查时间控制
     * @param longTermPass 长期通行证
     * @return TimeControlResult 检查结果，包含是否允许通行和错误消息
     */
    public static TimeControlResult checkTimeControl(LongTermPass longTermPass) {
        TimeControl[] timeControlArray = longTermPass.getTimeControl();
        
        // 如果timeControl为空或null，不限制通行时间，允许通行
        if (timeControlArray == null || timeControlArray.length == 0) {
            return new TimeControlResult(true, null);
        }

        // 获取当前星期几（1-7，1=周一，7=周日）
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar.DAY_OF_WEEK: 1=周日, 2=周一, ..., 7=周六
        // 转换为: 1=周一, 2=周二, ..., 7=周日
        int currentDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;

        // 获取当前时间（小时:分钟）
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentTimeInMinutes = currentHour * 60 + currentMinute;

        // 遍历timeControl数组，查找匹配当前星期几的配置
        for (TimeControl timeControl : timeControlArray) {
            if (timeControl.day == currentDay) {
                // 解析开始时间和结束时间
                String[] startParts = timeControl.startTime.split(":");
                String[] endParts = timeControl.endTime.split(":");
                
                if (startParts.length != 2 || endParts.length != 2) {
                    ALog.e("TimeControlUtil: 时间格式错误 - startTime: " + timeControl.startTime + ", endTime: " + timeControl.endTime);
                    continue;
                }

                try {
                    int startHour = Integer.parseInt(startParts[0]);
                    int startMinute = Integer.parseInt(startParts[1]);
                    int endHour = Integer.parseInt(endParts[0]);
                    int endMinute = Integer.parseInt(endParts[1]);

                    int startTimeInMinutes = startHour * 60 + startMinute;
                    int endTimeInMinutes = endHour * 60 + endMinute;

                    // 判断是否在时间段内（考虑跨天情况）
                    boolean inTimeRange;
//                    if (endTimeInMinutes >= startTimeInMinutes) {
//                         不跨天：12:00 - 13:00
                        inTimeRange = currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes;
//                    } else {
//                         跨天：12:00 - 03:00（次日凌晨）
//                        inTimeRange = currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes;
//                    }

                    if (inTimeRange) {
                        // 在允许的时间段内，允许通行
                        return new TimeControlResult(true, null);
                    }
                } catch (NumberFormatException e) {
                    ALog.e("TimeControlUtil: 解析时间失败 - " + e.getMessage());
                    continue;
                }
            }
        }

        // 没有找到匹配的时间段，不允许通行
        return new TimeControlResult(false, "该证件处于非工作期间");
    }

    /**
     * 时间控制检查结果
     */
    public static class TimeControlResult {
        private final boolean allowed;
        private final String errorMessage;

        public TimeControlResult(boolean allowed, String errorMessage) {
            this.allowed = allowed;
            this.errorMessage = errorMessage;
        }

        /**
         * 是否允许通行
         */
        public boolean isAllowed() {
            return allowed;
        }

        /**
         * 错误消息（如果不允许通行）
         */
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
