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

package com.sample.tom.asbuilibrary.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.sample.tom.uiwidgetssample.R;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;

/**
 * Convenience class to deal with setting custom fonts.
 * 
 * @author cwik
 */
public class FontUtils
{
    private static final String TAG = FontUtils.class.getSimpleName();
    
    /**
     * Maps the strings used in XML attributes to the R.raw.<font name> ID. This
     * should only ever be populated in the static method, and is the definitive
     * list of fonts we support. If we add a new font, we have to add it to the
     * map.
     */
    private static final Map<String, Integer> sFontMap = new HashMap<String, Integer>();
    
    /** Maps the R.raw.<font name> ID to the actual Typeface */
    private static final SparseArray<Typeface> sFontsArray = new SparseArray<Typeface>();
    
    static
    {
        sFontMap.put("RobotoBlack", R.raw.roboto_black);
        sFontMap.put("RobotoBold", R.raw.roboto_bold);
        sFontMap.put("RobotoLight", R.raw.roboto_light);
    }
    
    /**
     * Sets the font on the specified TextView. Also sets it to use subpixel
     * rendering so the font looks good. Using {@link #setFont(TextView, int)}
     * is less error prone and does the exact same thing, but this method is
     * needed because XML attributes set the font as a string.
     */
    public static void setFont(TextView v, String font)
    {
        Integer rawFontId = sFontMap.get(font);
        if (rawFontId == null)
        {
            Log.e(TAG, "Trying to access a font that does not exist, " + font);
            return;
        }
        else
        {
            setFont(v, rawFontId);
        }
    }
    
    /**
     * Sets the font on the specified TextView. Also sets it to use subpixel
     * rendering so the font looks good.
     */
    public static void setFont(TextView v, int rawFontId)
    {
        Typeface typeface = getFont(v.getContext(), rawFontId);
        if (typeface != null)
        {
            v.setTypeface(typeface);
            v.setPaintFlags(v.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
    }
    
    /**
     * Android Libraries don't support assets, so fonts are stored in
     * resources. However, Typeface only allows fonts to be created from assets
     * or files, so we're writing the raw resource to a file, create the
     * Typeface, and delete that file we've created. This caches the typeface
     * so we only ever have to read from disk once.
     * 
     * After fonts are put into the system/platform image, there's no need to
     * keep them in our resources, and thus we don't need to write them to disk
     * and do all of this fancy footwork.
     */
    private static Typeface getFont(Context context, int rawFontId)
    {
        if (sFontsArray.get(rawFontId) == null)
        {
            long startTime = SystemClock.uptimeMillis();
            
            Typeface typeface = null;
            
            // read the ttf file from res/raw
            InputStream inStream = null;
            try
            {
                inStream = context.getResources().openRawResource(rawFontId);
            }
            catch (NotFoundException e)
            {
                Log.e(TAG, "Could not find font in resources!");
                return null;
            }

            // attempt to write the ttf file to a random temp file
            String outPath = context.getFilesDir() + "/tmp" + SystemClock.uptimeMillis() + ".raw";
            BufferedOutputStream outStream = null;
            try
            {
                byte[] buffer = new byte[inStream.available()];
                outStream = new BufferedOutputStream(new FileOutputStream(outPath));

                int l = 0;
                while((l = inStream.read(buffer)) > 0)
                {
                    outStream.write(buffer, 0, l);
                }

                typeface = Typeface.createFromFile(outPath);

                // delete that random temp file
                new File(outPath).delete();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error reading in font!");
                return null;
            }
            finally
            {
                Utils.closeStream(outStream);
            }

            long endTime = SystemClock.uptimeMillis();
            Log.d(TAG, "Successfully loaded font, took " + (endTime - startTime) + "ms");

            sFontsArray.put(rawFontId, typeface);
        }
        return sFontsArray.get(rawFontId);
    }
}
