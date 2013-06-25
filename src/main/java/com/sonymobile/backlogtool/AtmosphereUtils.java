/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sonymobile.backlogtool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunnar Hillert
 * @since  1.0
 *
 */
public final class AtmosphereUtils {

	public static final Logger LOG = LoggerFactory.getLogger(AtmosphereUtils.class);

	public static AtmosphereResource getAtmosphereResource(HttpServletRequest request) {
		return getMeteor(request).getAtmosphereResource();
	}
	public static Meteor getMeteor(HttpServletRequest request) {
		return Meteor.build(request);
	}
	public static void suspend(final AtmosphereResource resource, String areaName) {

		final CountDownLatch countDownLatch = new CountDownLatch(1);
		resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
			@Override
			public void onSuspend(AtmosphereResourceEvent event) {
				countDownLatch.countDown();
				LOG.info("Suspending Client..." + resource.uuid());
				resource.removeEventListener(this);
			}

			@Override
			public void onDisconnect(AtmosphereResourceEvent event) {
				LOG.info("Disconnecting Client..." + resource.uuid());
				super.onDisconnect(event);
			}

			@Override
			public void onBroadcast(AtmosphereResourceEvent event) {
				LOG.info("Client is broadcasting..." + resource.uuid());
				super.onBroadcast(event);
			}

		});
		
		//TODO: Can several instances of the same resource exist in the Broadcaster's list? 
		Broadcaster b = AtmosphereUtils.lookupBroadcaster(areaName);
//		b.removeAtmosphereResource(resource);
		
		String uuid = (String) resource.getRequest().getAttribute(ApplicationConfig.SUSPENDED_ATMOSPHERE_RESOURCE_UUID);
        AtmosphereResource originalEvent = AtmosphereResourceFactory.getDefault().find(uuid);
        
        boolean found = false;
        System.out.println("=== INFO === suspend(), resources: " + b.getAtmosphereResources().size());
        for (AtmosphereResource res : b.getAtmosphereResources()) {
            if (res.equals(originalEvent)) {
                found = true;
            }
        }
        
        System.out.println("=== INFO === suspend(), found: " + String.valueOf(found));
        if(!found) {
        	System.out.println("=== INFO === suspend(), adding resource");
        	b.addAtmosphereResource(resource);
        }
        System.out.println("=== INFO === suspend(), resources: " + b.getAtmosphereResources().size());
		if (AtmosphereResource.TRANSPORT.LONG_POLLING.equals(resource.transport())) {
			resource.resumeOnBroadcast(true).suspend(-1, false);
		} else {
			resource.suspend(-1);
		}

		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			LOG.error("Interrupted while trying to suspend resource {}", resource);
		}
	}

	public static Broadcaster lookupBroadcaster(String areaName) {
		System.out.println("=== INFO === lookupBroadcaster() for area " + areaName);
		Broadcaster bc = BroadcasterFactory.getDefault().lookup(areaName);
		if(bc == null) {
			System.out.println("=== INFO === lookupBroadcaster() for area " + areaName + ", no bc for the area");
			bc = BroadcasterFactory.getDefault().get();
			bc.setID(areaName);
			BroadcasterFactory.getDefault().add(bc, bc.getID());
		}
				
		return bc;
	}
	
	public static void push(String areaName) {
		System.out.println("=== INFO === Push for area " + areaName);
		Broadcaster bc = BroadcasterFactory.getDefault().lookup(areaName);
		bc.broadcast("hej");
	}
	
	public static void push(String areaname, String data) {
		
	}

}
