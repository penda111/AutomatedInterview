package hk.edu.ouhk.automatedinterview;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AudioRecorder {
    static String FILENAME = "answer";
    static String FORMAT = ".mp3";
    static String  PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/audio/" + FILENAME+FORMAT;
    Context context;
    MediaRecorder mr;
    String flag = "0";
    static int CHANNEL = 2;
    static int BITRATE = 16 * 48000 ;
    static int SAMPLINGRATE = 48000;
    public AudioRecorder(Context c){
        context = c;
        Log.i("AudioRecorder", "[Start]setting up audio recorder");
        mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mr.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT); //output as wav
//        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //output as mp3
//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// encode for wav
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mr.setAudioChannels(CHANNEL);
        mr.setAudioEncodingBitRate(BITRATE);
        mr.setAudioSamplingRate(SAMPLINGRATE);
        mr.setOutputFile(PATH);

        Log.i("AudioRecorder", "[End]setting up audio recorder");
    }
    public AudioRecorder(Context c,  String path){
        context = c;
        Log.i("AudioRecorder", "[Start]setting up audio recorder");
        mr = new MediaRecorder();
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mr.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT); //output as wav
//        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //output as mp3
//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// encode for wav
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mr.setAudioChannels(CHANNEL);
        mr.setAudioEncodingBitRate(BITRATE);
        mr.setAudioSamplingRate(SAMPLINGRATE);

//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //encode for mp3
        mr.setOutputFile(path);
        Log.i("AUDIO PATH", path);
        Log.i("AudioRecorder", "[End]setting up audio recorder");
    }

    public void setMediaRecorder(){
        Log.i("AudioRecorder", "[Start]setting up audio recorder");
        mr = new MediaRecorder();
        /*
        * Must follow this order setup otherwise
        *   IllegalStateException will occur
        * 1. setAudioSource()
        * 2. setOuputFormat()
        * 3. setAudioEncoder()
        * */
        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//output as mp3
//        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mr.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT); //output as wav
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// encode for mp3
//        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// encode for wav

        mr.setAudioChannels(CHANNEL);
        mr.setAudioEncodingBitRate(BITRATE);
        mr.setAudioSamplingRate(SAMPLINGRATE);
        //mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); //encode for mp3
        mr.setOutputFile(PATH);
        Log.i("AUDIO PATH", PATH);
        Log.i("AudioRecorder", "[End]setting up audio recorder");
    }
    public void startRecord() {
        try{
            if (flag.equals("1")){
                // recorder is on recording state
            } else {
                setMediaRecorder();
                mr.prepare();
                mr.start();
                flag = "1";
                //Toast.makeText(context, "Start Recording", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    public void stopRecord(){
        if(flag.equals("0")){
            //recorder is on stopped state
        } else {
            mr.stop();
            mr.release();
            mr = null;
            flag = "0";
//            Toast.makeText(context, "Stop Recording",
//                    Toast.LENGTH_LONG).show();
        }
    }
}
