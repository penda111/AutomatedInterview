package hk.edu.ouhk.automatedinterview;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.Callable;

public class FirebaseImageUploader implements Callable<String> {
    static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/photo/"
            + "photo.jpg";
    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static StorageReference storageRef = storage.getReference();
    static StorageReference photoRef = storageRef.child("photo");
    static int count = 1;
    public String uploadPhoto(){
        Uri file = Uri.fromFile(new File(path));
        Log.i("FirebaseImageUploader", "Count: " + count);
        StorageReference photoFileRef = photoRef.child("photo"+count+".jpg");
        UploadTask uploadTask = photoFileRef.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return photoFileRef.getDownloadUrl();
            }
        });
        while(!urlTask.isSuccessful());
        Uri downloadUrl = urlTask.getResult();
        String url = downloadUrl.toString();
        Log.i("UploadPhoto URL", url);
        count++;
        return url;
    }
    public String call(){
        return uploadPhoto();
    }
}
