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

package com.sample.amazon.asbuilibrary.list;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.sample.amazon.asbuilibrary.view.ViewState;

/**
 * The base view used in any {@link ItemCarouselAdapter}. All UI-related methods should act on the
 * {@link View} returned by {@link #getItemViewContainer()}.
 */
public interface ItemView
{
    public void showItem(CoverItemProvider<?> item);

    public ImageView getCoverImageView();

    public View getSpinnerView();

    public void setCoverImage(Drawable image);

    public void setViewState(ViewState state, boolean animate);

    /**
     * Call when this item has been clicked by the user
     */
    public void onItemClick();

    void setExpandable(boolean expandable);

    /**
     * @return The layout id of the mini details to show, or 0 if no mini details should be shown.
     */
    public int getMiniDetailsLayoutId();

    /**
     * This is called when the mini details is shown and the content should be updated.
     */
    public void updateMiniDetailsUi(View miniDetailsLayout);

    /**
     * Gets called when an item is selected or unselected.
     */
    public void onCarouselSelectionChanged(boolean selected);

    /**
     * @return the parent view for the given ItemView.
     */
    public View getItemViewContainer();
}
