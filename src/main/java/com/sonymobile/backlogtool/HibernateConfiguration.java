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
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.sonymobile.backlogtool.dbupdate.SchemaVersion;
import com.sonymobile.backlogtool.permission.User;

/**
 * This class sets Hibernate settings, mostly by loading from the configuration file.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Configuration
@EnableTransactionManagement
public class HibernateConfiguration {

    @Autowired
    ServletContext context;

    @Value("#{dataSource}")
    private DataSource dataSource;

    @Bean
    public AnnotationSessionFactoryBean sessionFactoryBean() {
        Properties propertiesFile = new Properties();
        try {
            String backlogconf = System.getProperty("catalina.home") + File.separator + "conf/backlogtool.properties";
            InputStream input = new FileInputStream(backlogconf);
            propertiesFile.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        props.put("hibernate.dialect", propertiesFile.get("db.hibernate.dialect"));
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.show_sql", "false");
        
        props.put("hibernate.cache.region.factory_class", "net.sf.ehcache.hibernate.EhCacheRegionFactory");
        props.put("hibernate.cache.use_second_level_cache","true");
        props.put("hibernate.cache.use_query_cache", "false");
        
        AnnotationSessionFactoryBean bean = new AnnotationSessionFactoryBean();
        bean.setAnnotatedClasses(new Class[]{Story.class, Task.class, User.class,
                Area.class, Theme.class, Epic.class, Attribute.class,
                AttributeOption.class, LoginTableCreator.class, SchemaVersion.class, Note.class});
        bean.setHibernateProperties(props);
        bean.setDataSource(this.dataSource);
        bean.setSchemaUpdate(true);

        return bean;
    }

    public static SessionFactory sessionFactory() {
        return sessionFactory();
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        return new HibernateTransactionManager( sessionFactoryBean().getObject() );
    }

}
