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

import org.atmosphere.cpr.BroadcastFilter;
import static org.atmosphere.cpr.BroadcastFilter.BroadcastAction.ACTION;

/**
 * Filter for adding a delimiter after every pushed message.
 * Used for pushing several JSON objects like {msg1},{msg2},
 * where the last delimiter can be removed and the whole message placed
 * within [ ] in order to create a valid JSON array.
 * @author Fredrik Persson &lt;fredrik6.persson@sonymobile.com&gt;
 *
 */
public class BroadcastDelimiter implements BroadcastFilter {
    
    private static final String DELIMITER = ",";

    @Override
    public BroadcastAction filter(Object originalMessage, Object message) {
        return new BroadcastAction(ACTION.CONTINUE, message.toString() + DELIMITER);
    }

}
