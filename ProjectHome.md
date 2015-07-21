# Introduction #
This is a Java wrapper for the [SoundCloud API](http://soundcloud.com/developers).  The wrapper can be used to easily access the functionality of the [API](http://soundcloud.com/developers), including retrieving and manipulating resources such as user and track information, and uploading files.

# Getting Started #
To use the library, download the latest version and include the JAR files in your project. You can also access the library source from the repository.

The following library dependencies (and their dependencies) are included for convenience:
  * [signpost](http://github.com/kaeppler/signpost), a light-weight client-side [OAuth](http://oauth.net) library
  * [HttpClient 4.0](http://hc.apache.org/httpcomponents-client/index.html), a robust HTTP client library

When using the library on Android, you may only need the following included JAR files:
  * apache-mime4j-0.6.jar
  * httpmime-4.0.jar
  * org.urbanstew.soundcloudapi-0.9.0.jar
  * signpost-commonshttp4-1.1.jar
  * signpost-core-1.1.jar

Otherwise, you may need all of the included JAR files.

# Using the Code #
The library interface is designed with simplicity in mind.  Code samples below will illustrate a few basic use cases.

## Authentication ##

SoundCloud uses the [OAuth protocol](http://oauth.net) to authenticate apps accessing protected data via the API.  You will need to obtain a Consumer Key and Consumer Secret by [registering your app](http://soundcloud.com/you/apps/new) with SoundCloud.  A user can then allow your app to access protected data.

The `SoundCloudAPI` class is used to interface with the SoundCloud API.  You can initialize an instance of this class as follows, if you intend to have the user allow access:

```
SoundCloudAPI api = new SoundCloudAPI(YOUR_APP_CONSUMER_KEY, YOUR_APP_CONSUMER_SECRET);
```

To allow access, the user must visit an authentication URL (e.g., using a browser), and confirm that your app should be allowed access. SoundCloud will then issue a verification code, which needs to be delivered to your app.  This can either be delivered manually by the user, or via a callback URL.

### Step 1, Option 1: Manual Entry of Verification Code ###

If you need the user to manually provide your app with a verification code, do this:

```
String authorizationUrl = api.obtainRequestToken();
```

The user must then visit the `authorizationUrl`, obtain a verification code, and provide the code to your app.

### Step 1, Option 2: Callback Delivery of the Verification Code ###

If your app can receive the verification code via a callback URL, do this:

```
String authorizationUrl = api.obtainRequestToken(YOUR_CALLBACK_URL);
```

In this case, after the user visits the `authorizationUrl` and allows access to the app, SoundCloud will redirect the user to your callback URL. The request will include an `oauth_verifier` value, which is the verification code.

### Step 2: Obtaining the Access Token ###

Regardless of how your app obtains the verification code, proceed as follows:

```
api.obtainAccessToken(VERIFICATION_CODE);
```

You are now ready to start using the `api` object to send requests to the SoundCloud API.

### Saving the Authentication for Later Use ###

Once you have obtained the authentication, you may want to save the Access Token and the Access Token Secret, so you don't have to authenticate your app / the user again:
```
String token = api.getToken();
String tokenSecret = api.getTokenSecret();
```

Next time you need to initialize a `SoundCloudAPI` object for the same user, simply provide the Access Token and the Access Token Secret:

```
SoundCloudAPI api = new SoundCloudAPI(YOUR_APP_CONSUMER_KEY, YOUR_APP_CONSUMER_SECRET, token, tokenSecret);
```

You are then ready to issue requests to the API.

## Issuing Requests ##

Once the `api` object obtains the authentication information, it can be used to send requests to the SoundCloud API.  This will return an `org.apache.http.HttpResponse` object, which you can use to process the response.

### Example: Obtaining the User Information ###

The SoundCloud API provides the [User resource](http://wiki.github.com/soundcloud/api/03-resource-types#user) for the authenticated user as the resource `/me`. This can be retrieved as follows:

```
HttpResponse response = api.get("me");
if(response.getStatusLine().getStatusCode() == 200)
{
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document dom = db.parse(response.getEntity().getContent());
    // process the parsed XML via the dom object
    // ...
}
```

The above code sample uses `javax.xml.parsers.DocumentBuilder`, `javax.xml.parsers.DocumentBuilderFactory`, and `org.w3c.dom.Document` to parse the response.

### Example: Marking a Track as a Favorite ###

You can mark a track as a favorite of the authenticated user by [issuing a put request](http://wiki.github.com/soundcloud/api/101-resources-users):

```
HttpResponse response = api.put("me/favorites/" + TRACK_ID);
// process the response...
```
### Example: Posting a Comment ###

The following example will [post a comment](http://wiki.github.com/soundcloud/api/102-resources-tracks-cont) for a track whose `id` is `TRACK_ID`:

```
List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("comment[body]", "This is a test comment"));
				
HttpResponse response = api.post("tracks/" + TRACK_ID + "/comments", params);
// process the response, e.g. to get the id of the comment...
```

### Example: Deleting a Comment ###

The following example will [delete a comment](http://wiki.github.com/soundcloud/api/102-resources-tracks-cont) with `id` `COMMENT_ID`:

```
HttpResponse response = api.delete("comments/" + COMMENT_ID);
// process the response...
```

### Example: Uploading a File ###

The following example will [upload a file via a POST request](http://wiki.github.com/soundcloud/api/102-resources-tracks-cont):

```
File file = new File(YOUR_FILE_NAME);
		
List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("track[title]", "This is a test upload"));
params.add(new BasicNameValuePair("track[sharing]", "private"));

HttpResponse response = api.upload(file, params);
// process the response...
```

## Playing in The Sandbox ##

SoundCloud provides a [sandbox site](http://sandbox-soundcloud.com) which you can use to test your app's functionality.  Note that you have to register your app separately for the [sandbox site](http://sandbox-soundcloud.com). To use the sandbox, you can provide an option when constructing the `SoundCloudAPI` object:

```
SoundCloudAPI api = new SoundCloudAPI(YOUR_APP_CONSUMER_KEY, YOUR_APP_CONSUMER_SECRET, SoundCloudAPI.USE_SANDBOX);
```

or

```
SoundCloudAPI api = new SoundCloudAPI(YOUR_APP_CONSUMER_KEY, YOUR_APP_CONSUMER_SECRET, token, tokenSecret, SoundCloudAPI.USE_SANDBOX);
```

## Further Reading ##

  * [SoundCloud API documentation](http://wiki.github.com/soundcloud/api)
  * [SoundCloud Java wrapper Javadoc documentation](http://urbanstew.org/soundcloudapi-java/doc/)