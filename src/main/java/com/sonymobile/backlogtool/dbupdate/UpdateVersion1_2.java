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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;

import com.sonymobile.backlogtool.Epic;
import com.sonymobile.backlogtool.Story;
import com.sonymobile.backlogtool.Theme;

/**
 * Updates length of story description.
 * 
 * @author Fredrik Persson &lt;fredrik6.persson@sonymobile.com&gt;
 */
@DbUpdate
public class UpdateVersion1_2 extends DbUpdater {

    private static final int FROM_VERSION = 1;

    @Override
    public int getFromVersion() {
        return FROM_VERSION;
    }

    @Override
    public boolean update(SessionFactory sessionFactory) {
        boolean success = true;
        Properties propertiesFile = new Properties();
        try {
            String backlogconf = System.getProperty("catalina.home") + File.separator + "conf/backlogtool.properties";
            InputStream input = new FileInputStream(backlogconf);
            propertiesFile.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String dialect = (String) propertiesFile.get("db.hibernate.dialect");

        Transaction tx = null;
        Session session = sessionFactory.openSession();
        try {
            tx = session.beginTransaction();

            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    String storyQuery = null;
                    String epicQuery = null;
                    String themeQuery = null;
                    if (dialect.contains("PostgreSQL")) {
                        storyQuery = "ALTER TABLE stories ALTER COLUMN description TYPE varchar(%d)";
                        epicQuery = "ALTER TABLE epics ALTER COLUMN description TYPE varchar(%d)";
                        themeQuery = "ALTER TABLE themes ALTER COLUMN description TYPE varchar(%d)";
                    } else if (dialect.contains("HSQL")) {
                        storyQuery = "ALTER TABLE stories ALTER COLUMN description varchar(%d)";
                        epicQuery = "ALTER TABLE epics ALTER COLUMN description varchar(%d)";
                        themeQuery = "ALTER TABLE themes ALTER COLUMN description varchar(%d)";
                    }
                    if (storyQuery == null) {
                        throw new SQLException("Unable to find a matching query for the specified database type");
                    } else {
                        storyQuery = String.format(storyQuery, Story.DESCRIPTION_LENGTH);
                        epicQuery = String.format(epicQuery, Epic.DESCRIPTION_LENGTH);
                        themeQuery = String.format(themeQuery, Theme.DESCRIPTION_LENGTH);
                        
                        connection.prepareStatement(storyQuery).executeUpdate();
                        connection.prepareStatement(epicQuery).executeUpdate();
                        connection.prepareStatement(themeQuery).executeUpdate();
                    }
                }
            });
            tx.commit();

        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            success = false;
        } finally {
            session.close();
        }
        return success;
    }

}
