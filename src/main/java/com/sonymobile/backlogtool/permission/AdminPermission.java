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
package com.sonymobile.backlogtool.permission;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import com.sonymobile.backlogtool.Area;

/**
 * This class handles all admin permission checks.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
public class AdminPermission implements Permission {

    @Autowired
    SessionFactory sessionFactory;

    @Override
    /**
     * Checks if authenticated user is allowed to edit targetDomain area.
     */
    public boolean isAllowed(Authentication authentication, Object targetDomainObject) {
        if (authentication == null) {
            return false;
        }
        String areaName = targetDomainObject.toString();
        String username = authentication.getName();
        boolean hasPermission = false;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            User user = (User) session.get(User.class, username);
            Area area = (Area) session.get(Area.class, areaName);


            if ((area != null && area.isAdmin(username))
                    || (user != null && user.isMasterAdmin())) {
                //TODO: Check the area for LDAP groups here as well
                hasPermission = true;
                System.out.println("approved admin-permission for user " + authentication.getName()
                        + " area: " + areaName);
            } else {
                hasPermission = false;
                System.out.println("DENIED admin-permission for user " + authentication.getName()
                        + " area: " + areaName);
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return hasPermission;
    }

}