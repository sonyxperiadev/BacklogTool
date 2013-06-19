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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
    public ModelAndView areaedit(Locale locale, Model model, @PathVariable String areaName)
            throws JsonGenerationException, JsonMappingException, IOException {
        Area area = Util.getArea(areaName, sessionFactory);

        File dir = new File(context.getRealPath("/resources/image"));
        String[] icons = dir.list();
        
        //Maps: SeriesID -> comparevalue -> attributeID
        HashMap<Integer,HashMap<Integer,Integer>> seriesIds = new HashMap<Integer,HashMap<Integer,Integer>>();

        ArrayList<Attribute> allAttributes = new ArrayList<Attribute>();
        allAttributes.add(area.getStoryAttr1());
        allAttributes.add(area.getStoryAttr2());
        allAttributes.add(area.getStoryAttr3());
        allAttributes.add(area.getTaskAttr1());
        
        for (Attribute currentAttr : allAttributes) {
            Set<AttributeOption> options = currentAttr.getOptions();
            Set<AttributeOption> newOptions = groupSeries(options, seriesIds);
            currentAttr.setOptions(newOptions);
        }

        String seriesIdsString = new ObjectMapper().writeValueAsString(seriesIds);

        ModelAndView view = new ModelAndView("areaedit");
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("seriesIds", seriesIdsString);
        view.addObject("icons", icons);
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", SecurityContextHolder.getContext().getAuthentication().getName());
        return view;
    }

    /**
     * Helper for areaedit. Groups attribute options in series.
     * @param options the options to group
     * @param seriesIds map where the series ids are mapped to another map where
     *                  compare values are mapped to attribute ids.
     * @return Set of grouped attribute options
     */
    private static Set<AttributeOption> groupSeries(Set<AttributeOption> options,
            HashMap<Integer,HashMap<Integer,Integer>> seriesIds) {
        Set<AttributeOption> newOptions = new LinkedHashSet<AttributeOption>();
        String lastName = null;
        String lastIcon = null;
        boolean lastIconEnabled = false;
        int lastCompareValue = -1;
        int lastSeriesStart = -1;
        int lastSeriesEnd = -1;
        int lastSeriesId = -1;
        Integer lastSeriesIncrement = null;
        HashMap<Integer,Integer> lastIds = new HashMap<Integer, Integer>();
        for (AttributeOption option : options) {
            Integer seriesIncrement = option.getSeriesIncrement();
            if (seriesIncrement != null) { //If current is part of series.
                if (lastSeriesIncrement!= null && seriesIncrement != null
                        && Double.compare(lastSeriesIncrement, seriesIncrement) == 0
                        && lastName != null && lastName.equals(option.getNameNoNumber())
                        && lastIcon != null && lastIcon.equals(option.getIcon())
                        && option.getNumber() == lastSeriesEnd+lastSeriesIncrement) { //If current series was same as last
                    lastSeriesEnd = option.getNumber();
                } else { //Not same as last
                    if (lastSeriesIncrement != null) { //If it's not the first series
                        AttributeOptionSeries series = new AttributeOptionSeries(lastSeriesId, lastName, lastIcon, lastIconEnabled,
                                lastCompareValue, lastSeriesStart, lastSeriesEnd, lastSeriesIncrement);
                        newOptions.add(series);
                    }
                    
                    lastSeriesStart = option.getNumber();
                    lastSeriesEnd = option.getNumber();
                    lastCompareValue = option.getCompareValue();
                    lastName = option.getNameNoNumber();
                    lastIcon = option.getIcon();
                    lastIconEnabled = option.isIconEnabled();
                    lastSeriesId = option.getId();
                    lastIds = new HashMap<Integer, Integer>();
                }
                lastIds.put(option.getNumber(), option.getId());
            } else { //Current is not part of a series                
                if (lastSeriesIncrement != null) {//If current is not part of series, but last was
                    seriesIds.put(lastSeriesId, lastIds);
                    AttributeOptionSeries series = new AttributeOptionSeries(lastSeriesId, lastName, lastIcon, lastIconEnabled,
                            lastCompareValue, lastSeriesStart, lastSeriesEnd, lastSeriesIncrement);
                    newOptions.add(series);
                } 
                newOptions.add(option);
            }
            lastSeriesIncrement = seriesIncrement;
        }
        if (lastSeriesIncrement != null) { //Last was a series
            seriesIds.put(lastSeriesId, lastIds);
            AttributeOptionSeries series = new AttributeOptionSeries(lastSeriesId, lastName, lastIcon, lastIconEnabled,
                    lastCompareValue, lastSeriesStart, lastSeriesEnd, lastSeriesIncrement);
            newOptions.add(series);
        }
        return newOptions;
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
        Area area = Util.getArea(areaName, sessionFactory);

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
        Area area = Util.getArea(areaName, sessionFactory);

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
        Area area = Util.getArea(areaName, sessionFactory);

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
