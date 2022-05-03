package hk.edu.ouhk.automatedinterview;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class InterviewService {
    /*a class that controls other classes of different services or functions
    * AudioPlayer
    * AudioRecorder
    * CameraClass
    * ViewController
    *
    * CloudStorageService
    * FirebaseUploader
    * FirebaseUploader
    *
    *
    *
    * */

    Activity activity;
    View view;
    AudioPlayer audioPlayer;
    AudioRecorder audioRecorder;
    WaveRecorder waveRecorder;
    CameraClass cameraClass;
    ViewController viewController;
    Handler mUI_handler;
    ClientSocket clientSocket;
    public InterviewService(
            Activity a,
            View v,
            AudioPlayer ap,
            AudioRecorder ar,
            CameraClass cc,
            ViewController vc,
            Handler mUI_handler,
            ClientSocket cs,
            WaveRecorder wr){
        activity = a;
        view = v;
        audioPlayer = ap;
        audioRecorder = ar;
        cameraClass = cc;
        viewController = vc;
        this.mUI_handler = mUI_handler;
        clientSocket = cs;
        waveRecorder = wr;
    }
    public void setInstructionMessage(String instructionMessage){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                TextView instruction = view.findViewById(R.id.instruction_msg);
                instruction.setText(instructionMessage);
            }
        };
        mUI_handler.post(r);

    }
    public void initiateInterview(){
        // setting up UI
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //viewController.setInitialView();
                TextView instruction = view.findViewById(R.id.instruction_msg);
                ImageView strokeGrey = view.findViewById(R.id.chatbot_stroke_grey);
                ImageView strokeGreen = view.findViewById(R.id.chatbot_stroke_green);
                ImageView unmuted = view.findViewById(R.id.unmuted);
                ImageView muted = view.findViewById(R.id.muted);

                instruction.setText("Wait For Connection");
                strokeGrey.setVisibility(View.VISIBLE);
                strokeGreen.setVisibility(View.INVISIBLE);
                muted.setVisibility(View.VISIBLE);
                unmuted.setVisibility(View.INVISIBLE);
                Button start = view.findViewById(R.id.button_start_record);
                Button stop = view.findViewById(R.id.button_stop_record);
                Button upload = view.findViewById(R.id.button_upload);
                start.setEnabled(false);
                stop.setEnabled(false);
                upload.setEnabled(false);

            }
        };
        mUI_handler.post(r);
        //viewController.setInitialView();
        //initiate server connection and start receiving audio and send audio and photo
        //...
    }
    public void setButtonEvent_Answer(){
        //recordAnswer();
        Runnable r = new Runnable() {
            @Override
            public void run() {

                Button start = view.findViewById(R.id.button_start_record);
                Button stop = view.findViewById(R.id.button_stop_record);
                Button upload = view.findViewById(R.id.button_upload);

                TextView instruction = view.findViewById(R.id.instruction_msg);
                ImageView strokeGrey = view.findViewById(R.id.chatbot_stroke_grey);
                ImageView strokeGreen = view.findViewById(R.id.chatbot_stroke_green);
                ImageView unmuted = view.findViewById(R.id.unmuted);
                ImageView muted = view.findViewById(R.id.muted);

                start.setEnabled(true);
                stop.setEnabled(false);
                upload.setEnabled(false);
                start.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start.setEnabled(false);
                        //audioRecorder.startRecord();
                        waveRecorder.startRecording();
                        stop.setEnabled(true);
                        InterviewThread.setSecondTime(System.currentTimeMillis());
                    }
                });
                stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stop.setEnabled(false);
                        //audioRecorder.stopRecord();
                        waveRecorder.stopRecording();
                        upload.setEnabled(true);
                        muted.setVisibility(View.VISIBLE);
                        unmuted.setVisibility(View.INVISIBLE);
                        InterviewThread.setThirdTime(System.currentTimeMillis());
                    }
                });
                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        upload.setEnabled(false);
//                        FirebaseExecutor.uploadAudio(clientSocket);

                        instruction.setText("Wait for next the Question");

//                        Button start = view.findViewById(R.id.button_start_record);
//                        Button stop = view.findViewById(R.id.button_stop_record);
                        //Button upload = view.findViewById(R.id.button_upload);
//                        start.setEnabled(false);
//                        stop.setEnabled(false);
                        upload.setEnabled(false);
                        InterviewThread.setReadyToUploadAudioTrue();


                    }
                });

            }
        };
        mUI_handler.post(r);


/*        Button start = view.findViewById(R.id.button_start_record);
        Button stop = view.findViewById(R.id.button_stop_record);
        Button upload = view.findViewById(R.id.button_upload);
        start.setEnabled(true);
        stop.setEnabled(false);
        upload.setEnabled(false);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start.setEnabled(false);
                audioRecorder.startRecord();
                stop.setEnabled(true);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop.setEnabled(false);
                audioRecorder.stopRecord();
                upload.setEnabled(true);
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseExecutor.uploadAudio();
            }
        });*/
    }
    public void switchStrokeGrey(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                viewController.switchStroke("grey");
            }
        };
        mUI_handler.post(r);
    }
    public void askQuestion(){
        //UI control
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Button start = view.findViewById(R.id.button_start_record);
                Button stop = view.findViewById(R.id.button_stop_record);
                Button upload = view.findViewById(R.id.button_upload);
                start.setEnabled(false);
                stop.setEnabled(false);
                upload.setEnabled(false);
                viewController.switchStroke("green");
                viewController.switchMicrophoneImage("mute");
                viewController.setInstructionText(activity.getResources().getString(R.string.listen_instruction));
            }
        };
        mUI_handler.post(r);
//        viewController.switchStroke("green");
//        viewController.switchMicrophoneImage("mute");
//        viewController.setInstructionText(activity.getResources().getString(R.string.listen_instruction));
        //Play Question Audio
        //audioPlayer.playAudioFromFirebase(FirebaseAudioDownloader.callerAudioDownload());
        audioPlayer.playAudioFromFirebase(FirebaseExecutor.downloadAudio());

    }
    public void recordAnswer(){
        //UI control
        Runnable r = new Runnable() {
            @Override
            public void run() {
                viewController.switchStroke("grey");
                viewController.switchMicrophoneImage("unmute");
                viewController.setInstructionText(activity.getResources().getString(R.string.answer_instruction));
            }
        };
//        viewController.switchStroke("grey");
//        viewController.switchMicrophoneImage("unmute");
//        viewController.setInstructionText(activity.getResources().getString(R.string.answer_instruction));
        mUI_handler.post(r);
        Log.i("Interview Service","recordAnswer()");

    }
    public String receiveInstruction(){
        Log.i("Interview Service", "[Start] Receive Instruction");
/*        JSONObject json = clientSocket.receiveData();
        String instruction = "";
        try {
            instruction = json.getString("type");
            return instruction;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return instruction;*/
        return "";
    }
    public void sendDataToServer(){
        //ClientSocket.sendData("answer", "");
    }
}
