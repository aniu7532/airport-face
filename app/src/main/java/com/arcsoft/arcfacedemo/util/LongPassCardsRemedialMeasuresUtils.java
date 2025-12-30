package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.util.List;

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
	}

	private static volatile LongPassCardsRemedialMeasuresUtils instance = null;

	private static final String FILE_EXTENSION = ".jpg";
	private static final String DIR_REGISTER = "register";
	private static final String DIR_PHOTO = "photo";
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

}
