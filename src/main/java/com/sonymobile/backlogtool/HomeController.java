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
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.sonymobile.backlogtool.permission.User;

import static com.sonymobile.backlogtool.Util.isLoggedIn;
import static com.sonymobile.backlogtool.Util.getUserName;

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
    public ModelAndView home(Locale locale, Model model,
            HttpServletResponse response) {

        List<String> adminAreas = null;
        List<String> nonAdminAreas = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            String username = getUserName();
            User currentUser = null;
            if (username != null) {
                currentUser = (User) session.get(User.class, username);
            }

            Query allAreasQuery = session
                    .createQuery("from Area order by name");
            List<Area> allAreas = Util.castList(Area.class,
                    allAreasQuery.list());

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

        // Disables cache on this page so that the area list is refreshed every time.
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate");

        ModelAndView view = new ModelAndView("home");
        view.addObject("nonAdminAreas", nonAdminAreas);
        view.addObject("adminAreas", adminAreas);
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("view", "home");
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", getUserName());
        return view;
    }

    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value = "/areaedit/{areaName}", method = RequestMethod.GET)
    public ModelAndView areaedit(Locale locale, Model model,
            @PathVariable String areaName) throws JsonGenerationException,
            JsonMappingException, IOException {
        Area area = Util.getArea(areaName, sessionFactory);

        File dir = new File(context.getRealPath("/resources/image"));
        String[] icons = dir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png");
            }
        });

        // Maps: SeriesID -> comparevalue -> attributeID
        HashMap<Integer, HashMap<Integer, Integer>> seriesIds = new HashMap<Integer, HashMap<Integer, Integer>>();

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

        String seriesIdsString = new ObjectMapper()
                .writeValueAsString(seriesIds);

        ModelAndView view = new ModelAndView("areaedit");
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("area", area);
        view.addObject("seriesIds", seriesIdsString);
        view.addObject("icons", icons);
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("loggedInUser", getUserName());
        return view;
    }

    /**
     * Helper for areaedit. Groups attribute options in series.
     * 
     * @param options
     *            the options to group
     * @param seriesIds
     *            map where the series ids are mapped to another map where
     *            compare values are mapped to attribute ids.
     * @return Set of grouped attribute options
     */
    private static Set<AttributeOption> groupSeries(
            Set<AttributeOption> options,
            HashMap<Integer, HashMap<Integer, Integer>> seriesIds) {
        Set<AttributeOption> newOptions = new LinkedHashSet<AttributeOption>();
        String lastName = null;
        String lastIcon = null;
        boolean lastIconEnabled = false;
        int lastCompareValue = -1;
        int lastSeriesStart = -1;
        int lastSeriesEnd = -1;
        int lastSeriesId = -1;
        Integer lastSeriesIncrement = null;
        HashMap<Integer, Integer> lastIds = new HashMap<Integer, Integer>();
        for (AttributeOption option : options) {
            Integer seriesIncrement = option.getSeriesIncrement();
            if (seriesIncrement != null) { // If current is part of series.
                if (lastSeriesIncrement != null
                        && seriesIncrement != null
                        && Double.compare(lastSeriesIncrement, seriesIncrement) == 0
                        && lastName != null
                        && lastName.equals(option.getNameNoNumber())
                        && lastIcon != null
                        && lastIcon.equals(option.getIcon())
                        && option.getNumber() == lastSeriesEnd
                                + lastSeriesIncrement) { // If current series
                                                         // was same as last
                    lastSeriesEnd = option.getNumber();
                } else { // Not same as last
                    if (lastSeriesIncrement != null) { // If it's not the first
                                                       // series
                        AttributeOptionSeries series = new AttributeOptionSeries(
                                lastSeriesId, lastName, lastIcon,
                                lastIconEnabled, lastCompareValue,
                                lastSeriesStart, lastSeriesEnd,
                                lastSeriesIncrement);
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
            } else { // Current is not part of a series
                if (lastSeriesIncrement != null) {// If current is not part of
                                                  // series, but last was
                    seriesIds.put(lastSeriesId, lastIds);
                    AttributeOptionSeries series = new AttributeOptionSeries(
                            lastSeriesId, lastName, lastIcon, lastIconEnabled,
                            lastCompareValue, lastSeriesStart, lastSeriesEnd,
                            lastSeriesIncrement);
                    newOptions.add(series);
                }
                newOptions.add(option);
            }
            lastSeriesIncrement = seriesIncrement;
        }
        if (lastSeriesIncrement != null) { // Last was a series
            seriesIds.put(lastSeriesId, lastIds);
            AttributeOptionSeries series = new AttributeOptionSeries(
                    lastSeriesId, lastName, lastIcon, lastIconEnabled,
                    lastCompareValue, lastSeriesStart, lastSeriesEnd,
                    lastSeriesIncrement);
            newOptions.add(series);
        }
        return newOptions;
    }

    /**
     * Returns a printer-friendly page for stories
     * 
     * @param ids
     *            which stories to print
     * @return page
     */
    @RequestMapping(value = "/print-stories/{areaName}", method = RequestMethod.GET)
    public ModelAndView printStories(Locale locale, Model model,
            @RequestParam List<Integer> ids, @PathVariable String areaName) {
        List<Story> stories = null;
        Area area = null;
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            Hibernate.initialize(area.getStoryAttr1());
            Hibernate.initialize(area.getStoryAttr2());
            Hibernate.initialize(area.getStoryAttr3());

            Query storyQuery = session
                    .createQuery("select distinct s from Story s "
                            + "left join fetch s.children "
                            + "where s.area like ? and s.id in (:ids)");
            storyQuery.setParameter(0, area);
            storyQuery.setParameterList("ids", ids);

            stories = Util.castList(Story.class, storyQuery.list());

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
    public ModelAndView storytask(
            Locale locale,
            Model model,
            @PathVariable String areaName,
            @CookieValue(value = "backlogtool-orderby", defaultValue = "prio", required = false) String order,
            @RequestParam(required = false, value = "ids") Set<Integer> filterIds,
            @RequestParam(required = false, value="archived-view", defaultValue="false") boolean archivedView) {

        Area area = Util.getArea(areaName, sessionFactory);

        String username = getUserName();

        List<Story> nonArchivedStories = new ArrayList<Story>();
        Set<String> adminAreas = new HashSet<String>();
        ModelAndView view = new ModelAndView();

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("story-task");

            String jsonNonArchivedStories = "";
            String jsonAreaData = "";
            String jsonNotesData = "";

            // maps Story-IDs to Stories
            HashMap<Integer, Story> map = new HashMap<Integer, Story>();
            // maps Story-IDs to Notes
            HashMap<Integer, List<Note>> notesMap = new HashMap<Integer, List<Note>>();
            Session session = sessionFactory.openSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                //Get the areas that the user is admin for, which is used to
                //populate the move-to-area select box. Exclude the current area, since
                //it's no point of moving stories to the same area.
                //Allow only admins to move since missing story attributes are created
                //automatically.
                if (username != null) {
                    adminAreas = getAdminAreaNames(session, username);
                    adminAreas.remove(area.getName());
                }

                if (!archivedView) {
                    String queryString = null;
                    if (order.contains("storyAttr")) {
                        // If the user wants to sort by one of the custom created
                        // attributes,
                        // then the attributeOptions needs to be sorted by their
                        // compareValues.
                        queryString = "select distinct s from Story s "
                                + "left join fetch s.children "
                                + "left join fetch s.notes "
                                + "left join fetch s." + order + " as attr "
                                + "where s.area = ? " + "and s.archived = false "
                                + "order by attr.compareValue";
                    } else if (order.matches("title|description|contributor" +
                            "|customer|contributorSite|customerSite")) {
                        queryString = "select distinct s from Story s "
                                + "left join fetch s.children "
                                + "left join fetch s.notes "
                                + "where s.area = ? and s.archived = false "
                                + "order by s." + order;
                    } else { // Fall back to sorting by prio
                        queryString = "select distinct s from Story s "
                                + "left join fetch s.children "
                                + "left join fetch s.notes "
                                + "where s.area = ? and s.archived = false "
                                + "order by s.prio";
                    }
                    Query query = session.createQuery(queryString);
                    query.setParameter(0, area);
                    nonArchivedStories = Util.castList(Story.class, query.list());
                }
                ObjectMapper mapper = new ObjectMapper();

                for (Story s : nonArchivedStories) {
                    map.put(s.getId(), s);
                    notesMap.put(s.getId(), s.getTenNewestNotes());
                }
                try {
                    jsonNonArchivedStories = mapper.writeValueAsString(map);
                    jsonAreaData = mapper.writeValueAsString(area);
                    jsonNotesData = mapper.writeValueAsString(notesMap);
                } catch (Exception e) {
                    e.printStackTrace();
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

            Story placeholderStory = new Story();
            Task placeholderTask = new Task();
            Note placeholderNote = new Note();
            placeholderStory.setId(-1);
            placeholderTask.setId(-1);
            placeholderNote.setId(-1);
            //Prevents NPE from task.getStory().getId():
            placeholderTask.setStory(placeholderStory);
            placeholderNote.setStory(placeholderStory);

            view.addObject("placeholderStory", placeholderStory);
            view.addObject("placeholderTask", placeholderTask);
            view.addObject("placeholderNote", placeholderNote);
            view.addObject("area", area);
            view.addObject("adminAreas", adminAreas);
            view.addObject("disableEdits", isDisableEdits(areaName));
            view.addObject("view", "story-task");
            view.addObject("nonArchivedStories", nonArchivedStories);
            view.addObject("filterIds", filterIds);
            view.addObject("jsonDataNonArchivedStories", jsonNonArchivedStories);
            view.addObject("jsonAreaData", jsonAreaData);
            view.addObject("archivedView", archivedView);
            view.addObject("jsonNotesData", jsonNotesData);
        }
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("loggedInUser", getUserName());
        return view;
    }

    @RequestMapping(value = "/epic-story/{areaName}", method = RequestMethod.GET)
    public ModelAndView epicstory(
            Locale locale,
            Model model,
            @PathVariable String areaName,
            @CookieValue(value = "backlogtool-orderby", defaultValue = "prio", required = false) String order,
            @RequestParam(required = false, value = "ids") Set<Integer> filterIds,
            @RequestParam(required = false, value="archived-view", defaultValue="false") boolean archivedView) {

        Area area = Util.getArea(areaName, sessionFactory);
        List<Epic> nonArchivedEpics = new ArrayList<Epic>();

        ModelAndView view = new ModelAndView();

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("epic-story");

            String jsonNonArchivedEpics = "";
            String jsonAreaData = "";
            HashMap<Integer, Epic> map = new HashMap<Integer, Epic>();
            Session session = sessionFactory.openSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                if (!archivedView) {
                    String queryString = null;
                    if (order.matches("title|description")) {
                        queryString = "select distinct e from Epic e "
                                + "left join fetch e.children "
                                + "where e.area = ? and e.archived=false "
                                + "order by e." + order;
                    } else { // Fall back to sorting by prio
                        queryString = "select distinct e from Epic e "
                                + "left join fetch e.children "
                                + "where e.area = ? and e.archived = false "
                                + "order by e.prio";
                    }

                    Query query = session.createQuery(queryString);
                    query.setParameter(0, area);
                    nonArchivedEpics = Util.castList(Epic.class, query.list());
                }

                ObjectMapper mapper = new ObjectMapper();

                for (Epic e : nonArchivedEpics) {
                    map.put(e.getId(), e);
                }
                try {
                    jsonNonArchivedEpics = mapper.writeValueAsString(map);
                    jsonAreaData = mapper.writeValueAsString(area);
                } catch (Exception e) {
                    e.printStackTrace();
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

            Epic placeholderEpic = new Epic();
            Story placeholderStory = new Story();
            placeholderEpic.setId(-1);
            placeholderStory.setId(-1);
            //Prevents NPE from story.getEpic().getId():
            placeholderStory.setEpic(placeholderEpic);

            view.addObject("placeholderEpic", placeholderEpic);
            view.addObject("placeholderStory", placeholderStory);
            view.addObject("nonArchivedEpics", nonArchivedEpics);
            view.addObject("filterIds", filterIds);
            view.addObject("area", area);
            view.addObject("disableEdits", isDisableEdits(areaName));
            view.addObject("view", "epic-story");
            view.addObject("jsonDataNonArchivedEpics", jsonNonArchivedEpics);
            view.addObject("jsonAreaData", jsonAreaData);
            view.addObject("archivedView", archivedView);
        }
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("loggedInUser", getUserName());
        return view;
    }

    @RequestMapping(value = "/theme-epic/{areaName}", method = RequestMethod.GET)
    public ModelAndView themeepic(
            Locale locale,
            Model model,
            @PathVariable String areaName,
            @CookieValue(value = "backlogtool-orderby", defaultValue = "prio", required = false) String order,
            @RequestParam(required = false, value = "ids") Set<Integer> filterIds,
            @RequestParam(required = false, value="archived-view", defaultValue="false") boolean archivedView) {

        Area area = Util.getArea(areaName, sessionFactory);
        List<Theme> nonArchivedThemes = new ArrayList<Theme>();

        ModelAndView view = new ModelAndView();

        if (area == null) {
            view.setViewName("area-noexist");
        } else {
            view.setViewName("theme-epic");

            HashMap<Integer, Theme> map = new HashMap<Integer, Theme>();
            String jsonNonArchivedThemes = "";
            String jsonAreaData = "";
            Session session = sessionFactory.openSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                if (!archivedView) {
                    String queryString = null;
                    if (order.matches("title||description")) {
                        queryString = "select distinct t from Theme t "
                                + "left join fetch t.children "
                                + "where t.area = ? and t.archived = false "
                                + "order by t." + order;
                    } else { // Fall back to sorting by prio
                        queryString = "select distinct t from Theme t "
                                + "left join fetch t.children "
                                + "where t.area = ? and t.archived = false "
                                + "order by t.prio";
                    }
                    Query query = session.createQuery(queryString);
                    query.setParameter(0, area);

                    nonArchivedThemes = Util.castList(Theme.class, query.list());

                    for (Theme t : nonArchivedThemes) {
                        map.put(t.getId(), t);
                    }
                }
                ObjectMapper mapper = new ObjectMapper();

                try {
                    jsonNonArchivedThemes = mapper.writeValueAsString(map);
                    jsonAreaData = mapper.writeValueAsString(area);
                } catch (Exception e) {
                    e.printStackTrace();
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
            Theme placeholderTheme = new Theme();
            Epic placeholderEpic = new Epic();
            placeholderTheme.setId(-1);
            placeholderEpic.setId(-1);
            //Prevents NPE from epic.getTheme().getId():
            placeholderEpic.setTheme(placeholderTheme);

            view.addObject("placeholderTheme", placeholderTheme);
            view.addObject("placeholderEpic", placeholderEpic);
            view.addObject("nonArchivedThemes", nonArchivedThemes);
            view.addObject("filterIds", filterIds);
            view.addObject("area", area);
            view.addObject("disableEdits", isDisableEdits(areaName));
            view.addObject("view", "theme-epic");
            view.addObject("jsonDataNonArchivedThemes", jsonNonArchivedThemes);
            view.addObject("jsonAreaData", jsonAreaData);
            view.addObject("archivedView", archivedView);
        }
        view.addObject("version", version.getVersion());
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        view.addObject("isLoggedIn", isLoggedIn());
        view.addObject("loggedInUser", getUserName());
        return view;
    }

    @RequestMapping(value = "/comma-separated-data", method = RequestMethod.GET)
    public ModelAndView getCommaSepList(
            @RequestParam(required = false, value = "archived") Boolean archived,
            @RequestParam(required = false, value = "fields") List<String> fields) {
        String data = null;
        String error = null;
        if (fields != null && !fields.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Session session = sessionFactory.openSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();

                String queryString = null;
                if (archived == null) {
                    queryString = "from Story s";
                } else {
                    queryString = "from Story s " + "where s.archived = "
                            + archived;
                }

                Query query = session.createQuery(queryString);
                List<Story> stories = Util.castList(Story.class, query.list());
                HashSet<String> invalidFields = new HashSet<String>();

                SimpleDateFormat sdf = new SimpleDateFormat(
                        "M/d/yy HH:mm:ss.SS");
                for (Story s : stories) {
                    for (String field : fields) {
                        field = field.toLowerCase();
                        sb.append('"');
                        if (field.equals("id")) {
                            sb.append(s.getId());
                        } else if (field.equals("title")) {
                            sb.append(s.getTitle().replaceAll(
                                    "/\r\n+|\r+|\n+|\t+/i", ""));
                        } else if (field.equals("dateadded")) {
                            if (s.getAdded() != null) {
                                sb.append(sdf.format(s.getAdded()));
                            }
                        } else if (field.equals("contributor")) {
                            sb.append(s.getContributor());
                        } else if (field.equals("contributorsite")) {
                            sb.append(s.getContributorSite());
                        } else if (field.equals("datearchived")) {
                            if (s.getDateArchived() != null) {
                                sb.append(sdf.format(s.getDateArchived()));
                            }
                        } else if (field.equals("storyattr1")) {
                            AttributeOption attr = s.getStoryAttr1();
                            String attrStr = "";
                            if (attr != null) {
                                attrStr = attr.getName();
                            }
                            sb.append(attrStr);
                        } else if (field.equals("storyattr2")) {
                            AttributeOption attr = s.getStoryAttr2();
                            String attrStr = "";
                            if (attr != null) {
                                attrStr = attr.getName();
                            }
                            sb.append(attrStr);
                        } else if (field.equals("storyattr3")) {
                            AttributeOption attr = s.getStoryAttr3();
                            String attrStr = "";
                            if (attr != null) {
                                attrStr = attr.getName();
                            }
                            sb.append(attrStr);
                        } else if (field.equals("area")) {
                            sb.append(s.getArea().getName());
                        } else if (field.equals("deadline")) {
                            if (s.getDeadline() != null) {
                                sb.append(sdf.format(s.getDeadline()));
                            }
                        } else if (field.equals("customer")) {
                            sb.append(s.getCustomer());
                        } else if (field.equals("customersite")) {
                            sb.append(s.getCustomerSite());
                        } else if (field.equals("description")) {
                            sb.append(s.getDescription().replaceAll(
                                    "/\r\n+|\r+|\n+|\t+/i", ""));
                        } else {
                            invalidFields.add(field);
                        }
                        sb.append('"');
                        sb.append(',');
                    }
                    sb.deleteCharAt(sb.length() - 1); // remove last
                                                      // comma-character
                    sb.append('\n');
                }

                if (invalidFields.isEmpty()) {
                    data = sb.toString();
                } else {
                    StringBuilder errSB = new StringBuilder();
                    for (String s : invalidFields) {
                        errSB.append("'").append(s).append("', ");
                    }
                    errSB.append("are not valid field(s)");
                    error = errSB.toString();
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
        ModelAndView view = new ModelAndView();
        view.addObject("errorStr", error);
        view.addObject("dataString", data);
        view.addObject("versionNoDots", version.getVersion().replace(".", ""));
        return view;
    }

    /**
     * Returns a sorted set (by name) of area names that the
     * argument username is admin for.
     * 
     * @param session
     *            hibernate session
     * @param username
     * @return sorted set of area names
     */
    private Set<String> getAdminAreaNames(Session session, String username) {
        Set<String> adminAreas = new LinkedHashSet<String>();
        User currentUser = (User) session.get(User.class, username);

        Query allAreasQuery = session.createQuery("from Area order by name");
        List<Area> allAreas = Util.castList(Area.class, allAreasQuery.list());

        for (Area currentArea : allAreas) {
            if ((currentUser != null && currentUser.isMasterAdmin())
                    || currentArea.isAdmin(username)) {
                adminAreas.add(currentArea.getName());
            }
        }
        return adminAreas;
    }

    /**
     * Checks if the user is allowed to make edits to this specific area.
     * 
     * @param areaName
     *            Area name to check
     * @return disableEdits true if edits shall be disabled
     */
    private boolean isDisableEdits(String areaName) {
        if (!isLoggedIn()) {
            // Not logged in, edits must be disabled.
            return true;
        }
        String username = getUserName();
        boolean disableEdits = true;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            User currentUser = null;
            currentUser = (User) session.get(User.class, username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area != null
                    && (area.isAdmin(username) || area.isEditor(username))
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

}
