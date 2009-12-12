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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
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
     * been obtained, with non-default options.
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
     * Constructor for the case when the access token has been obtained, with non-default options.
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
	
    /**
     * Obtains the request token from Sound Cloud
     * @return authorization URL on success, null otherwise.
     */
	public String obtainRequestToken()
	{
		return obtainRequestToken(null);
	}

    /**
     * Obtains the request token from Sound Cloud, with a specified callback URL.
     * @return authorization URL on success, null otherwise.
     */
	public String obtainRequestToken(String callbackURL)
	{
		mState = State.UNAUTHORIZED;
      
        try
		{
        	if(callbackURL == null && mOptions.version == OAuthVersion.V1_0_A)
        		callbackURL = OAuth.OUT_OF_BAND;
			String url = mProvider.retrieveRequestToken(callbackURL);
			mState = State.REQUEST_TOKEN_OBTAINED;
			return url;
		} catch (Exception e)
		{
			mLastException = e;
		}
        
		return null;
	}
	
	/**
     * Swaps the authorized request token for an access token.
     */
	public boolean obtainAccessToken(String verificationCode)
	{
		try
		{
			mProvider.retrieveAccessToken(verificationCode);
			mState = State.AUTHORIZED;
			return true;
		} catch (Exception e)
		{
			mLastException = e;
		}
		return false;
	}
	
    /**
     * Performs a GET request on a specified resource.
     */
	public HttpResponse get(String resource)
	{
		return get(resource, null);
	}

    /**
     * Performs a GET request on a specified resource, with parameters.
     */
	public HttpResponse get(String resource, List<NameValuePair> params)
	{
		return performRequest(new HttpGet(urlEncode(resource, params)));
	}

    /**
     * Performs a PUT request on a specified resource.
     */
	public HttpResponse put(String resource)
	{
		return put(resource, null);
	}

    /**
     * Performs a PUT request on a specified resource, with parameters.
     */
	public HttpResponse put(String resource, List<NameValuePair> params)
	{
        return performRequest(new HttpPut(urlEncode(resource, params)));   
	}

    /**
     * Performs a POST request on a specified resource.
     */
	public HttpResponse post(String resource)
	{
		return post(resource, null);
	}

    /**
     * Performs a POST request on a specified resource, with parameters.
     */
	public HttpResponse post(String resource, List<NameValuePair> params)
	{
        return performRequest(new HttpPost(urlEncode(resource, params)));   
	}

    /**
     * Performs a DELETE request on a specified resource.
     */
	public HttpResponse delete(String resource)
	{
		return delete(resource, null);
	}

    /**
     * Performs a DELETE request on a specified resource, with parameters.
     */
	public HttpResponse delete(String resource, List<NameValuePair> params)
	{
        return performRequest(new HttpDelete(urlEncode(resource, params)));   
	}
	
	/**
     * Uploads a file by performing a POST request on the "tracks" resource.
     */
	public HttpResponse upload(File file, List<NameValuePair> params)
	{
		return upload(new FileBody(file), params);
	}

	/**
     * Uploads an arbitrary body by performing a POST request on the "tracks" resource.
     */
	public HttpResponse upload(ContentBody fileBody, List<NameValuePair> params)
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
	
	private HttpResponse performRequest(HttpUriRequest request)
	{
        try
		{
			mConsumer.sign(request);
	        return httpClient.execute(request);
		} catch (Exception e)
		{
			mLastException = e;
		}
		
		return null;
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
	
    /**
     * Returns the last exception thrown.
     */
	public Exception getLastException()
	{
		return mLastException;
	}
		    
    private State mState;
    
    OAuthConsumer mConsumer;
    OAuthProvider mProvider;
    
	String mSoundCloudURL, mSoundCloudApiURL;

	SoundCloudOptions mOptions;
	
    HttpClient httpClient = new DefaultHttpClient();

    private Exception mLastException = null;
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
