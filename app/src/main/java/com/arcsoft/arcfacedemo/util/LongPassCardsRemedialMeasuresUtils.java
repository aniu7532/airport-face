package com.arcsoft.arcfacedemo.util;

import android.graphics.Bitmap;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.data.FaceRepository;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.ui.callback.OnRegisterFinishedCallback;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LongPassCardsRemedialMeasuresUtils {

	private static volatile LongPassCardsRemedialMeasuresUtils instance = null;

	private final FaceRepository faceRepository;
	private static final String FILE_EXTENSION = ".jpg";
	private static final String DIR_REGISTER = "register";
	private static final String DIR_PHOTO = "photo";

	private LongPassCardsRemedialMeasuresUtils() {
		FaceDao faceDao = FaceDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao();
		faceRepository = new FaceRepository(20, faceDao, FaceServer.getInstance());
	}

	public static LongPassCardsRemedialMeasuresUtils getInstance() {
		if (instance == null) {
			synchronized (FaceServer.class) {
				if (instance == null) {
					instance = new LongPassCardsRemedialMeasuresUtils();
				}
			}
		}
		return instance;
	}

	/**
	 * 开始补救下载缺失的图片
	 */
	public void start() {
		InfoStorage infoStorage = new InfoStorage(ArcFaceApplication.getApplication());
		int interval = infoStorage.getInt("interval", ArcFaceApplication.UPDATE_DELAY_TIME);
		ThreadUtils.executeByFixedAtFixRate(ArcFaceApplication.POOL_SIZE, new SmallTask() {
			@Override
			public String doInBackground() throws Throwable {
				LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
				List<LongTermPass> longTermPassList = dao.getByStatusNotCancelled();

				if (longTermPassList == null || longTermPassList.isEmpty()) {
					ALog.i("没有需要补救下载的通行证");
					return null;
				}

				File registerDir = ensureDirectoryExists(DIR_REGISTER);
				File photoDir = ensureDirectoryExists(DIR_PHOTO);

				if (registerDir == null || photoDir == null) {
					ALog.e("创建目录失败，无法继续下载");
					return null;
				}

				ALog.i("开始补救下载，共 " + longTermPassList.size() + " 条记录");
				int successCount = 0;
				int failCount = 0;
				int skipCount = 0;

				for (LongTermPass longTermPass : longTermPassList) {
					if (longTermPass == null || longTermPass.id == null) {
						continue;
					}

					// 下载注册照片
					Boolean registerResult = downloadImageIfNotExists(
						registerDir, longTermPass.checkPhoto, longTermPass.id, longTermPass.nickname, false, "注册照片");
					if (registerResult == null) {
						skipCount++; // 文件已存在，跳过
					} else if (registerResult) {
						successCount++; // 下载成功
					} else {
						failCount++; // 下载失败
					}

					// 下载普通照片
					Boolean photoResult = downloadImageIfNotExists(
						photoDir, longTermPass.photo, longTermPass.id, longTermPass.nickname, true, "普通照片");
					if (photoResult == null) {
						skipCount++; // 文件已存在，跳过
					} else if (photoResult) {
						successCount++; // 下载成功
					} else {
						failCount++; // 下载失败
					}

					updateFace(Converters.convertToLongPassCard(longTermPass));
				}

				ALog.i(String.format("补救下载结束 - 成功: %d, 失败: %d, 跳过: %d", successCount, failCount, skipCount));
				return null;
			}
		}, interval * 60 * 1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * 确保目录存在，如果不存在则创建
	 *
	 * @param dirName 目录名称
	 * @return 目录文件对象，如果创建失败返回null
	 */
	private File ensureDirectoryExists(String dirName) {
		try {
			File dir = new File(ArcFaceApplication.getApplication().getExternalFilesDir(null), dirName);
			if (!dir.exists()) {
				boolean created = dir.mkdirs();
				if (!created) {
					ALog.e("创建目录失败: " + dir.getAbsolutePath());
					return null;
				}
			}
			return dir;
		} catch (Exception e) {
			ALog.e("创建目录异常: " + dirName, e);
			return null;
		}
	}

	/**
	 * 如果文件不存在则下载图片
	 *
	 * @param directory 保存目录
	 * @param imageUrl 图片URL
	 * @param imageId 图片ID
	 * @param nickname 昵称（用于日志）
	 * @param zip 是否压缩
	 * @param imageType 图片类型（用于日志）
	 * @return true-下载成功, false-下载失败, null-文件已存在（跳过）
	 */
	private Boolean downloadImageIfNotExists(File directory, String imageUrl, String imageId, String nickname, boolean zip, String imageType) {
		if (imageUrl == null || imageUrl.isEmpty()) {
			ALog.w(String.format("跳过下载 %s - %s: URL为空", nickname, imageType));
			return null;
		}

		File targetFile = new File(directory, imageId + FILE_EXTENSION);
		if (targetFile.exists()) {
			return null; // 文件已存在，跳过
		}

		boolean result = ImageDownloader.downloadImage(directory, imageUrl, imageId, nickname, zip);
		if (result) {
			ALog.d(String.format("补救下载成功 - %s: %s (%s)", nickname, imageType, imageId));
		} else {
			ALog.e(String.format("补救下载失败 - %s: %s (%s)", nickname, imageType, imageId));
		}
		return result;
	}

	/**
	 * 更新人脸数据库,先删除，再注册
	 */
	private void updateFace(LongPassCard longPassCard) {

		List<FaceEntity> faceEntityList = FaceDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao().getAllFaces();
		for (FaceEntity faceEntity : faceEntityList) {
			if (faceEntity.getUserName().equals(longPassCard.id)) {
				if (FaceServer.getInstance().getFaceEngine() == null
					&& FaceServer.getInstance().getFrEngine() == null) {
					ALog.e("FaceServer.getInstance().getFaceEngine() == null");
					continue;
				}

				ALog.e(longPassCard.nickname + ", " + faceEntity.toString2());
				if (ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity) {
					int flag1 = FaceServer.getInstance().getFrEngine()
						.removeFaceFeature((int) faceEntity.getFaceId());
					ALog.e("getFrEngine deleteFace removeFaceFeature：" + flag1);
				} else {
					int flag1 = FaceServer.getInstance().getFaceEngine()
						.removeFaceFeature((int) faceEntity.getFaceId());
					ALog.e("getFaceEngine deleteFace removeFaceFeature：" + flag1);
				}

				boolean flag = FaceServer.getInstance().removeOneFace(faceEntity);

				ALog.e("deleteFace removeOneFace：" + flag);
				// 删除人脸
				int result = FaceDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao().deleteFace(faceEntity);
				ALog.e("deleteFace result：" + result);
				ALog.e("FaceDatabase.getInstance(getApplication()).faceDao().deleteFace(faceEntity)："
					+ faceEntity.toString2());

			}
		}

		Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(longPassCard.id);
		// 获取图片
		// Bitmap bitmap = ImageDownloader.loadAndDecryptImage(longPassCard.id, getInstance());
		// 注册人脸
		registerFaceByBitmap(bitmap, longPassCard);
	}

	/**
	 * 单个注册人脸
	 */
	private void registerFaceByBitmap(Bitmap bitmap, LongPassCard longPassCard) {
		ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
			@Override
			public String doInBackground() throws Throwable {

				registerFace(bitmap, new OnRegisterFinishedCallback() {
					@Override
					public void onRegisterFinished(FacePreviewInfo facePreviewInfo, boolean success) {
						ALog.i("单个注册人脸: " + success + ", name：" + longPassCard.nickname + ", id：" + longPassCard.id);
					}
				}, longPassCard.id);
				return null;
			}
		});
	}

	public void registerFace(Bitmap bitmap, OnRegisterFinishedCallback callback, String applyId) {
		Bitmap alignedBitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
		ALog.e("alignedBitmap.getWidth():" + alignedBitmap.getWidth() + ",alignedBitmap.getHeight():"
			+ alignedBitmap.getHeight());
		if (FaceServer.getInstance().getFaceEngine() == null) {
			ALog.e("FaceServer.getInstance().getFaceEngine() == null");
			return;
		}
		Observable.create(new ObservableOnSubscribe<byte[]>() {
			@Override
			public void subscribe(ObservableEmitter<byte[]> emitter) throws Exception {
				byte[] bgr24Data = ArcSoftImageUtil.createImageData(alignedBitmap.getWidth(), alignedBitmap.getHeight(),
					ArcSoftImageFormat.BGR24);
				int transformCode =
					ArcSoftImageUtil.bitmapToImageData(alignedBitmap, bgr24Data, ArcSoftImageFormat.BGR24);
				if (transformCode == ArcSoftImageUtilError.CODE_SUCCESS) {
					emitter.onNext(bgr24Data);
				} else {
					emitter.onError(new Exception("transform failed, code is " + transformCode));
				}
			}
		}).flatMap(new Function<byte[], ObservableSource<FaceEntity>>() {

			@Override
			public ObservableSource<FaceEntity> apply(byte[] bgr24Data) throws Exception {
				Observable<FaceEntity> faceEntityObservable;
				if (ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity) {
					ALog.e("ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity");
					faceEntityObservable =
						Observable.just(faceRepository.registerBgr24(ArcFaceApplication.getApplication(), bgr24Data,
							alignedBitmap.getWidth(), alignedBitmap.getHeight(), applyId, true));

				} else {
					faceEntityObservable =
						Observable.just(faceRepository.registerBgr24(ArcFaceApplication.getApplication(), bgr24Data,
							alignedBitmap.getWidth(), alignedBitmap.getHeight(), applyId));
				}

//				loadData(true);
				// 注册成功时，数据也同步更新下
				return faceEntityObservable;
			}
		}).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<FaceEntity>() {
			@Override
			public void onSubscribe(Disposable d) {

			}

			@Override
			public void onNext(FaceEntity faceEntity) {
				if (faceEntity != null) {
					callback.onRegisterFinished(null, true);
				} else {
					callback.onRegisterFinished(null, false);
				}
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
				callback.onRegisterFinished(null, false);
			}

			@Override
			public void onComplete() {

			}
		});
	}

}
