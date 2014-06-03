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

package com.sample.amazon.asbuilibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.sample.amazon.uiwidgetssample.R;

/**
 * Created by jourdang on 6/10/13.
 */
public class ProgressView extends LinearLayout
{
    private View mProgressBarFiller;
    private ImageView mProgressBar;

    public ProgressView(Context context)
    {
        super(context);
    }

    public ProgressView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate ()
    {
        mProgressBar = (ImageView) findViewById(R.id.progress_bar);
        mProgressBarFiller = findViewById(R.id.progress_filler);
    }

    public void setProgress(int percentage)
    {
        // What we do is set the percentage bar's weight to "percentage"...

        ViewGroup.LayoutParams progressBarLayoutParams = mProgressBar.getLayoutParams();
        ((LinearLayout.LayoutParams)progressBarLayoutParams).weight = percentage;
        mProgressBar.setLayoutParams(progressBarLayoutParams);

        // ...And an invisible view's weigh to 100-percentage. That's a dynamic way to use proportions
        // with a cover that's of variable width.
        ViewGroup.LayoutParams progressBarFillerLayoutParams = mProgressBarFiller.getLayoutParams();
        ((LinearLayout.LayoutParams)progressBarFillerLayoutParams).weight = 100 - percentage;
        mProgressBarFiller.setLayoutParams(progressBarFillerLayoutParams);
    }
}
