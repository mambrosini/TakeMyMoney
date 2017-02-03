package net.yepsoftware.takemymoney.helpers;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by mambrosini on 10/02/15.
 */
public class ImageHelper {

    public static Bitmap getResizedBitmapKeepingAspectRatio(Bitmap bitmap, int requiredWidth, int requiredHeight) {
        if(requiredWidth >= bitmap.getWidth() && requiredHeight >= bitmap.getHeight()){
            return bitmap;
        }else{
            Bitmap resizedBitmap = null;
            int originalWidth = bitmap.getWidth();
            int originalHeight = bitmap.getHeight();
            int newWidth = -1;
            int newHeight = -1;
            float multFactor = -1.0F;
            if(originalHeight > originalWidth) {
                newHeight = requiredHeight;
                multFactor = (float) originalWidth/(float) originalHeight;
                newWidth = (int) (newHeight*multFactor);
            } else if(originalWidth > originalHeight) {
                newWidth = requiredWidth;
                multFactor = (float) originalHeight/ (float)originalWidth;
                newHeight = (int) (newWidth*multFactor);
            } else if(originalHeight == originalWidth) {
                newHeight = requiredHeight;
                newWidth = requiredWidth;
            }
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            return resizedBitmap;
        }
    }

}
