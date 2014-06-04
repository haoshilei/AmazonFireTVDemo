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

/**
 * This interface is used by classes that want to be notified when a Category
 * change happened.
 *
 * A Category is defined by the class that uses this interface to communicate
 * its state change to their subscribers.
 */
public interface OnCategoryChangeListener
{
    /**
     * Notifies that a new category was defined.
     *
     * @param newTitle The title of the new category.
     */
    public void onCategoryChanged(String newTitle);
}
