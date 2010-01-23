/*
 *  Copyright 2009 urbanSTEW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.urbanstew.soundcloudapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.NameValuePair;

public class SoundCloudAPI
{
	public enum OAuthVersion
	{
		V1_0,
		V1_0_A
	}
	
	public enum SoundCloudSystem
	{
		PRODUCTION,
		SANDBOX
	}
	
	public static final SoundCloudOptions USE_SANDBOX = new SoundCloudOptions(SoundCloudSystem.SANDBOX);
	public static final SoundCloudOptions USE_PRODUCTION = new SoundCloudOptions(SoundCloudSystem.PRODUCTION);
	
    public enum State
    {
    	UNAUTHORIZED,
    	REQUEST_TOKEN_OBTAINED,
    	AUTHORIZED
    };

    /**
     * Constructor for the case when neither the request or access token have
     * been obtained.
     */
    public SoundCloudAPI(String consumerKey, String consumerSecret)
	{
		this(consumerKey, consumerSecret, "", "", new SoundCloudOptions());
	}
    
    /**
     * Constructor for the case when neither the request or access token have
     * been obtained, with specified options.
     */
    public SoundCloudAPI(String consumerKey, String consumerSecret, SoundCloudOptions options)
	{
		this(consumerKey, consumerSecret, "", "", options);
	}

    /**
     * Constructor for the case when the access token has been obtained.
     */
    public SoundCloudAPI(String consumerKey, String consumerSecret, String token, String tokenSecret)
	{
    	this(consumerKey, consumerSecret, token, tokenSecret, new SoundCloudOptions());
	}
    
    /**
     * Constructor for the case when the access token has been obtained, with specified options.
     */
    public SoundCloudAPI(String consumerKey, String consumerSecret, String token, String tokenSecret, SoundCloudOptions options)
	{
    	mOptions = options;
		mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret, SignatureMethod.HMAC_SHA1);
    	if(token.length()==0 || tokenSecret.length()==0)
    	{
    		mState = State.UNAUTHORIZED;
    	}
    	else
    	{
    		mState = State.AUTHORIZED;
    		mConsumer.setTokenWithSecret(token, tokenSecret);
    	}
		setUsingSandbox(options.system == SoundCloudSystem.SANDBOX);
	}
	
    /**
     * Constructor from another SoundCloudAPI.
     */
    public SoundCloudAPI(SoundCloudAPI soundCloudAPI)
	{
        mState = soundCloudAPI.mState;
        mOptions = soundCloudAPI.mOptions;
        mConsumer = new CommonsHttpOAuthConsumer(soundCloudAPI.mConsumer.getConsumerKey(), soundCloudAPI.mConsumer.getConsumerSecret(), SignatureMethod.HMAC_SHA1);
        if(mState == State.AUTHORIZED)
        	mConsumer.setTokenWithSecret(soundCloudAPI.mConsumer.getToken(), soundCloudAPI.mConsumer.getTokenSecret());
    	mSoundCloudURL = soundCloudAPI.mSoundCloudURL;
    	mSoundCloudApiURL = soundCloudAPI.mSoundCloudApiURL;

    	mProvider = new DefaultOAuthProvider
	    	(
	    		mConsumer,
	    		mSoundCloudApiURL + "oauth/request_token",
	    		mSoundCloudApiURL + "oauth/access_token",
	    		mSoundCloudURL + "oauth/authorize"
	    	);
	}
	
	private void setUsingSandbox(boolean use)
	{
		if(!use)
		{
			mSoundCloudURL = "http://soundcloud.com/";
			mSoundCloudApiURL = "http://api.soundcloud.com/";
		}
		else
		{
			mSoundCloudURL = "http://sandbox-soundcloud.com/";
			mSoundCloudApiURL = "http://api.sandbox-soundcloud.com/";
		}
		
	    mProvider = new DefaultOAuthProvider
	    	(
	    		mConsumer,
	    		mSoundCloudApiURL + "oauth/request_token",
	    		mSoundCloudApiURL + "oauth/access_token",
	    		mSoundCloudURL + "oauth/authorize"
	    	);
	}
	
	public void unauthorize()
	{
		mConsumer.setTokenWithSecret("", "");
		mState = State.UNAUTHORIZED;
	}
	
    /**
     * Obtains the request token from Sound Cloud
     * @return authorization URL on success, null otherwise.
     * @throws OAuthCommunicationException 
     * @throws OAuthExpectationFailedException 
     * @throws OAuthNotAuthorizedException 
     * @throws OAuthMessageSignerException 
     */
	public String obtainRequestToken() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		return obtainRequestToken(null);
	}

    /**
     * Obtains the request token from Sound Cloud, with a specified callback URL.
     * @return authorization URL on success, null otherwise.
     * @throws OAuthCommunicationException 
     * @throws OAuthExpectationFailedException 
     * @throws OAuthNotAuthorizedException 
     * @throws OAuthMessageSignerException 
     */
	public String obtainRequestToken(String callbackURL) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		unauthorize();
      
    	if(callbackURL == null && mOptions.version == OAuthVersion.V1_0_A)
    		callbackURL = OAuth.OUT_OF_BAND;
		String url = mProvider.retrieveRequestToken(callbackURL);
		mState = State.REQUEST_TOKEN_OBTAINED;
		return url;
	}
	
    /**
     * Completes the OAuth 1.0a authorization steps with Sound Cloud, assuming the consumer application
     * can use a local port to receive the verification code.
     * 
     * <p>The function acts as a minimal HTTP server and will listen on the port specified in the
     * <code>url</code> (or the default HTTP port, if no port is specified in the <code>url</code>).  It will provide the
     * specified <code>response</code> when it receives a request for the path specified in the <code>url</code>, and
     * assuming the request includes the verification code, terminate successfully.
     * To all other requests it will respond with a <code>Not Found</code> error, and continue listening.
     * 
     * <p>The following example assumes the consumer application is running on the client's computer / device.
     * Hence, it uses a local URL ("http://localhost/") to receive the verification code callback. The function
     * will listen on specified port 8088 to receive the callback.</p>
     * 
     * <pre>
     * {@code
     *  soundcloudapi.authorizeUsingUrl
	 *	(
	 *		"http://localhost:8088/",
	 *		"Thank you for authorizing",
	 *		new AuthorizationURLOpener()
	 *		{
	 *			public void openAuthorizationURL(String authorizationURL)
	 *			{
	 *				System.out.println("Please visit " + authorizationURL);
	 *			}
	 *		}
	 *	);
	 * }
	 * </pre>
	 * 
     * @param url - a callback URL via which the user can provide the verification code.
     * @param response - a response given back to the user when they allow access and get redirected to the callback URL.
     * @param URLOpener - an AuthorizationURLOpener which can open the authorization URL to the user when needed.
	 *
     * @return true if the process is completed successfully, false if the process was canceled via <code>cancelAuthorizeUsingUrl</code>.  
     *  
     * @throws OAuthCommunicationException 
     * @throws OAuthExpectationFailedException 
     * @throws OAuthNotAuthorizedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 

     * @since 0.9.1
     * @see #cancelAuthorizeUsingUrl()
	*/
	public boolean authorizeUsingUrl(final String url, final String response, final AuthorizationURLOpener URLOpener) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException
	{
		mCancelAuthorization = false;
		unauthorize();

		URLOpener.openAuthorizationURL(obtainRequestToken(url));

		URL parsedUrl = new URL(url);
		int port = parsedUrl.getPort();
		if(port == -1)
			port = parsedUrl.getDefaultPort();

		ServerSocket server = new ServerSocket(port);
		server.setSoTimeout(500);
		String verificationCode=null;
		while(verificationCode==null)
		{
			Socket socket = null;
			try
			{
				socket = server.accept();
			} catch (java.io.InterruptedIOException e)
			{
				if(mCancelAuthorization)
				{
					server.close();
					unauthorize();
					return false;
				}
				else continue;
			}
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	
			String requestedUrl = is.readLine().split("\\s+")[1];
			
			URL parsedRequestedUrl = new URL("http://localhost" + requestedUrl);
	        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

	        if(!parsedRequestedUrl.getPath().equals(parsedUrl.getPath()))
			{
	        	out.print("HTTP/1.1 404 Not Found");
			}
	        else
	        {
		        out.print("HTTP/1.1 200 OK\n\n" + response);
		        for(String parameter : parsedRequestedUrl.getQuery().split("&"))
		        {
		        	String[] keyValue = parameter.split("=");
		        	if(keyValue[0].equals("oauth_verifier"))
		        		verificationCode=keyValue[1];
		        }
	        }
	        out.close();
	        socket.close();
		}
        server.close();
        
        obtainAccessToken(verificationCode);
        return true;
	}
	
	/**
     * If a call to authorizeUsingUrl is currently executing, it will be canceled and return
     * shortly after cancelAuthorizeUsingUrl is called.  If there is no active authorizeUsingUrl
     * call, there is no effect.
     * 
     * @see #authorizeUsingUrl(String, String, AuthorizationURLOpener)
     * @since 0.9.1
     */
	public void cancelAuthorizeUsingUrl()
	{
		mCancelAuthorization = true;
	}
	
	/**
     * Swaps the authorized request token for an access token.
	 * @throws OAuthCommunicationException 
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthNotAuthorizedException 
	 * @throws OAuthMessageSignerException 
     */
	public void obtainAccessToken(String verificationCode) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException
	{
		mProvider.retrieveAccessToken(verificationCode);
		mState = State.AUTHORIZED;
	}
	
    /**
     * Performs a GET request on a specified resource.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse get(String resource) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return get(resource, null);
	}

    /**
     * Performs a GET request on a specified resource, with parameters.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse get(String resource, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return performRequest(new HttpGet(urlEncode(resource, params)));
	}

    /**
     * Performs a PUT request on a specified resource.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse put(String resource) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return put(resource, null);
	}

    /**
     * Performs a PUT request on a specified resource, with parameters.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse put(String resource, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
        return performRequest(new HttpPut(urlEncode(resource, params)));   
	}

    /**
     * Performs a POST request on a specified resource.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse post(String resource) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return post(resource, null);
	}

    /**
     * Performs a POST request on a specified resource, with parameters.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse post(String resource, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
        return performRequest(new HttpPost(urlEncode(resource, params)));   
	}

    /**
     * Performs a DELETE request on a specified resource.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse delete(String resource) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return delete(resource, null);
	}

    /**
     * Performs a DELETE request on a specified resource, with parameters.
     * @throws OAuthExpectationFailedException 
     * @throws OAuthMessageSignerException 
     * @throws IOException 
     * @throws ClientProtocolException 
     */
	public HttpResponse delete(String resource, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
        return performRequest(new HttpDelete(urlEncode(resource, params)));   
	}
	
	/**
     * Uploads a file by performing a POST request on the "tracks" resource.
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthMessageSignerException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
     */
	public HttpResponse upload(File file, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		return upload(new FileBody(file), params);
	}

	/**
     * Uploads an arbitrary body by performing a POST request on the "tracks" resource.
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthMessageSignerException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
     */
	public HttpResponse upload(ContentBody fileBody, List<NameValuePair> params) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		HttpPost post = new HttpPost(urlEncode("tracks", null));  
		 
		MultipartEntity entity = new MultipartEntity();
		for(NameValuePair pair : params)
		{
			try
			{
				entity.addPart(pair.getName(), new StringBodyNoHeaders(pair.getValue()));
			} catch (UnsupportedEncodingException e)
			{
			}  
		}
		entity.addPart("track[asset_data]", fileBody);  

		post.setEntity(entity);
		return performRequest(post);  
	}
	
	/**
     * Signs and performs a request.
	 * @throws OAuthExpectationFailedException 
	 * @throws OAuthMessageSignerException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
     */
	private HttpResponse performRequest(HttpUriRequest request) throws OAuthMessageSignerException, OAuthExpectationFailedException, ClientProtocolException, IOException
	{
		mConsumer.sign(request);
        return httpClient.execute(request);
	}
	
	private String urlEncode(String resource, List<NameValuePair> params)
	{
		String resourceUrl = mSoundCloudApiURL + resource;
		return params == null ?
			resourceUrl :
			resourceUrl + "?" + URLEncodedUtils.format(params, "UTF-8");
	}
	
    /**
     * Returns the Request or Access Token.
     */
    public String getToken()
	{
		return mConsumer.getToken();
	}

    /**
     * Returns the Request or Access Token Secret.
     */
	public String getTokenSecret()
	{
		return mConsumer.getTokenSecret();
	}
	
    /**
     * Returns the current state of the API wrapper.
     */
	public State getState()
	{
		return mState;
	}
	 
    private State mState;
    
    OAuthConsumer mConsumer;
    OAuthProvider mProvider;
    volatile boolean mCancelAuthorization;
    
	String mSoundCloudURL, mSoundCloudApiURL;

	SoundCloudOptions mOptions;
	
    HttpClient httpClient = new DefaultHttpClient();
}

class StringBodyNoHeaders extends StringBody
{
	public StringBodyNoHeaders(String value) throws UnsupportedEncodingException
	{
		super(value);
	}	
	
	public String getMimeType()
	{
		return null;
	}

	public String getTransferEncoding()
	{
		return null;
	}	
}
