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

import org.urbanstew.SoundCloudAPI;

public class SoundCloudApiTest
{
	private SoundCloudApiTest() {};
	
	public static SoundCloudAPI newSoundCloudAPI()
	{
		SoundCloudAPI api;
		if(sToken == null || sTokenSecret == null)
			api = new SoundCloudAPI("7YEA234XGDyv1OoBg8k9cA", "Go08iXE2ikRHBBagjF32EDE7cF0wmBOX3VAfqHTVA", SoundCloudAPI.USE_SANDBOX);
		else
			api = new SoundCloudAPI("7YEA234XGDyv1OoBg8k9cA", "Go08iXE2ikRHBBagjF32EDE7cF0wmBOX3VAfqHTVA", sToken, sTokenSecret, SoundCloudAPI.USE_SANDBOX);
		return api;
	}
	public static String
		sToken = null, sTokenSecret = null;
	
	// to avoid having to re-authorize, you can hard-code the token / token secret here:
	// (this will also disable the AuthorizationTest)
//		sToken = "...",
//		sTokenSecret= "...";
}
