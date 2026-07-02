/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.fmradio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

/**
 * The view used to display the visualizer
 */
public final class FmVisualizerView extends View {

    private final Handler mHandler = new Handler();

    private Paint mPaint = new Paint();

    private float mColumnPadding = 4f;

    private boolean mAnimate = false;

    private int mFrequency = 80;

    // Colors for gradient: amber top, muted bottom
    private static final int COLOR_AMBER = 0xFFF5A623;
    private static final int COLOR_AMBER_MUTED = 0xFF3D3524;

    private static final int COLUMN_PADDING_COUNTS = 2;

    private static final int COLUMN_COUNTS = 5;

    private static final float[] DEFAULT_VISUALIZER_LEVEL = new float[] {
            +0.3f, 0.7f, -0.1f, 0.5f, 0.2f
    };

    private float[] mPrevLevels = DEFAULT_VISUALIZER_LEVEL;

    /**
     * Constructor method
     *
     * @param context The context instance
     * @param attrs The attribute set for this view
     * @param defStyleAttr The default style for this view
     */
    public FmVisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Constructor method
     *
     * @param context The context instance
     * @param attrs The attribute set for this view
     */
    public FmVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor method
     *
     * @param context The context instance
     */
    public FmVisualizerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(0f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.FILL);
        mAnimate = false;
    }

    /**
     * Set the padding between visualizer columns
     *
     * @param padding The padding between visualizer columns
     */
    public void setColumnPadding(int padding) {
        mColumnPadding = padding;
    }

    /**
     * Start the animation
     */
    public void startAnimation() {
        mAnimate = true;
    }

    /**
     * Start the animation
     */
    public void stopAnimation() {
        mAnimate = false;
    }

    /**
     * Whether currently is under animation
     *
     * @return The animation state
     */
    public boolean isAnimated() {
        return mAnimate;
    }

    /**
     * Set the animation frequency
     *
     * @param freguency The specify animation frequency to set
     */
    public void setAnimateFrequency(int freguency) {
        mFrequency = freguency;
    }

    /**
     * Defined to re-freash the view
     */
    private final Runnable mRefreashRunnable = new Runnable() {
        public void run() {
            FmVisualizerView.this.invalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawColor(Color.TRANSPARENT);
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        float colWidth = ((float) (viewWidth - paddingLeft - paddingRight - COLUMN_PADDING_COUNTS
                * mColumnPadding))
                / COLUMN_COUNTS;
        float colHeight = (float) (viewHeight - paddingBottom - paddingTop);

        float levels[] = new float[COLUMN_COUNTS];
        if (!mAnimate) {
            levels = DEFAULT_VISUALIZER_LEVEL;
        } else {
            levels = generate(COLUMN_COUNTS);
        }
        for (int i = 0; i < COLUMN_COUNTS; i++) {
            float left = paddingLeft + i * (mColumnPadding + colWidth);
            float right = left + colWidth;
            float startY = paddingTop + colHeight / 2;
            startY -= colHeight / 2 * levels[i];
            if (startY < paddingTop) {
                startY = paddingTop;
            }
            float bottom = viewHeight - paddingBottom;

            // Animated bars use amber, static bars use muted amber
            if (mAnimate) {
                mPaint.setColor(COLOR_AMBER);
            } else {
                mPaint.setColor(COLOR_AMBER_MUTED);
            }

            // Round the top of each bar
            float barWidth = right - left;
            if (barWidth > 0 && (bottom - startY) > 0) {
                float radius = Math.min(barWidth / 2, (bottom - startY) / 4);
                canvas.drawRoundRect(left, startY, right, bottom, radius, radius, mPaint);
            }
        }
        mHandler.removeCallbacks(mRefreashRunnable);
        mHandler.postDelayed(mRefreashRunnable, mFrequency);
    }

    /**
     * Used to generate out the float array with specify array count
     *
     * @param count The array count
     * @return A float array with specify array count
     */
    private float[] generate(int count) {
        if (count <= 0) {
            return null;
        }
        float[] result = new float[count];
        for (int i = 0; i < count; i++) {
            // Generate a random target level, then smooth toward it
            float target = (float) (1.0f - (Math.random() * 1.5f));
            // Clamp to reasonable range
            if (target < -0.3f) target = -0.3f;
            if (target > 1.0f) target = 1.0f;
            // Move toward target with damping (lerp)
            result[i] = mPrevLevels[i] + (target - mPrevLevels[i]) * 0.25f;
            // Ensure minimum motion
            if (Math.abs(result[i] - mPrevLevels[i]) < 0.08f) {
                result[i] = mPrevLevels[i] + (target > mPrevLevels[i] ? 0.08f : -0.08f);
            }
        }
        mPrevLevels = result;
        return result;
    }
}
