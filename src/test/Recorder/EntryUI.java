package test.Recorder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jayway.jsonpath.JsonPath;

public class EntryUI extends Activity {
    protected String videos_json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry_ui);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        loadMovies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.login:
                intent = new Intent(this, LoginUI.class);
                startActivity(intent);
                return true;
            case R.id.new_movie:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    protected void loadMovies()
    {
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    URL url = new URL("http://140.112.18.203/api.php?action=getVideos");
                    InputStream in = url.openConnection().getInputStream();
                    videos_json = IOUtils.toString(in);
                    List<Integer> ids = JsonPath.read(videos_json, "$.ids");
                    for(Integer id : ids)
                    {
                        String thumbnail = JsonPath.read(videos_json, String.format("$.%d.thumbnail", id));
                        URL url2 = new URL("http://140.112.18.203/data/video/thumbnails/" + thumbnail);
                        InputStream is = url2.openConnection().getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        is.close();
                        class InnerTask1 implements Runnable
                        {
                            int id;
                            Bitmap thumbnail;
                            public InnerTask1(int _id, Bitmap _thumbnail)
                            {
                                id = _id;
                                thumbnail = _thumbnail;
                            }
                            @Override
                            public void run()
                            {
                                addVideoThumbnail(id, thumbnail);
                            }
                        }
                        runOnUiThread(new InnerTask1(id, bitmap));
                    }
                }
                catch(IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }
        }).start();
    }
    
    protected void addVideoThumbnail(final int videoId, Bitmap thumbnail)
    {
        LinearLayout ll = (LinearLayout) findViewById(R.id.llVideos);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(thumbnail);
        iv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    Bundle bundle = new Bundle();
                    bundle.putInt("videoId", videoId);
                    String filename = JsonPath.read(videos_json, String.format("$.%d.filename", videoId));
                    bundle.putString("filename", filename);
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(EntryUI.this, PlayerUI.class);
                    startActivity(intent);
                }
                return true;
            }
        });
        ll.addView(iv);
    }
}
