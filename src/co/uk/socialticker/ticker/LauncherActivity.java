package co.uk.socialticker.ticker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class LauncherActivity  extends ActionBarActivity{
	final static String TAG = "Launcher";
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        

		Intent i = new Intent();
		i.setClass(this, TickerActivity.class);
		startActivity(i);
		finish();
	}
}
