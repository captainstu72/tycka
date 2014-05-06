tycka
==========

tycka: to think of, to have an opinion about

tycka is a twitter streaming app for the Google Chromecast. It is designed to gather a predefined number of tweets, and pass these through to a receiver application, aimed at being displayed on a TV screen or a projector.

# Prerequisites

For this code to work, you will need to have setup:
* v7-appcompat library
* v7-mediarouter library
* google-play-services library.
* twitter4j - These are included in the libs directory

All of these are part of the Android SDK and can be downloaded through the SDK manager. You will find details of this here: https://developers.google.com/cast/docs/downloads

Ideally you will set it up to use your own receiver application, so you will need to register as a cast developer and set up.

You will also need your own twitter API details, some are included, but I would prefer it if you used your own.

## Notes

Make sure you set your twitter API as a desktop app, otherwise it will try to return to an invalid site and not work. You'll see an error in logcat if you get this wrong.

# Getting started

Once all pre requisities are setup you will need to add the libraries to your project. In Eclipse this can be done by going to the Project Properties, then to Android, and setting up the libraries at the bottom.

## strings.xml

The bulk of the main settings are within here:

app_name - What ever you want your app to display as, right now this is pointless as I am loading a custom image in the action bar. You'll need to dsiable this within the TickerActivity.java file. More on that later.

* **app_desc** - We'd love you to keep this as Powered by tycka.
* **app_id** - Your AppID from the Cast Developers setup.
* **namespace** - The custom namespace you are using to communicate with your receiver.
* **twitter_api_key** - Your consumer key from the twitter setup.
* **twitter_api_secret** - Your consumer secret from the twitter setup.

##TickerActivity.java

There are 2 constants here to change. These will affect the frequency that data is pulled back from twitter, and how many tweets. I would not change the TWEET_INTERVAL to less than 15000. You are allowed 180 calls per 15 mins. This is every 15 seconds.
* static final int TWEET_COUNT = 5;
* static final int TWEET_INTERVAL = 15000; // (ms)

To disable the custom image in the action bar, you will want to delete the lines that say:
* actionBar.setCustomView(R.layout.actionbar_custom_view_home);
* actionBar.setDisplayShowTitleEnabled(false);
* actionBar.setDisplayShowCustomEnabled(true);
* 
Doing so will now show you the title.

        
