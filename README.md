The MIT License

Copyright 2013 Sony Mobile Communications AB. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

Introduction
============

Backlog tool is a planning tool that allows users to plan their daily work and easily rank their tasks using drag and drop. Backlog items can be inserted using a hierarchical structure consisting of themes, epics, stories and tasks. These items can be viewed in three different views for a good overview of the project, namely theme-epic, epic-story and story-task.

It's very easy to edit items in the backlog by double clicking on them, make some changes and press save. The changes you make will be pushed out in real time to all other clients viewing the same area as you.

Backlog tool is designed to be open and easy to access, which means that all users can view all created backlogs without being logged in. Each backlog is stored in a custom area, which can be created by logged in users on the main page. Once an area gets created, the owner can specify which users that should have edit rights and admin rights for the area.

In order to make backlog tool fit every team with their own requirements and way of working, the information fields for user stories are highly dynamic. By default, a field name "Status" will be created and have options like "ongoing" and "finished", however this can easily be changed and more options with custom icons can be added using the admin page for each area.

This tool supports login with existing LDAP user accounts, which means that users working in environments with LDAP logins can use the same account details as usual.

[Screenshots of the tool can be viewed here](https://github.com/sonyxperiadev/BacklogTool/wiki/Screenshots)

Online demo
===========

A demo instance of Backlog tool is available at http://backlogtool.net. Login with
* Username: admin
* Password: test

Note that the demo server is put on hibernate when not used, so it might take a minute to load the first page when it's resuming. 
The database is emptied regularly so use it only for testing.


Installation instructions
=========================

First, make sure you have the following installed:
* PostgreSQL database (http://www.postgresql.org/download/)
* Tomcat 7 (http://tomcat.apache.org/download-70.cgi). Might work with older versions, however it has not been tested
* LDAP server for keeping track of user accounts

For users
---------
1. Download the latest backlogtool.war file from https://github.com/sonyxperiadev/BacklogTool/releases and backlogtool.properties from https://github.com/sonyxperiadev/BacklogTool/raw/master/backlogtool.properties
2. Copy the .war-file to [Tomcat location]/webapps
3. Copy the backlog.properties-file to [Tomcat location]/conf and edit it with your own options. The database you set in the config file must exist; it will _not_ be created automatically. Also, the conf folder does not exist by default so you might have to create it.
4. (Optional) If you want to allow area names with special characters, set URIEncoding like this: http://struts.apache.org/2.0.6/docs/how-to-support-utf-8-uriencoding-with-tomcat.html
5. Make sure the database and LDAP servers are running and start Tomcat.
6. Backlogtool should now be available at http://localhost:8080/backlogtool. If you want to run backlogtool as the web root, rename 'backlogtool.war' to 'ROOT.war' and restart the server

For devs
--------
1. Download the source code using Git as usual
2. Install SpringSource Toolsuite (http://www.springsource.org/spring-tool-suite-download)
3. Launch SpringSource Toolsuite and choose File->Import. Expand "Maven" as import source and choose "Existing Maven Projects". Press Next.
4. Press the Browse button next to root directory and browse to the location of the source code. Press Ok and wait a few minutes until analysing is complete.
5. "pom.xml" should now be listed under Projects. Make sure it's selected and press Next, then Finish.
All dependencies should now be downloaded to your client.
6. Copy the backlogtool.properties file to your CATALINA_HOME location: [springsource location]/vfabric-tc-server-developer-2.X.X.RELEASE/tomcat-7.X.XX.X.RELEASE/conf and edit it with your own details. The database you set in the config file must exist; it will _not_ be created automatically. Also, the conf folder does not exist by default so you might have to create it.
7. (Optional) If you want to allow area names with special characters, set URIEncoding like this: http://struts.apache.org/2.0.6/docs/how-to-support-utf-8-uriencoding-with-tomcat.html. These settings are available in package explorer in Spring Tool Suite: Servers->VMWare vFabric tc... -> server.xml
8. Make some changes in the Springsource Toolsuite if you wish and right click on the project in package explorer and select Run as -> Run on server
9. Backlogtool should now be available at http://localhost:8080/backlogtool


Source code overview
====================

Backlog tool is built as a Spring MVC project on the server side. All requests for dynamic content are handled by Ajax technology with JSON data. The Atmosphere framework is used for triggering refresh on all clients that have the same area open when an object is modified. All objects are persisted using Hibernate framework which can easily be set to work with an in-memory database by changing the config file "backlogtool.properties" if needed.

The java class "JSONController" manages all Ajax requests (except rank updating) with one server side method bound to a URL per request type. HomeController handles all page requests in a similar way, and MoveController takes care of rank updating in lists.

Authentication is handled by Spring Security, which is set to work with a LDAP server. Many of the JSONController methods are annotated with “"hasPermission(isAdmin)" or similar which triggers a check on the user using the backlogtool.permission package before allowing access.

On the client side, most features have been developed with jQuery and plugins like jQuery UI, jQuery blockUI, jQuery truncator, jQuery dropdown and jQuery autosize. jQuery blockUI is for locking the UI when displaying an "updating" message when asking for server response. jQuery autosize is used for changing size on the text fields on the fly when editing backlog items.
