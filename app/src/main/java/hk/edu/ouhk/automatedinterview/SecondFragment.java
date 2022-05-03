package hk.edu.ouhk.automatedinterview;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import static hk.edu.ouhk.automatedinterview.FirebaseExecutor.POOL;

public class SecondFragment extends Fragment {
    final String audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/audio/";
    //final String photo_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AutomatedInterview/photo/";
    static Context CONTEXT;
    Handler mUI_handler = new Handler();
    Handler mThreadHandler;
    HandlerThread mThread;
    InterviewThread[] threads;

//    ViewController vc;
//    AudioPlayer ap;
//    AudioRecorder ar;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        CONTEXT = getContext();
//        ap = new AudioPlayer(getContext());
//        ar = new AudioRecorder(getContext());
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final CameraClass[] cc = new CameraClass[1];
        InterviewThread interviewThread;
        threads = new InterviewThread[1];

        //String mFileName_wav = audio_path + "test.wav";
        //String answerAudio = audio_path + "answer.mp3";
        //CloudStorageService css = new CloudStorageService();
        //FirebaseUploader fbu = new FirebaseUploader();
        //ExecutorService pool = Executors.newFixedThreadPool(1);
//        Future<String> future = POOL.submit(new CloudStorageService());
//        String audio_url = "";
//        try {
//            audio_url = future.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                cc[0] = new CameraClass(getContext(), view);
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                mUI_handler.post(r1);
                //CameraClass cc = new CameraClass(getContext(), view);
                Log.i("Second Fragment", "Creating Interview Thread");
                threads[0] = new InterviewThread(getContext(), getView(), getActivity(), cc,
                        mThreadHandler, mUI_handler, SecondFragment.this);
                threads[0].start();
            }
        };
/*        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                Log.i("Second Fragment", "TEST");
                endInterview();

            }
        };*/
        mThreadHandler.post(r2);
        //mUI_handler.post(r3);

//        CameraClass cc = new CameraClass(getContext(), view);
//        InterviewThread interviewThread = new InterviewThread(getContext(), getView(), getActivity(), cc, mThreadHandler);
//        interviewThread.start();

        //ar.setMediaRecorder();
        //interviewService.initiateInterview();
        //vc.setInstructionText(getActivity().getResources().getString(R.string.listen_instruction));

       // // set buttons on click listener
/*        view.findViewById(R.id.button_upload_image).setOnClickListener(view12 -> POOL.submit(new FirebaseImageUploader()));
        view.findViewById(R.id.button_upload).setOnClickListener(view11 -> POOL.submit(new FirebaseUploader()));
        view.findViewById(R.id.button_answer).setOnClickListener(view10 -> interviewService.recordAnswer());
        view.findViewById(R.id.button_ask).setOnClickListener(view9 -> interviewService.askQuestion());

        view.findViewById(R.id.button_take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cc.takePhoto();
                    }
                });
                thread.start();
            }
        });

        //view.findViewById(R.id.button_play_record).setOnClickListener(view7 -> ap.playAudioSource(mFileName_mp3));

        view.findViewById(R.id.button_play_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ap.playAudioFromFirebase(FirebaseExecutor.downloadAudio());
            }
        });

        view.findViewById(R.id.button_start_record).setOnClickListener(view5 -> ar.startRecord());

        view.findViewById(R.id.button_stop_record).setOnClickListener(view6 -> ar.stopRecord());

//        view.findViewById(R.id.button_play_audio).setOnClickListener(view4 -> ap.playAudio());

//        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(SecondFragment.this)
//                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
//            }
//        });*/


    }
    public void endInterview(){
        StringBuilder sb = new StringBuilder();
        sb.append("This is the end of the interview\n");
        sb.append("Thank you for attending the interview \n");
        sb.append("and using this application");
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setCancelable(false);
        dialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i("Second Fragment", "Message showed");
                //NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);

                if(threads[0].cameraClass[0].mCamera != null) {
                    threads[0].cameraClass[0].mCamera.stopPreview();
                    threads[0].cameraClass[0].mCamera.setPreviewCallback(null);
                    threads[0].cameraClass[0].mCamera.release();
                    threads[0].cameraClass[0].mCamera = null;
                }
                    //threads[0].interrupt();


                //Direct to First Fragment (Start Page)
                NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);
                //Destroy Second Fragment (Post-interivew Handling)
                getFragmentManager().beginTransaction().remove(SecondFragment.this).commit();
            }
        });
        dialog.setTitle("Message");
        dialog.setMessage(sb.toString());
        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(threads[0] != null) {
            if (threads[0].cameraClass[0] != null) {
                if(threads[0].cameraClass[0].mCamera != null) {
                    threads[0].cameraClass[0].mCamera.stopPreview();
                    threads[0].cameraClass[0].mCamera.setPreviewCallback(null);
                    threads[0].cameraClass[0].mCamera.release();
                    threads[0].cameraClass[0].mCamera = null;
                }

            }
            threads[0].stopThread();
            threads[0] = null;
        }
        if (mThread != null) {
            mThread.quit();
        }
        if (mUI_handler != null){
            mUI_handler.removeCallbacksAndMessages(null);
        }

    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if(threads[0] != null) {
            if (threads[0].cameraClass[0] != null) {
                if(threads[0].cameraClass[0].mCamera != null) {
                    threads[0].cameraClass[0].mCamera.stopPreview();
                    threads[0].cameraClass[0].mCamera.setPreviewCallback(null);
                    threads[0].cameraClass[0].mCamera.release();
                    threads[0].cameraClass[0].mCamera = null;
                }

            }
            threads[0].stopThread();
            threads[0] = null;
        }
/*        if (threads[0].cameraClass[0] != null){
            threads[0].cameraClass[0].mCamera.stopPreview();
            threads[0].cameraClass[0].mCamera.setPreviewCallback(null);
            threads[0].cameraClass[0].mCamera.release();
            threads[0].cameraClass[0].mCamera = null;
        }*/
        if (mThreadHandler != null) {
            //mThreadHandler.removeCallbacks();
        }
        if (mThread != null) {
            mThread.quit();
        }
        if (mUI_handler != null){
            mUI_handler.removeCallbacksAndMessages(null);
        }
    }

}


