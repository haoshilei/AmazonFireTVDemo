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

import android.util.Log;

import java.io.Closeable;
import java.io.IOException;

/**
 * Miscellaneous utility methods that don't fit elsewhere.
 */
public class Utils
{
    private static final String TAG = Utils.class.getSimpleName();

    /**
     * clamp imposes a floor and ceiling value on the value range of the inspected value.
     * @param val  The inspected value.
     * @param min  The minimum of the allowable range for the return value.
     * @param max  The maximum of the allowable range for the return value.
     * @return the integer value within the permissible range.
     */
    public static int clamp(int val, int min, int max)
    {
        if (val < min)
        {
            return min;
        }
        else
        {
            if (val > max)
            {
                return max;
            }
            else
            {
                return val;
            }
        }
    }

    /**
     * Util method to close a stream
     */
    public static void closeStream(Closeable stream)
    {
        if (stream != null)
        {
            try
            {
                stream.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error closing stream!", e);
            }
        }
    }
}
