//package com.arcsoft.arcfacedemo.db;//package com.arcsoft.arcfacedemo.db;
//
//import android.content.Context;
//
//import com.arcsoft.arcfacedemo.db.model.LongTermRecords;
//
//import org.greenrobot.greendao.query.QueryBuilder;
//
//import java.util.List;
//
//
//public class LongTermRecordsDaoOpe {
//
//    /**
//     * 添加数据至数据库
//     *
//     * @param context
//     * @param stu
//     */
//    public static void insertData(Context context, LongTermRecords stu) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().insert(stu);
//    }
//
//
//    /**
//     * 将数据实体通过事务添加至数据库
//     *
//     * @param context
//     * @param list
//     */
//    public static void insertData(Context context, List<LongTermRecords> list) {
//        if (null == list || list.size() <= 0) {
//            return;
//        }
//        DbManager.getDaoSession(context).getLongTermRecordsDao().insertInTx(list);
//    }
//
//    /**
//     * 添加数据至数据库，如果存在，将原来的数据覆盖
//     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
//     *
//     * @param context
//     * @param student
//     */
//    public static void saveData(Context context, LongTermRecords student) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().save(student);
//    }
//
////    /**
////     * 添加数据至数据库，如果存在，将原来的数据覆盖
////     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
////     *
////     * @param context
////     * @param list
////     */
////    public static void saveData(Context context, List<LongTermRecords> list) {
////        DbManager.getDaoSession(context).getLoginResultDao().updateInTx(list);
////    }
//
//
//    /**
//     * 删除数据至数据库
//     *
//     * @param context
//     * @param student 删除具体内容
//     */
//    public static void deleteData(Context context, LongTermRecords student) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().delete(student);
//    }
//
//    /**
//     * 根据id删除数据至数据库
//     *
//     * @param context
//     * @param id      删除具体内容
//     */
//    public static void deleteByKeyData(Context context, long id) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().deleteByKey(id);
//    }
//
//    /**
//     * 删除全部数据
//     *
//     * @param context
//     */
//    public static void deleteAllData(Context context) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().deleteAll();
//    }
//
//    /**
//     * 更新数据库
//     *
//     * @param context
//     * @param student
//     */
//    public static void updateData(Context context, LongTermRecords student) {
//        DbManager.getDaoSession(context).getLongTermRecordsDao().update(student);
//    }
//
//
//    /**
//     * 查询所有数据
//     *
//     * @param context
//     * @return
//     */
//    public static List<LongTermRecords> queryAll(Context context) {
//        QueryBuilder<LongTermRecords> builder = DbManager.getDaoSession(context).getLongTermRecordsDao().queryBuilder();
//        return builder.build().list();
//    }
//
//
//    /**
//     * 分页加载
//     *
//     * @param context
//     * @param pageSize 当前第几页(程序中动态修改pageSize的值即可)
//     * @param pageNum  每页显示多少个
//     * @return
//     */
//    public static List<LongTermRecords> queryPaging(int pageSize, int pageNum, Context context) {
//        LongTermRecordsDao taskInfoDao = DbManager.getDaoSession(context).getLongTermRecordsDao();
//        List<LongTermRecords> listMsg = taskInfoDao.queryBuilder().offset(pageSize * pageNum).limit(pageNum).list();
//        return listMsg;
//    }
//
//    /**
//     * 查询单个数据
//     *
//     * @param context
//     * @return
//     */
//    public static LongTermRecords query(Context context) {
//        QueryBuilder<LongTermRecords> builder =
//                DbManager.getDaoSession(context).getLongTermRecordsDao().queryBuilder().orderDesc(LongTermRecordsDao.Properties.Id).offset(0).limit(1);
//        return builder.build().unique();
//    }
//
//    /**
//     * 查询单个数据
//     *
//     * @param context
//     * @param cardid
//     * @return
//     */
//    public static LongTermRecords query(Context context, String cardid) {
//        QueryBuilder<LongTermRecords> builder = DbManager.getDaoSession(context).getLongTermRecordsDao().queryBuilder()
//                .where(LongTermRecordsDao.Properties.CardId.eq(cardid));
//        return builder.build().unique();
//    }
//}
