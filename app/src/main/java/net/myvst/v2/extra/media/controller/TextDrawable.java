package net.myvst.v2.extra.media.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

    /* Platform XML constants for typeface */
    private final static String TAG = "TextDrawable";
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;

    private Resources mResources;
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;
    private Layout.Alignment mTextAlignment = Layout.Alignment.ALIGN_NORMAL;
    private ColorStateList mTextColors;
    private Rect mTextBounds;
    private Rect mDrawableBounds = new Rect();
    private CharSequence mText = "";
    private Drawable mBackDrawable;
    private Rect mBackPadding = null;

    public void setBackDrawable(Drawable drawable) {
        if (drawable instanceof NinePatchDrawable) {
            mBackPadding = new Rect();
            drawable.getPadding(mBackPadding);
            Log.d(TAG, "mBackPadding=" + mBackPadding);
        }
        mBackDrawable = drawable;
        invalidateSelf();
    }

    private static final int[] themeAttributes = { android.R.attr.textAppearance };
    private static final int[] appearanceAttributes = { android.R.attr.textSize, android.R.attr.typeface,
            android.R.attr.textStyle, android.R.attr.textColor };

    public TextDrawable(Context context) {
        super();
        mResources = context.getResources();
        mTextBounds = new Rect();
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.density = mResources.getDisplayMetrics().density;
        mTextPaint.setDither(true);
        int textSize = 15;
        ColorStateList textColor = null;
        int styleIndex = -1;
        int typefaceIndex = -1;
        TypedArray a = context.getTheme().obtainStyledAttributes(themeAttributes);
        int appearanceId = a.getResourceId(0, -1);
        a.recycle();

        TypedArray ap = null;
        if (appearanceId != -1) {
            ap = context.obtainStyledAttributes(appearanceId, appearanceAttributes);
        }
        if (ap != null) {
            for (int i = 0; i < ap.getIndexCount(); i++) {
                int attr = ap.getIndex(i);
                switch (attr) {
                case 0: // Text Size
                    textSize = a.getDimensionPixelSize(attr, textSize);
                    break;
                case 1: // Typeface
                    typefaceIndex = a.getInt(attr, typefaceIndex);
                    break;
                case 2: // Text Style
                    styleIndex = a.getInt(attr, styleIndex);
                    break;
                case 3: // Text Color
                    textColor = a.getColorStateList(attr);
                    break;
                default:
                    break;
                }
            }
            ap.recycle();
        }
        setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
        setRawTextSize(textSize);

        Typeface tf = null;
        switch (typefaceIndex) {
        case SANS:
            tf = Typeface.SANS_SERIF;
            break;

        case SERIF:
            tf = Typeface.SERIF;
            break;

        case MONOSPACE:
            tf = Typeface.MONOSPACE;
            break;
        }

        setTypeface(tf, styleIndex);
    }

    public void setText(CharSequence text) {
        if (text == null)
            text = "";
        mText = text;
        measureContent();
    }

    /**
     * Return the text currently being displayed
     */
    public CharSequence getText() {
        return mText;
    }

    /**
     * Return the current text size, in pixels
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * Set the text size. The value will be interpreted in "sp" units
     * 
     * @param size
     *            Text size value, in sp
     */
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    /**
     * Set the text size, using the supplied complex units
     * 
     * @param unit
     *            Units for the text size, such as dp or sp
     * @param size
     *            Text size value
     */
    public void setTextSize(int unit, float size) {
        float dimension = TypedValue.applyDimension(unit, size, mResources.getDisplayMetrics());
        setRawTextSize(dimension);
    }

    /*
     * Set the text size, in raw pixels
     */
    private void setRawTextSize(float size) {
        if (size != mTextPaint.getTextSize()) {
            mTextPaint.setTextSize(size);
            measureContent();
        }
    }

    /**
     * Return the horizontal stretch factor of the text
     */
    public float getTextScaleX() {
        return mTextPaint.getTextScaleX();
    }

    /**
     * Set the horizontal stretch factor of the text
     * 
     * @param size
     *            Text scale factor
     */
    public void setTextScaleX(float size) {
        if (size != mTextPaint.getTextScaleX()) {
            mTextPaint.setTextScaleX(size);
            measureContent();
        }
    }

    /**
     * Return the current text alignment setting
     */
    public Layout.Alignment getTextAlign() {
        return mTextAlignment;
    }

    /**
     * Set the text alignment. The alignment itself is based on the text layout
     * direction. For LTR text NORMAL is left aligned and OPPOSITE is right
     * aligned. For RTL text, those alignments are reversed.
     * 
     * @param align
     *            Text alignment value. Should be set to one of:
     * 
     *            {@link Layout.Alignment#ALIGN_NORMAL},
     *            {@link Layout.Alignment#ALIGN_NORMAL},
     *            {@link Layout.Alignment#ALIGN_OPPOSITE}.
     */
    public void setTextAlign(Layout.Alignment align) {
        if (mTextAlignment != align) {
            mTextAlignment = align;
            measureContent();
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed. Note
     * that not all Typeface families actually have bold and italic variants, so
     * you may need to use {@link #setTypeface(Typeface, int)} to get the
     * appearance that you actually want.
     */
    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() != tf) {
            mTextPaint.setTypeface(tf);
            measureContent();
        }
    }

    /**
     * Sets the typeface and style in which the text should be displayed, and
     * turns on the fake bold and italic bits in the Paint if the Typeface that
     * you provided does not have all the bits in the style that you specified.
     * 
     */
    public void setTypeface(Typeface tf, int style) {
        if (style > 0) {
            if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            setTypeface(tf);
            int typefaceStyle = tf != null ? tf.getStyle() : 0;
            int need = style & ~typefaceStyle;
            mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
            mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
        } else {
            mTextPaint.setFakeBoldText(false);
            mTextPaint.setTextSkewX(0);
            setTypeface(tf);
        }
    }

    public Typeface getTypeface() {
        return mTextPaint.getTypeface();
    }

    public void setTextColor(int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    public void setTextColor(ColorStateList colorStateList) {
        mTextColors = colorStateList;
        updateTextColors(getState());
    }

    private void measureContent() {
        float desired = Layout.getDesiredWidth(mText, mTextPaint);
        mTextLayout = new StaticLayout(mText, mTextPaint, (int) desired, mTextAlignment, 1.0f, 0.0f, false);
        mTextBounds.set(0, 0, mTextLayout.getWidth(), mTextLayout.getHeight());
        Log.d(TAG, "mTextBounds=" + mTextBounds);
        invalidateSelf();
    }

    private boolean updateTextColors(int[] stateSet) {
        int newColor = mTextColors.getColorForState(stateSet, Color.WHITE);
        if (mTextPaint.getColor() != newColor) {
            mTextPaint.setColor(newColor);
            return true;
        }
        return false;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mDrawableBounds.set(bounds);
        Log.d(TAG, "mDrawableBounds=" + mDrawableBounds);
    }

    @Override
    public boolean isStateful() {
        return mTextColors.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return updateTextColors(state);
    }

    @Override
    public int getIntrinsicHeight() {
        int contentHeight = 0;
        int intrinsicHeight = -1;
        if (!mTextBounds.isEmpty()) {
            contentHeight = mTextBounds.bottom - mTextBounds.top;
        }
        if (mBackDrawable != null && mBackPadding != null) {
            intrinsicHeight = mBackDrawable.getIntrinsicHeight();
            int fillDrawableHeight = intrinsicHeight - mBackPadding.top - mBackPadding.bottom;
            if (contentHeight > fillDrawableHeight) {
                intrinsicHeight = contentHeight + mBackPadding.top + mBackPadding.bottom;
            }
        } else {
            intrinsicHeight = contentHeight;
        }
        return intrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        int contentWidth = 0;
        int intrinsicWidth = -1;
        if (!mTextBounds.isEmpty()) {
            contentWidth = mTextBounds.right - mTextBounds.left;
        }
        if (mBackDrawable != null && mBackPadding != null) {
            intrinsicWidth = mBackDrawable.getIntrinsicWidth();
            int fillDrawableWidth = intrinsicWidth - mBackPadding.left - mBackPadding.right;
            if (contentWidth > fillDrawableWidth) {
                intrinsicWidth = contentWidth + mBackPadding.left + mBackPadding.right;
            }
        } else {
            if (!mTextBounds.isEmpty()) {
                intrinsicWidth = contentWidth;
            }
        }
        return intrinsicWidth;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        if (!mDrawableBounds.isEmpty()) {
            canvas.translate(mDrawableBounds.left, mDrawableBounds.top);
        }
        drawBackground(canvas);
        drawText(canvas);
        canvas.restore();
    }

    private void drawBackground(Canvas canvas) {
        if (mBackDrawable != null) {
            mBackDrawable.setBounds(0, 0, getIntrinsicWidth(), getIntrinsicHeight());
            mBackDrawable.draw(canvas);
        }
    }

    private void drawText(Canvas canvas) {
        // Log.d(TAG, "drawText");
        canvas.save();
        if (mBackPadding != null) {
            int fillDrawableWidth = mDrawableBounds.right - mDrawableBounds.left - mBackPadding.left
                    - mBackPadding.right;
            int fillDrawableHeight = mDrawableBounds.bottom - mDrawableBounds.top - mBackPadding.top
                    - mBackPadding.bottom;
            int contentWidth = mTextBounds.right - mTextBounds.left;
            int contentHeight = mTextBounds.bottom - mTextBounds.top;
            // Log.d(TAG, "fillW=" + fillDrawableWidth + ",fillH=" +
            // fillDrawableHeight + ",contentW=" + contentWidth
            // + ",contentH=" + contentHeight);
            int left = fillDrawableWidth > contentWidth ? mBackPadding.left
                    + (fillDrawableWidth - contentWidth) / 2 : mBackPadding.left;
            int top = fillDrawableHeight > contentHeight ? mBackPadding.top
                    + (fillDrawableHeight - contentHeight) / 2 : mBackPadding.top;
            canvas.translate(left, top);
        }
        mTextLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mTextPaint.getAlpha() != alpha) {
            mTextPaint.setAlpha(alpha);
        }
    }

    @Override
    public int getOpacity() {
        return mTextPaint.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mTextPaint.getColorFilter() != cf) {
            mTextPaint.setColorFilter(cf);
        }
    }

}
