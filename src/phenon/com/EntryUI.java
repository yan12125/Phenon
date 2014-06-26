package phenon.com;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import phenon.com.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jayway.jsonpath.JsonPath;

public class EntryUI extends Activity implements OnClickListener {
	protected final int thumbnail_width = 320; // also hardcoded in server
	protected String videos_json;
	protected int screen_width;
	protected Button button;
	protected ListView listview1;
	protected ListView listview2;
	protected ArrayList<Bitmap> video_cover;
	protected ArrayList<String> video_name;
	protected String username;
	protected ArrayList<String> users;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		username = intent.getStringExtra("username");
		setContentView(R.layout.activity_entry_ui);
		button = (Button) findViewById(R.id.new_movie);
		listview1 = (ListView) findViewById(R.id.listView1);
		listview2 = (ListView) findViewById(R.id.listView2);
		video_cover = new ArrayList<Bitmap>();
		video_name = new ArrayList<String>();
		users=new ArrayList<String>();
		button.setOnClickListener(this);
		setlistview();

		getScreenSize();
	}

	private void setlistview() {
		// TODO Auto-generated method stub
		video_name.clear();
		video_cover.clear();
		users.clear();
		loadMovies();
		ArrayAdapter<String> usersadapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, users);
		MyCustomBaseAdapter adapter = new MyCustomBaseAdapter(this, video_name,
				video_cover);
		listview1.setAdapter(adapter);
		listview2.setAdapter(usersadapter);

		listview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Bundle bundle = new Bundle();
				bundle.putString("filename", video_name.get(arg2));
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(EntryUI.this, PlayerUI.class);
				// startActivity(intent);
				startActivityForResult(intent, 1);

			}

		});

		listview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				

			}

		});

	}

	private class MyCustomBaseAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<String> listname;
		private ArrayList<Bitmap> listcover;

		public MyCustomBaseAdapter(Context context, ArrayList<String> n,
				ArrayList<Bitmap> c) {
			listname = n;
			listcover = c;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listname.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			Log.w("id_getView", Integer.toString(arg0));
			arg1 = mInflater.inflate(R.layout.baseadapter, null);
			TextView textView = (TextView) arg1.findViewById(R.id.label);
			ImageView imageView = (ImageView) arg1.findViewById(R.id.icon);
			textView.setText(listname.get(arg0));
			imageView.setImageBitmap(listcover.get(arg0));
			return arg1;
		}

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	protected void loadMovies() {
		Thread mthread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(
							"http://140.112.18.203/api.php?action=getAllUsers");
					InputStream in = url.openConnection().getInputStream();
					String users_json = IOUtils.toString(in);
					users = JsonPath.read(users_json, "$");
					in.close();

					url = new URL(
							"http://140.112.18.203/api.php?action=getVideos");
					in = url.openConnection().getInputStream();
					videos_json = IOUtils.toString(in);
					List<Integer> ids = JsonPath.read(videos_json, "$.ids");
					for (Integer id : ids) {
						String thumbnail = JsonPath.read(videos_json,
								String.format("$.%d.thumbnail", id));
						String filename = JsonPath.read(videos_json,
								String.format("$.%d.filename", id));
						video_name.add(filename);
						Log.w("id", id.toString());
						URL url2 = new URL(
								"http://140.112.18.203/data/video/thumbnails/"
										+ thumbnail);
						InputStream is = url2.openConnection().getInputStream();
						video_cover.add(BitmapFactory.decodeStream(is));
						is.close();

					}
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		});
		mthread.start();
		try {
			mthread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * protected void addVideoThumbnail(final int videoId, Bitmap thumbnail) {
	 * TableLayout ll = () findViewById(R.id.llVideos); ImageView iv = new
	 * ImageView(this); iv.setImageBitmap(thumbnail); iv.setOnTouchListener(new
	 * OnTouchListener() {
	 * 
	 * @Override public boolean onTouch(View v, MotionEvent event) { if
	 * (event.getAction() == MotionEvent.ACTION_UP) { Bundle bundle = new
	 * Bundle(); bundle.putInt("videoId", videoId); String filename =
	 * JsonPath.read(videos_json, String.format("$.%d.filename", videoId));
	 * bundle.putString("filename", filename); Intent intent = new Intent();
	 * intent.putExtras(bundle); intent.setClass(EntryUI.this, PlayerUI.class);
	 * startActivity(intent); } return true; } }); // add to the last row
	 * TableRow lastRow = (TableRow) ll.getChildAt(ll.getChildCount() - 1); if
	 * ((lastRow.getChildCount() + 1) * thumbnail_width > screen_width) {
	 * TableRow newRow = new TableRow(this); newRow.addView(iv);
	 * ll.addView(newRow); } else { lastRow.addView(iv); } }
	 */
	protected void getScreenSize() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screen_width = size.x;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch (arg0.getId()) {
		case R.id.new_movie:
			intent = new Intent(this, MainActivity.class);
			intent.putExtra("username", username);
			startActivityForResult(intent, 1);
			break;
		default:
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				setlistview();
			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
				setlistview();
			}
		}
	}// onActivityResult

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.login:
			intent = new Intent(this, LoginUI.class);
			startActivityForResult(intent, 1);
			return true;
		case R.id.new_movie:
			intent = new Intent(this, MainActivity.class);
			intent.putExtra("username", username);
			startActivityForResult(intent, 1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
