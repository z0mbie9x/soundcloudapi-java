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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpResponse;
import org.urbanstew.soundcloudapi.SoundCloudAPI;
import org.w3c.dom.Document;

public class SoundCloudApiTest
{
	private SoundCloudApiTest() {};
	
	public static SoundCloudAPI newSoundCloudAPI()
	{
		SoundCloudAPI api;
		if(sToken == null || sTokenSecret == null)
			api = new SoundCloudAPI("x5vOJhXYQk5diUTsTa5FA", "QEKE7XfdwUdNl9qiqAx3xHZhtS6iPjT3NnBj6sx8", SoundCloudAPI.USE_SANDBOX);
		else
			api = new SoundCloudAPI("x5vOJhXYQk5diUTsTa5FA", "QEKE7XfdwUdNl9qiqAx3xHZhtS6iPjT3NnBj6sx8", sToken, sTokenSecret, SoundCloudAPI.USE_SANDBOX);
		return api;
	}
	
	public static void printXML(String title, HttpResponse response) throws Exception
	{
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = db.parse(response.getEntity().getContent());

		System.out.println(title + " response XML:");
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom), result);
	    System.out.println(writer.toString());
	}

	public static String
		sToken = null, sTokenSecret = null;

	// to avoid having to re-authorize, you can hard-code the token / token secret here:
	// (this will also disable the AuthorizationTest)
//		sToken = "...",
//		sTokenSecret= "...";
}
