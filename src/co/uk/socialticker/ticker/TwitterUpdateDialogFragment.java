package co.uk.socialticker.ticker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.EditText;

public class TwitterUpdateDialogFragment extends DialogFragment {
    
    /* twitter side stuff*/
    static String TWITTER_CONSUMER_KEY = "UV9HB4ehoApafmTiqcNzg";
    static String TWITTER_CONSUMER_SECRET = "IMuHFQP3cCApXnTGEpwK5DpyjZZUQUSETnYp5lvB7o";    
    
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	private static String KEY_CAST_HASHTAG = "PREF_CAST_HASHTAG";
	private static String mHashTag = "";
 
    // Progress dialog
    ProgressDialog pDialog;
    
    // Shared Preferences
    private static SharedPreferences p;
    private static SharedPreferences mSharedPreferences; // Twitter prefs.
    
    EditText input;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
        // Shared Preferences
        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences(
                "MyPref", 0);
        p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mHashTag = p.getString(KEY_CAST_HASHTAG,"");        
    	
    	final EditText input = new EditText(getActivity());        //put in the hashtag we are using to search, for fun, plus a space for padding
    	
        // Use the Builder class for convenient dialog construction
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
        
        	.setIcon(R.drawable.ic_action_twitter)
        	.setTitle(R.string.send_tweet)
        	.setView(input)
        	.setMessage("What would you like to tweet")
        	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}        		   
        	})
        	.setPositiveButton(R.string.send_tweet, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((TickerActivity) getActivity()).doDialogUpdatePositiveClick(input.getText().toString());
				}	
           }).create();
        
        //add icons to buttons, and set default input text
        builder.setOnShowListener(new OnShowListener() {

               @Override
               public void onShow(DialogInterface dialogInterface) {
                   Button btnPositive = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                   Button btnNegative = builder.getButton(AlertDialog.BUTTON_NEGATIVE);

                   // if you do the following it will be left aligned, doesn't look correct
                   // button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);

                   Drawable drawablePositive = getActivity().getResources().getDrawable(R.drawable.ic_action_twitter);
                   Drawable drawableNegative = getActivity().getResources().getDrawable(R.drawable.ic_action_cancel);

                   // set the bounds to place the drawable a bit right
                   drawablePositive.setBounds(
                		   (int) (drawablePositive.getIntrinsicWidth() * 0.5)
                		   , 0
                		   , (int) (drawablePositive.getIntrinsicWidth() * 1.5)
                		   , drawablePositive.getIntrinsicHeight()
                		   );
                   drawableNegative.setBounds(
                		   (int) (drawablePositive.getIntrinsicWidth() * 0.5)
                		   , 0
                		   , (int) (drawablePositive.getIntrinsicWidth() * 1.5)
                		   , drawablePositive.getIntrinsicHeight()
                		   );
                   btnPositive.setCompoundDrawables(drawablePositive, null, null, null);
                   btnNegative.setCompoundDrawables(drawableNegative, null, null, null);

                   // could modify the placement more here if desired
                   //  button.setCompoundDrawablePadding();
                   
                   //modify default input
	               	input.setText(" " + mHashTag);
	            	input.setHint(mHashTag);
	            	input.setSelection(0);
	            	input.requestFocus();
               }
           });
        return builder;
    }
}
