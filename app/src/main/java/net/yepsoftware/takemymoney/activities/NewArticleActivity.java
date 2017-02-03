package net.yepsoftware.takemymoney.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.yepsoftware.takemymoney.R;
import net.yepsoftware.takemymoney.helpers.ImageHelper;
import net.yepsoftware.takemymoney.helpers.PreferencesHelper;
import net.yepsoftware.takemymoney.model.Article;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class NewArticleActivity extends ChildActivity {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Button submitButton;
    private ArrayList<String> imagesUrls;
    private ArrayList<String> imagesNames;

    private LinearLayout imageLayout;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;

    private View overlay1;
    private View overlay2;
    private View overlay3;

    private TextView loadingText1;
    private TextView loadingText2;
    private TextView loadingText3;

    private DatabaseReference articlesDBRef;
    StorageReference imagesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article);

        articlesDBRef = FirebaseDatabase.getInstance().getReference().child("articles");

        titleEditText = (EditText) findViewById(R.id.title);
        descriptionEditText = (EditText) findViewById(R.id.description);
        priceEditText = (EditText) findViewById(R.id.price);
        submitButton = (Button) findViewById(R.id.button);

        imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (ImageView) findViewById(R.id.image3);
        overlay1 = findViewById(R.id.overlay1);
        overlay2 = findViewById(R.id.overlay2);
        overlay3 = findViewById(R.id.overlay3);
        loadingText1 = (TextView) findViewById(R.id.loadingText1);
        loadingText2 = (TextView) findViewById(R.id.loadingText2);
        loadingText3 = (TextView) findViewById(R.id.loadingText3);

        imagesUrls = new ArrayList<>();
        imagesNames = new ArrayList<>();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://takemymoney-c3e5b.appspot.com");
        imagesReference = storageReference.child("articles");

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titleEditText.getText().toString().isEmpty()
                        || descriptionEditText.getText().toString().isEmpty()
                        || priceEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"You must fill all the fields.", Toast.LENGTH_SHORT).show();
                } else {
                    writeNewArticle(titleEditText.getText().toString(), descriptionEditText.getText().toString(), Double.valueOf(priceEditText.getText().toString()));
                    finish();
                }
            }
        });
    }

    private ImageView removeImageView;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"Remove Image");
        menu.add(0,1,0,"Cancel");
        removeImageView = (ImageView) v;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 0:
                removeImage();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void removeImage(){
        if (removeImageView != null){
            if (removeImageView == image1){
                imagesReference.child(imagesNames.get(0)).delete();
                imagesNames.remove(0);
                imagesUrls.remove(0);
                if(image2.getDrawable() != null){
                    image1.setImageDrawable(image2.getDrawable());
                    if (image3.getDrawable() != null){
                        image2.setImageDrawable(image3.getDrawable());
                        image3.setImageDrawable(null);
                        unregisterForContextMenu(image3);
                    } else {
                        image2.setImageDrawable(null);
                        unregisterForContextMenu(image2);
                    }
                } else {
                    image1.setImageDrawable(null);
                    unregisterForContextMenu(image1);
                    imageLayout.setVisibility(View.GONE);
                }
            } else if (removeImageView == image2){
                imagesReference.child(imagesNames.get(1)).delete();
                imagesNames.remove(1);
                imagesUrls.remove(1);
                if (image3.getDrawable() != null){
                    image2.setImageDrawable(image3.getDrawable());
                    image3.setImageDrawable(null);
                    unregisterForContextMenu(image3);
                } else {
                    image2.setImageDrawable(null);
                    unregisterForContextMenu(image2);
                }
            } else if (removeImageView == image3){
                imagesReference.child(imagesNames.get(2)).delete();
                imagesNames.remove(2);
                imagesUrls.remove(2);
                image3.setImageDrawable(null);
                unregisterForContextMenu(image3);
            }
        }
    }

    private void writeNewArticle(String title, String description, double price) {
        Article article = new Article(PreferencesHelper.getUserId(getApplicationContext()), title, description, price,  imagesUrls, Article.State.ACTIVE);
        String key = articlesDBRef.push().getKey();
        articlesDBRef.child(key).setValue(article);
    }

    public void addImage(View v){
        CharSequence options[];
        options = new CharSequence[]{"From Gallery", "From Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(NewArticleActivity.this);
        builder.setTitle("Select Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    fromGallery();
                } else {
                    fromCamera();
                }
            }
        });
        builder.show();
    }

    private static final int CAMERA = 0;
    private static final int GALLERY = 1;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 109;
    private static final int MY_PERMISSIONS_REQUEST_GALLERY = 110;

    private void fromCamera(){
        askForCameraPermissions();
    }

    private void askForCameraPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(NewArticleActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(NewArticleActivity.this,
                        Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(NewArticleActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    ActivityCompat.requestPermissions(NewArticleActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                startCameraIntent();
            }
        } else {
            startCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraIntent();
                } else {
                    Toast.makeText(NewArticleActivity.this, "Camera access denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_GALLERY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryIntent();
                } else {
                    Toast.makeText(NewArticleActivity.this, "Gallery access denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void startCameraIntent(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA);
    }

    public void fromGallery(){
        askForGalleryPermissions();
    }

    private void askForGalleryPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(NewArticleActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(NewArticleActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(NewArticleActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_GALLERY);
                } else {
                    ActivityCompat.requestPermissions(NewArticleActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_GALLERY);
                }
            } else {
                startGalleryIntent();
            }
        } else {
            startGalleryIntent();
        }
    }

    private void startGalleryIntent(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case CAMERA:
                if(resultCode == RESULT_OK){
                    handleOnActivityResult(imageReturnedIntent);
                }

                break;
            case GALLERY:
                if(resultCode == RESULT_OK){
                    handleOnActivityResult(imageReturnedIntent);
                }
                break;
        }
    }

    private void handleOnActivityResult(Intent imageReturnedIntent) {
        imageLayout.setVisibility(View.VISIBLE);
        if (imageReturnedIntent.getData() == null){
            fillImageViews((Bitmap) imageReturnedIntent.getExtras().get("data"));
        } else {
            Uri imageUri = imageReturnedIntent.getData();
            Bitmap image = null;
            InputStream is = null;
            if (imageUri.toString().startsWith("content://com.google.android.apps.photos.content")){
                try {
                    is = getContentResolver().openInputStream(Uri.parse(imageUri.toString()));
                    image = BitmapFactory.decodeStream(is);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else{
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(imageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                image =  BitmapFactory.decodeFile(picturePath);
            }

            fillImageViews(image);
        }
    }

    private void fillImageViews(Bitmap image){
        Bitmap bitmap = ImageHelper.getResizedBitmapKeepingAspectRatio(image, 1024, 1024);
        int imageNumber = 0;
        if (image1.getDrawable() == null){
            image1.setImageBitmap(bitmap);
            imageNumber = 1;
        } else if (image2.getDrawable() == null){
            image2.setImageBitmap(bitmap);
            imageNumber = 2;
        } else if (image3.getDrawable() == null){
            image3.setImageBitmap(bitmap);
            imageNumber = 3;
        } else {

        }
        uploadImage(image, imageNumber);
    }

    private void uploadImage(final Bitmap image, final int imageNumber){
        final TextView textView;
        final View overlay;
        final ImageView imageView;
        switch (imageNumber){
            case 1:
                textView = loadingText1;
                overlay = overlay1;
                imageView = image1;
                break;
            case 2:
                textView = loadingText2;
                overlay = overlay2;
                imageView = image2;
                break;
            case 3:
                textView = loadingText3;
                overlay = overlay3;
                imageView = image3;
                break;
            default:
                textView = null;
                overlay = null;
                imageView = null;
                break;
        }

        overlay.setVisibility(View.VISIBLE);

        submitButton.setEnabled(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        String key = articlesDBRef.push().getKey();
        UploadTask uploadTask = imagesReference.child(key + ".jpg").putBytes(data);
        imagesNames.add(key+".jpg");
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                imagesNames.remove(imageNumber - 1);
                overlay.setVisibility(View.GONE);
                imageView.setImageDrawable(null);
                Toast.makeText(NewArticleActivity.this, "Image upload failed. Please retry.", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                overlay.setVisibility(View.GONE);
                registerForContextMenu(imageView);
                imagesUrls.add(String.valueOf(taskSnapshot.getDownloadUrl()));
                submitButton.setEnabled(true);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                textView.setText(progress+"%");
            }
        });
    }
}
