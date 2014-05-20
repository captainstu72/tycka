package co.uk.socialticker.ticker;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import co.uk.tycka.app.R;

public class CustomPreferenceActivity extends ActionBarActivity {
	
	private static final String TAG = CustomPreferenceActivity.class.getSimpleName();
	
	private static SharedPreferences p;
	private static Editor pe;
	
	static EditText etTitle;
	static EditText etImgUrl;
	static EditText etHashTag;
	static RadioGroup rg;
	
	private static String KEY_APP_ID = "PREF_APP_ID";
	private static String KEY_APP_RGID = "PREF_APP_RGID";
	private static String KEY_CAST_TITLE = "PREF_CAST_TITLE";
	private static String KEY_CAST_IMGURL = "PREF_CAST_IMGURL";
	private static String KEY_CAST_HASHTAG = "PREF_CAST_HASHTAG";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custompreference);
        
        p = PreferenceManager.getDefaultSharedPreferences(this);
        pe = p.edit();
		
		ActionBar actionBar = getSupportActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(
        //        android.R.color.transparent));
        actionBar.setTitle(getString(R.string.settings));
        actionBar.setSubtitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        //load the current prefs
        getPrefs();

	}
	
	private void getPrefs() {
		
	}
	
	public void updatePrefs(View id) {
		//update the preferences
		pe.putString(KEY_CAST_TITLE,etTitle.getText().toString()).commit();
		pe.putString(KEY_CAST_IMGURL,etImgUrl.getText().toString()).commit();
		pe.putString(KEY_CAST_HASHTAG,etHashTag.getText().toString()).commit();
		
	}
	
	@Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    /* on menu click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	      // action with ID action_refresh was selected
	    	case android.R.id.home:
	    		updatePrefs(findViewById(R.id.btnUpdate));
	    		this.finish();
	    		return true;
	
	    	default:
	    		break;
	      }

    	return true;
    } 
	
}
