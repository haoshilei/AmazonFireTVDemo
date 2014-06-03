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

package com.sample.amazon.asbuilibrary.util;

import com.sample.amazon.asbuilibrary.list.CarouselView;

/**
 * Callback interface for consumers of CarouselZoom, allowing
 * context specific actions to be taken before and after
 * each animation.
 */
public interface CarouselZoomAnimationCallback
{
    /**
     *  Perform any actions needed before the CarouselZoom's
     *  animation from the carousel list to the 1d list.
     */
    public void onCarouselZoomInAnimationStart(CarouselView<?> carousel);

    /**
     *  Perform any actions needed at the end of the CarouselZoom's
     *  animation from the 1d list back to the carousel list.
     */
    public void onCarouselZoomInAnimationEnd(CarouselView<?> carousel);

    /**
     *  Perform any actions needed before the CarouselZoom's
     *  animation from the carousel list to the 1d list.
     */
    public void onCarouselZoomOutAnimationStart(CarouselView<?> carousel);

    /**
     *  Perform any actions needed at the end of the CarouselZoom's
     *  animation from the 1d list back to the carousel list.
     */
    public void onCarouselZoomOutAnimationEnd(CarouselView<?> carousel);
}
