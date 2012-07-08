package mobi.omegacentauri.FastLaunch;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import mobi.omegacentauri.FastLaunch.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Popup extends Activity {
	ListView appsList;
	Resources res;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.popup);
        
        appsList = (ListView)findViewById(R.id.apps);
//        LayoutParams lp = getWindow().getAttributes();
//        
//        lp.screenBrightness = 0.1f;
//        lp.flags |= LayoutParams.FLAG_NOT_TOUCH_MODAL;
        
///*        	
//        	LayoutParams.FLAG_NOT_FOCUSABLE |
//        	LayoutParams.FLAG_NOT_TOUCH_MODAL |
//        	LayoutParams.FLAG_NOT_TOUCHABLE; */
//        this.getWindow().setAttributes(lp);
//        this.getWindow().setCallback(new Callback(){
//
//			@Override
//			public boolean dispatchKeyEvent(KeyEvent event) {
//				Popup.this.getWindow().superDispatchKeyEvent(event);
//				Log.v("Key", ""+event);
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean dispatchPopulateAccessibilityEvent(
//					AccessibilityEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean dispatchTouchEvent(MotionEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean dispatchTrackballEvent(MotionEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public void onAttachedToWindow() {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onContentChanged() {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public boolean onCreatePanelMenu(int featureId, Menu menu) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public View onCreatePanelView(int featureId) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public void onDetachedFromWindow() {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public boolean onMenuItemSelected(int featureId, MenuItem item) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean onMenuOpened(int featureId, Menu menu) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public void onPanelClosed(int featureId, Menu menu) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public boolean onPreparePanel(int featureId, View view, Menu menu) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public boolean onSearchRequested() {
//				// TODO Auto-generated method stub
//				return false;
//			}
//
//			@Override
//			public void onWindowAttributesChanged(LayoutParams attrs) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onWindowFocusChanged(boolean hasFocus) {
//				// TODO Auto-generated method stub
//				
//			}});
//        return;
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		
		makeList();
	}
	
	public void makeList() {
    	final ArrayList<Entry> entries = new ArrayList<Entry>();
    	
    	PackageManager pm = getPackageManager();
    	
    	SharedPreferences pref = getSharedPreferences(Apps.PREF_APPS, 0);
    	Map<String,?> map = pref.getAll();
    	
    	for (String key:map.keySet()) {
		    entries.add(new Entry(key, 
		    		MyApplicationInfo.getSmartLabel(this, key, (String)map.get(key))));
    	}
    	
    	final Context context = this;
    	
		ArrayAdapter<Entry> adapter = 
			new ArrayAdapter<Entry>(this, 
					R.layout.onelinenocheck,
					entries) {

			public View getView(int position, View convertView, ViewGroup parent) {
				View v;				
				
				if (convertView == null) {
	                v = View.inflate(context, R.layout.onelinenocheck, null);
	            }
				else {
					v = convertView;
				}

				final Entry e = entries.get(position);
				TextView tv = (TextView)v.findViewById(R.id.text);
				tv.setText(e.label);
				File iconFile = Apps.getIconFile(Popup.this, e.component);
				ImageView img = (ImageView)v.findViewById(R.id.icon);
				img.setVisibility(View.INVISIBLE);
				if (!iconFile.exists())
					Apps.saveIcon(Popup.this, e.component);
				if (iconFile.exists()) {
					try {
						img.setImageDrawable(Drawable.createFromStream(new FileInputStream(iconFile), null));
						img.setVisibility(View.VISIBLE);
					}
					catch (Exception ex) {
						Log.e("FastLaunch", ""+ex);
					}
				}
				return v;
			}				
		};

		adapter.sort(Entry.entryComparator);
		appsList.setAdapter(adapter);
		appsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				launch(context, entries.get(position).component);
				finish();
			}
		});

    }
	
	public static void launch(Context c, String component) {
		Log.v("launch", component);
		if (component.equals(MyApplicationInfo.HOME)) {
			Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(i);
		}
		else if (component.equals(MyApplicationInfo.WIFI)) {
			WifiManager w = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
			if (w!=null) {
				switch(w.getWifiState()) {
				case WifiManager.WIFI_STATE_ENABLED:
				case WifiManager.WIFI_STATE_ENABLING:
					w.setWifiEnabled(false);
					break;
				default:
					Log.v("DoublePower", "enabling WiFi");
					w.setWifiEnabled(true);
					break;
				}
			}
		}
		else if (component.equals(MyApplicationInfo.AIRPLANE)) {
			try {
				android.provider.Settings.System.putInt(c.getContentResolver(), 
						android.provider.Settings.System.AIRPLANE_MODE_ON,
						1-android.provider.Settings.System.getInt(c.getContentResolver(), 
								android.provider.Settings.System.AIRPLANE_MODE_ON));
			} catch (SettingNotFoundException e) {
			}
		}
		else if (component.equals(MyApplicationInfo.BLUETOOTH)) {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null) {
				switch(adapter.getState()) {
				case BluetoothAdapter.STATE_ON:
				case BluetoothAdapter.STATE_TURNING_ON:
					adapter.disable();
					break;
				default:
					adapter.enable();
					break;
				}
			}
		}
		else {
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			i.setComponent(ComponentName.unflattenFromString(component));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(i);
		}
	}
}

class Entry {
	public static final Comparator<Entry> entryComparator = 
	new Comparator<Entry>() {
	public int compare(Entry a, Entry b) {
		return a.label.compareToIgnoreCase(b.label);
	}
	};

	public String component;
	public String label;
	
	public Entry(String component, String label) {
		this.component = component;
		this.label = label;
	}
}
