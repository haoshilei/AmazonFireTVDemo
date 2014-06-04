/**
 * Amazon Fire TV Development Resources
 *
 * Copyright 2004-2014 Amazon.com, Inc. or its affiliates.  All Rights Reserved.
 
 * These materials are licensed as "Program Materials" under the Program Materials 
 * License Agreement (the "License") of the Amazon Mobile App Distribution program, 
 * which is available at https://developer.amazon.com/sdk/pml.html.  See the License 
 * for the specific language governing permissions and limitations under the License.
 *
 * These materials are distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.sample.tom.asbuilibrary.list;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterViewAnimator;
import android.widget.StackView;

import com.sample.tom.uiwidgetssample.R;

public class RollupView extends StackView
{
    @SuppressWarnings("unused")
    private static final String TAG = RollupView.class.getSimpleName();
    private static final int DEFAULT_COLLAPSED_SPACING = 40;
    private static final int DEFAULT_COLLAPSED_REVEAL_SPACING = 100;
    private static final int DEFAULT_EXPANDED_SPACING = 20;
    private static final int DEFAULT_STACK_SIZE = 3;
    private static final int DEFAULT_VISIBLE_ITEMS = 20;
    private static Method sConfigureViewAnimator;
    private static Field sFramePadding;

    static
    {
        /**
         * AdapterViewAnimator.configureViewAnimator() is a package-private method
         * that sets how many child views can be shown at once.  For some reason
         * they decided to make this an inaccessible API but would be wonderful for
         * any child classes utilizing the AdapterViewAnimator class.
         * We need to reflect it out and mark it as accessible in order to use it
         * without making framework modifications.
         */
        try
        {
            sConfigureViewAnimator = AdapterViewAnimator.class.getDeclaredMethod("configureViewAnimator", int.class,
                    int.class);
            sConfigureViewAnimator.setAccessible(true);

            sFramePadding = StackView.class.getDeclaredField("mFramePadding");
            sFramePadding.setAccessible(true);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Reflection exception", e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException("Reflection exception", e);
        }
    }

    private RollupState mState;
    private int mCollapsedSpacing;
    private int mRevealedSpacing;
    private int mExpandedSpacing;
    private int mStackSize;
    private int mVisibleItems;

    public RollupView(Context context)
    {
        this(context, null);
    }

    public RollupView(Context context, AttributeSet attrs)
    {
        this(context, attrs, R.attr.rollupViewStyle);
    }

    public RollupView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RollupView, defStyle, 0);

        mCollapsedSpacing = (int) a.getDimension(R.styleable.RollupView_collapsed_spacing, DEFAULT_COLLAPSED_SPACING);
        mRevealedSpacing =
                (int) a.getDimension(R.styleable.RollupView_revealed_spacing, DEFAULT_COLLAPSED_REVEAL_SPACING);
        mExpandedSpacing = (int) a.getDimension(R.styleable.RollupView_expanded_spacing, DEFAULT_EXPANDED_SPACING);
        mStackSize = a.getInt(R.styleable.RollupView_stack_size, DEFAULT_STACK_SIZE);
        mVisibleItems = a.getInt(R.styleable.RollupView_visible_items, DEFAULT_VISIBLE_ITEMS);

        init();

        try
        {
            sFramePadding.setInt(this, 0);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Reflection invocation exception", e);
        }
    }

    private void init()
    {
        /**
         * Since this is a method that's a part of the framework it should always
         * succeed.  If for some reason it changes then we need to update this View
         * accordingly.
         */
        try
        {
            sConfigureViewAnimator.invoke(this, mVisibleItems, 1);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Reflection exception", e);
        }

        mState = RollupState.Collapsed;
    }

    /** When this view gets selected by, say, a CarouselView we need to change the state to show more of the items */
    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);

        setState(selected ? RollupState.Hovering : RollupState.Collapsed);
    }

    /**
     * Sets the state of the RollupView which determines how it is displayed on-screen:
     * Collapsed: Items are tightly packed together
     * Hovering: Items fan out a little bit in order to show more of each item
     * Expanded: Items are spaced out evenly as if in a CarouselView
     *
     * @param state The desired state to display
     */
    public void setState(RollupState state)
    {
        int spacing = 0;
        int max = mStackSize;
        int count = getChildCount() - 1;

        mState = state;

        switch (state)
        {
            case Collapsed:
                setSelection(0);
                spacing = mCollapsedSpacing;
                break;
            case Hovering:
                setSelection(0);
                spacing = mRevealedSpacing;
                break;
            case Expanded:
                max = count;
                spacing = mExpandedSpacing + getChildAt(0).getMeasuredWidth();
                break;
        }

        for (int i = count; i >= 0; i--)
        {
            View child = getChildAt(i);
            int multiplier = count - i;
            if ((count - i) >= max)
            {
                multiplier = max - 1;
            }
            child.setY(0);
            child.setX(multiplier * spacing);
        }
    }

    /**
     * Slight modification to StackView's onMeasure which measures based on the collapsed state and does not grow on
     * the Y-axis when there are more children
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        final int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        View child = getChildAt(0);

        int width;
        int height;
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED)
        {
            int spacing = mCollapsedSpacing;
            if (mState == RollupState.Hovering)
            {
                spacing = mRevealedSpacing;
            }
            else if (mState == RollupState.Expanded)
            {
                spacing = mExpandedSpacing + child.getMeasuredWidth();
            }
            width = child.getMeasuredWidth() + (spacing * (mStackSize - 1));
        }
        else
        {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED)
        {
            height = child.getMeasuredHeight();
        }
        else
        {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(width, height);

        setState(mState);
    }

    /**
     * Slight modification to the StackView's onLayout method that does not modify the position when setting their
     * bounds.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            final View child = getChildAt(i);

            int childRight = paddingLeft + child.getMeasuredWidth();
            int childBottom = paddingTop + child.getMeasuredHeight();

            child.layout(paddingLeft, paddingTop, childRight, childBottom);
        }
    }

    public static enum RollupState
    {
        Collapsed, Hovering, Expanded
    }
}
