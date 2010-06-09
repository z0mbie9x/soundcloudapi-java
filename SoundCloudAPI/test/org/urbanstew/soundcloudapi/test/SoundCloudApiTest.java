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
import org.urbanstew.soundcloudapi.SoundCloudOptions;
import org.w3c.dom.Document;

public class SoundCloudApiTest
{
	private SoundCloudApiTest() {};
	
	public static SoundCloudAPI newSoundCloudAPI()
	{
		SoundCloudAPI api;
		String consumerKey, consumerSecret;
		
		if(sSoundCloudOptions.system == SoundCloudAPI.SoundCloudSystem.SANDBOX)
		{
			consumerKey = "eRxc2hzctTJzalJvG27A";
			consumerSecret = "KA7s7pfk8ZFRRXnhIhMWGh7tEprxdzlD7I1Q5cYD4";
		}
		else
		{
			consumerKey = "HXPgy7JJG5DLCMkyqBvksA";
			consumerSecret = "mXvEvhHstPLTqG8wtrfD6G4ZSZDhDrRkYzN8AREuA";			
		}
		
		if(sToken == null || sTokenSecret == null)
			api = new SoundCloudAPI(consumerKey, consumerSecret, sSoundCloudOptions);
		else
			api = new SoundCloudAPI(consumerKey, consumerSecret, sToken, sTokenSecret, sSoundCloudOptions);
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

	public static SoundCloudOptions sSoundCloudOptions =
		SoundCloudAPI.USE_SANDBOX;
//		SoundCloudAPI.USE_PRODUCTION;
	
	public static String
		sToken = null, sTokenSecret = null;

	// to avoid having to re-authorize, you can hard-code the token / token secret here:
	// (this will also disable the AuthorizationTest)
//		sToken = "...",
//		sTokenSecret= "...";
}
