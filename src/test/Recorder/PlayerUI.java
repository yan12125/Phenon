package test.Recorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

public class PlayerUI extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_ui);
        loadVideoFromIntent();
    }
    
    protected void loadVideoFromIntent()
    {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String filename = bundle.getString("filename");
        VideoView vv = (VideoView) findViewById(R.id.mainVideoView);
        vv.setVideoURI(Uri.parse("http://140.112.18.203/data/video/output/" + filename));
        vv.start();
    }
}
