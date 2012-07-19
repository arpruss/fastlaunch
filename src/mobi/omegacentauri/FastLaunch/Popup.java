package mobi.omegacentauri.FastLaunch;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class Popup extends Activity {
	ListView[] appsLists;
	int numLists;
	Resources res;
	private LinearLayout main;
	boolean tile;
	static final int[] listIds = { R.id.apps1, R.id.apps2, R.id.apps3, R.id.apps4 };
	static final int[] dividerIds = { R.id.divider1, R.id.divider2, R.id.divider3 };  

	ViewTreeObserver.OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener(){

		@Override
		public void onGlobalLayout() {
			Log.v("FastLaunch", "laying out");
			if (main.getWidth() != 0 && main.getHeight() != 0) {
				makeLists();
				main.getViewTreeObserver().removeGlobalOnLayoutListener(layoutListener);
			}
		}
	
	};

	
	ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener(){

		@Override
		public boolean onPreDraw() {
			Log.v("FastLaunch", "laying out");
			makeLists();
			main.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
			return true;
		}};

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(this);
    	tile = options.getBoolean(Options.PREF_TILE, false);
    	
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (tile) {
			setTheme(android.R.style.Theme_Black);
		}

		super.onCreate(savedInstanceState);
        
        
		main = (LinearLayout)getLayoutInflater().inflate(R.layout.popup, null);
        setContentView(main);
        
    	ViewGroup.LayoutParams lp = main.getLayoutParams();
        if (!tile) {
        	lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        	lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        else {
        	lp.height = ViewGroup.LayoutParams.FILL_PARENT;        	
        	lp.width = ViewGroup.LayoutParams.FILL_PARENT;        	
        }
        main.setLayoutParams(lp);
        
        appsLists = new ListView[] {
        		(ListView)findViewById(R.id.apps1),
        		(ListView)findViewById(R.id.apps2),
        		(ListView)findViewById(R.id.apps3),
        		(ListView)findViewById(R.id.apps4) };
        
	}
	
    void resize() {
    	if (!tile)
    		return;
    	
    	LinearLayout ll = main;
    	FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)ll.getLayoutParams();

    	int h = getWindowManager().getDefaultDisplay().getHeight();
    	int w = getWindowManager().getDefaultDisplay().getWidth();
    	
    	lp.width = w;
    	
//    	if (w>h) {
//    		lp.setMargins((w-h)/2,0,(w-h)/2,0);
//    	}
//    	else {
//    		lp.setMargins(0,0,0,0);    		
//    	}
		ll.setLayoutParams(lp);
    }
    
	@Override 
	public void onResume() {
		super.onResume();
		
		resize();

		if (tile) {
//        	main.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
			main.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		}
		else {
			makeLists();
		}

	}
	
	private static long min(long a, long b) {
		return a < b ? a : b; 
	}
	
	public void makeLists() {
//    	PackageManager pm = getPackageManager();
    	SharedPreferences pref = getSharedPreferences(Apps.PREF_APPS, 0);
    	
    	Map<String,?> map = pref.getAll();

    	int numApps = map.keySet().size();
    	int cellWidth = -1;
    	final int forceHeight;

    	if (tile && 6 <= numApps) {
        	int h = main.getHeight();
        	int w = main.getWidth();
        	Log.v("FastLaunch", "dimensions "+w+" "+h);

        	int bestColumnCount = 1;
        	long bestSize = 0;
        	for (int i=1; i<=listIds.length; i++) {
            	int perList = (numApps + i - 1) / i;
        		
            	long size = min((h / perList) * 15l, (w/i) * 10l);
            	
            	if (bestSize < size) {
            		bestSize = size;
            		bestColumnCount = i;
            	}
        	}
        	
        	int perList = (numApps + bestColumnCount - 1) / bestColumnCount;
        	
        	if (h / (float)perList < getResources().getDisplayMetrics().scaledDensity * 18) {
        		numLists = 1;
        		forceHeight = -1;
        	}
        	else {
        		numLists = bestColumnCount;
        		forceHeight = (int)(h / perList) - 4;
        		cellWidth = (int)(w / numLists);
        	}
    	}
    	else {
    		numLists = 1;
    		forceHeight = -1;
    	}
    	
    	Log.v("FastLaunch", "layout a");
    	
    	int maxItemsPerList = (numApps + numLists - 1) / numLists;
    	
    	@SuppressWarnings("unchecked")
		final
		ArrayList<Entry>[] entries = new ArrayList[numLists];
    	ArrayList<Entry> allEntries = new ArrayList<Entry>();
    	
    	Object[] keys = (Object[]) map.keySet().toArray();

    	for (String k : map.keySet()) 
    		allEntries.add(new Entry(k, 
	    		MyApplicationInfo.getSmartLabel(this, k, (String)map.get(k))));
    		
    	Collections.sort(allEntries, new EntryComparator());
    	
    	for (int i = 0; i < numLists ; i++)
    		entries[i] = new ArrayList<Entry>();
        	
    	for (int j = 0 ; j < numLists ; j++) {
    		
        	Log.v("FastLaunch", "layout "+j);
        	
    		final int listNum = j;
    		
    		appsLists[listNum].setVisibility(View.VISIBLE);

    		for (int i = listNum * maxItemsPerList ; i < (listNum + 1) * maxItemsPerList &&
    					i < numApps ; i++) {
			    entries[listNum].add(allEntries.get(i));
	    	}
    		
	    	final Context context = this;
	    	final int textWidth = (int)(cellWidth - 1 -
			getResources().getDisplayMetrics().density * (48+4)
			- getResources().getDisplayMetrics().scaledDensity * 8);
	    	
			ArrayAdapter<Entry> adapter = 
				new ArrayAdapter<Entry>(this, 
						R.layout.onelinenocheck,
						entries[listNum]) {
	
				public View getView(int position, View convertView, ViewGroup parent) {
					View v;				
					
					if (convertView == null) {
		                v = View.inflate(context, R.layout.onelinenocheck, null);
		            }
					else {
						v = convertView;
					}

					if (0<=forceHeight) {
						v.setMinimumHeight(forceHeight);
						v.setPadding(0, 0, 0, 0);
					}
//					ViewGroup.LayoutParams lp = v.getLayoutParams();
//					lp.height = forceHeight; // ViewGroup.LayoutParams.FILL_PARENT;
//					v.setLayoutParams(lp);
	
					final Entry e = entries[listNum].get(position);
					File iconFile = Apps.getIconFile(Popup.this, e.component);
					ImageView img = (ImageView)v.findViewById(R.id.icon);
					img.setVisibility(View.GONE);
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

					TextView tv = (TextView)v.findViewById(R.id.text);
					tv.setText(e.label);
					if (tile)
						tv.setPadding(
								(int)(4*getResources().getDisplayMetrics().scaledDensity),
								(int)(4*getResources().getDisplayMetrics().scaledDensity),
								(int)(4*getResources().getDisplayMetrics().scaledDensity),
								0);
					if (tile && 0 <= forceHeight) {
						tv.setTextSize(fit(e.label, textWidth, (int)(forceHeight-getResources().getDisplayMetrics().density * 8)));
					}
					return v;
				}				
			};
	
			appsLists[listNum].setAdapter(adapter);
			appsLists[listNum].setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					launch(context, entries[listNum].get(position).component);
					finish();
				}
			});
			
    	}
    	
    	for (int listNum = numLists - 1 ; listNum < dividerIds.length ; listNum++) {
    		(findViewById(dividerIds[listNum])).setVisibility(View.GONE);
    	}
    	
    	for (int listNum = numLists ; listNum < appsLists.length ; listNum++) {
    		appsLists[listNum].setVisibility(View.GONE);
    	}

    }
	
	protected float fit(String label, int width, int height) {
		TextPaint paint = new TextPaint();
		String[] words = label.split("\\s+");
		Rect bounds = new Rect();
		
		for (int i=0; i+1<words.length; i++)
			words[i] += " ";

		for(int size = 20; size > 6; size--) {
			paint.setTextSize((float)size * getResources().getDisplayMetrics().scaledDensity);
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			StaticLayout layout = new StaticLayout(label, paint, width,
					Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
			if (layout.getHeight()<=height) {
				int i;
				
				for (i=0; i<words.length; i++) {
					paint.getTextBounds(words[i], 0, words[i].length(), bounds);
					Log.v("FastLaunch", words[i]+" "+bounds.width()+ " "+width);
					if (bounds.width() > width) {
						break;
					}
				}
				
				if (i >= words.length)
					return size;
			}
		}
		return 6;
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


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.popup, menu);
	    return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.options:
    		startActivity(new Intent(this, Options.class));
    		return true;
    	case R.id.edit:
    		startActivity(new Intent(this, Apps.class));
    		return true;
    	default:
    		return false;
    	}
    }

}

class Entry {
	public String component;
	public String label;
	
	public Entry(String component, String label) {
		this.component = component;
		this.label = label;
	}


}

class EntryComparator implements Comparator<Entry> {

	@Override
	public int compare(Entry a, Entry b) {
		if (a.component.startsWith(" ") && !b.component.startsWith(" "))
			return -1;
		else if (!a.component.startsWith(" ") && b.component.startsWith(" "))
			return 1;
		else 
			return a.label.compareToIgnoreCase(b.label);
	}

}