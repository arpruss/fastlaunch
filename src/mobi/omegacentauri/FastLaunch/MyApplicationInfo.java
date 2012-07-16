package mobi.omegacentauri.FastLaunch;

import java.util.Comparator;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class MyApplicationInfo {
	public static final String HOME = " home";
	public static final String WIFI = " wifi";
	public static final String AIRPLANE = " airplane";
	public static final String BLUETOOTH = " bluetooth";
	private String label;
	private String component;
	private int versionCode;
	private int uid;
	public String packageName;
	
	public static final Comparator<MyApplicationInfo> LabelComparator = 
		new Comparator<MyApplicationInfo>() {

		public int compare(MyApplicationInfo a, MyApplicationInfo b) {
//			Log.v("DoublePower", a.component+" "+b.component);
			if (a.component.startsWith(" ")) {
				if (b.component.startsWith(" ")) {
					return a.label.compareToIgnoreCase(b.label);
				}
				else {
					return -1;
				}
			}
			else if (b.component.startsWith(" ")) {
				return 1;
			}
			else {
				return a.label.compareToIgnoreCase(b.label);
			}
		}
	};
	
	String getKey() {
		return Locale.getDefault().toString() + "." + uid + "." + versionCode + "." + component;
	}
	
	public String getComponent() {
		return component;
	}
	
	public MyApplicationInfo(String command) {
		packageName = command;
		component = command;
		versionCode = 0;
		uid = 0;
		if (command == HOME) {
			 label = "HOME";
		} 
		else if (command == WIFI) {
			 label = "WiFi toggle";
		} 
		if (command == AIRPLANE) { 
			 label = "Airplane Mode toggle";
		} 
		if (command == BLUETOOTH) {
			 label = "Bluetooth toggle";
		}
	}
	
	public MyApplicationInfo(MyCache cache, PackageManager pm, ResolveInfo r) {
		packageName = r.activityInfo.packageName;
		component = (new ComponentName(packageName, r.activityInfo.name)).flattenToString();
		uid = r.activityInfo.applicationInfo.uid;
		
		try {
			versionCode = (pm.getPackageInfo(packageName, 0)).versionCode;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			versionCode = 0;
		}
		
		if (cache != null) {
			String cached = cache.lookup(getKey());
			if (cached != null) {
				label = cached;
				return;
			}
		}
		
		CharSequence l = r.activityInfo.loadLabel(pm); 
		if (l == null) {
			label = component;
		}
		else {			
			label = l.toString();
			if (label.equals("Angry Birds")) {
				if(packageName.startsWith("com.rovio.angrybirdsrio")) {
					label = label + " Rio";
				}
				else if (packageName.startsWith("com.rovio.angrybirdsseasons")) {
					label = label + " Seasons";
				}
			}
			if (cache != null)
				cache.add(getKey(), label);
		}
	}
	
	public String getLabel() {
		return label;
	}
	
	static public String getSmartLabel(Context c, String component, String label) {
		if (component.equals(WIFI)) {
			WifiManager w = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
			if (w == null)
				return "WiFi (unavailable)";
			switch(w.getWifiState()) {
			case WifiManager.WIFI_STATE_ENABLED:
			case WifiManager.WIFI_STATE_ENABLING:
				return "WiFi disable";
			default:
				return "WiFi enable";
			}			
		}
		else if (component.equals(AIRPLANE)){
			try {
				if(android.provider.Settings.System.getInt(c.getContentResolver(), 
						android.provider.Settings.System.AIRPLANE_MODE_ON)==0) {
					return "Airplane Mode enable";
				}
				else {
					return "Airplane Mode disable";
				}
			} catch (SettingNotFoundException e) {
				// TODO Auto-generated catch block
				return "Airplane Mode (unavailable)";
			}			
		}
		else if (component.equals(BLUETOOTH)) {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (adapter != null) {
				switch(adapter.getState()) {
				case BluetoothAdapter.STATE_ON:
				case BluetoothAdapter.STATE_TURNING_ON:
					return "Bluetooth disable";
				default:
					return "Bluetooth enable";
				}
			}
			else {
				return "Bluetooth (unavailable)";
			}
		}
		else {
			return label;
		}
	}	
}

