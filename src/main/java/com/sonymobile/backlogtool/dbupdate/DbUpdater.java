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
package com.sonymobile.backlogtool.dbupdate;

import org.hibernate.SessionFactory;

/**
 * This abstract class can be extended for making schema updates to the database.
 * The getFromVersion-method decides the ordering in which the subclasses are being run.
 * Subclasses should be named as "UpdateVersionX_Y" if it updates from schema version X to Y.
 * 
 * @author Fredrik Persson &lt;fredrik6.persson@sonymobile.com&gt;
 */
public abstract class DbUpdater implements Comparable<DbUpdater> {

    /**
     * @return which version this update goes FROM.
     */
    public abstract int getFromVersion();
    
    /**
     * @return which version this update goes TO.
     */
    public int getToVersion() {
        return getFromVersion() + 1;
    }

    /**
     * Performs the schema update.
     * @param sessionFactory hibernate sessionfactory
     * @return true if the update went well, otherwise false
     */
    public abstract boolean update(SessionFactory sessionFactory);
    
    @Override
    public int compareTo(DbUpdater o) {
        return getFromVersion()-o.getFromVersion();
    }

}
