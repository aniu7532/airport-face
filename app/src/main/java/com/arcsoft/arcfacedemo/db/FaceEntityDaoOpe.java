//package com.arcsoft.arcfacedemo.db;//package com.arcsoft.arcfacedemo.db;
//
//import android.content.Context;
//
//import com.arcsoft.arcfacedemo.db.model.FaceEntity;
//
//import org.greenrobot.greendao.query.QueryBuilder;
//
//import java.util.List;
//
//
//public class FaceEntityDaoOpe {
//
//    /**
//     * 添加数据至数据库
//     *
//     * @param context
//     * @param stu
//     */
//    public static void insertData(Context context, FaceEntity stu) {
//        DbManager.getDaoSession(context).getFaceEntityDao().insert(stu);
//    }
//
//
//    /**
//     * 将数据实体通过事务添加至数据库
//     *
//     * @param context
//     * @param list
//     */
//    public static void insertData(Context context, List<FaceEntity> list) {
//        if (null == list || list.size() <= 0) {
//            return;
//        }
//        DbManager.getDaoSession(context).getFaceEntityDao().insertInTx(list);
//    }
//
//    /**
//     * 添加数据至数据库，如果存在，将原来的数据覆盖
//     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
//     *
//     * @param context
//     * @param student
//     */
//    public static void saveData(Context context, FaceEntity student) {
//        DbManager.getDaoSession(context).getFaceEntityDao().save(student);
//    }
//
////    /**
////     * 添加数据至数据库，如果存在，将原来的数据覆盖
////     * 内部代码判断了如果存在就update(entity);不存在就insert(entity)；
////     *
////     * @param context
////     * @param list
////     */
////    public static void saveData(Context context, List<FaceEntity> list) {
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
//    public static void deleteData(Context context, FaceEntity student) {
//        DbManager.getDaoSession(context).getFaceEntityDao().delete(student);
//    }
//
//    /**
//     * 根据id删除数据至数据库
//     *
//     * @param context
//     * @param id      删除具体内容
//     */
//    public static void deleteByKeyData(Context context, long id) {
//        DbManager.getDaoSession(context).getFaceEntityDao().deleteByKey(id);
//    }
//
//    /**
//     * 删除全部数据
//     *
//     * @param context
//     */
//    public static void deleteAllData(Context context) {
//        DbManager.getDaoSession(context).getFaceEntityDao().deleteAll();
//    }
//
//    /**
//     * 更新数据库
//     *
//     * @param context
//     * @param student
//     */
//    public static void updateData(Context context, FaceEntity student) {
//        DbManager.getDaoSession(context).getFaceEntityDao().update(student);
//    }
//
//
//    /**
//     * 查询所有数据
//     *
//     * @param context
//     * @return
//     */
//    public static List<FaceEntity> queryAll(Context context) {
//        QueryBuilder<FaceEntity> builder = DbManager.getDaoSession(context).getFaceEntityDao().queryBuilder();
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
//    public static List<FaceEntity> queryPaging(int pageSize, int pageNum, Context context) {
//        FaceEntityDao taskInfoDao = DbManager.getDaoSession(context).getFaceEntityDao();
//        List<FaceEntity> listMsg = taskInfoDao.queryBuilder().offset(pageSize * pageNum).limit(pageNum).list();
//        return listMsg;
//    }
//
//    /**
//     * 查询单个数据
//     *
//     * @param context
//     * @return
//     */
//    public static FaceEntity query(Context context) {
//        QueryBuilder<FaceEntity> builder =
//                DbManager.getDaoSession(context).getFaceEntityDao().queryBuilder().orderDesc(FaceEntityDao.Properties.Id).offset(0).limit(1);
//        return builder.build().unique();
//    }
//
////    /**
////     * 查询单个数据
////     *
////     * @param context
////     * @param applyId
////     * @return
////     */
////    public static FaceEntity queryByApplyId(Context context, String applyId) {
////        QueryBuilder<FaceEntity> builder = DbManager.getDaoSession(context).getFaceEntityDao().queryBuilder()
////                .where(FaceEntityDao.Properties.ApplyId.eq(applyId));
////        return builder.build().unique();
////    }
//}
