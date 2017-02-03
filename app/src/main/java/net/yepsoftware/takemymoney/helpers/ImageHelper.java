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

    public static Bitmap decodeBitmap(Uri selectedImage, ContentResolver contentResolver) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o);
        } catch (FileNotFoundException e) {
            Log.d("ImageHelper", "IMAGE NOT FOUND");
        }

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            return BitmapFactory.decodeStream(contentResolver.openInputStream(selectedImage), null, o2);
        } catch (FileNotFoundException e) {
            Log.d("ImageHelper", "IMAGE NOT FOUND");
        }
        return null;
    }

    public  static Bitmap getBitmapFromStringPath(String imageStringPath, ContentResolver contentResolver) {

        String uriString = "file://"+imageStringPath.replace(" ", "%20");
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(uriString));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap){
        byte[] byteArray = {};
        if(bitmap != null){
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
            byteArray = blob.toByteArray();
        }

        return byteArray;
    }

    public static Bitmap byteArrayToBitmap (byte[] byteArray){

        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray .length);

        return bitmap;

    }

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
