package hk.edu.ouhk.automatedinterview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Stream;

import static hk.edu.ouhk.automatedinterview.FirebaseExecutor.POOL;

public class InterviewThread extends Thread{
    Context context;
    View view;
    Activity activity;
    CameraClass[] cameraClass;
    Handler mThreadHandler;
    Handler mUI_handler;
    SecondFragment secondFragment;
    volatile boolean shouldStop = false;
    static boolean playingAudio = false;
    static boolean photoReady = false;
    static boolean readyToUploadAudio = false;
    static boolean interviewFinished = false;
    private static long firstTime = 0;
    private static long secondTime = 0;
    private static long thirdTime = 0;
    public InterviewThread(Context c, View v, Activity a, CameraClass [] cc, Handler mThreadHandler, Handler mUI_handler
            ,SecondFragment sf){
        context = c;
        view = v;
        activity = a;
        cameraClass = cc;
        this.mThreadHandler = mThreadHandler;
        this.mUI_handler = mUI_handler;
        secondFragment = sf;
    }
    public void run() {
        Log.i("Interview Thread", "Thread Started");
        //interviewFinished = true;
        ViewController vc = new ViewController(
                view,
                R.id.instruction_msg,
                R.id.chatbot_stroke_grey,
                R.id.chatbot_stroke_green,
                R.id.unmuted,
                R.id.muted,
                R.id.camera_preview
        );
        AudioPlayer ap = new AudioPlayer(context);
        AudioRecorder ar = new AudioRecorder(context);
        WaveRecorder wr = new WaveRecorder(WaveRecorder.FOLDER);
        ClientSocket clientSocket = new ClientSocket();
        InterviewService interviewService = new InterviewService(activity, view, ap, ar, cameraClass[0], vc, mUI_handler, clientSocket, wr);

        //setButtonEvent(ap, ar, cameraClass, interviewService, wr);

        int n = 0;
        //takePhotoAndUpload(cameraClass);
        //change to while(true) for permanent connection to server socket
        //while(true){

        //Setup IO classes
        DataInputStream inputStream = null;
        String strInputstream = "";
        if(clientSocket.s.isConnected()) {
            try {
                Log.i("Interview Thread", "Setup inputStream object");
                inputStream = new DataInputStream(new BufferedInputStream(clientSocket.s.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


            while (!shouldStop) {
                Log.i("Interview Thread", "Looping " + n++);
                //Log.i("InterviewThread: ", "Initiated Connection");
                //String str = interviewService.receiveInstruction();
                String[] str = {"", ""};
                //str = clientSocket.receiveData();
                try {
                    sleep(3000);
                    //clientSocket.sendData("answer", "uploaded", "URL","","");
                    //System.out.println(str);
                    Log.i("Interview Thread", "\n" + "Connected: [" + clientSocket.s.isConnected() + "] " + " \n Input Stream is shut down: [" + clientSocket.s.isInputShutdown() + "]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                str = clientSocket.receiveData(inputStream);
                LocalDateTime dateTime = LocalDateTime.now();
                Log.i("Interview Thread ", dateTime + ": " + str[0]);
                String instruction = str[0];
                //String instruction = "finish";
                String photoRequired = str[1];
                Log.i("Interview Thread", "instruction: " + instruction + " photoRequired: " + photoRequired);
                String photoURL = "";
                String response_time = "";
                String duration_time = "";
                setFirstTime(0);
                setSecondTime(0);
                setThirdTime(0);
                //str = clientSocket.receiveData();
                //String str = "question";
                setPlayingAudioFalse();
                setPhotoReadyFalse();
                setReadyToUploadAudioFalse();
                //Log.i("InterviewThread: ", "Received "+str);


                if (instruction.equals("comment")) { // If the instruction is "comment"
                    Log.i("Interview Thread", "Session: Comment");
                    setPlayingAudioTrue();
                    interviewService.askQuestion();
                    while (playingAudio) ;
                    interviewService.switchStrokeGrey();

                } else if (instruction.equals("question")) { // If the instruction is "question"
                    Log.i("Interview Thread", "Session: Ask question");
                    setPlayingAudioTrue();
                    interviewService.askQuestion();
                    while (playingAudio) ;
                    if (photoRequired.equals("true")) { // If photo of the interviewee is required [photo] == "true"
                        takePhoto(cameraClass);
                        while (!photoReady) ;
                        photoURL = FirebaseExecutor.uploadImage();
                    }
                    firstTime = System.currentTimeMillis();
                    interviewService.recordAnswer();
                    interviewService.setButtonEvent_Answer();
                    while (!readyToUploadAudio) ;
                    //Calculate time elapsed in second(s)
                    response_time = String.valueOf((secondTime - firstTime) / 1000); // Calculate the interviewee response time
                    duration_time = String.valueOf((thirdTime - secondTime) / 1000); // Calculate the duration of answering
                    Log.i("InterviewThread", "FirstTime: " + firstTime);
                    Log.i("InterviewThread", "SecondTime: " + secondTime);
                    Log.i("InterviewThread", "ThirdTime: " + thirdTime);
                    Log.i("InterviewThread", "ResponseTime: " + response_time);
                    Log.i("InterviewThread", "DurationTime: " + duration_time);
                    FirebaseExecutor.uploadAudio(clientSocket, response_time, duration_time, photoURL);
                    interviewService.setInstructionMessage(context.getString(R.string.processing_instruction));
                } else if (instruction.equals("finish")) {// If the instruction is "finish", implying the interview is completed
                    Log.i("Interview Thread", "Session: Finish");

                    setPlayingAudioTrue();
                    interviewService.askQuestion();
                    while (playingAudio) ;
                    interviewService.switchStrokeGrey();
                    if (photoRequired.equals("true")) { // If photo of the interviewee is required [photo] == "true"
                        takePhoto(cameraClass);
                        while (!photoReady) ;
                        photoURL = FirebaseExecutor.uploadImage();
                    }
                    FirebaseExecutor.uploadAudio(clientSocket, "0", "0", photoURL);
                    interviewFinished = true;
/*                    try {
                        inputStream.close();
                        clientSocket.closeSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    break;

                }

            }
            try {
                inputStream.close();
                clientSocket.closeSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("Interview Thread", "Break out from while loop ");
            if(interviewFinished) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        secondFragment.endInterview();
                    }
                };
                mUI_handler.post(r);
            }
//        NavHostFragment.findNavController(secondFragment).navigate(R.id.action_SecondFragment_to_FirstFragment);
            //Post Interview handling
            //Back to the initial fragment screen
    }

    public void stopThread(){
        shouldStop = true;
    }
    public static void setFirstTime(long value){
        firstTime = value;
    }
    public static long getFirstTime(){
        return firstTime;
    }
    public static void setSecondTime(long value){
        secondTime = value;
    }
    public static long getSecondTime(){
        return secondTime;
    }
    public static void setThirdTime(long value){
        thirdTime = value;
    }
    public static long getThirdTime(){
        return thirdTime;
    }
    public void cancel(){
        interrupt();
    }
    synchronized public static void setReadyToUploadAudioTrue(){
        readyToUploadAudio = true;
    }
    synchronized public static void setReadyToUploadAudioFalse(){
        readyToUploadAudio = false;
    }
    synchronized public static void setPhotoReadyTrue(){
        photoReady = true;
    }
    synchronized public static void setPhotoReadyFalse(){
        photoReady = false;
    }
    synchronized public static void setPlayingAudioTrue(){
        playingAudio = true;
    }
    synchronized public static void setPlayingAudioFalse(){
        playingAudio = false;
    }
    synchronized public void takePhoto(CameraClass[] cc){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (cc[0] != null){
                    cc[0].takePhoto();
                    //FirebaseExecutor.uploadImage();

                }

            }
        };
        mUI_handler.post(r);

    }
    public void setButtonEvent(AudioPlayer ap, AudioRecorder ar, CameraClass[] cc, InterviewService interviewService, WaveRecorder wr){
        // // set button on click listener
//        view.findViewById(R.id.button_upload_image).setOnClickListener(view12 -> POOL.submit(new FirebaseImageUploader()));
        //view.findViewById(R.id.button_upload).setOnClickListener(view11 -> POOL.submit(new FirebaseUploader()));
//        view.findViewById(R.id.button_answer).setOnClickListener(view10 -> interviewService.recordAnswer());
//        view.findViewById(R.id.button_ask).setOnClickListener(view9 -> interviewService.askQuestion());

        view.findViewById(R.id.button_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        cc[0].takePhoto();
                    }
                };
                mUI_handler.post(r);

            }
        });

        //view.findViewById(R.id.button_play_record).setOnClickListener(view7 -> ap.playAudioSource(mFileName_mp3));

        view.findViewById(R.id.button_play_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ap.playAudioFromFirebase(FirebaseExecutor.downloadAudio());
            }
        });

        //setup the button for start recording
        view.findViewById(R.id.button_start_record).setOnClickListener(view5 -> wr.startRecording());
        //setup the button for stop recording
        view.findViewById(R.id.button_stop_record).setOnClickListener(view6 -> wr.stopRecording(WaveRecorder.FILENAME));

//        view.findViewById(R.id.button_play_audio).setOnClickListener(view4 -> ap.playAudio());

//        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });

    }

}
