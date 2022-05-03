package hk.edu.ouhk.automatedinterview;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewController {
    View view;
    TextView instruction;
    ImageView strokeGrey;
    ImageView strokeGreen;
    ImageView unmuted;
    ImageView muted;
    FrameLayout camera_preview;
    public ViewController(View view, int instructionTextId, int grey_id, int green_id, int unmuted_id, int muted_id,
                          int camera_preview_id
                          ){
        Log.i("AI", "[Start]setting up view controller");
        instruction = view.findViewById(instructionTextId);
        strokeGrey = view.findViewById(grey_id);
        strokeGreen = view.findViewById(green_id);
        unmuted = view.findViewById(unmuted_id);
        muted = view.findViewById(muted_id);

        camera_preview = view.findViewById(camera_preview_id);
        Log.i("AI", "[End]setting up view controller");
    }

    public void setInitialView(){
        instruction.setText("Wait For Connection");
        strokeGrey.setVisibility(View.VISIBLE);
        strokeGreen.setVisibility(View.INVISIBLE);
        muted.setVisibility(View.VISIBLE);
        unmuted.setVisibility(View.INVISIBLE);
    }
    public void switchStroke(String color){

        if (color.equals("green")){
            // switch to green stroke
            strokeGrey.setVisibility(View.INVISIBLE);
            strokeGreen.setVisibility(View.VISIBLE);
        } else if (color.equals("grey")){
            // switch to grey stroke
            strokeGrey.setVisibility(View.VISIBLE);
            strokeGreen.setVisibility(View.INVISIBLE);
        }
    }
    public void switchMicrophoneImage(String action){
        if (action.equals("unmute")){
            // switch to unmuted
            unmuted.setVisibility(View.VISIBLE);
            muted.setVisibility(View.INVISIBLE);
        } else if (action.equals("mute")){
            // switch to muted
            unmuted.setVisibility(View.INVISIBLE);
            muted.setVisibility(View.VISIBLE);
        }
    }
    public void setInstructionText(String str){
        instruction.setText(str);
    }

}
