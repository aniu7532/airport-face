//package com.arcsoft.arcfacedemo.util;
//
//import java.text.MessageFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.blankj.utilcode.util.DeviceUtils;
//
//public class SnowflakeIdUtil {
//    private static Logger logger = LoggerFactory.getLogger(SnowflakeIdUtil.class.getName());
//    /** 业务系统上线的时间 2024-10-01 0:0:0，41位最多可以表示约69.7年 */
//    private static final long twepoch = 1727712000000L;
//    /** 毫秒内序列 */
//    private long sequence = 0L;
//    /** 机器ID */
//    private int workerId;
//    /** 上次生成ID的时间戳 */
//    private long lastTimestamp = -1L;
//    private volatile static SnowflakeIdUtil instance = null;
//
//    public void setWorkerId(int workerId) {
//        if (workerId > 1023 || workerId < 0) {
//            throw new IllegalArgumentException("workerId must be between 0 and 1023");
//        }
//        this.workerId = workerId;
//    }
//
//    /**
//     * SnowflakeIdUtil 类的构造函数
//     *
//     * @throws IllegalArgumentException 如果传入的 workerId 或 datacenterId 不在 0 到 31 的范围内，则抛出此异常
//     */
//    private SnowflakeIdUtil() {
//        workerId = getWorkId();
//    }
//
//    /**
//     * 获取 SnowflakeIdUtil 的单例对象。
//     * 此方法首先获取工作机器ID和数据中心ID，然后使用这两个ID调用另一个 getInstance 方法来获取 SnowflakeIdUtil 的单例对象。
//     * @return 返回 SnowflakeIdUtil 的单例对象。
//     */
//    public static SnowflakeIdUtil getInstance() {
//        if (instance == null) {
//            synchronized (SnowflakeIdUtil.class) {
//                if (instance == null) {
//                    instance = new SnowflakeIdUtil();
//                }
//            }
//        }
//        return instance;
//    }
//
//    /**
//     * workId使用IP生成
//     * @return workId
//     */
//    private int getWorkId() {
//        // try {
//        // String hostAddress = SystemInfo.getHostAddress();
//        String hostAddress = DeviceUtils.getUniqueDeviceId();
//        int[] ints = StringUtils.toCodePoints(hostAddress);
//        int sums = 0;
//        for (int b : ints) {
//            sums = sums + b;
//        }
//        return (sums % 1024);
//        // } catch (UnknownHostException ex) {
//        // ex.printStackTrace();
//        // // 失败就随机生成
//        // return RandomUtils.nextInt(0, 1024);
//        // }
//    }
//
//    /**
//     * 生成下一个唯一的ID
//     *
//     * @return 下一个唯一的ID
//     * @throws RuntimeException 如果系统时钟回退，则抛出RuntimeException异常
//     */
//    public synchronized long nextId() {
//        long now = getTimestamp(); // 获取时间戳
//        // 时钟回退处理：如果当前时间小于上一次ID生成的时间戳
//        if (now < lastTimestamp) {
//            // 最多支持1.5秒以内的回拨（1500毫秒），否则抛出异常
//            long offset = lastTimestamp - now;
//            if (offset <= 1500) {
//                try {
//                    offset = offset << 2;// 等待2两倍的时间
//                    Thread.sleep(offset);
//                    now = getTimestamp();
//                    // 还是小，抛异常
//                    if (now < lastTimestamp) {
//                        throw new RuntimeException(String.format("时钟回拨，无法生成ID %d milliseconds", lastTimestamp - now));
//                    }
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        // 如果是同一时间生成的，则进行毫秒内序列
//        if (lastTimestamp == now) {
//            // 毫秒级顺序号，使用掩码4095取低12位的数，限制自增取值在1~4095之间，（掩码4095表示二进制12位均为1的值，即：1111 1111 1111）
//            sequence = (sequence + 1) & 4095;
//            // 溢出
//            if (sequence == 0) {
//                // 毫秒内序列溢出，等待到下一毫秒再继续
//                now = getNextMillis(now);
//            }
//        } else {
//            // 置0之前，序列号在同一时间并发后自增到这里说明时间不同了，版本号所以置0
//            sequence = 0;
//        }
//        lastTimestamp = now;
//        /*
//         * 长度64位，其中： 1位符号位，0正数，1负数 41位毫秒级时间戳，41111111111111111111111111111 10位机器ID，11 1111 1111 12位序列号，1111 1111 1111
//         */
//        long id = ((now - twepoch) << 22) | (workerId << 12) | sequence;
//        return id;
//    }
//
//    /**
//     * 将长整型ID解码为字符串格式
//     *
//     * @param id 需要解码的长整型ID
//     * @return 解码后的字符串，格式为"时间戳\t序列号\t工作机ID\t中心ID"
//     */
//    public static String idDecode(long id) {
//        long sequence = id & 4095; // 取低12位的数
//        long workerId = (id >> 10) & 1023;// 左移后取低10位的数
//        long time = (id >> 22); // 左移后取低41位的数
//        return MessageFormat.format("time:{0,number,#}\treq:{1}\twid:{2}\t{3}", time, sequence, workerId,
//                getDataTime(time));
//    }
//
//    private static String getDataTime(long timeInterval) {
//        long timestamp = twepoch + timeInterval;
//        Date date = new Date(timestamp);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        String dtStr = format.format(date);
//        return dtStr;
//    }
//
//    protected long getTimestamp() {
//        return System.currentTimeMillis();
//    }
//
//    // 等待下一个毫秒,直到获得新的时间戳
//    protected long getNextMillis(long lastTimestamp) {
//        // logger.info("wait until next millis : "+lastTimestamp);
//        long timestamp = getTimestamp();
//        while (timestamp <= lastTimestamp) {
//            timestamp = getTimestamp();
//        }
//        return timestamp;
//    }
//}
