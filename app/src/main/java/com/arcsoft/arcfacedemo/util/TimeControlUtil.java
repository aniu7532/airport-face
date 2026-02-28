package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.TimeControl;
import com.arcsoft.arcfacedemo.util.log.ALog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间控制工具类
 * 用于检查通行证是否在禁止通行的时间或时间段内（在配置的时间/时间段内为不允许通行）
 * 支持两种类型：
 * 1. week - 按星期几控制（day: 1-7，1=周一，7=周日）
 * 2. date - 按日期范围控制（startDate, endDate）
 */
public class TimeControlUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm";

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

        Calendar calendar = Calendar.getInstance();
        
        // 遍历timeControl数组，检查是否落在禁止通行的时间/时间段内
        for (TimeControl timeControl : timeControlArray) {
            if (timeControl == null) {
                continue;
            }
            
            String type = timeControl.type;
            if (type == null || type.isEmpty()) {
                // 兼容旧数据：如果没有type字段，默认为week类型
                type = "week";
            }
            
            boolean isMatch;
            
            if ("week".equals(type)) {
                // week 类型：按星期几判断
                isMatch = checkWeekType(timeControl, calendar);
            } else if ("date".equals(type)) {
                // date 类型：按日期范围判断
                isMatch = checkDateType(timeControl, calendar);
            } else {
                ALog.w("TimeControlUtil: 未知的type类型 - " + type);
                continue;
            }
            
            if (isMatch) {
                // 当前时间在配置的禁止通行时间段内，不允许通行
                return new TimeControlResult(false, "在禁止通行的时间段内");
            }
        }

        // 没有落在任何禁止通行的时间段内，允许通行
        return new TimeControlResult(true, null);
    }

    /**
     * 检查 week 类型的时间控制（是否落在配置的禁止通行星期+时间段内）
     * @param timeControl 时间控制配置
     * @param calendar 当前时间日历
     * @return 是否匹配（匹配则表示当前处于禁止通行时间段）
     */
    private static boolean checkWeekType(TimeControl timeControl, Calendar calendar) {
        // 获取当前星期几（1-7，1=周一，7=周日）
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // Calendar.DAY_OF_WEEK: 1=周日, 2=周一, ..., 7=周六
        // 转换为: 1=周一, 2=周二, ..., 7=周日
        int currentDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        
        // 检查星期几是否匹配
        if (timeControl.day != currentDay) {
            return false;
        }
        
        // 检查时间是否在配置的时间段内
        return checkTimeRange(timeControl.startTime, timeControl.endTime, calendar);
    }

    /**
     * 检查 date 类型的时间控制（是否落在配置的禁止通行日期范围+时间段内）
     * @param timeControl 时间控制配置
     * @param calendar 当前时间日历
     * @return 是否匹配（匹配则表示当前处于禁止通行时间段）
     */
    private static boolean checkDateType(TimeControl timeControl, Calendar calendar) {
        // 检查日期是否在范围内
        if (timeControl.startDate == null || timeControl.endDate == null) {
            ALog.e("TimeControlUtil: date类型缺少startDate或endDate");
            return false;
        }
        
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Date startDate = dateFormat.parse(timeControl.startDate);
            Date endDate = dateFormat.parse(timeControl.endDate);
            Date currentDate = calendar.getTime();
            
            // 获取当前日期（只比较日期部分，忽略时间）
            Calendar currentCal = Calendar.getInstance();
            currentCal.setTime(currentDate);
            currentCal.set(Calendar.HOUR_OF_DAY, 0);
            currentCal.set(Calendar.MINUTE, 0);
            currentCal.set(Calendar.SECOND, 0);
            currentCal.set(Calendar.MILLISECOND, 0);
            
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, 0);
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);
            
            // 检查当前日期是否在范围内
            if (currentCal.before(startCal) || currentCal.after(endCal)) {
                return false;
            }
            
            // 日期在范围内，检查时间是否在配置的时间段内
            return checkTimeRange(timeControl.startTime, timeControl.endTime, calendar);
            
        } catch (ParseException e) {
            ALog.e("TimeControlUtil: 解析日期失败 - startDate: " + timeControl.startDate + ", endDate: " + timeControl.endDate + ", error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查当前时间是否在指定的时间范围内
     * @param startTime 开始时间，格式如 "12:00"
     * @param endTime 结束时间，格式如 "13:00"
     * @param calendar 当前时间日历
     * @return 是否在时间范围内
     */
    private static boolean checkTimeRange(String startTime, String endTime, Calendar calendar) {
        if (startTime == null || endTime == null) {
            ALog.e("TimeControlUtil: startTime或endTime为空");
            return false;
        }
        
        // 解析开始时间和结束时间
        String[] startParts = startTime.split(":");
        String[] endParts = endTime.split(":");
        
        if (startParts.length != 2 || endParts.length != 2) {
            ALog.e("TimeControlUtil: 时间格式错误 - startTime: " + startTime + ", endTime: " + endTime);
            return false;
        }

        try {
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            // 获取当前时间（小时:分钟）
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            int currentTimeInMinutes = currentHour * 60 + currentMinute;

            int startTimeInMinutes = startHour * 60 + startMinute;
            int endTimeInMinutes = endHour * 60 + endMinute;

            // 判断是否在时间段内（不跨天）
            return currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes;
            
        } catch (NumberFormatException e) {
            ALog.e("TimeControlUtil: 解析时间失败 - startTime: " + startTime + ", endTime: " + endTime + ", error: " + e.getMessage());
            return false;
        }
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

    /**
     * 测试方法 - 用于验证时间控制逻辑
     * 可以在需要时调用此方法进行测试
     */
    public static void testTimeControl() {
        ALog.i("========== TimeControlUtil 测试开始 ==========");
        
        // 获取当前时间信息
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int currentDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        String currentDayName = weekDays[currentDay];
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        String currentTime = String.format("%02d:%02d", currentHour, currentMinute);
        
        ALog.i("当前时间: " + currentDayName + " " + currentTime);
        
        // 创建测试用的 LongTermPass 对象
        LongTermPass testPass = new LongTermPass();
        testPass.id = "test_id";
        
        // 测试1: 空 timeControl - 不限制，应允许通行
        ALog.i("--- 测试1: 空 timeControl ---");
        testPass.setTimeControl(null);
        TimeControlResult result1 = checkTimeControl(testPass);
        ALog.i("结果: " + (result1.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result1.getErrorMessage() != null ? " - " + result1.getErrorMessage() : ""));
        
        // 测试2: week 类型 - 匹配当前星期和时间 → 在禁止时间段内，应不允许通行
        ALog.i("--- 测试2: week 类型 - 匹配当前星期和时间（禁止通行） ---");
        TimeControl[] weekMatch = new TimeControl[1];
        weekMatch[0] = new TimeControl();
        weekMatch[0].type = "week";
        weekMatch[0].day = currentDay;
        weekMatch[0].startTime = String.format("%02d:%02d", Math.max(0, currentHour - 1), currentMinute);
        weekMatch[0].endTime = String.format("%02d:%02d", Math.min(23, currentHour + 1), currentMinute);
        testPass.setTimeControl(weekMatch);
        TimeControlResult result2 = checkTimeControl(testPass);
        ALog.i("配置: " + currentDayName + " " + weekMatch[0].startTime + " - " + weekMatch[0].endTime);
        ALog.i("结果: " + (result2.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result2.getErrorMessage() != null ? " - " + result2.getErrorMessage() : ""));
        
        // 测试3: week 类型 - 不匹配星期 → 不在禁止时间段内，应允许通行
        ALog.i("--- 测试3: week 类型 - 不匹配星期 ---");
        TimeControl[] weekNotMatch = new TimeControl[1];
        weekNotMatch[0] = new TimeControl();
        weekNotMatch[0].type = "week";
        int otherDay = (currentDay == 7) ? 1 : currentDay + 1;
        weekNotMatch[0].day = otherDay;
        weekNotMatch[0].startTime = "00:00";
        weekNotMatch[0].endTime = "23:59";
        testPass.setTimeControl(weekNotMatch);
        TimeControlResult result3 = checkTimeControl(testPass);
        ALog.i("配置: " + weekDays[otherDay] + " 00:00 - 23:59");
        ALog.i("结果: " + (result3.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result3.getErrorMessage() != null ? " - " + result3.getErrorMessage() : ""));
        
        // 测试4: week 类型 - 匹配星期但不匹配时间 → 不在禁止时间段内，应允许通行
        ALog.i("--- 测试4: week 类型 - 匹配星期但不匹配时间 ---");
        TimeControl[] weekTimeNotMatch = new TimeControl[1];
        weekTimeNotMatch[0] = new TimeControl();
        weekTimeNotMatch[0].type = "week";
        weekTimeNotMatch[0].day = currentDay;
        // 设置一个不包含当前时间的范围
        int testHour = (currentHour + 12) % 24;
        weekTimeNotMatch[0].startTime = String.format("%02d:00", testHour);
        weekTimeNotMatch[0].endTime = String.format("%02d:30", testHour);
        testPass.setTimeControl(weekTimeNotMatch);
        TimeControlResult result4 = checkTimeControl(testPass);
        ALog.i("配置: " + currentDayName + " " + weekTimeNotMatch[0].startTime + " - " + weekTimeNotMatch[0].endTime);
        ALog.i("结果: " + (result4.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result4.getErrorMessage() != null ? " - " + result4.getErrorMessage() : ""));
        
        // 测试5: date 类型 - 匹配日期和时间 → 在禁止时间段内，应不允许通行
        ALog.i("--- 测试5: date 类型 - 匹配日期和时间（禁止通行） ---");
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String today = dateFormat.format(now.getTime());
        Calendar tomorrowCal = Calendar.getInstance();
        tomorrowCal.add(Calendar.DAY_OF_MONTH, 1);
        String tomorrow = dateFormat.format(tomorrowCal.getTime());
        
        TimeControl[] dateMatch = new TimeControl[1];
        dateMatch[0] = new TimeControl();
        dateMatch[0].type = "date";
        dateMatch[0].startDate = today;
        dateMatch[0].endDate = tomorrow;
        dateMatch[0].startTime = String.format("%02d:%02d", Math.max(0, currentHour - 1), currentMinute);
        dateMatch[0].endTime = String.format("%02d:%02d", Math.min(23, currentHour + 1), currentMinute);
        testPass.setTimeControl(dateMatch);
        TimeControlResult result5 = checkTimeControl(testPass);
        ALog.i("配置: " + today + " 至 " + tomorrow + " " + dateMatch[0].startTime + " - " + dateMatch[0].endTime);
        ALog.i("结果: " + (result5.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result5.getErrorMessage() != null ? " - " + result5.getErrorMessage() : ""));
        
        // 测试6: date 类型 - 不匹配日期 → 不在禁止时间段内，应允许通行
        ALog.i("--- 测试6: date 类型 - 不匹配日期 ---");
        Calendar futureCal = Calendar.getInstance();
        futureCal.add(Calendar.DAY_OF_MONTH, 10);
        String futureDate = dateFormat.format(futureCal.getTime());
        futureCal.add(Calendar.DAY_OF_MONTH, 5);
        String futureEndDate = dateFormat.format(futureCal.getTime());
        
        TimeControl[] dateNotMatch = new TimeControl[1];
        dateNotMatch[0] = new TimeControl();
        dateNotMatch[0].type = "date";
        dateNotMatch[0].startDate = futureDate;
        dateNotMatch[0].endDate = futureEndDate;
        dateNotMatch[0].startTime = "00:00";
        dateNotMatch[0].endTime = "23:59";
        testPass.setTimeControl(dateNotMatch);
        TimeControlResult result6 = checkTimeControl(testPass);
        ALog.i("配置: " + futureDate + " 至 " + futureEndDate + " 00:00 - 23:59");
        ALog.i("结果: " + (result6.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result6.getErrorMessage() != null ? " - " + result6.getErrorMessage() : ""));
        
        // 测试7: 混合类型 - week 不匹配、date 匹配 → 落在 date 禁止段内，应不允许通行
        ALog.i("--- 测试7: 混合类型 - week 和 date ---");
        TimeControl[] mixed = new TimeControl[2];
        // week 类型 - 不匹配（不禁止）
        mixed[0] = new TimeControl();
        mixed[0].type = "week";
        mixed[0].day = otherDay;
        mixed[0].startTime = "00:00";
        mixed[0].endTime = "23:59";
        // date 类型 - 匹配（禁止通行）
        mixed[1] = new TimeControl();
        mixed[1].type = "date";
        mixed[1].startDate = today;
        mixed[1].endDate = tomorrow;
        mixed[1].startTime = String.format("%02d:%02d", Math.max(0, currentHour - 1), currentMinute);
        mixed[1].endTime = String.format("%02d:%02d", Math.min(23, currentHour + 1), currentMinute);
        testPass.setTimeControl(mixed);
        TimeControlResult result7 = checkTimeControl(testPass);
        ALog.i("配置: [week: " + weekDays[otherDay] + " 00:00-23:59] [date: " + today + " 至 " + tomorrow + " " + mixed[1].startTime + "-" + mixed[1].endTime + "]");
        ALog.i("结果: " + (result7.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result7.getErrorMessage() != null ? " - " + result7.getErrorMessage() : ""));
        
        // 测试8: 兼容旧数据 - 没有 type 字段，匹配 → 在禁止时间段内，应不允许通行
        ALog.i("--- 测试8: 兼容旧数据 - 没有 type 字段 ---");
        TimeControl[] legacy = new TimeControl[1];
        legacy[0] = new TimeControl();
        legacy[0].type = null; // 模拟旧数据
        legacy[0].day = currentDay;
        legacy[0].startTime = String.format("%02d:%02d", Math.max(0, currentHour - 1), currentMinute);
        legacy[0].endTime = String.format("%02d:%02d", Math.min(23, currentHour + 1), currentMinute);
        testPass.setTimeControl(legacy);
        TimeControlResult result8 = checkTimeControl(testPass);
        ALog.i("配置: day=" + currentDay + " " + legacy[0].startTime + " - " + legacy[0].endTime + " (type=null)");
        ALog.i("结果: " + (result8.isAllowed() ? "允许通行 ✓" : "不允许通行 ✗") + 
                (result8.getErrorMessage() != null ? " - " + result8.getErrorMessage() : ""));
        
        ALog.i("========== TimeControlUtil 测试结束 ==========");
    }
}
