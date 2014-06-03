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

package com.sample.amazon.uiwidgetssample;

import java.util.List;

import android.os.Bundle;

import com.sample.amazon.asbuilibrary.list.CoverItemProvider;

public class SampleItem implements CoverItemProvider<String>
{
    private final String mId;
    private final String mType;
    private final String mTitle;
    private final String mThumbnail;
    private final Class<?> mClass;
    private List<SampleItem> mChildren = null;

    /**
     * Item as shown in the recently used item carousel in the home screen
     *
     * @param mId unique CMS id (created by CMS)
     * @param mKey producer id for the item (used to remove the item from the carousel, might not be
     *            used if we ask the producer to do it instead of us.
     * @param mType item type (video, music, app...)
     * @param mTitle item title
     * @param thumbnail item thumbnail url
     * @param lastAccessed item last access date (milliseconds)
     */
    public SampleItem(String id, String type, String title, Class<?> cls)
    {
        mId = id;
        mType = type;
        mTitle = title;
        mClass = cls;
        mThumbnail = null;
    }

    public SampleItem(String id, String title, List<SampleItem> children)
    {
        mId = id;
        mType = "Stack";
        mTitle = title;
        mClass = null;
        mThumbnail = null;
        mChildren = children;
    }

    public String toString()
    {
        return "{id=" + mId + ", type=" + mType + ", title=" + mTitle + "}";
    }

    public List<SampleItem> getChildren()
    {
        return mChildren;
    }

    public Class<?> getItemClass()
    {
        return mClass;
    }

    @Override
    public String getTitle()
    {
        return mTitle;
    }

    @Override
    public String getType()
    {
        return mType;
    }

    @Override
    public String getImageUrl()
    {
        return mThumbnail;
    }

    @Override
    public String getId()
    {
        return mId;
    }

    @Override
    public String getLocalImageStorageFilename()
    {
        return mThumbnail;
    }

    @Override
    public void putIdInBundle(Bundle bundle, String key)
    {
        bundle.putString(key, mId);
    }

    @Override
    public boolean equalsId(Bundle bundle, String key)
    {
        return mId != null && mId.equals(bundle.getString(key));
    }

    @Override
    public boolean isLocalOnlyImage()
    {
        return true;
    }
}
