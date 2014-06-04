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

package com.sample.tom.asbuilibrary.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import com.sample.tom.uiwidgetssample.R;

/**
 * GradientTextView draw text with a vertical color gradient across the face of
 * the text.
 * <p/>
 * There are several modes of gradient drawing (EACH_LINE, ALL_TEXT,
 * NO_GRADIENT)
 * <p/>
 * The top of the text is always the standard android textColor The bottom of
 * the gradient is defined by bottomColor
 * <p/>
 * xml attrs<br />
 * android:textColor - color of the top of the gradient<br />
 * app:bottomColor - color of the bottom of the gradient this can be a color or
 * a reference to a ColorStateList that will change based on the current state
 * of the view. Note if the bottom color is transparent or is identical to the
 * topColor the gradient will be automatically disabled.<br />
 * app:gradientType - gradient mode<br />
 * app:xline_offset - [0-1] specifies how far from the baseline to top of a the
 * text as a percentage that the top color should start fading to the bottom
 * color. By default this line is where the top of a lower case 'x' is on a
 * normal font. Used in {EACH_LINE} mode
 */
public class GradientTextView extends FontableTextView {
	/**
	 * This text view should draw as a normal text view
	 * <p/>
	 * gradientType="noGradient"
	 */
	public static final int NO_GRADIENT = 1;
	/**
	 * The gradient applies to each line of the text view with the top color at
	 * the top of each line and the bottom color at the bottom of each line
	 * <p/>
	 * gradientType="eachLine"
	 */
	public static final int EACH_LINE = 2;
	/**
	 * The gradient spans across the entire text
	 * <p/>
	 * gradientType="allText"
	 */
	public static final int ALL_TEXT = 3;
	private static final String TAG = GradientTextView.class.getSimpleName();
	private static final float DEFAULT_XLINE = 0.5f;
	private static final int DEFAULT_BOTTOM_COLOR = 0;
	private int mStyle;
	private ColorStateList mBottomColor;
	private int mCurTopColor;
	private int mCurBottomColor;
	private float mXLineOffset;

	public GradientTextView(Context context) {
		super(context);
		mStyle = NO_GRADIENT;
		setBottomColor(DEFAULT_BOTTOM_COLOR);
		mXLineOffset = DEFAULT_XLINE;
	}

	public GradientTextView(Context context, AttributeSet attrs) {
		// call super constructor with
		super(context, attrs);
		init(attrs);

	}

	public GradientTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		// load the layout parameters from attrs
		TypedArray styledAttributes = getContext().obtainStyledAttributes(
				attrs, R.styleable.GradientText);

		mStyle = styledAttributes.getInt(R.styleable.GradientText_gradientType,
				NO_GRADIENT);
		mBottomColor = styledAttributes
				.getColorStateList(R.styleable.GradientText_bottomColor);
		mXLineOffset = styledAttributes.getFloat(
				R.styleable.GradientText_xLineOffset, DEFAULT_XLINE);

		styledAttributes.recycle();
	}

	@Override
	public void setTextColor(ColorStateList colors) {
		super.setTextColor(colors);
		updateTextColors();
	}

	/**
	 * Changes the color for the bottom of the gradient
	 */
	public void setBottomColor(int color) {
		setBottomColor(ColorStateList.valueOf(color));
	}

	public void setBottomColor(ColorStateList color) {
		mBottomColor = color;
		updateTextColors();
	}

	/**
	 * This function sets the internal colors of the text based on the current
	 * draw state.
	 */
	private void updateTextColors() {
		final int[] state = getDrawableState();
		boolean invalid = false;

		int color;
		// update the bottom color
		if (mBottomColor != null) {
			color = mBottomColor.getColorForState(state, DEFAULT_BOTTOM_COLOR);
			if (color != mCurBottomColor) {
				mCurBottomColor = color;
				invalid = true;
			}
		}
		// update the top color
		color = this.getTextColors().getColorForState(state,
				getTextColors().getDefaultColor());
		if (color != mCurTopColor) {
			mCurTopColor = color;
			invalid = true;
		}
		if (invalid) {
			updateGradientShader();
		}

	}

	/**
	 * Changes the gradient style one of NO_GRADIENT, EACH_LINE, or ALL_TEXT
	 */
	public void setGradientStyle(int style) {
		if (style != mStyle) {
			mStyle = style;
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// trigger an update of the gradient now that we
		// have Layout specs for the textview
		updateGradientShader();
	}

	private void updateGradientShader() {
		Shader shader;
		int expressedStyle = mStyle;

		// check if we can disable the gradient if bottom alpha is 0 or ==
		// topColor
		if (Color.alpha(mCurBottomColor) == 0
				|| mCurBottomColor == mCurTopColor) {
			expressedStyle = NO_GRADIENT;
		}

		switch (expressedStyle) {
		case NO_GRADIENT:
			shader = null;
			break;
		case EACH_LINE:
			shader = createShaderEachLine();
			break;
		case ALL_TEXT:
			final float textHeight = getLayout().getHeight();
			shader = new LinearGradient(0, 0, 0, textHeight, mCurTopColor,
					mCurBottomColor, Shader.TileMode.CLAMP);
			break;
		default:
			shader = null;
			Log.e(TAG, "Invalid gradient mode " + mStyle);
			break;
		}

		this.getPaint().setShader(shader);
		invalidate();

	}

	/**
	 * updates the gradient shader for EACH_LINE mode
	 */
	private Shader createShaderEachLine() {
		// get the layout of the text, we need to set the gradient
		// on each line so it lines up right, for each line
		// we need to create 4 stops on the gradient
		// to get the appearance correct the top color is constant
		// from the opt of the text to the x-line of the text
		// the color fades from the top color to the bottom color
		// between the x-line and the baseline and remains at the
		// bottom color across the descenders and to the bottom
		// of the text.
		final Layout layout = this.getLayout();
		if (layout == null) {
			// called before layout disable shader until layout
			return null;
		}

		final int lineCount = layout.getLineCount();
		final int numStops = lineCount * 4;
		int[] colorStops = new int[numStops];
		float[] gradStops = new float[numStops];

		// loop through each line
		int stopIdx = 0;
		for (int lineNum = 0; lineNum < layout.getLineCount(); lineNum++) {
			// Insert four color stops
			// start @ topColor from the top of text line
			final float top = layout.getLineTop(lineNum);
			colorStops[stopIdx] = mCurTopColor;
			gradStops[stopIdx++] = top;

			// stay @ topColor until x-line
			final float baseline = layout.getLineBaseline(lineNum);
			final float xLine = baseline - (baseline - top) * mXLineOffset;
			colorStops[stopIdx] = mCurTopColor;
			gradStops[stopIdx++] = xLine;

			// gradient to bottomColor @ baseline
			colorStops[stopIdx] = mCurBottomColor;
			gradStops[stopIdx++] = baseline;

			// stay @ bottomColor until bottom of text line
			final float bottom = layout.getLineTop(lineNum + 1);
			colorStops[stopIdx] = mCurBottomColor;
			gradStops[stopIdx++] = bottom;
		}

		// all the gradstops are in actual px need to convert to percentages of
		// height
		// and save in final array Float->float
		final float textHeight = layout.getHeight();
		for (int i = 0; i < gradStops.length; i++) {
			gradStops[i] = gradStops[i] / textHeight;
		}

		return new LinearGradient(0, 0, 0, textHeight, colorStops, gradStops,
				Shader.TileMode.CLAMP);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		// when the state is changed update the display colors
		// of the text
		updateTextColors();
	}
}
