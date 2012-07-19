package mobi.omegacentauri.FastLaunch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mobi.omegacentauri.FastLaunch.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;

public class Apps extends Activity {
	ListView appsList;
	Resources res;
	final static String PREF_APPS = "apps";
	
	public static void saveIcon(Context c, String componentName) {
		if (componentName.startsWith(" "))
			return;
		
		deleteIcon(c, componentName);
		try {
			ComponentName cn = ComponentName.unflattenFromString(componentName);
			String packageName = cn.getPackageName();
			PackageManager pm = c.getPackageManager();
			Resources res = pm.getResourcesForActivity(cn);
			Drawable icon = res.getDrawable(pm.getPackageInfo(packageName, 0).applicationInfo.icon);
//			Drawable icon = pm.getPackageInfo(packageName, 0).applicationInfo.loadIcon(c.getPackageManager());
			if (icon instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable)icon).getBitmap();
				Log.v("FastLaunch", "icon "+bmp.getWidth()+"x"+bmp.getHeight());
				File iconFile = getIconFile(c, componentName);
				FileOutputStream out = new FileOutputStream(iconFile);
				bmp.compress(CompressFormat.PNG, 100, out);
				out.close();
				Log.v("FastLaunch", "saved icon");
			}
		} catch (Exception e) {
			deleteIcon(c, componentName);
		}		
	}
	
	public static File getIconFile(Context c, String componentName) {
		return new File(c.getCacheDir(), 
				Uri.encode(componentName)+".png");
	}
	
	public static void deleteIcon(Context c, String componentName) {
		if (componentName.startsWith(" "))
			return;
		if (getIconFile(c, componentName).delete()) {
			Log.v("FastLaunch", "successful delete of "+componentName+" icon");
		}
	}

	public void activateApp(MyApplicationInfo appInfo) {
		Log.v("FastLaunch activate", appInfo.getComponent());
		SharedPreferences.Editor ed = getSharedPreferences(PREF_APPS, 0).edit();
		ed.putString(appInfo.getComponent(), appInfo.getLabel());
		ed.commit();
		saveIcon(this, appInfo.getComponent());

        startServiceIfNeeded(this);
	}
	
//	public void deactivateApp(Context c, String component) {
//		SharedPreferences.Editor ed = c.getSharedPreferences(PREF_APPS, 0).edit();
//		ed.remove(component);
//		ed.commit();
//	}
	
	public void deactivateApp(MyApplicationInfo appInfo) {		
		Log.v("deactivate", appInfo.getComponent());
//		deactivateApp(this, appInfo.getComponent());
		SharedPreferences.Editor ed = getSharedPreferences(PREF_APPS, 0).edit();
		ed.remove(appInfo.getComponent());
		ed.commit();
		deleteIcon(this, appInfo.getComponent());
	}	

	private void message(String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, 
				getResources().getText(R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {} });
		alertDialog.show();

	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
//        message("Test version", "This is a test version that expires after September 30, 2011.");
        
//        if (System.currentTimeMillis()>(new Date(2011, 9, 30)).getTime()) { 
//        	finish();
//        }
        
        setContentView(R.layout.apps);
        
        appsList = (ListView)findViewById(R.id.apps);
        
        res = getResources();        

        appsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	private void clear() {
		SharedPreferences.Editor ed = getSharedPreferences(PREF_APPS, 0).edit();
		ed.clear();
		ed.commit();
		ListAdapter a = appsList.getAdapter();
		appsList.setAdapter(null);
		appsList.setAdapter(a);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.clear:
    		clear();
    		return true;
    	case R.id.options:
    		startActivity(new Intent(this, Options.class));
    		return true;
    	default:
    		return false;
    	}
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
	    return true;
	}

	@Override
    public void onResume() {
    	super.onResume();

        (new GetApps(this, appsList)).execute();
        
        startServiceIfNeeded(this);
    }

	static void startServiceIfNeeded(Context c) {
//		Log.v("DoubleStart","stop service");
//		Intent i = new Intent(c, ScreenOnListen.class);
//		c.stopService(i);
//
//		if (c.getSharedPreferences(PREF_APPS, 0).getAll().size()>0 ||
//				PreferenceManager.getDefaultSharedPreferences(c).getBoolean(Options.PREF_AUTO_WIFI, false)) {
//			Log.v("DoubleStart","starting service");
//			c.startService(i);
//		}
	}
	
}

