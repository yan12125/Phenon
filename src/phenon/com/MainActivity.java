package phenon.com;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import phenon.com.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity 
{
    MediaRecorder recorder;
    SurfaceHolder holder;
    boolean recording = false;
    File f;
    String username;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        recorder = new MediaRecorder();
        
        setContentView(R.layout.main);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String dateString = sdf.format(new Date());
        f = new File(Environment.getExternalStorageDirectory(), dateString+".mp4");

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.cameraView);
        holder = cameraView.getHolder();
        
        cameraView.setClickable(true);
        cameraView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (recording) {
                    recorder.stop();
                    recording = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run()
                        {
                            uploadFile(f);
                            Intent returnIntent=new Intent();
                            returnIntent.putExtra("result", 1);
                            setResult(RESULT_OK,returnIntent);
                            finish(); // return to the last activity
                        }
                    }).start();
                } else {
                    recording = true;
                    initRecorder();
                    prepareRecorder();
                    recorder.start();
                }
            }
        });
    }

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoSize(640, 480);
        recorder.setVideoEncodingBitRate(3000000);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        
        recorder.setOutputFile(f.getPath());
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        finish();
    }
    
    protected void uploadFile(File file)
    {
        boolean useSSL = true;
        HttpClient httpclient;
        if(useSSL)
        {
            SSLContextBuilder socketbuilder = new SSLContextBuilder();
            SSLConnectionSocketFactory sslsf = null;
            try
            {
                socketbuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                sslsf = new SSLConnectionSocketFactory(socketbuilder.build());
            }
            catch(GeneralSecurityException e)
            {
                e.printStackTrace(System.err);
                return;
            }
            
            httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();  
        }
        else
        {
            httpclient = new DefaultHttpClient();
        }
        
        HttpPost httppost = new HttpPost("http://140.112.18.203/upload.php");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("video", file);
        builder.addTextBody("submit", "1");
        builder.addTextBody("uploader", username);
        
        httppost.setEntity(builder.build());
         
        System.out.println("executing request " + httppost.getRequestLine());
        try
        {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
         
            System.out.println(response.getStatusLine());
             
            if (resEntity != null) {
              System.out.println(EntityUtils.toString(resEntity));
              resEntity.consumeContent();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
         
        httpclient.getConnectionManager().shutdown();
    }
}
