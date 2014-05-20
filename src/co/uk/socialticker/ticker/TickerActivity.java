/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package co.uk.socialticker.ticker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.tycka.app.R;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Main activity to send messages to the receiver.
 */
public class TickerActivity extends ActionBarActivity {

	/*
	 *  when releasing set this to false!!!!
	 *  This simply displays me the json when I am not connected
	 *  to a cast and I have set this to true. Testing only!
	 *  */
	boolean debugOn = false;
	
    private static final String TAG = TickerActivity.class.getSimpleName();
    
    private static SharedPreferences p;
	private static Editor pe;
	public boolean prefChanged = false;
    
    private static String KEY_APP_ID = "PREF_APP_ID";
    private static String mAppID = "";
	private static String KEY_CAST_TITLE = "PREF_CAST_TITLE";
	private static String mCastTitle = "";
	private static String KEY_CAST_IMGURL = "PREF_CAST_IMGURL";
	private static String mImgUrl = "";
	private static String KEY_CAST_HASHTAG = "PREF_CAST_HASHTAG";
	private static String mHashTag = "";

    private static final int REQUEST_CODE = 1;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private HelloWorldChannel mHelloWorldChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    
    /* twitter side stuff*/
    static String TWITTER_CONSUMER_KEY = "";
    static String TWITTER_CONSUMER_SECRET = "";    
    // how may to return
    static final int TWEET_COUNT = 5;
    // 180 calls per 15 mins.
    static final int TWEET_INTERVAL = 15000; // (ms)
 
    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
 
    static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
 
    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    
    // update settings and send button
    private static Button btnUpdate;
    //stop runnable button
    private static Button btnKillUpdate;
 
    // Login button
    Button btnLoginTwitter;
    // Update status button
    Button btnUpdateStatus;
    // Logout button
    Button btnLogoutTwitter;
    // EditText for update
    EditText txtUpdate;
    // lbl update
    TextView lblUpdate;
    
    //Twitter card
    LinearLayout llTwitter;
    
    //Cast card
    LinearLayout llCast;
 
    // Progress dialog
    ProgressDialog pDialog;
 
    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;
     
    // Shared Preferences
    private static SharedPreferences mSharedPreferences; // Twitter prefs.
     
    // Internet Connection detector
    private ConnectionDetector cd;
     
    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();
    
    // cast pref crap
	static EditText etTitle;
	static EditText etImgUrl;
	static EditText etHashTag;
	
	//runnable for constant updates
	Handler mHandler = new Handler();
	boolean mHandlerCancelled = false;
	int rCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticker);
        
        p = PreferenceManager.getDefaultSharedPreferences(this);
        pe = p.edit(); 
        // Shared Preferences for twitter
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);      

        onCreatePrefs();
        onCreateTwitter();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setSubtitle(getString(R.string.app_desc));
        actionBar.setCustomView(R.layout.actionbar_custom_view_home);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View v) {
            	boolean doMe = false;
            	String warn = "";
            	//check if we are in debug mode, if not check to make sure we have a chromecast connection. If we don't, warn adn fail.
            	if (debugOn || mApiClient != null) {
            		doMe = true;
            	}
        		if (mApiClient == null && debugOn == false) {
        			warn = "You do not appear to be connected to a cast device, you may wish to fix that ...";
        		}
            		
            	if (doMe) {
	            	updatePrefs(v);
	            	if (mHandlerCancelled == false) { //save changes without spawning another runnable
			            mHandlerCancelled = false;
		    			rCount = rCount + 1;
		    			Log.d(TAG, "rCount == " + String.valueOf(rCount));
		        		mHandler.post(sendToCastRunnable);
	            	}
            	}
            	
            	if (!warn.isEmpty()) {
            		Log.d(TAG,warn);
            		Toast.makeText(TickerActivity.this, warn, Toast.LENGTH_LONG).show();
            	}
        }});

        btnKillUpdate = (Button) findViewById(R.id.btnKillUpdate);
        btnKillUpdate.setOnClickListener(new OnClickListener() {        	
            @Override
            public void onClick(View v) {    
            	stopRunnable();
            }
        });
        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(mAppID)).build();
        mMediaRouterCallback = new MyMediaRouterCallback();

    }
    
    private void onCreatePrefs() {        
        //set objects
        etTitle = (EditText) findViewById(R.id.etTitle);
		etImgUrl = (EditText) findViewById(R.id.etImgUrl);
		etHashTag = (EditText) findViewById(R.id.etHashTag);
        
        //load the current prefs
        getPrefs();
    }
    
	private void getPrefs() {
        mAppID = getString(R.string.app_id);
		etTitle.setText(p.getString(KEY_CAST_TITLE, getString(R.string.app_name)));
		etImgUrl.setText(p.getString(KEY_CAST_IMGURL,null));
		etHashTag.setText(p.getString(KEY_CAST_HASHTAG,null));
		
		// Get twitter settings from the String resources or it will not work, at all.
		TWITTER_CONSUMER_KEY = getString(R.string.twitter_api_key);
		TWITTER_CONSUMER_SECRET = getString(R.string.twitter_api_secret);
		
	}
    
    public void updatePrefs(View id) {
		//update the preference
		pe.putString(KEY_APP_ID, mAppID).commit();	
		pe.putString(KEY_CAST_TITLE,etTitle.getText().toString()).commit();
		pe.putString(KEY_CAST_IMGURL,etImgUrl.getText().toString()).commit();
		pe.putString(KEY_CAST_HASHTAG,etHashTag.getText().toString()).commit();
		
        mAppID = getString(R.string.app_id);
        mCastTitle = p.getString(KEY_CAST_TITLE,getString(R.string.app_name));
        mImgUrl = p.getString(KEY_CAST_IMGURL,"");
        mHashTag = p.getString(KEY_CAST_HASHTAG,"");
        
        Log.d(TAG,"Preferences updated");
	}
    
    private void onCreateTwitter() {
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
         
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TickerActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
         
        // Check if twitter keys are set
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
            // Internet Connection is not present
            alert.showAlertDialog(TickerActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }
 
        // All UI elements
        btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
        btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
        btnLogoutTwitter = (Button) findViewById(R.id.btnLogoutTwitter);
        txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
        lblUpdate = (TextView) findViewById(R.id.lblUpdate);
        
        llTwitter = (LinearLayout) findViewById(R.id.llTwitter);
        llCast = (LinearLayout) findViewById(R.id.llCast);

 
        /**
         * Twitter login button click event will call loginToTwitter() function
         * */
        btnLoginTwitter.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // Call login twitter function
                loginToTwitter();
            }
        });
 
        /**
         * Button click event to Update Status, will call updateTwitterStatus()
         * function
         * */
        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View v) {
                // Call update status function
                // Get the status from EditText
                String status = txtUpdate.getText().toString();
 
                // Check for blank text
                if (status.trim().length() > 0) {
                    // update status
                    new updateTwitterStatus().execute(status);
                } else {
                    // EditText is empty
                    Toast.makeText(getApplicationContext(),
                            "Please enter status message", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
 
        /**
         * Button click event for logout from twitter
         * */
        btnLogoutTwitter.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // Call logout twitter function
                logoutFromTwitter();
            }
        });
 
        /** This if conditions is tested once is
         * redirected from twitter page. Parse the uri to get oAuth
         * Verifier
         * */
        if (!isTwitterLoggedInAlready()) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
 
                try {
                    // Get the access token
                    AccessToken accessToken = twitter.getOAuthAccessToken(
                            requestToken, verifier);
 
                    // Shared Preferences
                    Editor e = mSharedPreferences.edit();
 
                    // After getting access token, access token secret
                    // store them in application preferences
                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_OAUTH_SECRET,
                            accessToken.getTokenSecret());
                    // Store login status - true
                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                    e.commit(); // save changes
 
                    Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
 
                    // Hide login button
                    btnLoginTwitter.setVisibility(View.GONE);
 
                    // Show Update Twitter
                    lblUpdate.setVisibility(View.VISIBLE);
                    txtUpdate.setVisibility(View.VISIBLE);
                    btnUpdateStatus.setVisibility(View.VISIBLE);
                    btnLogoutTwitter.setVisibility(View.VISIBLE);
                     
                    // Getting user details from twitter
                    // For now i am getting his name only
                    long userID = accessToken.getUserId();
                    User user = twitter.showUser(userID);

                } catch (Exception e) {
                    // Check log for login errors
                    Log.e("Twitter Login Error", "> " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        

        
        p = PreferenceManager.getDefaultSharedPreferences(this);
        pe = p.edit(); 
        // Shared Preferences for twitter
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "MyPref", 0);    
        // Start media router discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
//        mAppID = p.getString(KEY_APP_ID, getString(R.string.app_id));
        mAppID = getString(R.string.app_id);
        mCastTitle = p.getString(KEY_CAST_TITLE,getString(R.string.app_name));
        mImgUrl = p.getString(KEY_CAST_IMGURL,"");
        mHashTag = p.getString(KEY_CAST_HASHTAG,"");
        Log.d(TAG,"Layout: " + mAppID);
        
        if (isTwitterLoggedInAlready()) {

        	// Hide login button
            btnLoginTwitter.setVisibility(View.GONE);

            // Show Update Twitter
            lblUpdate.setVisibility(View.VISIBLE);
            txtUpdate.setVisibility(View.VISIBLE);
            btnUpdateStatus.setVisibility(View.VISIBLE);
            btnLogoutTwitter.setVisibility(View.VISIBLE);
            
            //even better lets just hide the top section if we are logged in, and hide the bottom part if we aren't
            llTwitter.setVisibility(View.GONE);
            llCast.setVisibility(View.VISIBLE);
        } else {
            llTwitter.setVisibility(View.VISIBLE);
            llCast.setVisibility(View.GONE);
        }
        
        
    }

    @Override
    protected void onPause() {
    	prefChanged = false;
        if (isFinishing()) {
            // End media router discovery
            mMediaRouter.removeCallback(mMediaRouterCallback);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        teardown();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    /**
     * Callback for MediaRouter events
     */
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteSelected");
            // Handle the user route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            teardown();
            mSelectedDevice = null;
        }
    }

    /**
     * Start the receiver app
     */
    private void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown();
                }

            };
            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();
            mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();

            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        teardown();
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                    mHelloWorldChannel.getNamespace(),
                                    mHelloWorldChannel);
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {
                    // Launch the receiver app
                	//Once the sender application is connected to the receiver application,
                	//	the custom channel can be created using Cast.CastApi.setMessageReceivedCallbacks:
                    Cast.CastApi
                            .launchApplication(mApiClient,mAppID, false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult: statusCode"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                String sessionId = result
                                                        .getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result
                                                        .getWasLaunched();
                                                Log.d(TAG,
                                                        "application name: "
                                                                + applicationMetadata
                                                                        .getName()
                                                                + ", status: "
                                                                + applicationStatus
                                                                + ", sessionId: "
                                                                + sessionId
                                                                + ", wasLaunched: "
                                                                + wasLaunched);
                                                mApplicationStarted = true;

                                                // Create the custom message
                                                // channel
                                                mHelloWorldChannel = new HelloWorldChannel();
                                                try {
                                                    Cast.CastApi
                                                            .setMessageReceivedCallbacks(
                                                                    mApiClient,
                                                                    mHelloWorldChannel.getNamespace(),
                                                                    mHelloWorldChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG,
                                                            "Exception while creating channel",
                                                            e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                sendMessage(getString(R.string.instructions));
                                                //and now send our actual data and start the runnable
                                                mHandlerCancelled = false;
                                                try {
													sendUpdate(mCastTitle
															, getString(R.string.instructions)
															, mImgUrl
															, mHashTag
															, doSearch((View) btnUpdate));
												} catch (TwitterException e1) {
													// TODO Auto-generated catch block
													Log.e(TAG,"ConnectionCallbacks: Update not sent");
													e1.printStackTrace();
												}
                                            } else {
                                                Log.e(TAG,
                                                        "application could not launch");
                                                teardown();
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");

            teardown();
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown() {
        if (mApiClient != null) {
            if (mApplicationStarted) {
                try {
                    Cast.CastApi.stopApplication(mApiClient);
                    if (mHelloWorldChannel != null) {
                        Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,
                                mHelloWorldChannel.getNamespace());
                        mHelloWorldChannel = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception while removing channel", e);
                }
                mApplicationStarted = false;
            }
            if (mApiClient.isConnected()) {
                mApiClient.disconnect();
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        
        stopRunnable();
    }

    /**
     * Send a text message to the receiver
     * 
     * @param message
     */
    private void sendMessage(String message) {
    	if (debugOn) {
    		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	        Log.d(TAG,message);
    	} else {
	    	if (message != null) {
		        if (mApiClient != null) {
		            try {
		                Cast.CastApi.sendMessage(mApiClient,
		                        mHelloWorldChannel.getNamespace(), message)
		                        .setResultCallback(new ResultCallback<Status>() {
		                            @Override
		                            public void onResult(Status result) {
		                                if (!result.isSuccess()) {
		                                    Log.e(TAG, "Sending message failed");
		                                }
		                            }
		                        });
		            } catch (Exception e) {
		                Log.e(TAG, "Exception while sending message", e);
		            }
		        Log.d(TAG,message);
		        } else {
		        	Toast.makeText(this, "You do not seem to be connected to a cast device...", Toast.LENGTH_LONG).show();
		        }
	    	} else {
	        	Toast.makeText(this, "A null message has been received. That's not right?...", Toast.LENGTH_LONG).show();
	        }
    	}
    }
    
    /**
     *  Wrapper to send a new message to the chromecast with a JSON object
     */
    private void sendUpdate(String title, String message, String imgurl, String hashTag, JSONArray jsA) {
	    	JSONObject json = writeJSON(title,message, imgurl, hashTag, jsA);
	    	sendMessage(json.toString());
    }
    
    /*
     * Build JSON object to send to cast from sendUpdate()
     */
    private JSONObject writeJSON(String title, String message, String imgurl, String hashTag, JSONArray jsA) {
//    	JSONArray output = new JSONArray();
    	JSONObject meta = new JSONObject();
    	try {
    		meta.put("title", title);
    		meta.put("message", message);
    		meta.put("imgurl", imgurl);
    		meta.put("hashtag",hashTag);
    		meta.put("jsA", jsA);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	//System.out.println(meta);
    	
//    	output.put(meta);
//    	output.put(jsA);
		return meta;
    }
    
    //open custom preferences activity
    public void openSettings() {
    	startActivity(new Intent(this,CustomPreferenceActivity.class));
    }
    
    /* on menu click */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
      // action with ID action_refresh was selected
	      /*case R.id.settings:
	    	  openSettings();
	    	  break;
	      case R.id.twitter:
	    	  startActivity(new Intent(this,TwitterActivity.class));
	    	  break;*/
    		case R.id.menu_twitter_update:
			   DialogFragment newFragment = new TwitterUpdateDialogFragment();
			   Bundle args = new Bundle();
			   newFragment.setArguments(args);
			   newFragment.show(getSupportFragmentManager(), "sendTweet");
			   break;
    		case R.id.menu_twitter_logout:
    			logoutFromTwitter();
    			break;	
    		default:
    			break;
	      }
    	return true;
	}

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }

    }
    
    /**
     * Function to login twitter
     * */
    private void loginToTwitter() {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();
             
            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();
 
            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }
 
    /**
     * Function to update status
     * */
    class updateTwitterStatus extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TickerActivity.this);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Status tweeted successfully", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    txtUpdate.setText("");
                }
            });
        }
 
    }
 
    /**
     * Function to logout from twitter
     * It will just clear the application shared preferences
     * */
    private void logoutFromTwitter() {
        // Clear the shared preferences
        Editor e = mSharedPreferences.edit();
        e.remove(PREF_KEY_OAUTH_TOKEN);
        e.remove(PREF_KEY_OAUTH_SECRET);
        e.remove(PREF_KEY_TWITTER_LOGIN);
        e.commit();
 
        // After this take the appropriate action
        // I am showing the hiding/showing buttons again
        // You might not needed this code
        btnLogoutTwitter.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        txtUpdate.setVisibility(View.GONE);
        lblUpdate.setVisibility(View.GONE);
 
        btnLoginTwitter.setVisibility(View.VISIBLE);
    }
 
    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
    
    /**
     * Test code to try and retrieve some data from twitter in a search!
     * @throws TwitterException 
     * */
    public JSONArray doSearch(View v) throws TwitterException {
    	

        if (mApiClient != null || debugOn) {
	    	// The factory instance is re-useable and thread safe.
	    	//get the hashtag - check to make sure if returned value is set to something with a length
	    	JSONArray jsA = new JSONArray();
	    	String qHash = p.getString(KEY_CAST_HASHTAG, "");
	    	Log.d(TAG,"Hash to search: " + qHash);
	    	if (qHash.length() == 0) {
	    		Toast.makeText(this
	    				, "The hashtag looks like it is not setup. May want to fix that"
	    				, Toast.LENGTH_LONG).show();
	    	} else {
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
		            //Query query = new Query("#MOTD2014");
		            Query query = new Query(qHash);
		            query.count(TWEET_COUNT);
		            QueryResult result = twitter.search(query);
		            for (twitter4j.Status status : result.getTweets()) {
		            	
		            	MediaEntity[] me = status.getMediaEntities();
		            	String meUrl = "";
		            	if (me.length > 0 ) {
			            	Log.d(TAG, "me[0] : " + me[0].getMediaURL());
			            	//meUrl = me[0].getDisplayURL(); //sjort URl = useless.
			            	meUrl = me[0].getMediaURL();
		            	}	            	
		            	
		            	JSONObject jso = tweetJSON(
		            			status.getUser().getScreenName()
		            			, status.getUser().getName()
	//	            			, status.getUser().getOriginalProfileImageURL() //Whatever the size was it was uploaded in
	//	            			, status.getUser().getProfileImageURL() // 48x48
		            			, status.getUser().getBiggerProfileImageURL() // 73x73
		            			, status.getText()
		            			, status.getCreatedAt().toString()
		            			, status.getFavoriteCount()
		            			, status.getRetweetCount()
		            			, meUrl
		            			);		            	
		            	jsA.put(jso);
		            }
		
		        } catch (TwitterException e) {
		            // Error in updating status
		            Log.d("Twitter Search Error", e.getMessage());
		            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		        }
	    	}
	    	;
			return jsA;
        } else {
        	Toast.makeText(this, "You do not seem to be connected to a cast device...", Toast.LENGTH_LONG).show();
    		return null;
        }
    }
    
    private JSONObject tweetJSON(String user, String realName, String profileImg
    		, String text, String date, int favourites, int retweets, String meUrl) {
    	JSONObject object = new JSONObject();
    	try {
    		object.put("user", user);
    		object.put("realName",realName);
    		object.put("profileImg",profileImg);
    		object.put("text", text);
    		object.put("date", date);
    		object.put("favourites", favourites);
    		object.put("retweets", retweets);
    		object.put("meUrl", meUrl);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	//System.out.println(object);
		return object;
    }
    
    //disable twitter menu actions if we are not signed in to twitter
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Boolean toggle = true;
    	
		MenuItem update = menu.findItem(R.id.menu_twitter_update);
		MenuItem logout = menu.findItem(R.id.menu_twitter_logout);
		
    	if (!isTwitterLoggedInAlready()) {
    		toggle = false;
    	}
    	update.setEnabled(toggle);
    	logout.setEnabled(toggle);
    	
    	super.onPrepareOptionsMenu(menu);
    	return true;
    }
    
    /* Runnable to work as a constant task to update the cast with sendUpdate based on
     * the frequency of TWEET_INTERVAL
     */
    Runnable sendToCastRunnable = new Runnable(){  
    	public void run() {
    		if (rCount < 2) {
	    		if 	(		(!mHandlerCancelled && !(mApiClient == null))
	    				|| 	(!mHandlerCancelled && debugOn)
	    			) {
		            try {
						sendUpdate(mCastTitle,getString(R.string.instructions),mImgUrl, mHashTag, doSearch((View) btnUpdate));
					} catch (TwitterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		mHandler.postDelayed(this, TWEET_INTERVAL);
	    		} else {
	    			Log.d(TAG,"Killing runnable");
	    	        mHandlerCancelled = true; //incase we have teardown
	    			mHandler.removeCallbacksAndMessages(sendToCastRunnable);
	    			Toast.makeText(TickerActivity.this, "Updates stopped", Toast.LENGTH_LONG).show();
	    		}
    		} else {
    			Log.d(TAG,"Not making another runnable, you already have one");
    			rCount = rCount - 1; //make our count go back down then.
    			Log.d(TAG, "rCount == " + String.valueOf(rCount));
    			
    		}
    	}  
	 };
	 
	 /*
	  * Handler for the Update Tweet dialog. Begins the code to send the tweet
	  */
	public void doDialogUpdatePositiveClick(String status) {
		// Do stuff here.
		Log.i(TAG, "Sending status to twitter: " + status);
		//send tweet!
		 
		// Check for blank text
		if (status.trim().length() > 0) {
		    // update status
			new updateTwitterStatus().execute(status);
		} else {
	    // EditText is empty
		Toast.makeText(this,"Please enter status message", Toast.LENGTH_SHORT).show();
		}
	 }
	
	/*
	 * attempt to stop the runnable by setting a constant to true. This is read on sendToCastRunnable
	 * which checks to see if it is true or not. Also manage a count of runnables so it does not happen
	 * more than once = excess api calls. that'd be bad.
	 */
	public void stopRunnable() {
        Log.d(TAG,"Stopping Updates");
        mHandlerCancelled = true;
		rCount = rCount - 1;
		Log.d(TAG, "rCount == " + String.valueOf(rCount));
	 }
}