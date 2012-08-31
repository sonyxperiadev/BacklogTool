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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.sonymobile.backlogtool.permission.User;

/**
 * This class listens for startup events and updates the master admin list
 * from the configuration file on server launch.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 */
@Component
public class StartUpListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    SessionFactory sessionFactory;

    /**
     * Updates the master admin list when the application has been launched.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            String backlogconf = System.getProperty("catalina.home") + File.separator + "conf/backlogtool.properties";
            InputStream input = new FileInputStream(backlogconf);
            Properties propertiesFile = new Properties();
            propertiesFile.load(input);

            String[] masterAdmins = propertiesFile.getProperty("masterAdmins").split(",");

            //Delete all current masterAdmins.
            Query query = session.createQuery("from User");
            List<User> users = query.list();
            for (User user : users) {
                user.setMasterAdmin(false);
            }

            //Add all new masterAdmins.
            for (String userName : masterAdmins) {
                userName = userName.trim();
                if (!userName.isEmpty()) {
                    User user = (User) session.get(User.class, userName);
                    if (user == null) {
                        user = new User();
                    }
                    user.setId(userName);
                    user.setMasterAdmin(true);
                    session.saveOrUpdate(user);
                }
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
    }
}
