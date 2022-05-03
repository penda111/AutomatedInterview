package hk.edu.ouhk.automatedinterview;

import android.util.JsonReader;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class ClientSocket {
    //Create socket
    //Host = 192.168.0.100
    //Port = 8080


//    static final int PORT = 8080;
    static final String HOST = "HOST ADDRESS";
    static final int PORT = 8852;
    Socket s;
    //InputStream in;

/*    static {
        try {
            //Log.i("Client Socket", "Connect to HOST: "+HOST+" PORT: "+PORT);
            //s = new Socket(HOST, PORT);
            //Log.i("Client Socket", "Connect to HOST: "+HOST+" PORT: "+PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public ClientSocket(){
        try {
            Log.i("Client Socket", "Start Connection");
            s = new Socket(HOST, PORT);
            s.setKeepAlive(true);
            //in = s.getInputStream();
            Log.i("Client Socket", "Connect to HOST: "+HOST+" PORT: "+PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    synchronized public void sendData(String... values){

        String[] names = new String[] {"type", "status","url", "response_time", "duration_time", "photoURL"};
        JSONObject json = new JSONObject();
        try{
            for (int i = 0 ; i < names.length; i++){
                String str = "";
                if(!values[i].isEmpty()){
                    str = values[i];
                }
                json.put(names[i], str);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        try {
            OutputStreamWriter out = new OutputStreamWriter(s.getOutputStream()
                    , StandardCharsets.UTF_8);
            out.write(json.toString());
            Log.i("Client Socket", "Sent: "+json.toString());
            out.flush();

        } catch (IOException e){

        }
    }
    synchronized public String[] receiveData(DataInputStream dis){
        final String[] str = {"", ""};
/*
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //Log.i("Client Socket", "Start Reading");
                    InputStreamReader in = new InputStreamReader(s.getInputStream(),"UTF-8");
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    //Log.i("Client Socket", "socket input stream is null " + (s.getInputStream()==null));
                    BufferedReader br = new BufferedReader(in);
                    StringBuilder sb = new StringBuilder();
                    //JsonReader jr = new JsonReader(in);
                    String input;
*/
/*            while((input = br.readLine()) != null){
                Log.i("Client Socket", "Start Reading Line");
                sb.append(input);
            }
            Log.i("Client Socket", "Ended Read");
            str[0] = sb.toString();
            Log.i("Client Socket", "Receive "+sb.toString());*/
        /*

                   // Log.i("Client Socket", "available "+dis.available());
                    while((input = br.readLine()) != null){
                        System.out.println(input);
                        Log.i("Client Socket", "Receive Line" + input );
                        sb.append(input);
                    }
                    str[0] = sb.toString();
                    Log.i("Client Socket", "Receive "+str[0]);
                    //in.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.start();
*/
/*        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataInputStream inputStream = null;
                    String strInputstream = "";
                    inputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] by = new byte[4096];
                    int n;
                    //Log.i("Client Socket", "Receiver Reader setup completed");
                    while ((n = inputStream.read(by)) != -1) {
                        baos.write(by, 0, n);
                    }
                    strInputstream = new String(baos.toByteArray());
                    //Log.i("Client Socket", "Received Data " + strInputstream);
                    //s.shutdownInput();
                    // inputStream.close();
                    baos.close();
                    if(!strInputstream.isEmpty()) {
                        JSONObject json = new JSONObject(strInputstream);
                        String instruction = json.getString("type");
                        str[0] = instruction;
                        //Log.i("Client Socket", "Return received " + str[0]);
                    }
                    //return str[0];
                    //Log.i("Client Socket", "Received json" + json[0].toString());
                }catch (IOException | JSONException e){
                    e.printStackTrace();
                }
            }
        });
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.start();*/
        try {
            DataInputStream inputStream = dis;//null;
            String strInputstream = "";
            //inputStream = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] by = new byte[4096];
            int n;
            //Log.i("Client Socket", "Receiver Reader setup completed");
            if ((n = inputStream.read(by)) != -1) {
                Log.i("Client Socket", "Bytes "+String.valueOf(n));
                baos.write(by, 0, n);
                Log.i("Client Socket", "Wrote");

            }
            strInputstream = new String(baos.toByteArray());
            //Log.i("Client Socket", "Received Data " + strInputstream);
            //s.shutdownInput();
            // inputStream.close();
            baos.close();
            LocalDateTime dateTime = LocalDateTime.now();
            if(!strInputstream.isEmpty()) {
                Log.i("Client Socket", "4");
                JSONObject json = new JSONObject(strInputstream);
                if(json.has("type")) {
                    String instruction = json.getString("type");
                    Log.i("Client Socket", dateTime+" Received type [" + instruction+"]");
                    str[0] = instruction;
                }
                if(json.has("photo")) {
                    String photoRequired = json.getString("photo"); // "true" or "false"
                    str[1] = photoRequired;
                }
                //Log.i("Client Socket", "Return received " + str[0]);
            }
            //return str[0];
            //Log.i("Client Socket", "Received json" + json[0].toString());
        }catch (IOException | JSONException e){
            e.printStackTrace();
        }
/*        try {
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;
            String strInputstream = "";
            inputStream = new DataInputStream(s.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] by = new byte[2048];
            int n;
            //Log.i("Client Socket", "Receiver Reader setup completed");
            while ((n = inputStream.read(by)) != -1) {
                baos.write(by, 0, n);
            }
            strInputstream = new String(baos.toByteArray());
            //Log.i("Client Socket", "Received Data " + strInputstream);
            //s.shutdownInput();
            // inputStream.close();
            baos.close();
            JSONObject json = new JSONObject(strInputstream);
            String instruction = json.getString("type");
            str[0] = instruction;
            Log.i("Client Socket", "Return received" + instruction);
            //return str[0];
            //Log.i("Client Socket", "Received json" + json[0].toString());
        }catch (IOException | JSONException e){
            e.printStackTrace();
        }*/
        Log.i("Client Socket", "Return received " + str[0]);
        return str;
/*        try {
            DataInputStream inputStream = null;

            String strInputstream = "";
            inputStream = new DataInputStream(s.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] by = new byte[2048];
            int n;
            while ((n = inputStream.read(by)) != -1) {
                baos.write(by, 0, n);
            }
            strInputstream = new String(baos.toByteArray());

            // inputStream.close();
            baos.close();
            str[0] = strInputstream;

            Log.i("Client Socket", "Received Data" + );
            return str[0];
        } catch (IOException | JSONException e ){
            e.printStackTrace();
        }*/


        //Log.i("Client Socket", "Start Receiving");
/*        try{
            //Log.i("Client Socket", "Start Reading");
            InputStreamReader in = new InputStreamReader(s.getInputStream(),"UTF-8");
            DataInputStream dis = new DataInputStream(s.getInputStream());
            //Log.i("Client Socket", "socket input stream is null " + (s.getInputStream()==null));
            BufferedReader br = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            //JsonReader jr = new JsonReader(in);
            String input;
*//*            while((input = br.readLine()) != null){
                Log.i("Client Socket", "Start Reading Line");
                sb.append(input);
            }
            Log.i("Client Socket", "Ended Read");
            str[0] = sb.toString();
            Log.i("Client Socket", "Receive "+sb.toString());*//*
            while(dis.available() > 0){
                sb.append(dis.readUTF());
            }
            str[0] = sb.toString();
            Log.i("Client Socket", "Receive "+sb.toString());
            in.close();
            return str[0];
        } catch (IOException e){
            e.printStackTrace();
        }*/
//        Log.i("Client Socket", "Return received" + str[0]);
//        return str[0];
    }

    public  void closeSocket(){
        try {
            this.s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  void startSocket(){
        try {
            this.s = new Socket(HOST, PORT);
            this.s.setKeepAlive(true);
            Log.i("Client Socket", "Connect to HOST: "+HOST+" PORT: "+PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
