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

package org.urbanstew.soundcloudapi.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.urbanstew.soundcloudapi.SoundCloudAPI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FullExample extends TestCase
{

	public FullExample(String name)
	{
		super(name);
	}

	public final void testFullExample() throws Exception
	{
		// initialize using the test application's consumer key and secret
		// (these are valid for the sandbox only
		//   - to use your consumer key/secret on the production SoundCloud site,
		//     omit the SoundCloudAPI.USE_SANDBOX parameter) 
		SoundCloudAPI api = new SoundCloudAPI("x5vOJhXYQk5diUTsTa5FA", "QEKE7XfdwUdNl9qiqAx3xHZhtS6iPjT3NnBj6sx8", SoundCloudAPI.USE_SANDBOX);
		
		// obtain the request token
		String authorizationUrl = api.obtainRequestToken();
		
		// the user must now visit the authorization URL, and obtain the verification code
		System.out.println("Please visit the following URL to authorize: " + authorizationUrl);

		InputStreamReader reader = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(reader);

		System.out.println("Please enter the verification code:");
		String verificationCode = in.readLine();
		
		// swap the request token for the access token
		api.obtainAccessToken(verificationCode);
		
		// save the access token and secret for future use
		mAccessToken = api.getToken();
		mAccessTokenSecret = api.getTokenSecret();
		
		// send a request
		HttpResponse response = api.put("me/favorites/996704");
		System.out.println("Response from testFullExample:" + response.getStatusLine().toString());
		SoundCloudApiTest.printXML("", response);
	}

	public final void testReAuthorizationExample() throws Exception
	{
		// initialize using the test application's consumer key and secret,
		// and the stored access token and secret.
		// (the consumer key and secret are valid for the sandbox only
		//   - to use your consumer key/secret on the production SoundCloud site,
		//     omit the SoundCloudAPI.USE_SANDBOX parameter) 
		SoundCloudAPI api = new SoundCloudAPI("x5vOJhXYQk5diUTsTa5FA", "QEKE7XfdwUdNl9qiqAx3xHZhtS6iPjT3NnBj6sx8", mAccessToken, mAccessTokenSecret, SoundCloudAPI.USE_SANDBOX);
		
		// send a request
		HttpResponse response = api.put("me/favorites/996705");
		System.out.println("Response from testReAuthorizationExample:" + response.getStatusLine().toString());
		SoundCloudApiTest.printXML("", response);
	}
	
	public static Test suite()
	{
		return new TestSuite(FullExample.class);
	}
	
	static String mAccessToken, mAccessTokenSecret;
}
