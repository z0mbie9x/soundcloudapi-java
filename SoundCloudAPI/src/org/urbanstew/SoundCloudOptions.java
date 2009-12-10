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

package org.urbanstew;

import org.urbanstew.SoundCloudAPI.OAuthVersion;
import org.urbanstew.SoundCloudAPI.SoundCloudSystem;

public class SoundCloudOptions
{
	SoundCloudOptions()
	{}
	
	public SoundCloudOptions(SoundCloudSystem system)
	{
		this.system = system;
	}
	public void setVersion(OAuthVersion newVersion)
	{	version = newVersion; }
	
	public void setSystem(SoundCloudSystem newSystem)
	{	system = newSystem; }
	
	public OAuthVersion version = OAuthVersion.V1_0_A;
	public SoundCloudSystem system = SoundCloudSystem.PRODUCTION;
}
