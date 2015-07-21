# Introduction #

The 1.0.0 BETA version of the library (available on the [download page](http://code.google.com/p/soundcloudapi-java/downloads/list)) offers support for username/password authorization via the [OAuth2 protocol](https://github.com/soundcloud/api/wiki/13-X-OAuth-2).  What is currently missing, and will be present in the final 1.0.0 release, is support for refreshing the access tokens after they expire.

# Example #

To use OAuth2 username/password authorization, you **must** initialize the SoundCloudAPI object with the V2.0 OAuthVersion setting, for example:

```
    SoundCloudAPI api = new SoundCloudAPI(consumerKey, consumerSecret, SoundCloudAPI.USE_PRODUCTION.with(OAuthVersion.V2_0));
```

You can then collect the user's username and password and use them to obtain the Access Token and Refresh Token:

```
    api.obtainAccessToken(username, password);
```

You can then use the SoundCloudAPI object as usual, until the Access Token expires.  As stated earlier, support for refreshing access tokens after they expire will soon be added to the library.  Do not store the username and password.

# Future Development #

Version 1.0.0 will include refresh support.  We will then start on version 2 of the library, which will fully support OAuth 2 as provided by SoundCloud, and no longer support OAuth 1.
