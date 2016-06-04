package in.silive.audiosync;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javazoom.jl.decoder.*;

public class HomeActivity extends AppCompatActivity {
    static final ArrayList<DatagramSocket> connectedClient = new ArrayList<>();
    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 4326;
    final int CLIENT_PORT = 4321;
    private DatagramSocket serverSocket;
    byte[] buffer ;

    final String TAG = "HomeActivity";
    File tempMp3;


    final static int BUFFER_SIZE = 10240;
    Thread server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
/*
        try {
            PlayAudioTrack("");
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

    }


    public void connect(View v) {
        Thread client1 = new Thread(new ClientThread("Client1"));

        client1.start();
    }

    public void hostServer(View v) {
        server = new Thread(new ServerThread());
        server.start();

    }

    public class ServerThread implements Runnable {
        byte[] recieveData = new byte[BUFFER_SIZE];
        byte[] sendData = new byte[BUFFER_SIZE];
        public Thread streamer;

        @Override
        public void run() {
            try {
                serverSocket = new DatagramSocket(SERVER_PORT);
                Log.d(TAG, "Server Listening for clients.");
                while (true) {

                    DatagramPacket recievePacket = new DatagramPacket(recieveData, recieveData.length);
                    serverSocket.receive(recievePacket);
                    Log.d(TAG, "Client Connected");
                    String str = new String(recievePacket.getData(), 0, recievePacket.getLength());
                    Log.d(TAG, "Client : " + str);
                    InputStream songStream = getApplicationContext().getAssets().open("song.mp3");
                    File tempFile = new File(getFilesDir(), "temp");
                    FileOutputStream fos = new FileOutputStream(tempFile, true);
                    streamer = new Thread(new ClientStreamWriter(recievePacket.getAddress(),
                            new FileInputStream(tempFile), CLIENT_PORT));
                    MP3Decoder decoder = new MP3Decoder(streamer, songStream, fos);
                    decoder.execute();

                    //   streamer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public class MP3Decoder extends AsyncTask {
        InputStream songStream;
        public Thread streamer;
        FileOutputStream fos;

        public MP3Decoder(Thread streamer, InputStream songStream, FileOutputStream fos) {
            this.songStream = songStream;
            this.streamer = streamer;
            this.fos = fos;

        }

        @Override
        protected Object doInBackground(Object[] params) {

            try {
                buffer = decode_path(HomeActivity.this,"",0,sampleRate);
            } catch (IOException e) {
                e.printStackTrace();
            }
finally {
                streamer.start();

            }
return null;        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //     listner.OnComplete();
            Log.d("PO", "PO");

        }
    }

    public class ClientStreamWriter implements Runnable {
        InetAddress clientAddress;
        int port;
        InputStream is;
        DatagramSocket clientSocket;

        public ClientStreamWriter(InetAddress clientAddress, InputStream is, int port) {

            this.clientAddress = clientAddress;
            this.is = is;
            this.port = port;
            try {
                clientSocket = new DatagramSocket();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                int offset = 0;
                /*while ((is.read(buffer, 0, buffer.length)) > -1) {
                    Log.d("SendData", buffer+"");
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, port);
                    clientSocket.send(sendPacket);
                    Thread.sleep(100);
                }*/
while(offset<buffer.length){
    byte[] buffertoSend = new byte[BUFFER_SIZE];
    for(int i =0;i<BUFFER_SIZE;i++){
        buffertoSend[i] = buffer[offset+i];
    }
    DatagramPacket sendPacket = new DatagramPacket(buffertoSend, buffertoSend.length, clientAddress, port);
    clientSocket.send(sendPacket);
    offset+=BUFFER_SIZE;
      // Thread.sleep(50);
}
                //for(byte i:buffer){
/*
                    DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, clientAddress, port);
                    clientSocket.send(sendPacket);
                    Thread.sleep(100);
*/

                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static int sampleRate = 44100;

    public class ClientThread implements Runnable {
        String name;
        byte[] recieveData = new byte[BUFFER_SIZE];
        byte[] sendData = new byte[BUFFER_SIZE];

        public ClientThread(String name) {
            this.sendData = name.getBytes();
        }
        int  iAudioBufSize       = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack track    = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, iAudioBufSize, AudioTrack.MODE_STREAM);

        MediaCodec codec;

        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;

        MediaCodec.BufferInfo bufferInfo;
        int inputBufferIndex;
        int outputBufferIndex;
        int temp=0;
        int mutex=0;
        int count = 512*1024;

        @Override
        public void run() {
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(SERVER_IP), SERVER_PORT);
                DatagramSocket socket = new DatagramSocket();
                socket.send(sendPacket);
                DatagramSocket recieverSocket = new DatagramSocket(CLIENT_PORT);
                // Audio Stream initialisieren:
                track.play();
                while (true) {
mutex=1;
                    DatagramPacket recievePacket = new DatagramPacket(recieveData, recieveData.length);
                    recieverSocket.receive(recievePacket);
                    Log.d("Receive Data", recievePacket.getData() + "");

                     //   while (temp < recievePacket.getLength()) {
                            Log.d("Byte is", recievePacket.getData() + "  " + temp);
                            track.write(recievePacket.getData(), temp, recievePacket.getLength());
                            temp += recievePacket.getLength();
                   //     }
                        //track.write(recievePacket.getData(), temp, count);
                        temp = 0;

                        mutex =0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
    public void PlayAudioTrack(String filePath) throws IOException{
        int intSize = android.media.AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);

        //Reading the file..
        int count = 512 * 1024;
        // 512 kb
        //        byte[] byteData = null;
        //        byteData = new byte[(int)count];

        //we can decode correct byte data here
        byte[] byteData = null;
        byteData = decode_path(HomeActivity.this,filePath, 0, sampleRate);
        int temp =0;
        at.play();
        while (temp<byteData.length)
        {
            Log.d("Byte is",byteData+"  "+temp);
            at.write(byteData, temp, count);
            temp+= count;
        }
        at.stop();
        at.release();
    }

    public static byte[] decode_path(Context context,String path, int startMs, int maxMs)
            throws IOException{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

        float totalMs = 0;
        boolean seeking = true;

        InputStream inputStream = context.getResources().getAssets().open("song.mp3");
        try {
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();

            boolean done = false;
            while (! done) {
                Header frameHeader = bitstream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    totalMs += frameHeader.ms_per_frame();

                    if (totalMs >= startMs) {
                        seeking = false;
                    }
                    if (! seeking) {
                        SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

                        if (output.getSampleFrequency() != sampleRate
                                || output.getChannelCount() != 2) {
                            throw new IllegalArgumentException("mono or non-44100 MP3 not supported");
                        }
                        short[] pcm = output.getBuffer();
                        for (short s : pcm) {
                            outStream.write(s & 0xff);
                            outStream.write((s >> 8 ) & 0xff);
                        }
                    }
                    if (totalMs >= (startMs + maxMs)) {
                        done = true;
                    }
                }
                bitstream.closeFrame();
            }
            return outStream.toByteArray();
        } catch (BitstreamException e) {
            throw new IOException("Bitstream error: " + e);
        } catch (DecoderException e) {
            throw new IOException("Decoder error: " + e);
        }
    }
}
