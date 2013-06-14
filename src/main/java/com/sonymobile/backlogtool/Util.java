/*
 *  The MIT License
 *
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session; 


/**
 * Utility class.
 *
 * @author David Pursehouse &lt;david.pursehouse@sonymobile.com&gt;
 * @author Fredrik Persson &lt;fredrik6.persson@sonymobile.com&gt; 
 */
public final class Util {
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
        List<T> r = new ArrayList<T>(c.size());
        for (Object o: c) {
            r.add(clazz.cast(o));
        }
        return r;
    }

    /**
     * Returns next available rank. Used when creating new backlog items that should be placed 
     * at the bottom of the backlog.
     * @param type backlog type to check rank on
     * @param area the area to look in
     * @param session hibernate session
     * @return next available rank (the last1)
     * @throws RuntimeException if invalid type was specified
     */
    public static int getNextPrio(BacklogType type, Area area, Session session) throws RuntimeException {
        Query q = null;
        switch (type) {
        case STORY:
            q=session.createQuery("from Story where area like ? and archived=false");
            break;
        case EPIC:
            q=session.createQuery("from Epic where area like ? and archived=false");
            break;
        case THEME: 
            q=session.createQuery("from Theme where area like ? and archived=false");
            break;
        default:
            throw new RuntimeException("Invalid type specified");
        }
        q.setParameter(0, area);
        return q.list().size() + 1;
    } 
}
