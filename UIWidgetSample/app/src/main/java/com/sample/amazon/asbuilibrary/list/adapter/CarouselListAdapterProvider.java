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

package com.sample.amazon.asbuilibrary.list.adapter;

/**
 * Interface for Adapter implementations used in a CarouselList.
 */
public interface CarouselListAdapterProvider
{
    /**
     * disable showing of images
     */
    public void disableImages();

    /**
     * enable showing of images
     */
    public void enableImages();

    /**
     *  Update which items are currently visible
     */
    public void updateVisible(int visibleStart, int visibleEnd);
}
