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
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
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

	public final void testUploadFile() throws Exception
	{
		File file = new File("empty.wav");
		assertTrue(file.exists());
		
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("track[title]", "This is a test upload"));
		params.add(new BasicNameValuePair("track[sharing]", "private"));

		HttpResponse response = mApi.upload(file, params);
		assertEquals(201, response.getStatusLine().getStatusCode());
		
		sCreatedTrack1Id = getId(response);
	}

	public final void testGetMyTracks() throws Exception
	{
		HttpResponse response = mApi.get("me/tracks");
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		sTrackId = getId(response);
	}
	
	public final void testPostComment() throws Exception
	{
		assertTrue(sTrackId >= 0);
		
		List<NameValuePair> params = new java.util.ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("comment[body]", "This is a test comment"));
				
		HttpResponse response = mApi.post("tracks/" + sTrackId + "/comments", params);
		assertEquals(201, response.getStatusLine().getStatusCode());

		sCommentId = getId(response);
	}
	
	public final void testDeleteComment() throws Exception
	{
		HttpResponse response = mApi.delete("comments/" + sCommentId);
		assertEquals(200, response.getStatusLine().getStatusCode());		
	}
	
	public final void testPutTrackFavorite() throws Exception
	{
		HttpResponse response = mApi.put("me/favorites/" + sCreatedTrack1Id);
		assertEquals(201, response.getStatusLine().getStatusCode());
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
	
	SoundCloudAPI mApi;
	static int sUserId = -1, sTrackId = -1, sCommentId = -1, sCreatedTrack1Id = -1, sCreatedTrack2Id = -1;
}
