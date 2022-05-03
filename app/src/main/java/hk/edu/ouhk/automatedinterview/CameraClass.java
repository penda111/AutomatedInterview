package hk.edu.ouhk.automatedinterview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public  class CameraClass implements Callable<Void> {
    private String TAG = "CameraClass";
    public static final int MEDIA_TYPE_IMAGE = 1;
    Context context;
    View view;
    Camera mCamera;
    CameraPreview mPreview;
    public CameraClass(Context c, View v){
        context = c;
        view = v;
        if(isCameraAvailable(context)) {
            createNewCamera();
        }
       /* mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(context, mCamera);
        FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        mPicture = new PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null){
                    Log.d(TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };*/
    }
    public void releaseCamera(){
        mCamera.release();
    }
    public void createNewCamera(){
        mCamera = getCameraInstance();
        if (mCamera != null) {
            CameraPreview mPreview = new CameraPreview(context, mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);
        }
    }
    public static Camera getCameraInstance(){
        Camera c = null;
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for(int camIndex = 0 ; camIndex < cameraCount; camIndex++){
            Camera.getCameraInfo(camIndex, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                try {
                    c = Camera.open(camIndex); // attempt to get a Camera instance
                } catch (Exception e) {
                    // Camera is not available (in use or does not exist)
                }
            }
        }
        return c; // returns null if camera is unavailable
    }
    public void takePhoto(){
        if (mCamera != null) {
            PictureCallback mPicture = new PictureCallback() {

                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                    if (pictureFile == null) {
                        Log.d(TAG, "Error creating media file, check storage permissions");
                        return;
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }
                    camera.startPreview();
                    InterviewThread.setPhotoReadyTrue();
                }
            };
            mCamera.takePicture(null, null, mPicture);

        }
    }
    private static File getOutputMediaFile(int type){


        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/photo/"
                    + "photo.jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
    public static boolean isCameraAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public Void call(){
        takePhoto();
        return null;
    }
}


