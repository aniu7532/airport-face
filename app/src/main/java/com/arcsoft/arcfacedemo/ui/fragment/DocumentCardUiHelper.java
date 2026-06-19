package com.arcsoft.arcfacedemo.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.arcsoft.arcfacedemo.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DocumentCardUiHelper {

    private DocumentCardUiHelper() {
    }

    public static String formatValidityPeriod(String startDate, String endDate) {
        String start = formatCardDate(startDate);
        String end = formatCardDate(endDate);
        if (TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) {
            return "";
        }
        if (TextUtils.isEmpty(start)) {
            return end;
        }
        if (TextUtils.isEmpty(end)) {
            return start;
        }
        return start + "-" + end;
    }

    public static String formatPersonWithIdCode(String name, String idCode) {
        if (TextUtils.isEmpty(name)) {
            return safeText(idCode);
        }
        if (TextUtils.isEmpty(idCode)) {
            return name;
        }
        return name + "  " + idCode;
    }

    /**
     * 石河子临时证：两位引领人时，引领单位用顿号拼接。
     */
    public static String formatEscortUnits(String unit1, String unit2, boolean hasSecondEscort) {
        if (!hasSecondEscort) {
            return safeText(unit1);
        }
        if (TextUtils.isEmpty(unit1) && TextUtils.isEmpty(unit2)) {
            return "";
        }
        if (TextUtils.isEmpty(unit1)) {
            return safeText(unit2);
        }
        if (TextUtils.isEmpty(unit2)) {
            return unit1;
        }
        return unit1 + "、" + unit2;
    }

    public static void bindAreaBadges(LinearLayout container, String areaDisplayCode,
            @DrawableRes int badgeBackgroundRes) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<String> codes = splitAreaCodes(areaDisplayCode);
        if (codes.isEmpty()) {
            container.setVisibility(android.view.View.GONE);
            return;
        }
        container.setVisibility(android.view.View.VISIBLE);
        float density = container.getResources().getDisplayMetrics().density;
        int horizontalPadding = (int) (5 * density);
        int verticalPadding = (int) (1 * density);
        int margin = (int) (3 * density);
        int minWidth = (int) (20 * density);
        int minHeight = (int) (20 * density);

        for (String code : codes) {
            TextView badge = new TextView(container.getContext());
            badge.setText(code);
            badge.setTextColor(Color.WHITE);
            badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            badge.setGravity(Gravity.CENTER);
            badge.setBackgroundResource(badgeBackgroundRes);
            badge.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            badge.setMinWidth(minWidth);
            badge.setMinHeight(minHeight);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(margin);
            container.addView(badge, params);
        }
    }

    /**
     * 洛阳长期证：底部区域块网格（红框红字，每行 columnsPerRow 个）。
     */
    public static void bindAreaBadgeGrid(LinearLayout container, String areaDisplayCode,
            @DrawableRes int badgeBackgroundRes, int textColor, int columnsPerRow) {
        if (container == null) {
            return;
        }
        container.removeAllViews();
        List<String> codes = splitAreaCodes(areaDisplayCode);
        if (codes.isEmpty()) {
            container.setVisibility(android.view.View.GONE);
            return;
        }
        container.setVisibility(android.view.View.VISIBLE);
        float density = container.getResources().getDisplayMetrics().density;
        int margin = (int) (6 * density);
        int cellHeight = (int) (40 * density);

        for (int i = 0; i < codes.size(); i += columnsPerRow) {
            LinearLayout row = new LinearLayout(container.getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                rowParams.topMargin = margin;
            }
            container.addView(row, rowParams);

            int end = Math.min(i + columnsPerRow, codes.size());
            for (int j = i; j < end; j++) {
                TextView badge = createGridBadge(row, badgeBackgroundRes, textColor, cellHeight);
                badge.setText(codes.get(j));
                row.addView(badge, createGridBadgeLayoutParams(j > i, margin, cellHeight));
            }
            for (int pad = end - i; pad < columnsPerRow; pad++) {
                TextView badge = createGridBadge(row, badgeBackgroundRes, textColor, cellHeight);
                row.addView(badge, createGridBadgeLayoutParams(pad > 0 || end > i, margin, cellHeight));
            }
        }
    }

    private static TextView createGridBadge(LinearLayout row, @DrawableRes int badgeBackgroundRes,
            int textColor, int cellHeight) {
        TextView badge = new TextView(row.getContext());
        badge.setTextColor(textColor);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        badge.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        badge.setAllCaps(true);
        badge.setIncludeFontPadding(false);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(badgeBackgroundRes);
        badge.setMinHeight(cellHeight);
        return badge;
    }

    private static LinearLayout.LayoutParams createGridBadgeLayoutParams(boolean marginStart, int margin,
            int cellHeight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, cellHeight, 1f);
        if (marginStart) {
            params.setMarginStart(margin);
        }
        return params;
    }

    /** 临时证二维码内容：优先 applyId，否则用证件 id。 */
    public static String resolveQrText(String applyId, String passId) {
        if (!TextUtils.isEmpty(applyId) && !"null".equalsIgnoreCase(applyId.trim())) {
            return applyId.trim();
        }
        return safeText(passId);
    }

    public static Bitmap generateQrCodeBitmap(String text, int size) {
        if (TextUtils.isEmpty(text) || size <= 0) {
            return null;
        }
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 0);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size, hints);
            return scaleBitMatrixToSquareBitmap(bitMatrix, size);
        } catch (WriterException e) {
            return null;
        }
    }

    /** 裁掉 quiet zone 后放大到目标尺寸，使二维码图案铺满整张图。 */
    private static Bitmap scaleBitMatrixToSquareBitmap(BitMatrix matrix, int targetSize) {
        int[] rect = matrix.getEnclosingRectangle();
        int left = rect[0];
        int top = rect[1];
        int qrWidth = rect[2];
        int qrHeight = rect[3];
        if (qrWidth <= 0 || qrHeight <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.RGB_565);
        for (int y = 0; y < targetSize; y++) {
            int srcY = top + (y * qrHeight) / targetSize;
            for (int x = 0; x < targetSize; x++) {
                int srcX = left + (x * qrWidth) / targetSize;
                bitmap.setPixel(x, y, matrix.get(srcX, srcY) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private static List<String> splitAreaCodes(String areaDisplayCode) {
        List<String> codes = new ArrayList<>();
        if (TextUtils.isEmpty(areaDisplayCode)) {
            return codes;
        }
        String normalized = areaDisplayCode.replace('\n', ' ').replace('\r', ' ').trim();
        String[] parts = normalized.split("\\s+");
        for (String part : parts) {
            if (!TextUtils.isEmpty(part)) {
                codes.add(part.trim());
            }
        }
        return codes;
    }

    private static String formatCardDate(String rawDate) {
        if (TextUtils.isEmpty(rawDate) || "null".equalsIgnoreCase(rawDate)) {
            return "";
        }
        String trimmed = rawDate.trim();
        if (trimmed.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
            return trimmed;
        }
        String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd"};
        for (String pattern : patterns) {
            try {
                Date date = new SimpleDateFormat(pattern, Locale.getDefault()).parse(trimmed);
                if (date != null) {
                    return new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return trimmed;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }
}
