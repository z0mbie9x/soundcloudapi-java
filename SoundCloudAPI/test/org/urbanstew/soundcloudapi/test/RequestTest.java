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

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.urbanstew.soundcloudapi.ProgressFileBody;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RequestTest extends TestCase
{

	public RequestTest(String name)
	{
		super(name);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		
		mApi = SoundCloudApiTest.newSoundCloudAPI();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public final void testGetMe() throws Exception
	{
		HttpResponse response = mApi.get("me");
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		sUserId = getId(response);
		assertTrue(sUserId > 0);
		
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("q", "seba"));

		response = mApi.get("users", params);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}
	
	public final void testUrlGetMe() throws Exception
	{
		String protocol = SoundCloudApiTest.sSoundCloudOptions.version == SoundCloudAPI.OAuthVersion.V2_0 ? "https" : "http";
		HttpResponse response = mApi.get(protocol +
			(SoundCloudApiTest.sSoundCloudOptions.system == SoundCloudAPI.SoundCloudSystem.SANDBOX ?
				"://api.sandbox-soundcloud.com/me"
				: "://api.soundcloud.com/me"));
		
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	public final void testAbsoluteUrlGetMe() throws Exception
	{
		HttpResponse response = mApi.get("/me");
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	public final void testGetMyTracks() throws Exception
	{
		HttpResponse response = mApi.get("me/tracks");
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		sTrackId = getId(response);

		response = mApi.get("me/tracks");
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		sStreamUrl = getStreamUrl(response);
	}
	
	public final void testGetStream() throws Exception
	{
		HttpResponse response = mApi.getStream(sStreamUrl);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}
	
	public final void testGetStreamRedirect() throws Exception
	{
		String redirectedUrl = mApi.getRedirectedStreamUrl(sStreamUrl);
		assertTrue(redirectedUrl.startsWith("http://ak-media") || redirectedUrl.startsWith("https://ak-media"));
	}

	public final void testPostComment() throws Exception
	{
		assertTrue(sTrackId >= 0);

		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("comment[body]", "This is a test comment"));

		String commentUrl = "tracks/" + sTrackId + "/comments";
		HttpResponse response = mApi.post(commentUrl, params);
		assertEquals(201, response.getStatusLine().getStatusCode());

		sCommentId = getId(response);
	}
	
	public final void testDeleteComment() throws Exception
	{
		HttpResponse response = mApi.delete("comments/" + sCommentId);
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}
	
	public final void testPostDeleteCommentXML() throws Exception
	{
		assertTrue(sTrackId >= 0);

		String commentUrl = "tracks/" + sTrackId + "/comments";

		StringEntity entity = new StringEntity("<comment><body>This is a test XML comment</body></comment>");
		entity.setContentType("application/xml");

		HttpResponse response = mApi.post(commentUrl, entity);
		assertEquals(201, response.getStatusLine().getStatusCode());

		response = mApi.delete("comments/" + getId(response));
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}
	
	public final void testPutMeWebsite() throws Exception
	{
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user[website]", "http://website.example.com"));

		HttpResponse response = mApi.put("/me", params);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}
	
	public final void testPutMeWebsiteXML() throws Exception
	{
		StringEntity entity = new StringEntity("<user><website>http://website.example.com</website></user>");
		entity.setContentType("application/xml");

		HttpResponse response = mApi.put("/me", entity);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	public final void testPutTrackFavorite() throws Exception
	{
		HttpResponse response = mApi.put("me/favorites/" + sCreatedTrack1Id);
		assertEquals(2, response.getStatusLine().getStatusCode() / 100);
	}
	
	public final void testUploadFile() throws Exception
	{
		File file = new File("empty.wav");
		assertTrue(file.exists());
		
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("track[title]", "This is a test upload from empty.wav"));
		params.add(new BasicNameValuePair("track[sharing]", "private"));

		HttpResponse response = mApi.upload(file, params);
		assertEquals(201, response.getStatusLine().getStatusCode());
		
		sCreatedTrack1Id = getId(response);
	}

	public final void testUploadFileFromByteArray() throws Exception
	{
		byte[] emptywav = {
				0x52, 0x49, 0x46, 0x46, 0x7C, 0x00, 0x00, 0x00, 0x57, 0x41, 0x56, 0x45, 0x66, 0x6D, 0x74, 0x20, 0x10, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x44, (byte) 0xAC, 0x00, 0x00, (byte) 0x88, 0x58, 0x01, 0x00, 0x02, 0x00, 0x10, 0x00, 0x64, 0x61, 0x74, 0x61, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
			};
		
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("track[title]", "This is a test upload"));
		params.add(new BasicNameValuePair("track[sharing]", "private"));

		HttpResponse response = mApi.upload(new StringBody(new String(emptywav)), params);
		assertEquals(201, response.getStatusLine().getStatusCode());		

		sCreatedTrack2Id = getId(response);
	}
	
	public final void testUploadCountedFile() throws Exception
	{
		File file = new File("empty.wav");
		assertTrue(file.exists());
		
		final List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("track[title]", "This is a test upload"));
		params.add(new BasicNameValuePair("track[sharing]", "private"));

		final ProgressFileBody fileBody = new ProgressFileBody(file);
		
		Thread progressThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					HttpResponse response = mApi.upload(fileBody, params);
					assertEquals(201, response.getStatusLine().getStatusCode());
					sCreatedTrack3Id = getId(response);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		progressThread.start();
		while(progressThread.isAlive())
		{
			Thread.sleep(100);
//			System.out.println(fileBody.getBytesTransferred());
		}

		assertEquals(73772, fileBody.getBytesTransferred());
	}
	
	public final void testFile1Delete() throws Exception
	{
		assertTrue(sCreatedTrack1Id > 0);

		HttpResponse response = mApi.delete("tracks/" + sCreatedTrack1Id);
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}
	
	public final void testFile2Delete() throws Exception
	{
		assertTrue(sCreatedTrack2Id > 0);

		HttpResponse response = mApi.delete("tracks/" + sCreatedTrack2Id);
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}

	public final void testFile3Delete() throws Exception
	{
		assertTrue(sCreatedTrack3Id > 0);

		HttpResponse response = mApi.delete("tracks/" + sCreatedTrack3Id);
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}
	
	public static Test suite()
	{
		return new TestSuite(RequestTest.class);
	}

	
	private int getId(HttpResponse response) throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = db.parse(response.getEntity().getContent());

		return Integer.parseInt(dom.getElementsByTagName("id").item(0).getFirstChild().getNodeValue());
	}
	
	private String getStreamUrl(HttpResponse response) throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = db.parse(response.getEntity().getContent());

		return dom.getElementsByTagName("stream-url").item(0).getFirstChild().getNodeValue();
	}
	
	SoundCloudAPI mApi;
	static int sUserId = -1, sTrackId = -1, sCommentId = -1, sCreatedTrack1Id = -1, sCreatedTrack2Id = -1, sCreatedTrack3Id = -1;
	static String sStreamUrl = null;
}
