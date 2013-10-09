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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session; 
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


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
    
    /**
     * Rebuilds the rank ordering for a given backlog type
     * (in case there are missing numbers).
     * @param type backlog type to check rank on
     * @param area the area to look in
     * @param session hibernate session
     * @throws RuntimeException if invalid type was specified
     */
    public static void rebuildRanks(BacklogType type, Area area, Session session) throws RuntimeException {
        Query q = null;
        int counter = 1;
        switch (type) {
        case STORY:
            q=session.createQuery("from Story where area like ? and archived=false order by prio");
            q.setParameter(0, area);
            for (Story story : Util.castList(Story.class, q.list())) {
                story.setPrio(counter++);
            }
            break;
        case EPIC:
            q=session.createQuery("from Epic where area like ? and archived=false order by prio");
            q.setParameter(0, area);
            for (Epic epic : Util.castList(Epic.class, q.list())) {
                epic.setPrio(counter++);
            }
            break;
        case THEME: 
            q=session.createQuery("from Theme where area like ? and archived=false order by prio");
            q.setParameter(0, area);
            for (Theme theme : Util.castList(Theme.class, q.list())) {
                theme.setPrio(counter++);
            }
            break;
        default:
            throw new RuntimeException("Invalid type specified");
        }
        
    }
    
    /**
     * Returns the area with argument name if it exists.
     * @param areaName Area name to search for
     * @param sessionFactory hibernate session factory
     * @return area
     */
    public static Area getArea(String areaName, SessionFactory sessionFactory) {
        Area area = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            area = (Area) session.createCriteria(Area.class)
                    .add( Restrictions.like("name", areaName) )
                    .setFetchMode("storyAttr1", FetchMode.JOIN)
                    .setFetchMode("storyAttr2", FetchMode.JOIN)
                    .setFetchMode("storyAttr3", FetchMode.JOIN)
                    .setFetchMode("taskAttr1", FetchMode.JOIN)
                    .setFetchMode("editors", FetchMode.JOIN)
                    .setFetchMode("admins", FetchMode.JOIN)
                    .uniqueResult();

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return area;
    }

    /**
     * @return text where <a>-tags have been added around URLs
     * and newline-chars have been replaced with <br />.
     */
    public static String textAsHtmlLinksAndLineBreaks(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(?i)((https|http):\\/\\/[^\\s]+)", "<a href='$1'>$1</a>")
                .replaceAll("\\n", "<br />");
    }
    
    /**
     * Checks if active user is logged in.
     * @return true if logged in, otherwise false
     */
    public static boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        if (auth == null) {
            return false;
        }
        GrantedAuthority anonymous = new SimpleGrantedAuthority(
                "ROLE_ANONYMOUS");
        return !auth.getAuthorities().contains(anonymous);
    }
    
    /**
     * Gets the username of the active user.
     * @return username or null if the user is not logged in
     */
    public static String getUserName() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getName();
    }
}
