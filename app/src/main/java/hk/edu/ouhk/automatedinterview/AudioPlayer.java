package hk.edu.ouhk.automatedinterview;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.IOException;

public class AudioPlayer {
    Context context;
    //MediaPlayer mp = new MediaPlayer();
    public AudioPlayer(Context c){
        Log.i("AudioPlayer", "[Start]setting up audio player");
        context = c;
        Log.i("AudioPlayer", "[End]setting up audio player");
    }
    public void playAudio(){
        MediaPlayer mp = MediaPlayer.create(context, R.raw.beep);
        try{
            //mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //release resource
                    mp.release();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void playAudioSource(String path){
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
            //Toast.makeText(context, "Playing audio", Toast.LENGTH_LONG).show();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //release resource
                    mp.release();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void playAudioFromFirebase(String url){
        String audioUrl = url;
        // initializing media player
        MediaPlayer mediaPlayer = new MediaPlayer();

        // below line is use to set the audio
        // stream type for our media player.
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // below line is use to set our
        // url to our media player.
        try {
            mediaPlayer.setDataSource(audioUrl);
            // below line is use to prepare
            // and start our media player.
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
            mediaPlayer.prepare();
            //Toast.makeText(context, "Playing audio", Toast.LENGTH_LONG).show();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //release resource
                    mp.release();
                    InterviewThread.setPlayingAudioFalse();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
