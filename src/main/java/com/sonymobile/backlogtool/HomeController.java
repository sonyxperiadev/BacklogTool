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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.sonymobile.backlogtool.permission.User;


/**
 * Handles requests for the application web pages.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 *
 */
@Controller
public class HomeController {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ServletContext context;
    
    @Autowired
    ApplicationVersion version;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home(Locale locale, Model model, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<String> adminAreas = null;
        List<String> nonAdminAreas = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            User currentUser = (User) session.get(User.class, username);

            Query allAreasQuery = session.createQuery("from Area order by name");
            List<Area> allAreas = Util.castList(Area.class, allAreasQuery.list());

            adminAreas = new ArrayList<String>();
            nonAdminAreas = new ArrayList<String>();
            for (Area area : allAreas) {
                if ((currentUser != null && currentUser.isMasterAdmin())
                        || area.isAdmin(username)) {
                    adminAreas.add(area.getName());
                } else {
                    nonAdminAreas.add(area.getName());
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

        //Disables cache on this page so that the area list is refreshed every time.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        ModelAndView view = new ModelAndView("home");
        view.addObject("nonAdminAreas", nonAdminAreas);
        view.addObject("adminAreas", adminAreas);
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("view", "home");
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        return view;
    }

    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value = "/areaedit/{areaName}", method = RequestMethod.GET)
    public ModelAndView areaedit(Locale locale, Model model, @PathVariable String areaName) {
        Area area = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            area = (Area) session.get(Area.class, areaName);
            Hibernate.initialize(area.getAdmins());
            Hibernate.initialize(area.getEditors());

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        File dir = new File(context.getRealPath("/resources/image"));
        String[] icons = dir.list();

        ModelAndView view = new ModelAndView("areaedit");
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("icons", icons);
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        return view;
    }

    /**
     * Returns a printer-friendly page for stories
     * @param ids which stories to print
     * @return page
     */
    @RequestMapping(value = "/print-stories/{areaName}", method = RequestMethod.GET)
    public ModelAndView printStories(Locale locale, Model model, @RequestParam int[] ids, @PathVariable String areaName) {
        List<Story> stories = new ArrayList<Story>();
        Area area = null;
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            for (int id : ids) {
                Story story = (Story) session.get(Story.class, id);
                if (story != null && story.getArea() == area) {
                    stories.add(story);
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
        ModelAndView view = new ModelAndView("print-stories");
        view.addObject("area", area);
        view.addObject("stories", stories);
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        return view;
    }

    @RequestMapping(value = "/story-task/{areaName}", method = RequestMethod.GET)
    public ModelAndView storytask(Locale locale, Model model, @PathVariable String areaName) {
        Area area = getArea(areaName);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<String> adminAreas = null;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            User currentUser = (User) session.get(User.class, username);

            Query allAreasQuery = session.createQuery("from Area order by name");
            List<Area> allAreas = Util.castList(Area.class, allAreasQuery.list());

            adminAreas = new ArrayList<String>();
            for (Area currentArea : allAreas) {
                if (!areaName.equals(currentArea.getName()) &&
                        ((currentUser != null && currentUser.isMasterAdmin()) 
                                || currentArea.isAdmin(username))) {
                    adminAreas.add(currentArea.getName());
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

        ModelAndView view = new ModelAndView();
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("adminAreas", adminAreas); 
        view.addObject("disableEdits", isDisableEdits(areaName));
        view.addObject("view", "story-task");
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("story-task");
        }
        return view;
    }

    @RequestMapping(value = "/epic-story/{areaName}", method = RequestMethod.GET)
    public ModelAndView epicstory(Locale locale, Model model, @PathVariable String areaName) {
        Area area = getArea(areaName);

        ModelAndView view = new ModelAndView();
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("disableEdits", isDisableEdits(areaName));
        view.addObject("view", "epic-story");
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("epic-story");
        }
        return view;
    }

    @RequestMapping(value = "/theme-epic/{areaName}", method = RequestMethod.GET)
    public ModelAndView themeepic(Locale locale, Model model, @PathVariable String areaName) {
        Area area = getArea(areaName);

        ModelAndView view = new ModelAndView();
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("disableEdits", isDisableEdits(areaName));
        view.addObject("view", "theme-epic");
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("theme-epic");
        }
        return view;
    }

    /**
     * Returns the area with argument name if it exists.
     * @param areaName Area name to search for
     * @return area
     */
    private Area getArea(String areaName) {
        Area area = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            area = (Area) session.get(Area.class, areaName);

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
     * Checks if the user is allowed to make edits to this specific area.
     * @param areaName Area name to check
     * @return disableEdits true if edits shall be disabled
     */
    private boolean isDisableEdits(String areaName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isLoggedIn()) {
            //Not logged in, edits must be disabled.
            return true;
        }
        String username = auth.getName();
        boolean disableEdits = true;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            User currentUser = (User) session.get(User.class, username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area != null && (area.isAdmin(username) || area.isEditor(username))
                    || (currentUser != null && currentUser.isMasterAdmin())) {
                disableEdits = false;
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
        return disableEdits;
    }

    private boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GrantedAuthority anonymous = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
        return !auth.getAuthorities().contains(anonymous);
    }

}
