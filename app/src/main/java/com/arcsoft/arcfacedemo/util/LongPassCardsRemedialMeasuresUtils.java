package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.data.FaceRepository;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class LongPassCardsRemedialMeasuresUtils {

	/**
	 * 补救措施进度回调接口
	 */
	public interface RemedialProgressCallback {
		/**
		 * 进度更新回调
		 *
		 * @param currentIndex 当前处理的序号（第几个）
		 * @param failedCount  失败数量
		 * @param totalCount   总数量
		 */
		void onProgress(int currentIndex, int failedCount, int totalCount);

		void end();
	}

	/**
	 * 批量注册的回调
	 */
	public interface BatchRegisterCallback1 {
		/**
		 * 批量注册过程中的回调
		 *
		 * @param current 当前已处理的数量
		 * @param failed  处理失败的数量
		 * @param total   处理总数
		 */
		void onProcess(int current, int failed, int total, String errMsg);

		/**
		 * 批量注册结束的回调
		 *
		 * @param current 当前已处理的数量
		 * @param failed  处理失败的数量
		 * @param total   处理总数
		 * @param errMsg  错误消息
		 */
		void onFinish(int current, int failed, int total, String errMsg);
	}


	private static volatile LongPassCardsRemedialMeasuresUtils instance = null;

	private static final String FILE_EXTENSION = ".jpg";
	private static final String DIR_REGISTER = "register";
	private static final String DIR_PHOTO = "photo";
	private static final String SUFFIX_JPEG = ".jpeg";
	private static final String SUFFIX_JPG = ".jpg";
	private static final String SUFFIX_PNG = ".png";
	private static final String TAG = "LongPassCardsRemedialMeasuresUtils";
	private boolean doing = false;

	private LongPassCardsRemedialMeasuresUtils() { }

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
	 *
	 * @param callback 进度回调，返回已注册数量和总数
	 */
	public void start(RemedialProgressCallback callback) {
		ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
			@Override
			public String doInBackground() throws Throwable {

				if (doing) return null;

				doing = true;

				LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
				List<LongTermPass> longTermPassList = dao.getByStatusNotCancelled();

				if (longTermPassList == null || longTermPassList.isEmpty()) {
					ALog.i("没有需要补救下载的通行证");
					if (callback != null) {
						callback.onProgress(0, 0, 0);
					}
					doing = false;
					return null;
				}

				final int totalCount = longTermPassList.size();
				final int[] currentIndex = {0};  // 当前处理的序号
				final int[] failedCount = {0};   // 失败数量

				// 初始回调
				if (callback != null) {
					callback.onProgress(0, 0, totalCount);
				}

				ALog.i(String.format("开始下载 - 总数: %d", totalCount));

				File registerDir = ensureDirectoryExists(DIR_REGISTER);
				File photoDir = ensureDirectoryExists(DIR_PHOTO);

				if (registerDir == null || photoDir == null) {
					ALog.e("创建目录失败，无法继续下载");
					doing = false;
					return null;
				}

				for (LongTermPass longTermPass : longTermPassList) {
					if (longTermPass == null || longTermPass.id == null) {
						continue;
					}

					currentIndex[0]++;  // 当前处理的序号+1

					boolean hasFailure = false;  // 本次是否有失败

					// 下载注册照片
					Boolean registerResult = downloadImageIfNotExists(
						registerDir, longTermPass.checkPhoto, longTermPass.id, longTermPass.nickname, false, "注册照片");
					if (registerResult == null) {
						ALog.i("registerDir文件已存在，跳过");
					} else if (registerResult) {
						ALog.i("registerDir文件下载成功");
					} else {
						ALog.i("registerDir文件下载失败");
						hasFailure = true;
					}

					// 下载普通照片
					Boolean photoResult = downloadImageIfNotExists(
						photoDir, longTermPass.photo, longTermPass.id, longTermPass.nickname, true, "普通照片");
					if (photoResult == null) {
						ALog.i("photoDir文件已存在，跳过");
					} else if (photoResult) {
						ALog.i("photoDir文件下载成功");
					} else {
						ALog.i("photoDir文件下载失败");
						hasFailure = true;
					}

					// 如果本次有失败，增加失败计数
					if (hasFailure) {
						failedCount[0]++;
					}

					// 更新进度回调
					if (callback != null) {
						final int current = currentIndex[0];
						final int failed = failedCount[0];
						callback.onProgress(current, failed, totalCount);
					}
				}

				ALog.i(String.format("下载完成 - 总数: %d, 当前: %d, 失败: %d", totalCount, currentIndex[0], failedCount[0]));
				callback.end();
				doing = false;
				return null;
			}
		});
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
	 * 批量注册人脸
	 *
	 * @param context        上下文
	 * @param dir            批量注册的文件夹
	 * @param callback       注册回调
	 */
	public void registerFromFile(Context context, File dir, BatchRegisterCallback1 callback) {
		ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
			@Override
			public String doInBackground() throws Throwable {
				if (!dir.exists()) {
					callback.onFinish(0, 0, 0, "目录不存在: " + dir.getAbsolutePath());
					return null;
				}

				// 初始化 FaceServer 和 FaceRepository
				FaceServer faceServer = FaceServer.getInstance();
				FaceDao faceDao = FaceDatabase.getInstance(context).faceDao();
				FaceRepository faceRepository = new FaceRepository(20, faceDao, faceServer);

				File[] allFiles = dir.listFiles((dir1, name) -> {
					String nameLowerCase = name.toLowerCase();
					return nameLowerCase.endsWith(SUFFIX_JPG) || nameLowerCase.endsWith(SUFFIX_JPEG)
							|| nameLowerCase.endsWith(SUFFIX_PNG);
				});

				if (allFiles == null || allFiles.length == 0) {
					callback.onFinish(0, 0, 0, "目录中没有图片文件: " + dir.getAbsolutePath());
					return null;
				}

				// 获取所有非注销状态的 LongTermPass 记录的 id 集合
				LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
				List<LongTermPass> longTermPassList = dao.getByStatusNotCancelled();


				Set<String> validIds = new HashSet<>();
				if (longTermPassList != null) {
					for (int i = 0; i < longTermPassList.size(); i++) {
						LongTermPass pass = longTermPassList.get(i);
						if (pass.id != null && faceDao.queryByUserName(pass.id) == null) {
							validIds.add(pass.id);
						}
						callback.onProcess(0, 0, 0, String.format("查询未注册的人脸当前第%d个/共%d个，找到%d个", i + 1, longTermPassList.size(), validIds.size()));
					}
				}

				callback.onProcess(0, 0, 0, "开始注册中...");
				// 过滤文件：只保留文件名（去掉扩展名）在 validIds 中的文件
				List<File> validFiles = new LinkedList<>();
				for (File file : allFiles) {
					String fileName = file.getName();
					int suffixIndex = fileName.indexOf(".");
					String nameWithoutExt = suffixIndex > 0 ? fileName.substring(0, suffixIndex) : fileName;
					if (validIds.contains(nameWithoutExt)) {
						validFiles.add(file);
					} else {
						ALog.d(TAG, "跳过文件（不在 LongTermPass 表中或已注销）: " + fileName);
					}
				}

				if (validFiles.isEmpty()) {
					callback.onFinish(0, 0, allFiles.length, "没有符合条件的文件需要注册");
					return null;
				}

				File[] files = validFiles.toArray(new File[0]);
				int total = files.length;
				final ArrayList<String> failFileNames = new ArrayList<>();
				final int[] failed = { 0 };
				final int[] success = { 0 };
				Observable.fromArray(files).flatMap((Function<File, ObservableSource<Boolean>>) file -> {
							byte[] decryptedBytes = AESUtils.decryptFileToByte(file);
							if (decryptedBytes == null) {
								ALog.e(TAG, "解密文件失败: " + file.getName());
								failed[0]++;
								failFileNames.add(file.getName());
								return observer -> observer.onNext(true);
							}
							String name = file.getName();
							int suffixIndex = name.indexOf(".");
							if (suffixIndex > 0) {
								name = name.substring(0, suffixIndex);
							}
							FaceEntity faceEntity;
							try {
								faceEntity = faceRepository.registerJpeg(context, decryptedBytes, name);
								if (faceEntity == null) {
									failed[0]++;
									failFileNames.add(file.getName());
								} else {
									success[0]++;
								}
							} catch (Exception e) {
								ALog.e(TAG, "注册文件失败: " + file.getName(), e);
								failed[0]++;
								failFileNames.add(file.getName());
								faceEntity = null;
							}
							FaceEntity finalFaceEntity = faceEntity;
							return observer -> observer.onNext(finalFaceEntity == null);
						}).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Observer<Boolean>() {
							@Override
							public void onSubscribe(Disposable d) {
							}

							@Override
							public void onNext(Boolean res) {
								int succeedSize = success[0];
								int failedSize = failed[0];
								if (total == succeedSize + failedSize) {
									ALog.e(TAG, "批量注册出错", failFileNames);
									if (!failFileNames.isEmpty()) {
										ToastUtils.showLong(failFileNames.toString());
									}
									callback.onFinish(success[0], failed[0], total, null);
								} else {
									callback.onProcess(success[0], failed[0], total, "");
								}
							}

							@Override
							public void onError(Throwable e) {
								ALog.e(TAG, "批量注册出错", e);
								if (!failFileNames.isEmpty()) {
									ToastUtils.showLong(failFileNames.toString());
								}
								callback.onFinish(success[0], failed[0], total, e.getMessage());
							}

							@Override
							public void onComplete() {
							}
						});
				return null;
			}
		});

	}

	/**
	 * 解析 register 目录中的加密图片，保存为未加密的图片
	 *
	 * @param context  上下文
	 * @param fileIds   文件ID列表（LongTermPass 的 id，不包含 .jpg 扩展名）
	 * @param outputDir 输出目录（如果为 null，则保存到 "decrypted_register" 目录）
	 * @return 成功解析的文件数量
	 */
	public int parseRegisterImages(Context context, List<String> fileIds, File outputDir) {
		if (fileIds == null || fileIds.isEmpty()) {
			ALog.e(TAG, "文件ID列表为空");
			return 0;
		}

		// 确定输出目录
		if (outputDir == null) {
			outputDir = new File(ArcFaceApplication.getApplication().getExternalFilesDir(null), "decrypted_register");
		}
		if (!outputDir.exists()) {
			boolean created = outputDir.mkdirs();
			if (!created) {
				ALog.e(TAG, "创建输出目录失败: " + outputDir.getAbsolutePath());
				return 0;
			}
		}

		File registerDir = new File(ArcFaceApplication.getApplication().getExternalFilesDir(null), DIR_REGISTER);
		if (!registerDir.exists()) {
			ALog.e(TAG, "register 目录不存在: " + registerDir.getAbsolutePath());
			return 0;
		}

		int successCount = 0;
		for (String fileId : fileIds) {
			if (fileId == null || fileId.isEmpty()) {
				continue;
			}

			try {
				// 解密图片
				Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(fileId);
				if (bitmap == null) {
					ALog.w(TAG, "解密图片失败或图片不存在: " + fileId);
					continue;
				}

				// 保存为未加密的图片
				File outputFile = new File(outputDir, fileId + ".jpg");
				FileOutputStream fos = new FileOutputStream(outputFile);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();

				// 回收 Bitmap
				if (!bitmap.isRecycled()) {
					bitmap.recycle();
				}

				successCount++;
				ALog.d(TAG, "成功解析图片: " + fileId + " -> " + outputFile.getAbsolutePath());
			} catch (Exception e) {
				ALog.e(TAG, "解析图片失败: " + fileId, e);
			}
		}

		ALog.i(TAG, "解析完成，成功: " + successCount + "/" + fileIds.size());
		return successCount;
	}

	/**
	 * 解析单个 register 目录中的加密图片
	 *
	 * @param context   上下文
	 * @param fileId    文件ID（LongTermPass 的 id，不包含 .jpg 扩展名）
	 * @param outputDir 输出目录（如果为 null，则保存到 "decrypted_register" 目录）
	 * @return 成功返回输出文件路径，失败返回 null
	 */
	public String parseRegisterImage(Context context, String fileId, File outputDir) {
		if (fileId == null || fileId.isEmpty()) {
			ALog.e(TAG, "文件ID为空");
			return null;
		}

		// 确定输出目录
		if (outputDir == null) {
			outputDir = new File(ArcFaceApplication.getApplication().getExternalFilesDir(null), "decrypted_register");
		}
		if (!outputDir.exists()) {
			boolean created = outputDir.mkdirs();
			if (!created) {
				ALog.e(TAG, "创建输出目录失败: " + outputDir.getAbsolutePath());
				return null;
			}
		}

		try {
			// 解密图片
			Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(fileId);
			if (bitmap == null) {
				ALog.w(TAG, "解密图片失败或图片不存在: " + fileId);
				return null;
			}

			// 保存为未加密的图片
			File outputFile = new File(outputDir, fileId + ".jpg");
			FileOutputStream fos = new FileOutputStream(outputFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();

			// 回收 Bitmap
			if (!bitmap.isRecycled()) {
				bitmap.recycle();
			}

			ALog.d(TAG, "成功解析图片: " + fileId + " -> " + outputFile.getAbsolutePath());
			return outputFile.getAbsolutePath();
		} catch (Exception e) {
			ALog.e(TAG, "解析图片失败: " + fileId, e);
			return null;
		}
	}

}
