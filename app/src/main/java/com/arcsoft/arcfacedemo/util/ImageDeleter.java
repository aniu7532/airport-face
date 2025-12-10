package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;

import java.io.File;

public class ImageDeleter {

	public static boolean deleteImage(File directory, String imageUrl, String imageName, String nickname) {
		File file = new File(directory, imageName + ".jpg");
		if (!file.exists()) {
			ALog.w("File not found: " + file.getAbsolutePath());
			return false;
		}
		ALog.i("Image Delete imageName: " + imageName);
		boolean result = file.delete();
		if (result) {
			ALog.i("Image Successfully deleted: " + imageName);
		} else {
			ALog.e("Image Failed to delete: " + imageName);
		}
		return result;
	}

}
