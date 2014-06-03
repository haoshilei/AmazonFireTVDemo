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

import android.os.Bundle;

/**
 * CoverItemProvider
 * Classes intending to present data in a CoverItemPagingCarousel's adapter must
 * implement this Interface to provide the data needed by the CoverItemPagingCarouselAdapter in
 * displaying the appropriate images in the Carousel.
 */
public interface CoverItemProvider<IdentifierType>
{
    public String getTitle();
    public String getType();
    public String getImageUrl();
    public IdentifierType getId();
    public String getLocalImageStorageFilename();

    /**
     * @return <code>true</code> indicates the image is local, and not a url. <code>false</code>
     *         otherwise.
     */
    public boolean isLocalOnlyImage();

    /**
     * Put getId() into the following bundle with the given key
     */
    public void putIdInBundle(Bundle bundle, String key);

    /**
     * Compare getId() to the bundle entry at key
     */
    public boolean equalsId(Bundle bundle, String key);
}
