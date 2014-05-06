tycka
==========

tycka: to think of, to have an opinion about

tycka is a twitter streaming app for the Google Chromecast. It is designed to gather a predefined number of tweets, and pass these through to a receiver application, aimed at being displayed on a TV screen or a projector.

# Prerequisites

For this code to work, you will need to have setup:
* v7-appcompat library
*v7-mediarouter library
*google-play-services library.

All of these are part of the Android SDK and can be downloaded through the SDK manager. You will find details of this here: https://developers.google.com/cast/docs/downloads

Ideally you will set it up to use your own receiver application, so you will need to register as a cast developer and set up.

You will also need your own twitter API details, some are included, but I would prefer it if you used your own.

# Getting started

Once all pre requisities are setup you will need to add the libraries to your project. In Eclipse this can be done by going to the Project Properties, then to Android, and setting up the libraries at the bottom.

The main details here to change are the twitter_api_key and twitter_api_secret. These are defined as strings within the resource files. These are then read in and put into a constant onResume. [getPrefs()]

