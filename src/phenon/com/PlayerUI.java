package phenon.com;

import phenon.com.R;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

public class PlayerUI extends Activity {

	private VideoView vv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_ui);
        loadVideoFromIntent();
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
        {           
            public void onCompletion(MediaPlayer mp) 
            {
                // Do whatever u need to do here
              Intent returnIntent=new Intent();
              returnIntent.putExtra("result", 1);
              setResult(RESULT_OK,returnIntent);
              finish(); // return to the last activity

            }           
        });
        
    }
    
    protected void loadVideoFromIntent()
    {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String filename = bundle.getString("filename");
        vv = (VideoView) findViewById(R.id.mainVideoView);
        vv.setVideoURI(Uri.parse("http://140.112.18.203/data/video/output/" + filename));
        vv.start();

        
    }
}
