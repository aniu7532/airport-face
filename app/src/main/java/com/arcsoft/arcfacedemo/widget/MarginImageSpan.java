package com.arcsoft.arcfacedemo.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

/**
 * 支持垂直对齐与缩放的行内图片 Span，用于在文本中嵌入图标并保持视觉居中。
 */
public class MarginImageSpan extends ImageSpan {
    Drawable drawable;

    public MarginImageSpan(@NonNull Drawable drawable) {
        super(drawable);
        this.drawable = drawable;
    }

    /**
     * 按对齐方式计算纵向偏移并缩放绘制图片。
     */
    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Drawable b = drawable;
        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        } else if (mVerticalAlignment == ALIGN_CENTER) {
            transY = (bottom - top) / 2 - b.getBounds().height() / 2;
        }
        canvas.scale(0.9f,0.9f);

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
