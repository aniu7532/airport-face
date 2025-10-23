//package com.arcsoft.arcfacedemo.db;//package com.arcsoft.arcfacedemo.db;
//
//import android.content.Context;
//
//import com.arcsoft.arcfacedemo.db.model.LongTermPass;
//
//import org.greenrobot.greendao.query.QueryBuilder;
//
//import java.util.List;
//
//
//public class LongTermPassDaoOpe {
//
//    /**
//     * 添加数据至数据库
//     *
//     * @param context
//     * @param stu
//     */
//    public static void insertData(Context context, LongTermPass stu) {
//        DbManager.getDaoSession(context).getLongTermPassDao().insert(stu);
//    }
//
//
//    /**
//     * 将数据实体通过事务添加至数据库
//     *
//     * @param context
//     * @param list
//     */
//    public static void insertData(Context context, List<LongTermPass> list) {
//        if (null == list || list.size() <= 0) {
//            return;
//        }
//        DbManager.getDaoSession(context).getLongTermPassDao().insertInTx(list);
//    }
//
//    /**
//     * 添加数据至数据库，如果存在，将原来的数据覆盖
//     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
//     *
//     * @param context
//     * @param student
//     */
//    public static void saveData(Context context, LongTermPass student) {
//        DbManager.getDaoSession(context).getLongTermPassDao().save(student);
//    }
//
//    /**
//     * 添加数据至数据库，如果存在，将原来的数据覆盖
//     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
//     *
//     * @param context
//     * @param list
//     */
//    public static void saveData(Context context, List<LongTermPass> list) {
//        DbManager.getDaoSession(context).getLongTermPassDao().updateInTx(list);
//    }
//
//
//    /**
//     * 删除数据至数据库
//     *
//     * @param context
//     * @param student 删除具体内容
//     */
//    public static void deleteData(Context context, LongTermPass student) {
//        DbManager.getDaoSession(context).getLongTermPassDao().delete(student);
//    }
//
//    /**
//     * 根据id删除数据至数据库
//     *
//     * @param context
//     * @param id      删除具体内容
//     */
//    public static void deleteByKeyData(Context context, long id) {
//        DbManager.getDaoSession(context).getLongTermPassDao().deleteByKey(id);
//    }
//
//    /**
//     * 删除全部数据
//     *
//     * @param context
//     */
//    public static void deleteAllData(Context context) {
//        DbManager.getDaoSession(context).getLongTermPassDao().deleteAll();
//    }
//
//    /**
//     * 更新数据库
//     *
//     * @param context
//     * @param student
//     */
//    public static void updateData(Context context, LongTermPass student) {
//        DbManager.getDaoSession(context).getLongTermPassDao().update(student);
//    }
//
//
//    /**
//     * 查询所有数据
//     *
//     * @param context
//     * @return
//     */
//    public static List<LongTermPass> queryAll(Context context) {
//        QueryBuilder<LongTermPass> builder = DbManager.getDaoSession(context).getLongTermPassDao().queryBuilder();
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
//    public static List<LongTermPass> queryPaging(int pageSize, int pageNum, Context context) {
//        LongTermPassDao taskInfoDao = DbManager.getDaoSession(context).getLongTermPassDao();
//        List<LongTermPass> listMsg = taskInfoDao.queryBuilder().offset(pageSize * pageNum).limit(pageNum).list();
//        return listMsg;
//    }
//
//
//    /**
//     * 查询所有数据
//     *
//     * @param context
//     * @param time
//     * @return
//     */
//    public static List<LongTermPass> queryAllByTime(Context context, String time) {
//        QueryBuilder<LongTermPass> builder = DbManager.getDaoSession(context).getLongTermPassDao().queryBuilder()
//                .where(LongTermRecordsDao.Properties.CheckTime.like("%" + time + "%")).orderDesc(LongTermRecordsDao.Properties.CheckTime);
//        return builder.build().list();
//    }
//
//    /**
//     * 查询单个数据
//     *
//     * @param context
//     * @return
//     */
//    public static LongTermPass query(Context context) {
//        QueryBuilder<LongTermPass> builder =
//                DbManager.getDaoSession(context).getLongTermPassDao().queryBuilder().orderDesc(LongTermRecordsDao.Properties.Id).offset(0).limit(1);
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
//    public static LongTermPass queryByCardId(Context context, String cardid) {
//        QueryBuilder<LongTermPass> builder = DbManager.getDaoSession(context).getLongTermPassDao().queryBuilder()
//                .where(LongTermRecordsDao.Properties.CardId.eq(cardid));
//        return builder.build().unique();
//    }
//
//    /**
//     * 查询单个数据
//     *
//     * @param context
//     * @param applyId
//     * @return
//     */
//    public static LongTermPass queryByApplyId(Context context, String applyId) {
//        QueryBuilder<LongTermPass> builder = DbManager.getDaoSession(context).getLongTermPassDao().queryBuilder()
//                .where(LongTermRecordsDao.Properties.ApplyId.eq(applyId));
//        return builder.build().unique();
//    }
//}
