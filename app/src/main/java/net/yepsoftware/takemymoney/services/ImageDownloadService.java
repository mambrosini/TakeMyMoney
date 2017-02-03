package net.yepsoftware.takemymoney.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import net.yepsoftware.takemymoney.helpers.ImageHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by mambrosini on 2/3/17.
 */
public class ImageDownloadService extends IntentService {

    public static final int DOWNLOAD_ERROR = 10;
    public static final int DOWNLOAD_SUCCESS = 11;
    public static final String MESSAGE = "Message";
    public static final String FILEPATH = "filePath";
    public static final String IMAGE_NUMBER = "imageNumber";
    public static final String IMAGE_DOWLOADED_BROADCAST = "IMAGE_DOWLOADED_BROADCAST";

    private int imageNumber;

    public ImageDownloadService() {
        super(ImageDownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        imageNumber = intent.getIntExtra("imageNumber", -1);

        File downloadFile = getFileForPicture();
        try {
            downloadFile.createNewFile();
            URL downloadURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) downloadURL
                    .openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200)
                throw new Exception("Error in connection");
            InputStream is = conn.getInputStream();
            FileOutputStream os = new FileOutputStream(downloadFile);
            byte buffer[] = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                os.write(buffer, 0, byteCount);
            }
            os.close();
            is.close();

            String filePath = downloadFile.getPath();

            ImageHelper.scanMedia(getApplicationContext(), filePath);

            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent broadcastIntent = new Intent(IMAGE_DOWLOADED_BROADCAST);
            broadcastIntent.putExtra(MESSAGE, DOWNLOAD_SUCCESS);
            broadcastIntent.putExtra(FILEPATH, filePath);
            broadcastIntent.putExtra(IMAGE_NUMBER, imageNumber);
            broadcaster.sendBroadcast(broadcastIntent);

        } catch (Exception e) {
            e.printStackTrace();

            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent broadcastIntent = new Intent(IMAGE_DOWLOADED_BROADCAST);
            broadcastIntent.putExtra(MESSAGE, DOWNLOAD_ERROR);
            broadcastIntent.putExtra(IMAGE_NUMBER, imageNumber);
            broadcaster.sendBroadcast(broadcastIntent);
        }
    }

    public File getFileForPicture (){
        String root = getCacheDir().toString();

        String fileNameFull = "tmm_IMG_" + new Date().getTime() + ".jpg";
        File file = new File(root, fileNameFull);

        return file;
    }

}