package com.videoplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
	
	public final int REQ_CD_FP = 101;
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private FloatingActionButton _fab;
	private HashMap<String, Object> mapvar = new HashMap<>();
	private double index = 0;
	private String path = "";
	private String directory = "";
	private double index1 = 0;
	private double folderExist = 0;
	private double size = 0;
	private String humanReadableSize = "";
	private double folderView = 0;
	private double duration = 0;
	private String videoDuration = "";
	private String selectedPath = "";
	private String videoPath = "";
	private String videoSize = "";
	private String videoHeight = "";
	private String videoWidth = "";
	private String videoOrientation = "";
	private String videoDate = "";
	private double createdTimeMillisec = 0;
	private String formattedDate = "";
	private String url = "";
	
	private ArrayList<HashMap<String, Object>> folders = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> videos = new ArrayList<>();
	private ArrayList<String> list = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> allVideosList = new ArrayList<>();
	
	private LinearLayout linear1;
	private ListView listview1;
	private ListView listview2;
	
	private Intent fp = new Intent(Intent.ACTION_GET_CONTENT);
	private Calendar calendar = Calendar.getInstance();
	private Intent i = new Intent();
	private Intent k = new Intent();
	private AlertDialog.Builder dialog;
	private Intent d = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
			Log.d("__TAG", "onCreate: Permissions");
		} else {
			Log.d("__TAG", "onCreate: Logic");
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		_fab = findViewById(R.id._fab);
		
		linear1 = findViewById(R.id.linear1);
		listview1 = findViewById(R.id.listview1);
		listview2 = findViewById(R.id.listview2);
		fp.setType("*/*");
		fp.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		dialog = new AlertDialog.Builder(this);
		
		listview1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				folderView = 0;
				_getVideos(folders.get((int)_position).get("directory").toString());
			}
		});
		
		listview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> _param1, View _param2, int _param3, long _param4) {
				final int _position = _param3;
				i.setClass(getApplicationContext(), VideoActivity.class);
				mapvar = videos.get((int)_position);
				i.putExtra("data", new Gson().toJson(mapvar));
				startActivity(i);
			}
		});
		
		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				dialog.setTitle("Enter Video link to play");
				LinearLayout layout = new LinearLayout(MainActivity.this);
				layout.setOrientation(LinearLayout.VERTICAL);
				
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				
				final EditText edittxt1 = new EditText(MainActivity.this);
				edittxt1.setHint("Enter Video link");
				layout.addView(edittxt1);
				dialog.setView(layout);
				dialog.setPositiveButton("Play", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						url = edittxt1.getText().toString();
						if (url.equals("")) {
							Utilities.showMessage(getApplicationContext(), "Enter link");
						}
						else {
							d.setClass(getApplicationContext(), VideoActivity.class);
							d.putExtra("k", url);
							startActivity(d);
						}
					}
				});
				dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface _dialog, int _which) {
						
					}
				});
				dialog.create().show();
			}
		});
	}
	
	private void initializeLogic() {
		
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View _v) {
								//do nothing
						}
				});
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white);
		_getAllVideos();
		folderView = 1;
		_getFolders();
		_SortMap(folders, "lowerCaseDirectoryName", false, true);
		listview2.setVisibility(View.GONE);
		listview1.setAdapter(new Listview1Adapter(folders));
		((BaseAdapter)listview1.getAdapter()).notifyDataSetChanged();
		try{ 
			Intent intent = getIntent(); Uri data = intent.getData(); 
			url = data.toString();
			if (!url.equals("")) {
				k.setClass(getApplicationContext(), VideoActivity.class);
				k.putExtra("k", url);
				startActivity(k);
			}
			
			 } catch (Exception e){
		}
	}
	
	@Override
	public void onBackPressed() {
		if (folderView == 0) {
			listview1.setVisibility(View.VISIBLE);
			listview2.setVisibility(View.GONE);
			folderView = 1;
		}
		else {
			finish();
		}
	}
	public void _getAllVideos() {
		Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {
			android.provider.MediaStore.Video.VideoColumns.DATA,
			 android.provider.MediaStore.Video.VideoColumns.SIZE,
			 android.provider.MediaStore.Video.VideoColumns.HEIGHT,
			 android.provider.MediaStore.Video.VideoColumns.WIDTH,
			 android.provider.MediaStore.Video.VideoColumns.DURATION,
			 android.provider.MediaStore.Video.VideoColumns.DATE_MODIFIED
		};
		android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				videoPath = cursor.getString(0);
				videoSize = cursor.getString(1);
				videoHeight = cursor.getString(2);
				videoWidth = cursor.getString(3);
				videoDuration = cursor.getString(4);
				videoDate = cursor.getString(5);
				mapvar = new HashMap<>();
				mapvar.put("videoPath", videoPath);
				mapvar.put("videoSize", videoSize);
				mapvar.put("videoHeight", videoHeight);
				mapvar.put("videoWidth", videoWidth);
				mapvar.put("videoDate", videoDate);
				mapvar.put("videoDuration", videoDuration);
				createdTimeMillisec = new java.io.File(videoPath).lastModified();
				calendar.setTimeInMillis((long)(createdTimeMillisec));
				formattedDate = new SimpleDateFormat("dd MMM").format(calendar.getTime());
				mapvar.put("formattedDate", formattedDate.toUpperCase());
				allVideosList.add(mapvar);
			}
			cursor.close();
		}
	}
	
	
	public void _getFolders() {
		index = 0;
		for(int _repeat12 = 0; _repeat12 < (int)(allVideosList.size()); _repeat12++) {
			path = allVideosList.get((int)index).get("videoPath").toString();
			java.io.File file = new java.io.File(path);
			directory = file.getParent();
			index1 = 0;
			folderExist = 0;
			for(int _repeat36 = 0; _repeat36 < (int)(folders.size()); _repeat36++) {
				if (directory.equals(folders.get((int)index1).get("directory").toString())) {
					folderExist = 1;
					folders.get((int)index1).put("items", String.valueOf((long)(Double.parseDouble(folders.get((int)index1).get("items").toString()) + 1)));
					folders.get((int)index1).put("size", String.valueOf((long)(Double.parseDouble(folders.get((int)index1).get("size").toString()) + FileUtil.getFileLength(path))));
				}
				index1++;
			}
			if (folderExist == 0) {
				mapvar = new HashMap<>();
				mapvar.put("directory", directory);
				mapvar.put("directoryName", Uri.parse(directory).getLastPathSegment());
				mapvar.put("lowerCaseDirectoryName", Uri.parse(directory).getLastPathSegment().toLowerCase());
				mapvar.put("items", "1");
				mapvar.put("size", String.valueOf((long)(FileUtil.getFileLength(path))));
				folders.add(mapvar);
				Log.d("__TAG", "Folder Exist");
			}
			else{
				Log.d("__TAG", "Folder not Exist");
			}
			Log.d("__TAG", "getView: "+folders.toString());
			index++;
		}
	}
	
	
	public void _SortMap(final ArrayList<HashMap<String, Object>> _listMap, final String _key, final boolean _isNumber, final boolean _Ascending) {
		Collections.sort(_listMap, new Comparator<HashMap<String,Object>>(){
			public int compare(HashMap<String,Object> _compareMap1, HashMap<String,Object> _compareMap2){
				if (_isNumber) {
					int _count1 = Integer.valueOf(_compareMap1.get(_key).toString());
					int _count2 = Integer.valueOf(_compareMap2.get(_key).toString());
					if (_Ascending) {
						return _count1 < _count2 ? -1 : _count1 < _count2 ? 1 : 0;
					}
					else {
						return _count1 > _count2 ? -1 : _count1 > _count2 ? 1 : 0;
					}
				}
				else {
					if (_Ascending) {
						return (_compareMap1.get(_key).toString()).compareTo(_compareMap2.get(_key).toString());
					}
					else {
						return (_compareMap2.get(_key).toString()).compareTo(_compareMap1.get(_key).toString());
					}
				}
			}});
	}
	
	
	public void _library() {
	}
	private String bytesIntoHumanReadable(long bytes) {
		    long kilobyte = 1024;
		    long megabyte = kilobyte * 1024;
		    long gigabyte = megabyte * 1024;
		    long terabyte = gigabyte * 1024;
		
		    if ((bytes >= 0) && (bytes < kilobyte)) {
			        return bytes + " B";
			
			    } else if ((bytes >= kilobyte) && (bytes < megabyte)) {
			        return (bytes / kilobyte) + " KB";
			
			    } else if ((bytes >= megabyte) && (bytes < gigabyte)) {
			        return (bytes / megabyte) + " MB";
			
			    } else if ((bytes >= gigabyte) && (bytes < terabyte)) {
			        return (bytes / gigabyte) + " GB";
			
			    } else if (bytes >= terabyte) {
			        return (bytes / terabyte) + " TB";
			
			    } else {
			        return bytes + " Bytes";
			    }
	}
	{
	}
	private String stringForTime(int timeMs) {
		StringBuilder mFormatBuilder = null;
		Formatter mFormatter = null;
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
		        int totalSeconds = timeMs / 1000;
		
		        int seconds = totalSeconds % 60;
		        int minutes = (totalSeconds / 60) % 60;
		        int hours = totalSeconds / 3600;
		
		        mFormatBuilder.setLength(0);
		        if (hours > 0) {
			            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
			        } else {
			            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
			        }
		    }
	{
	}
	
	
	public void _shapeRadius(final View _v, final String _color, final double _radius) {
		android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
		  shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
		
		shape.setCornerRadius((int)_radius);
		
		shape.setColor(Color.parseColor(_color));
		_v.setBackgroundDrawable(shape);
	}
	
	
	public void _getVideos(final String _directory) {
		videos.clear();
		index = 0;
		for(int _repeat11 = 0; _repeat11 < (int)(allVideosList.size()); _repeat11++) {
			path = allVideosList.get((int)index).get("videoPath").toString();
			java.io.File file = new java.io.File(path);
			directory = file.getParent();
			if (directory.equals(_directory)) {
				mapvar = allVideosList.get((int)index);
				videos.add(mapvar);
			}
			index++;
		}
		listview1.setVisibility(View.GONE);
		listview2.setAdapter(new Listview2Adapter(videos));
		((BaseAdapter)listview2.getAdapter()).notifyDataSetChanged();
		Log.d("__TAG", "Adapter Notified "+videos.toString());
		listview2.setVisibility(View.VISIBLE);
	}
	
	public class Listview1Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Listview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.folder_item, null);
			}
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final ImageView imageview1 = _view.findViewById(R.id.imageview1);
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final TextView textview1 = _view.findViewById(R.id.textview1);
			final LinearLayout linear3 = _view.findViewById(R.id.linear3);
			final TextView textview2 = _view.findViewById(R.id.textview2);
			final TextView textview3 = _view.findViewById(R.id.textview3);
			
			_shapeRadius(textview3, "#425B7C", 5);
			textview1.setText(folders.get((int)_position).get("directoryName").toString());
			textview2.setText(folders.get((int)_position).get("items").toString().concat(" videos"));
			size = Double.parseDouble(folders.get((int)_position).get("size").toString());
			humanReadableSize = bytesIntoHumanReadable((long)size);
			textview3.setText(humanReadableSize);

			Log.d("__TAG", "getView: "+folders.toString());
			
			return _view;
		}
	}
	
	public class Listview2Adapter extends BaseAdapter {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Listview2Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.video_item, null);
			}
			
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout linear2 = _view.findViewById(R.id.linear2);
			final LinearLayout linear4 = _view.findViewById(R.id.linear4);
			final LinearLayout linear3 = _view.findViewById(R.id.linear3);
			final LinearLayout linear7 = _view.findViewById(R.id.linear7);
			final ImageView imageview1 = _view.findViewById(R.id.imageview1);
			final TextView textview1 = _view.findViewById(R.id.textview1);
			final TextView textview2 = _view.findViewById(R.id.textview2);
			final LinearLayout linear5 = _view.findViewById(R.id.linear5);
			final TextView textview3 = _view.findViewById(R.id.textview3);
			final TextView textview4 = _view.findViewById(R.id.textview4);
			
			_shapeRadius(textview1, "#000000", 5);
			_shapeRadius(textview3, "#425B7C", 5);
			_shapeRadius(textview4, "#425B7C", 5);
			duration = Double.parseDouble(videos.get((int)_position).get("videoDuration").toString());
			videoDuration = stringForTime((int)duration);
			textview1.setText(videoDuration);
			path = videos.get((int)_position).get("videoPath").toString();
			//using cardview programmatically inside listview is not good. use recyclerview instead of listview.
			androidx.cardview.widget.CardView cardview1 = new androidx.cardview.widget.CardView(MainActivity.this);
			cardview1.setCardElevation(0);
			cardview1.setRadius(10);
			ViewGroup imageParent = ((ViewGroup)imageview1.getParent()); imageParent.removeView(imageview1);
			cardview1.addView(imageview1);
			imageParent.addView(cardview1);
			textview2.setText(Uri.parse(path).getLastPathSegment());
			size = Double.parseDouble(videos.get((int)_position).get("videoSize").toString());
			humanReadableSize = bytesIntoHumanReadable((long)size);
			textview3.setText(humanReadableSize);
			com.bumptech.glide.Glide.with(getApplicationContext())
			.load(path)
			.into(imageview1);
			textview4.setText(videos.get((int)_position).get("formattedDate").toString());
			
			return _view;
		}
	}
	

}
