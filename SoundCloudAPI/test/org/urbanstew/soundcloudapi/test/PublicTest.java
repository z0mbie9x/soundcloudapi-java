package org.urbanstew.soundcloudapi.test;

import org.apache.http.HttpResponse;
import org.urbanstew.soundcloudapi.SoundCloudAPI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PublicTest extends TestCase
{

	public PublicTest(String name)
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

	public final void testGetUsers() throws Exception
	{
		HttpResponse response = mApi.get("users");
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	public static Test suite()
	{
		return new TestSuite(PublicTest.class);
	}
	
	SoundCloudAPI mApi;
}
