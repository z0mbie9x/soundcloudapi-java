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

package org.urbanstew.test.SoundCloudAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.urbanstew.SoundCloudAPI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AuthorizationTest extends TestCase
{

	public AuthorizationTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		mApi = SoundCloudApiTest.newSoundCloudAPI();

		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public final void testAuthorization() throws IOException
	{
		if(SoundCloudApiTest.sToken != null && SoundCloudApiTest.sTokenSecret != null)
			return;

		assertEquals(mApi.getState(), SoundCloudAPI.State.UNAUTHORIZED);
		
		String authorizationUrl = mApi.obtainRequestToken();
		assertNotNull(authorizationUrl);
		assertEquals(mApi.getState(), SoundCloudAPI.State.REQUEST_TOKEN_OBTAINED);
		
		String verificationCode = "";
		if(authorizationUrl != null)
		{
			System.out.println(authorizationUrl);
			InputStreamReader reader = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(reader);

			verificationCode = in.readLine();
			
			System.out.println("Verification code is: " + verificationCode);
		}

		boolean success = mApi.obtainAccessToken(verificationCode);
		if(!success)
		{
			mApi.getLastException().printStackTrace();
		}
		assertTrue(success);
		assertEquals(mApi.getState(), SoundCloudAPI.State.AUTHORIZED);
		
		System.out.println("sToken = \"" + mApi.getToken() + "\",");
		System.out.println("sTokenSecret = \"" + mApi.getTokenSecret() + "\";");
		SoundCloudApiTest.sToken = mApi.getToken();
		SoundCloudApiTest.sTokenSecret = mApi.getTokenSecret();
	}
	
	public static Test suite()
	{
		return new TestSuite(AuthorizationTest.class);
	}
	
	SoundCloudAPI mApi;
}
