package co.uk.socialticker.ticker;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterUpdateDialogFragment extends DialogFragment {
    
    /* twitter side stuff*/
    static String TWITTER_CONSUMER_KEY = "UV9HB4ehoApafmTiqcNzg";
    static String TWITTER_CONSUMER_SECRET = "IMuHFQP3cCApXnTGEpwK5DpyjZZUQUSETnYp5lvB7o";    
    
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
 
    // Progress dialog
    ProgressDialog pDialog;
    
    // Shared Preferences
    private static SharedPreferences mSharedPreferences; // Twitter prefs.
    
    EditText input;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
        // Shared Preferences
        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences(
                "MyPref", 0);   	
    	
    	final EditText input = new EditText(getActivity());
    	
        // Use the Builder class for convenient dialog construction
        final AlertDialog builder = new AlertDialog.Builder(getActivity())
        
        	.setIcon(R.drawable.ic_action_twitter)
        	.setTitle(R.string.app_name)
        	.setView(input)
        	.setMessage("What would you like to tweet")
        	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}        		   
        	})
        	.setPositiveButton(R.string.send_tweet, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//send tweet!
					String status = input.getText().toString();
					 
	                // Check for blank text
	                if (status.trim().length() > 0) {
	                    // update status
	                	new updateTwitterStatus().execute(status);
	                } else {
	                    // EditText is empty
	                    Toast.makeText(getActivity().getApplicationContext(),
	                            "Please enter status message", Toast.LENGTH_SHORT)
	                            .show();
	                }
				}	
           }).create();
        
        builder.setOnShowListener(new OnShowListener() {

               @Override
               public void onShow(DialogInterface dialogInterface) {
                   Button btnPositive = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                   Button btnNegative = builder.getButton(AlertDialog.BUTTON_NEGATIVE);

                   // if you do the following it will be left aligned, doesn't look correct
                   // button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);

                   Drawable drawablePositive = getActivity().getResources().getDrawable(R.drawable.ic_action_twitter);
                   Drawable drawableNegative = getActivity().getResources().getDrawable(R.drawable.ic_action_twitter);

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
               }
           });
        return builder;
    }
    /*
    * Function to update status
    * */
   class updateTwitterStatus extends AsyncTask<String, String, String> {

       /**
        * Before starting background thread Show Progress Dialog
        * */
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           pDialog = new ProgressDialog(getActivity());
           pDialog.setMessage("Updating to twitter...");
           pDialog.setIndeterminate(false);
           pDialog.setCancelable(false);
           pDialog.show();
       }

       /**
        * getting Places JSON
        * */
       protected String doInBackground(String... args) {
           Log.d("Tweet Text", "> " + args[0]);
           String status = args[0];
           try {
               ConfigurationBuilder builder = new ConfigurationBuilder();
               builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
               builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
                
               // Access Token 
               String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
               // Access Token Secret
               String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
                
               AccessToken accessToken = new AccessToken(access_token, access_token_secret);
               Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
                
               // Update status
               twitter4j.Status response = twitter.updateStatus(status);
                
               Log.d("Status", "> " + response.getText());
           } catch (TwitterException e) {
               // Error in updating status
               Log.d("Twitter Update Error", e.getMessage());
           }
           return null;
       }

       /**
        * After completing background task Dismiss the progress dialog and show
        * the data in UI Always use runOnUiThread(new Runnable()) to update UI
        * from background thread, otherwise you will get error
        * **/
       protected void onPostExecute(String file_url) {
           // dismiss the dialog after getting all products
           pDialog.dismiss();
           // updating UI from Background Thread
          /*Toast.makeText(getActivity(),
                           "Status tweeted successfully", Toast.LENGTH_SHORT)
                           .show();*/
                   // Clearing EditText field
                   //input.setText("");

       }

   }
}
