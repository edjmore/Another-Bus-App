package com.droid.mooresoft.anotherbusapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ed on 8/25/15.
 */
public class TabLayout extends HorizontalScrollView {

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mStrip = new TabStrip(getContext());
        addView(mStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        populateTapStrip();
    }

    public void refresh() {
        mStrip.removeAllViews();
        populateTapStrip();
    }

    private void populateTapStrip() {
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            String title = (String) mViewPager.getAdapter().getPageTitle(i);
            TextView tv = new TextView(getContext());
            tv.setAllCaps(true);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(i == mViewPager.getCurrentItem() ?
                    getResources().getColor(R.color.accent) :
                    getResources().getColor(R.color.white_text_inactive));
            tv.setTextSize(16);
            float density = getResources().getDisplayMetrics().density;
            tv.setPadding((int) (10 * density), 0, (int) (10 * density), 0); // = 6dp padding on both sides
            tv.setText(title);
            tv.setOnClickListener(mTabClickListener);
            mStrip.addView(tv, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        }
    }

    private void scrollToTab(int tabPosition, float offset) {
        View tab = getChildAt(tabPosition);
        if (tab != null) {
            int left = tab.getLeft() + (int) (offset * tab.getWidth());
            scrollTo(left, 0);
        }
    }

    private TabStrip mStrip;
    private ViewPager mViewPager;

    private final View.OnClickListener mTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // find the index of this tab
            for (int i = 0; i < mStrip.getChildCount(); i++) {
                if (v == mStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i, true);
                }
            }
        }
    };

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            mStrip.onViewPageChanged(position, positionOffset);
            scrollToTab(position, positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            // change text color
            for (int i = 0; i < mStrip.getChildCount(); i++) {
                TextView tabText = (TextView) mStrip.getChildAt(i);
                int color = i == position ?
                        getResources().getColor(R.color.accent) :
                        getResources().getColor(R.color.white_text_inactive);
                tabText.setTextColor(color);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // do nothing
        }
    };

    private class TabStrip extends LinearLayout {
        public TabStrip(Context context) {
            this(context, null);
        }

        public TabStrip(Context context, AttributeSet attrs) {
            super(context, attrs);
            setWillNotDraw(false);
            float density = getResources().getDisplayMetrics().density;
            mUnderlineThickness = 4 * density; // = 4dp
            mSeperatorThickness = 1 * density; // = 1dp
        }

        public void onViewPageChanged(int position, float positionOffset) {
            mCurrentPosition = position;
            mPositionOffset = positionOffset;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (getChildCount() > 0) {
                View currTab = getChildAt(mCurrentPosition);
                int left = currTab.getLeft(),
                        right = currTab.getRight(),
                        bottom = currTab.getBottom();
                if (mPositionOffset > 0f && mCurrentPosition < getChildCount() - 1) {
                    // need to draw undlerline midway between tabs
                    View nextTab = getChildAt(mCurrentPosition + 1);
                    left += (nextTab.getLeft() - left) * mPositionOffset;
                    right += (nextTab.getRight() - right) * mPositionOffset;
                }
                Rect underline = new Rect(left, (int) (bottom - mUnderlineThickness), right, bottom);
                mPaint.setColor(getResources().getColor(R.color.accent));
                canvas.drawRect(underline, mPaint);
            }
            // ToDo: do we want seperators?
            /* draw tab seperators
            mPaint.setColor(getResources().getColor(android.R.color.white));
            mPaint.setStrokeWidth(mSeperatorThickness);
            float height = getHeight() - mUnderlineThickness;
            for (int i = 0; i < getChildCount() - 1; i++) {
                View tab = getChildAt(i);
                int x = tab.getRight();
                canvas.drawLine(x, height * 0.1f, x, height * 0.9f, mPaint);
            } */
        }

        private float mUnderlineThickness;
        private float mSeperatorThickness;
        private float mPositionOffset;
        private int mCurrentPosition;

        private final Paint mPaint = new Paint();
    }
}
