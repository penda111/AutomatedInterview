package hk.edu.ouhk.automatedinterview;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static hk.edu.ouhk.automatedinterview.FirebaseExecutor.POOL;

public class FirebaseAudioDownloader implements Callable<String> {

    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static StorageReference storageRef = storage.getReference();
    static StorageReference audioRef = storageRef.child("audio");

    static StorageReference fileRef = audioRef.child("question.mp3");


//    public CloudStorageService(){
//        storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference();
//        audioRef = storageRef.child("audio");
//
//        setFileRef("answer.mp3");
//    }
//    public void setFileRef(String fileName){
//        String audioFileName = fileName;
//        //String audioFileName = "321go.mp3";
//        fileRef = audioRef.child(audioFileName);
//    }
    public static String callerAudioDownload(){
        Future<String> future = POOL.submit(new FirebaseAudioDownloader());
        String audio_url = "";
        try {
            audio_url = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return audio_url;
    }
    public String getFileRefURL(){
        Task<Uri> urlTask = fileRef.getDownloadUrl();
        while(!urlTask.isSuccessful());
        Uri downloadUrl = urlTask.getResult();
        String url = downloadUrl.toString();
        Log.i("DownloadAudio", url);
        return url;
    }
    public String call() {
        return getFileRefURL();
    }

}
