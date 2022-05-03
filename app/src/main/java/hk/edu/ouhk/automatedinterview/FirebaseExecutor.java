package hk.edu.ouhk.automatedinterview;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FirebaseExecutor {
    public static ExecutorService POOL = Executors.newFixedThreadPool(1);

    synchronized public static String downloadAudio(){
        Future<String> future = POOL.submit(new FirebaseAudioDownloader());
        String url = "";
        try {
            url = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return url;
        //return FirebaseAudioDownloader.getFileRefURL();
    }
    synchronized public static String uploadAudio(ClientSocket cs, String response, String duration, String photoURL){
        //String result = FirebaseUploader.uploadAudio();
        Future<String> future = POOL.submit(new FirebaseUploader());
        String result = null;
        try {
            result = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String mp3_url = "gs://adept-ethos-339308.appspot.com/audio/answer.mp3";
        String wav_url = "gs://adept-ethos-339308.appspot.com/audio/answer.wav";
        cs.sendData("answer", "uploaded", wav_url, response,duration, photoURL);
        return result;
    }
    synchronized public static String uploadImage(){
        Future<String> future = POOL.submit(new FirebaseImageUploader());
        String url = "";
        try {
            url = future.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return url;
    }
}
