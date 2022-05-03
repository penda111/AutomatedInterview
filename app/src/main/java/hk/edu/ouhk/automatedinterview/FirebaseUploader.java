package hk.edu.ouhk.automatedinterview;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.Callable;

public class FirebaseUploader implements Callable<String> {
    static String FILENAME = "answer";
    static String FORMAT = ".wav";
    static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/audio/" +FILENAME+FORMAT;
    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static StorageReference storageRef = storage.getReference();
    static StorageReference audioRef = storageRef.child("audio");


    public String uploadAudio(){
        Uri file = Uri.fromFile(new File(path));
        StorageReference answerAudioRef = audioRef.child(FILENAME+FORMAT);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/wav")
                .build();
        UploadTask uploadTask = answerAudioRef.putFile(file, metadata);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return answerAudioRef.getDownloadUrl();
            }
        });
        while(!urlTask.isSuccessful());
        Uri downloadUrl = urlTask.getResult();
        String url = downloadUrl.toString();
        Log.i("UploadAudio URL", url);
        return url;
    }
    public String call(){
        return uploadAudio();
    }
}
