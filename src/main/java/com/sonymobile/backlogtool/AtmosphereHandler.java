/*
 *  The MIT License
 *
 *  Copyright 2013 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package com.sonymobile.backlogtool;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.Meteor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Responsible for providing Atmosphere-specific functions, such as suspending
 * clients, pushing data and resolve method parameters into argument values
 * 
 * @author Christoffer Lauri <christoffer.lauri@sonymobile.com>
 */
public final class AtmosphereHandler implements HandlerMethodArgumentResolver {

    /**
     * Suspend the client and add it to the Broadcaster associated with the
     * specified area (register the client for push-notifications for the
     * specified area)
     * 
     * @param resource
     *            The AtmosphereResource for the client
     * @param areaName
     *            The area(name) to associate the resource/client with
     */
    public static void suspendClient(final AtmosphereResource resource,
            String areaName) {
        Broadcaster broadcaster = getBroadcasterForArea(areaName);
        resource.setBroadcaster(broadcaster).suspend();
    }

    /**
     * Find the Broadcaster for the specified area. Either returns an existing
     * Broadcaster, or creates a new one if none exists.
     * 
     * @param areaName
     *            The name of the area
     * @return The Broadcaster for the specified area
     */
    public static Broadcaster getBroadcasterForArea(String areaName) {
        Broadcaster bc = BroadcasterFactory.getDefault().lookup(areaName);
        if (bc == null) {
            bc = BroadcasterFactory.getDefault().get();
            bc.setID(areaName);
            bc.getBroadcasterConfig().addFilter(new BroadcastDelimiter());
            BroadcasterFactory.getDefault().add(bc, bc.getID());
        }
        return bc;
    }

    /**
     * Push a notification with data "hello" to all clients registered on the
     * specified area
     * 
     * @param areaName
     *            The name of the area
     */
    public static void push(String areaName) {
        push(areaName, "{\"hello\":1}");
    }

    /**
     * Send several messages in one push
     * @param area Area to send push-messages to
     * @param messages A List of text-messages
     */
    public static void pushJsonMessages(String area, List<String> messages) {
        StringBuilder sb = new StringBuilder();
        if (messages != null) {
            for (String s : messages) {
                sb.append(s).append(',');
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        push(area, sb.toString());
    }

    /**
     * Push a notification to all clients registered on the specified area
     * 
     * @param areaName
     *            The name of the area
     * @param data
     *            The data to send in the push
     */
    public static void push(String areaName, String data) {
//        System.out.println("=== INFO === Pushing data:\n \t " + data
//                + "\n to area " + areaName);
        Broadcaster bc = getBroadcasterForArea(areaName);
        bc.broadcast(data);
    }

    @Override
    public Object resolveArgument(MethodParameter param,
            ModelAndViewContainer cont, NativeWebRequest req,
            WebDataBinderFactory bFac) throws Exception {
        HttpServletRequest httpReq = req
                .getNativeRequest(HttpServletRequest.class);
        return Meteor.build(httpReq).getAtmosphereResource();
    }

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return AtmosphereResource.class.isAssignableFrom(param
                .getParameterType());
    }

}
