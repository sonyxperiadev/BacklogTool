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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator; 

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.atmosphere.cpr.AtmosphereResource;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sonymobile.backlogtool.permission.User;

import static com.sonymobile.backlogtool.Util.isLoggedIn;
import static com.sonymobile.backlogtool.Util.getUserName;


/**
 * Handles Ajax application requests with JSON data.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Controller
@RequestMapping(value="/json")
public class JSONController {

    public static final int ELEMENTS_PER_ARCHIVED_PAGE = 20;
    public static final int NOTES_PER_PART = 10;
    public static final String STORY_TASK_VIEW = "story-task";
    public static final String EPIC_STORY_VIEW = "epic-story";
    public static final String THEME_EPIC_VIEW = "theme-epic";
    public static final String PUSH_ACTION_DELETE = "Delete";
    public static final String ALL_VIEWS = "*";
    
    private static final int UPDATE_ITEM_ARCHIVED = 1;
    private static final int UPDATE_ITEM_UNARCHIVED = 2;

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ServletContext context;

    @RequestMapping(value="/readstory-task/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody List<Story> printJsonStories(@PathVariable String areaName,
            @RequestParam String order) {
        List<Story> list = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (order.contains("storyAttr")) {
                //If the user wants to sort by one of the custom created attributes, then the attributeOptions
                //needs to be sorted by their compareValues.
                String queryString1 = "select distinct s from Story s " +
                        "left join fetch s.children " +
                        "left join fetch s." + order + " as attr " +
                        "where s.area.name like ? " +
                        "order by attr.compareValue";
                Query query1 = session.createQuery(queryString1);
                query1.setParameter(0, areaName);

                list = Util.castList(Story.class, query1.list());
            } else if (order.equals("prio")) {
                String nonArchivedQueryString = "select distinct s from Story s " +
                        "left join fetch s.children " +
                        "where s.area.name like ? and " +
                        "s.archived=false " +
                        "order by s.prio";
                
                //Since the archived stories don't have any prio, we order them by their date archived.
                String archivedQueryString = "select distinct s from Story s " +
                        "left join fetch s.children " +
                        "where s.area.name like ? " +
                        "and s.archived=true " +
                        "order by s.dateArchived desc";

                Query nonArchivedQuery = session.createQuery(nonArchivedQueryString);
                Query archivedQuery = session.createQuery(archivedQueryString);

                archivedQuery.setParameter(0, areaName);
                nonArchivedQuery.setParameter(0, areaName);

                list = Util.castList(Story.class, nonArchivedQuery.list());
                list.addAll(Util.castList(Story.class, archivedQuery.list()));
            } else {
                String queryString = "select distinct s from Story s " +
                        "left join fetch s.children " +
                        "where s.area.name like ? " +
                        "order by s." + order;
                Query query = session.createQuery(queryString);
                query.setParameter(0, areaName);

                list = Util.castList(Story.class, query.list());
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
        return list;
    }

    @RequestMapping(value="/readepic-story/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody ResponseEntity<String> printJsonEpics(@PathVariable String areaName, @RequestParam String order)
            throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        List<Epic> epics = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (order.equals("prio")) {
                String nonArchivedQueryString = "select distinct e from Epic e " +
                        "left join fetch e.children " +
                        "where e.area.name like ? and " +
                        "e.archived=false " +
                        "order by e.prio";

                //Since the archived epics don't have any prio, we order them by their date archived.
                String archivedQueryString = "select distinct e from Epic e " +
                        "left join fetch e.children " +
                        "where e.area.name like ? " +
                        "and e.archived=true " +
                        "order by e.dateArchived desc";

                Query nonArchivedQuery = session.createQuery(nonArchivedQueryString);
                Query archivedQuery = session.createQuery(archivedQueryString);

                archivedQuery.setParameter(0, areaName);
                nonArchivedQuery.setParameter(0, areaName);

                epics = Util.castList(Epic.class, archivedQuery.list());
                epics.addAll(Util.castList(Epic.class, nonArchivedQuery.list()));
            } else {
                String queryString = "select distinct e from Epic e " +
                        "left join fetch e.children " +
                        "where e.area.name like ? " +
                        "order by e." + order;
                Query query = session.createQuery(queryString);
                query.setParameter(0, areaName);
                epics = Util.castList(Epic.class, query.list());
            }

            mapper.getSerializationConfig().addMixInAnnotations(Story.class, ChildrenExcluder.class);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return new ResponseEntity<String>(mapper.writeValueAsString(epics), responseHeaders, HttpStatus.CREATED);

    }

    @RequestMapping(value="/read-notes/{storyid}/{part}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody Map<String, Object> readNotes(@PathVariable Integer storyid, @PathVariable int part) throws JsonGenerationException, JsonMappingException, IOException {
        List<Note> list = new ArrayList<Note>();
        boolean moreNotesAvailable = true;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        long nbrOfItems = 0;
        try {
            tx = session.beginTransaction();
            
            String queryString1 = "from Note " +
                    "where story.id = ? " +
                    "order by created desc";
            Query query1 = session.createQuery(queryString1);
            query1.setParameter(0, storyid);
            query1.setMaxResults(10);
            query1.setFirstResult(NOTES_PER_PART * (part-1));
            query1.setMaxResults(NOTES_PER_PART);
            list = Util.castList(Note.class, query1.list());
            
            Query countQuery = session.createQuery("select count(id) from Note" + 
                                        " where story.id = ?");
            countQuery.setParameter(0, storyid);

            nbrOfItems = ((Long) countQuery.iterate().next()).longValue();
            
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        int totalNbrOfParts = (int) Math.ceil((double) nbrOfItems / JSONController.NOTES_PER_PART);
        if (part >= totalNbrOfParts) {
            moreNotesAvailable = false;
        }

        Map<String,Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("moreNotesAvailable", moreNotesAvailable);
        jsonMap.put("notesList", list);
        return jsonMap;
    }  

    @RequestMapping(value="/readtheme-epic/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody ResponseEntity<String> printJsonThemes(@PathVariable String areaName, @RequestParam String order)
            throws JsonGenerationException, JsonMappingException, IOException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        List<Theme> themes = null;
        ObjectMapper mapper = new ObjectMapper();

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (order.equals("prio")) {
                String nonArchivedQueryString = "select distinct t from Theme t " +
                        "left join fetch t.children " +
                        "where t.area.name like ? and " +
                        "t.archived=false " +
                        "order by t.prio";

                //Since the archived themes don't have any prio, we order them by their date archived.
                String archivedQueryString = "select distinct t from Theme t " +
                        "left join fetch t.children " +
                        "where t.area.name like ? " +
                        "and t.archived=true " +
                        "order by t.dateArchived desc";

                Query nonArchivedQuery = session.createQuery(nonArchivedQueryString);
                Query archivedQuery = session.createQuery(archivedQueryString);

                archivedQuery.setParameter(0, areaName);
                nonArchivedQuery.setParameter(0, areaName);

                themes = Util.castList(Theme.class, archivedQuery.list());
                themes.addAll(Util.castList(Theme.class, nonArchivedQuery.list()));
            } else {
                String queryString = "select distinct t from Theme t " +
                        "left join fetch t.children " +
                        "where t.area.name like ? " +
                        "order by t." + order;
                Query query = session.createQuery(queryString);
                query.setParameter(0, areaName);
                themes = Util.castList(Theme.class, query.list());
            }

            mapper.getSerializationConfig().addMixInAnnotations(Epic.class, ChildrenExcluder.class);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return new ResponseEntity<String>(mapper.writeValueAsString(themes), responseHeaders, HttpStatus.CREATED);
    }
    
    @RequestMapping(value="/read-archived/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody String readArchived(@PathVariable String areaName,
           @RequestParam(required = false, value = "ids") Set<Integer> filterIds,
           @RequestParam String type, @RequestParam int page) throws JsonGenerationException, JsonMappingException, IOException {

        List<Object> archivedItems = new ArrayList<Object>();
        Map<Integer, List<Note>> notesForStories = new HashMap<Integer, List<Note>>();
        int nbrOfPages = 0;
        Area area = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            area = (Area) session.get(Area.class, areaName);
            if (area != null && type.matches("Story|Epic|Theme")) {
                Query archivedQuery = null;
                if (filterIds == null || filterIds.isEmpty()) {
                    archivedQuery = session.createQuery("from " + type + " " +
                            "where area = ? " +
                            "and archived=true " +
                            "order by dateArchived desc");
                } else {
                    archivedQuery = session.createQuery("from " + type + " " +
                            "where area = ? " +
                            "and archived=true " +
                            "and id in (:filterIds) " +
                            "order by dateArchived desc");
                    archivedQuery.setParameterList("filterIds", filterIds);
                }

                archivedQuery.setParameter(0, area);
                archivedQuery.setFirstResult(ELEMENTS_PER_ARCHIVED_PAGE * (page-1));
                archivedQuery.setMaxResults(ELEMENTS_PER_ARCHIVED_PAGE);

                Query countQuery = null;
                if (filterIds == null || filterIds.isEmpty()) {
                    countQuery = session.createQuery("select count(*) from " + type 
                            + " where archived = true and area = ?");
                } else {
                    countQuery = session.createQuery("select count(*) from " + type  +
                            " where archived = true and area = ? "+
                            " and id in (:filterIds)");
                    countQuery.setParameterList("filterIds", filterIds);
                }
                countQuery.setParameter(0, area);

                long nbrOfItems = ((Long) countQuery.iterate().next()).longValue();
                nbrOfPages = (int) Math.ceil((double) nbrOfItems / JSONController.ELEMENTS_PER_ARCHIVED_PAGE);

                archivedItems = Util.castList(Object.class, archivedQuery.list());

                for (Object item : archivedItems) {
                    if (type.equals("Story")) {
                        Story s = (Story) item;
                        Hibernate.initialize(s.getChildren());
                        Hibernate.initialize(s.getNotes());
                        notesForStories.put(s.getId(), s.getTenNewestNotes());
                    } else if (type.equals("Epic")) {
                        Hibernate.initialize(((Epic) item).getChildren());
                    } else if (type.equals("Theme")) {
                        Hibernate.initialize(((Theme) item).getChildren());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        ObjectMapper mapper = new ObjectMapper();
        if (type.equals("Epic")) {
            mapper.getSerializationConfig().addMixInAnnotations(Story.class, ChildrenExcluder.class);
        } else if (type.equals("Theme")) {
            mapper.getSerializationConfig().addMixInAnnotations(Epic.class, ChildrenExcluder.class);
        }
        Map<String,Object> archivedInfo = new HashMap<String, Object>();
        archivedInfo.put("nbrOfPages", nbrOfPages);
        archivedInfo.put("archivedItems", archivedItems);
        archivedInfo.put("notesMap", notesForStories);
        return mapper.writeValueAsString(archivedInfo);
    }

    @RequestMapping(value="/autocompletethemes/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody List<String> autocompleteThemes(@PathVariable String areaName, @RequestParam String term) {
        List<String> titles = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Query query = session.createQuery("select title from Theme where area.name like ? and lower(title) like ?");
            query.setParameter(0, areaName);
            query.setParameter(1, "%" + term.toLowerCase() + "%");
            //% for contains
            titles = Util.castList(String.class, query.list());
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return titles;
    }

    @RequestMapping(value="/autocompleteepics/{areaName}", method=RequestMethod.GET)
    @Transactional
    public @ResponseBody List<String> autocompleteEpics(@PathVariable String areaName,
            @RequestParam String term, @RequestParam String theme) {
        List<String> titles = null;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query query = null;
            if (theme.isEmpty()) {
                query = session.createQuery("select title from Epic where area.name like ? and lower(title) like ? " +
                        "and theme is null");
            } else {
                query = session.createQuery("select title from Epic where area.name like ? and lower(title) like ? " +
                        "and lower(theme.title) like ?");
                query.setParameter(2, theme.toLowerCase());
            }
            query.setParameter(0, areaName);
            query.setParameter(1, "%" + term.toLowerCase() + "%");
            //% for contains
            titles = Util.castList(String.class, query.list());
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return titles;
    }

    @RequestMapping(value="/createnote/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Note createNote(@PathVariable String areaName, @RequestBody NewNoteContainer newNote) throws Exception {
        if (!isLoggedIn()) {
            throw new Error("Trying to create note without being authenticated");
        }
        String username = getUserName();

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Story story = (Story) session.get(Story.class, newNote.getStoryId());
            if (!story.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            newNote.setStory(story);
            newNote.setUser(username);
            Date d = new Date();
            newNote.setCreatedDate(d);
            newNote.setModifiedDate(d);
            session.save("com.sonymobile.backlogtool.Note", newNote);

            story.getNotes().add(newNote);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Note.class, newNote, STORY_TASK_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return newNote;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createtask/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Task createTask(@PathVariable String areaName, @RequestBody NewTaskContainer newTask) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Story story = (Story) session.get(Story.class, newTask.getParentId());
            if (!story.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            ListItem lastItem = newTask.getLastItem();
            newTask.setStory(story);

            session.save("com.sonymobile.backlogtool.Task", newTask);

            int newPrioInStory = story.getChildren().size() + 1;
            if (lastItem != null && lastItem.getType().equals("child")) {
                for (Task task : story.getChildren()) {
                    //Find what prioInStory lastItem has if it belongs to this story
                    if (task.getId() == lastItem.getId()) {
                        newPrioInStory = task.getPrioInStory() + 1;

                        //Move down all tasks within the story below the new task
                        for (Task currentTask : story.getChildren()) {
                            int prioInStory = currentTask.getPrioInStory();
                            if (prioInStory >= newPrioInStory) {
                                currentTask.setPrioInStory(prioInStory + 1);
                            }
                        }
                        break;
                    }
                }
            }
            newTask.setPrioInStory(newPrioInStory);

            newTask.setTitle("New task " + newTask.getId());
            story.addTask(newTask);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Task.class, newTask, STORY_TASK_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return newTask;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createstory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Story createStory(@PathVariable String areaName, @RequestBody NewStoryContainer newStory) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            boolean createIfDoesNotExist = true;
            Theme theme = getTheme(newStory.getThemeTitle(), area, session, createIfDoesNotExist);
            Epic epic = getEpic(newStory.getEpicTitle(), theme, area, session, createIfDoesNotExist);
            ListItem lastItem = newStory.getLastItem();

            if (epic != null) {
                int newPrioInEpic = epic.getChildren().size() + 1;
                if (lastItem != null && lastItem.getType().equals("child")) {
                    for (Story story : epic.getChildren()) {
                        //Find what prioInEpic lastItem has if it belongs to this epic
                        if (story.getId() == lastItem.getId()) {
                            newPrioInEpic = story.getPrioInEpic() + 1;

                            //Move down all stories within the epic below the new story
                            for (Story currentStory : epic.getChildren()) {
                                int prioInEpic = currentStory.getPrioInEpic();
                                if (prioInEpic >= newPrioInEpic) {
                                    currentStory.setPrioInEpic(prioInEpic + 1);
                                }
                            }
                            break;
                        }
                    }
                }
                newStory.setPrioInEpic(newPrioInEpic);
            }

            //Move other stories
            Query storyQuery = session.createQuery("from Story where area like ? and archived=false order by prio desc");
            storyQuery.setParameter(0, area);
            List<Story> storyList = Util.castList(Story.class, storyQuery.list());

            if (storyList.isEmpty()) {
                newStory.setPrio(1);
            } else {
                int newPrio = storyList.get(0).getPrio() + 1;
                if (lastItem != null && lastItem.getType().equals("parent")) {
                    for (Story story : storyList) {
                        //Find what prio lastItem has
                        if (story.getId() == lastItem.getId()) {
                            newPrio = story.getPrio() + 1;

                            //Move down all stories below the new story
                            for (Story currentStory : storyList) {
                                int prio = currentStory.getPrio();
                                if (prio >= newPrio) {
                                    currentStory.addPrio(1);
                                }
                            }
                            break;
                        }
                    }
                }
                newStory.setPrio(newPrio);
            }
            newStory.setArea(area);
            newStory.setEpic(epic);
            newStory.setTheme(theme);

            session.save("com.sonymobile.backlogtool.Story", newStory);
            newStory.setTitle("New story " + newStory.getId());

            String pushViews = STORY_TASK_VIEW;
            if (epic != null) {
                epic.getChildren().add(newStory);
                pushViews += "|" + EPIC_STORY_VIEW;
            }

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Story.class, newStory, pushViews));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return newStory;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createepic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Epic createEpic(@PathVariable String areaName, @RequestBody NewEpicContainer newEpic) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            boolean createThemeIfDoesNotExist = true;
            Theme theme = getTheme(newEpic.getThemeTitle(), area, session, createThemeIfDoesNotExist);
            ListItem lastItem = newEpic.getLastItem();

            if (theme != null) {
                int newPrioInTheme = theme.getChildren().size() + 1;
                if (lastItem != null && lastItem.getType().equals("child")) {
                    for (Epic epic : theme.getChildren()) {
                        //Find what prioInTheme lastItem has if it belongs to this theme
                        if (epic.getId() == lastItem.getId()) {
                            newPrioInTheme = epic.getPrioInTheme() + 1;

                            //Move down all epics within the theme below the new epic
                            for (Epic currentEpic : theme.getChildren()) {
                                int prioInTheme = currentEpic.getPrioInTheme();
                                if (prioInTheme >= newPrioInTheme) {
                                    currentEpic.setPrioInTheme(prioInTheme + 1);
                                }
                            }
                            break;
                        }
                    }
                }
                newEpic.setPrioInTheme(newPrioInTheme);
            }

            //Move other epics
            Query epicQuery = session.createQuery("from Epic where area like ? and archived=false order by prio desc");
            epicQuery.setParameter(0, area);
            List<Epic> epicList = Util.castList(Epic.class, epicQuery.list());

            if (epicList.isEmpty()) {
                newEpic.setPrio(1);
            } else {
                int newPrio = epicList.get(0).getPrio() + 1;
                if (lastItem != null && lastItem.getType().equals("parent")) {
                    for (Epic epic : epicList) {
                        //Find what prio lastItem has
                        if (epic.getId() == lastItem.getId()) {
                            newPrio = epic.getPrio() + 1;

                            //Move down all epics below the new epic
                            for (Epic currentEpic : epicList) {
                                int prio = currentEpic.getPrio();
                                if (prio >= newPrio) {
                                    currentEpic.setPrio(prio + 1);
                                }
                            }
                            break;
                        }
                    }
                }
                newEpic.setPrio(newPrio);
            }

            newEpic.setArea(area);
            newEpic.setTheme(theme);
            session.save("com.sonymobile.backlogtool.Epic", newEpic);
            newEpic.setTitle("New epic " + newEpic.getId());

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Epic.class, newEpic, EPIC_STORY_VIEW + "|" + THEME_EPIC_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        
        return newEpic;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createtheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Theme createTheme(@PathVariable String areaName, @RequestBody NewThemeContainer newTheme) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            ListItem lastItem = newTheme.getLastItem();

            //Move other themes
            Query allThemesQuery = session.createQuery("from Theme where area like ? and archived=false order by prio desc");
            allThemesQuery.setParameter(0, area);
            List<Theme> themeList = Util.castList(Theme.class, allThemesQuery.list());

            if (themeList.isEmpty()) {
                newTheme.setPrio(1);
            } else {
                int newPrio = themeList.get(0).getPrio() + 1;
                if (lastItem != null && lastItem.getType().equals("parent")) {
                    for (Theme theme : themeList) {
                        //Find what prio lastItem has
                        if (theme.getId() == lastItem.getId()) {
                            newPrio = theme.getPrio() + 1;

                            //Move down all themes below the new theme
                            for (Theme currentTheme : themeList) {
                                int prio = currentTheme.getPrio();
                                if (prio >= newPrio) {
                                    currentTheme.setPrio(prio + 1);
                                }
                            }
                            break;
                        }
                    }
                }
                newTheme.setPrio(newPrio);
            }

            newTheme.setArea(area);
            session.save("com.sonymobile.backlogtool.Theme", newTheme);
            newTheme.setTitle("New theme " + newTheme.getId());

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Theme.class, newTheme, THEME_EPIC_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        
        return newTheme;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatetask/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Task updateTask(@PathVariable String areaName,
            @RequestBody NewTaskContainer updatedTask) throws JsonGenerationException, JsonMappingException, IOException {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Task task = null;
        try {
            tx = session.beginTransaction();

            task = (Task) session.get(Task.class, updatedTask.getId());
            if (!task.getStory().getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            AttributeOption attr1 = null;
            try {
                attr1 = (AttributeOption) session.get(AttributeOption.class, Integer.parseInt(updatedTask.getTaskAttr1Id()));
            } catch(NumberFormatException e) {
            } //AttrId can be empty, in that case we want null as attr1.

            task.setTitle(updatedTask.getTitle());
            task.setOwner(updatedTask.getOwner());
            task.setCalculatedTime(updatedTask.getCalculatedTime());
            task.setTaskAttr1(attr1);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringExclChildren(Task.class, task, STORY_TASK_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return task;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatestory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Story updateStory(@PathVariable String areaName,
           @RequestBody NewStoryContainer updatedStory) throws JsonGenerationException, JsonMappingException, IOException {
        String username = getUserName();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Story story = null;

        try {
            tx = session.beginTransaction();

            story = (Story) session.get(Story.class, updatedStory.getId());
            Hibernate.initialize(story.getChildren());
            if (!story.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            boolean createIfDoesNotExist = true;
            Theme theme = getTheme(updatedStory.getThemeTitle(), story.getArea(), session, createIfDoesNotExist);
            Epic newEpic = getEpic(updatedStory.getEpicTitle(), theme, story.getArea(), session, createIfDoesNotExist);

            Set<Epic> parentsToPush = new HashSet<Epic>();
            //Move story from old epic if it was changed
            if (updatedStory.getEpicTitle() != null) {
                Epic oldEpic = story.getEpic();
                if (oldEpic != newEpic) {
                    if (oldEpic != null) {
                        oldEpic.getChildren().remove(story);
                        oldEpic.rebuildChildrenOrder();
                        parentsToPush.add(oldEpic);
                    }
                    if (newEpic != null) {
                        newEpic.getChildren().add(story);
                        story.setPrioInEpic(Integer.MAX_VALUE); //The prio gets rebuilt on newEpic.rebuildChildrenOrder().
                        newEpic.rebuildChildrenOrder();
                        parentsToPush.add(newEpic);
                    }
                }
            }
            int archivedStatus = 0;
            if (updatedStory.isArchived() && !story.isArchived()) {
                archivedStatus = UPDATE_ITEM_ARCHIVED;
                //Was moved to archive
                story.setDateArchived(new Date());

                //Move up all stories under this one in rank
                Query query = session.createQuery("from Story where prio > ? and area.name like ? and archived=false");
                query.setParameter(0, story.getPrio());
                query.setParameter(1, areaName);
                List<Story> storyList = Util.castList(Story.class, query.list());

                for (Story otherStory : storyList) {
                    otherStory.setPrio(otherStory.getPrio() - 1);
                }
                story.setPrio(-1);
            } else if (!updatedStory.isArchived() && story.isArchived()) {
                archivedStatus = UPDATE_ITEM_UNARCHIVED;
                //Was moved from archive
                story.setDateArchived(null);

                //Find the last story and place this one after
                Query storyQuery = session.createQuery("from Story where area.name like ? and archived=false order by prio desc");
                storyQuery.setParameter(0, areaName);
                List<Story> storyList = Util.castList(Story.class, storyQuery.list());

                if (storyList.isEmpty()) {
                    story.setPrio(1);
                } else {
                    int lastPrio = storyList.get(0).getPrio();
                    story.setPrio(lastPrio + 1);
                }
            }

            AttributeOption attr1 = null;
            try {
                attr1 = (AttributeOption) session.get(AttributeOption.class, Integer.parseInt(updatedStory.getStoryAttr1Id()));
            } catch(NumberFormatException e) {
            } //AttrId can be empty, in that case we want null as attr1.

            AttributeOption attr2 = null;
            try {
                attr2 = (AttributeOption) session.get(AttributeOption.class, Integer.parseInt(updatedStory.getStoryAttr2Id()));
            } catch(NumberFormatException e) {}

            AttributeOption attr3 = null;
            try {
                attr3 = (AttributeOption) session.get(AttributeOption.class, Integer.parseInt(updatedStory.getStoryAttr3Id()));
            } catch(NumberFormatException e) {}

            List<String> messages = new ArrayList<String>();
            String message = "User %s set %s to %s";
            String updatedAttr = getUpdatedAttrValue(story.getStoryAttr1(), attr1);
            if (updatedAttr != null) {
                String storyAttr1Name = story.getArea().getStoryAttr1().getName();
                Note n = Note.createSystemNote(String.format(message, username, storyAttr1Name, updatedAttr), story, session);
                messages.add(getJsonStringInclChildren(Note.class.getSimpleName(), n, STORY_TASK_VIEW));
            }

            updatedAttr = getUpdatedAttrValue(story.getStoryAttr2(), attr2);
            if (updatedAttr != null) {
                String storyAttr1Name = story.getArea().getStoryAttr2().getName();
                Note n = Note.createSystemNote(String.format(message, username, storyAttr1Name, updatedAttr), story, session);
                messages.add(getJsonStringInclChildren(Note.class.getSimpleName(), n, STORY_TASK_VIEW));
            }

            updatedAttr = getUpdatedAttrValue(story.getStoryAttr3(), attr3);
            if (updatedAttr != null) {
                String storyAttr1Name = story.getArea().getStoryAttr3().getName();
                Note n = Note.createSystemNote(String.format(message, username, storyAttr1Name, updatedAttr), story, session);
                messages.add(getJsonStringInclChildren(Note.class.getSimpleName(), n, STORY_TASK_VIEW));
            }

            story.setStoryAttr1(attr1);
            story.setStoryAttr2(attr2);
            story.setStoryAttr3(attr3);

            story.setDescription(updatedStory.getDescription());
            story.setTitle(updatedStory.getTitle());
            story.setAdded(updatedStory.getAdded());
            story.setDeadline(updatedStory.getDeadline());
            story.setContributorSite(updatedStory.getContributorSite());
            story.setCustomerSite(updatedStory.getCustomerSite());
            story.setContributor(updatedStory.getContributor());
            story.setCustomer(updatedStory.getCustomer());
            story.setArchived(updatedStory.isArchived());

            story.setTheme(theme);
            story.setEpic(newEpic);
            if (theme != null && newEpic != null) {
                theme.getChildren().add(newEpic);
                newEpic.setTheme(theme);
            } 

            if (theme != null) {
                messages.add(getJsonStringInclChildren(Theme.class.getSimpleName(), theme, THEME_EPIC_VIEW));
            }
            if (!parentsToPush.isEmpty()) {
                HashMap<String, Object> moveActionMap = new HashMap<String, Object>();
                moveActionMap.put("lastItem", null);
                moveActionMap.put("objects", parentsToPush);
                messages.add(JSONController.getJsonStringInclChildren("childMove", moveActionMap, EPIC_STORY_VIEW));
            }

            StringBuilder updatedStoryViews = new StringBuilder();
            if (newEpic != null) {
                updatedStoryViews.append(EPIC_STORY_VIEW).append("|");
            }
            if (archivedStatus == UPDATE_ITEM_ARCHIVED) {
                Note.createSystemNote(String.format("User %s archived the story", username), story, session);

                messages.add(getJsonStringInclChildren(PUSH_ACTION_DELETE, story.getId(), STORY_TASK_VIEW));
            } else if (archivedStatus == UPDATE_ITEM_UNARCHIVED) {
                Note.createSystemNote(String.format("User %s unarchived the story", username), story, session);

                messages.add(getJsonStringInclChildren(Story.class.getSimpleName(), story, STORY_TASK_VIEW));
            } else {
                updatedStoryViews.append(STORY_TASK_VIEW);
            }

            messages.add(getJsonStringExclChildren(Story.class, story, updatedStoryViews.toString()));

            tx.commit();
            AtmosphereHandler.pushJsonMessages(areaName, messages);
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return story;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updateepic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody String updateEpic(@PathVariable String areaName,
            @RequestBody NewEpicContainer updatedEpic) throws Exception {

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Epic epic = null;
        try {
            tx = session.beginTransaction();

            epic = (Epic) session.get(Epic.class, updatedEpic.getId());
            Hibernate.initialize(epic.getChildren());
            if (!epic.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            boolean createThemeIfDoesNotExist = true;
            Theme theme = getTheme(updatedEpic.getThemeTitle(), epic.getArea(), session, createThemeIfDoesNotExist);

            boolean createEpicIfDoesNotExist = false;
            Epic sameNameEpic = getEpic(updatedEpic.getTitle(), theme, area, session, createEpicIfDoesNotExist);

            Set<Theme> affectedThemes = new HashSet<Theme>();
            //Only make changes if the title does not already exist on another object
            if (sameNameEpic == null || sameNameEpic.getId() == epic.getId()) {
                Theme oldTheme = epic.getTheme();
                if (oldTheme != theme) {
                    if (oldTheme != null) {
                        oldTheme.getChildren().remove(epic);
                        oldTheme.rebuildChildrenOrder();
                        affectedThemes.add(epic.getTheme());
                    }
                    if (theme != null) {
                        theme.getChildren().add(epic);
                        epic.setPrioInTheme(Integer.MAX_VALUE);
                        theme.rebuildChildrenOrder();
                        affectedThemes.add(theme);

                        epic.setTheme(theme);
                        for (Story story : epic.getChildren()) {
                            story.setTheme(theme);
                        }
                        epic.setTheme(theme);
                    }
                }
                int archivedStatus = 0;
                if (updatedEpic.isArchived() && !epic.isArchived()) {
                    archivedStatus = UPDATE_ITEM_ARCHIVED;
                    //Was moved to archive
                    epic.setDateArchived(new Date());

                    //Move up all epics under this one in rank
                    Query query = session.createQuery("from Epic where prio > ? and area like ? and archived=false");
                    query.setParameter(0, epic.getPrio());
                    query.setParameter(1, area);
                    List<Epic> epicList = Util.castList(Epic.class, query.list());

                    for (Epic otherEpic : epicList) {
                        otherEpic.setPrio(otherEpic.getPrio() - 1);
                    }
                    epic.setPrio(-1);
                } else if (!updatedEpic.isArchived() && epic.isArchived()) {
                    archivedStatus = UPDATE_ITEM_UNARCHIVED;
                    //Was moved from archive
                    epic.setDateArchived(null);

                    //Find the last epic and place this one after
                    Query epicQuery = session.createQuery("from Epic where area like ? and archived=false order by prio desc");
                    epicQuery.setParameter(0, area);
                    List<Epic> epicList = Util.castList(Epic.class, epicQuery.list());

                    if (epicList.isEmpty()) {
                        epic.setPrio(1);
                    } else {
                        int lastPrio = epicList.get(0).getPrio();
                        epic.setPrio(lastPrio + 1);
                    }
                }
                epic.setTitle(updatedEpic.getTitle());
                epic.setDescription(updatedEpic.getDescription());
                epic.setArchived(updatedEpic.isArchived());

                List<String> messages = new ArrayList<String>();
                for (Story s : epic.getChildren()) {
                    messages.add(getJsonStringExclChildren(Story.class, s, STORY_TASK_VIEW));
                }
                String updatedEpicViews = THEME_EPIC_VIEW;
                if (archivedStatus == UPDATE_ITEM_ARCHIVED) {
                    messages.add(getJsonStringInclChildren(PUSH_ACTION_DELETE, epic.getId(), EPIC_STORY_VIEW));
                } else if (archivedStatus == UPDATE_ITEM_UNARCHIVED) {
                    messages.add(getJsonStringInclChildren(Epic.class.getSimpleName(), epic, EPIC_STORY_VIEW));
                } else {
                    updatedEpicViews += "|" + EPIC_STORY_VIEW;
                }
                messages.add(getJsonStringExclChildren(Epic.class, epic, updatedEpicViews));

                if (!affectedThemes.isEmpty()) {
                    HashMap<String, Object> moveActionMap = new HashMap<String, Object>();
                    moveActionMap.put("lastItem", null);
                    moveActionMap.put("objects", affectedThemes);
                    messages.add(JSONController.getJsonStringInclChildren("childMove", moveActionMap, THEME_EPIC_VIEW));
                }

                tx.commit();
                AtmosphereHandler.pushJsonMessages(areaName, messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations(Story.class, ChildrenExcluder.class);
        return mapper.writeValueAsString(epic);
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatetheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody String updateTheme(@PathVariable String areaName,
            @RequestBody Theme updatedTheme) throws Exception {

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Theme theme = null;
        try {
            tx = session.beginTransaction();

            theme = (Theme) session.get(Theme.class, updatedTheme.getId());
            Hibernate.initialize(theme.getChildren());
            if (!theme.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            Theme sameNameTheme = getTheme(updatedTheme.getTitle(), area, session, false);
            if (sameNameTheme == null || sameNameTheme.getId() == theme.getId()) {
                int archivedStatus = 0;
                if (updatedTheme.isArchived() && !theme.isArchived()) {
                    archivedStatus = UPDATE_ITEM_ARCHIVED;
                    //Was moved to archive
                    theme.setDateArchived(new Date());

                    //Move up all themes under this one in rank
                    Query query = session.createQuery("from Theme where prio > ? and area like ? and archived=false");
                    query.setParameter(0, theme.getPrio());
                    query.setParameter(1, area);
                    List<Theme> themeList = Util.castList(Theme.class, query.list());

                    for (Theme otherTheme : themeList) {
                        otherTheme.setPrio(otherTheme.getPrio() - 1);
                    }
                    theme.setPrio(-1);
                } else if (!updatedTheme.isArchived() && theme.isArchived()) {
                    archivedStatus = UPDATE_ITEM_UNARCHIVED;
                    //Was moved from archive
                    theme.setDateArchived(null);

                    //Find the last theme and place this one after
                    Query themeQuery = session.createQuery("from Theme where area like ? and archived=false order by prio desc");
                    themeQuery.setParameter(0, area);
                    List<Theme> themeList = Util.castList(Theme.class, themeQuery.list());

                    if (themeList.isEmpty()) {
                        theme.setPrio(1);
                    } else {
                        int lastPrio = themeList.get(0).getPrio();
                        theme.setPrio(lastPrio + 1);
                    }
                }
                theme.setTitle(updatedTheme.getTitle());
                theme.setDescription(updatedTheme.getDescription());
                theme.setArchived(updatedTheme.isArchived());

                List<String> messages = new ArrayList<String>();
                for (Epic e : theme.getChildren()) {
                    messages.add(getJsonStringExclChildren(Epic.class, e, EPIC_STORY_VIEW));

                    for (Story s : e.getChildren()) {
                        messages.add(getJsonStringExclChildren(Story.class, s, STORY_TASK_VIEW));
                    }
                }

                if (archivedStatus == UPDATE_ITEM_ARCHIVED) {
                    messages.add(getJsonStringInclChildren(PUSH_ACTION_DELETE, theme.getId(), THEME_EPIC_VIEW));
                } else if (archivedStatus == UPDATE_ITEM_UNARCHIVED) {
                    messages.add(getJsonStringInclChildren(Theme.class.getSimpleName(), theme, THEME_EPIC_VIEW));
                } else {
                    messages.add(getJsonStringExclChildren(Theme.class, theme, THEME_EPIC_VIEW));
                }
                tx.commit();

                AtmosphereHandler.pushJsonMessages(areaName, messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations(Epic.class, ChildrenExcluder.class);
        return mapper.writeValueAsString(theme);
    }

    /**
     * Retrieves a Theme from DB that matches the params.
     * @param themeTitle
     * @param area
     * @param session
     * @param autoCreate if the theme should be created if it does not exist
     * @return
     */
    private Theme getTheme(String themeTitle, Area area, Session session, boolean autoCreate) {
        Theme theme = null;
        if (themeTitle != null && !themeTitle.isEmpty()) {
            Query themeQuery = session.createQuery("from Theme where area like ?");
            themeQuery.setParameter(0, area);
            List<Theme> themes = Util.castList(Theme.class, themeQuery.list());

            for (Theme dbTheme : themes) {
                if (dbTheme.getTitle().toLowerCase().equals(themeTitle.toLowerCase())) {
                    theme = dbTheme;
                    break;
                }
            }

            if (theme == null && autoCreate) {
                //New theme was specified
                theme = new Theme();
                theme.setTitle(themeTitle);
                theme.setArea(area);

                //Set prio for theme
                Query themeQuery2 = session.createQuery("from Theme where area like ? and archived=false order by prio desc");
                themeQuery2.setParameter(0, area);
                List<Theme> themeList = Util.castList(Theme.class, themeQuery2.list());

                if (themeList.isEmpty()) {
                    theme.setPrio(1);
                } else {
                    int lastPrio = themeList.get(0).getPrio();
                    theme.setPrio(lastPrio + 1);
                }

                session.save(theme);
            }
        }
        return theme;
    }

    /**
     * Retrieves an Epic from DB that matches the params.
     * @param epicTitle
     * @param theme
     * @param area
     * @param session
     * @param autoCreate if the epic should be created if it does not exist
     * @return
     */
    private Epic getEpic(String epicTitle, Theme theme, Area area, Session session, boolean autoCreate) {
        Epic epic = null;
        if (epicTitle != null && !epicTitle.isEmpty()) {
            Query epicQuery = null;
            if (theme == null) {
                epicQuery = session.createQuery("from Epic where area like ? " +
                        "and theme is null");
            } else {
                epicQuery = session.createQuery("from Epic where area like ? " +
                        "and theme = ?");
                epicQuery.setParameter(1, theme);
            }
            epicQuery.setParameter(0, area);

            List<Epic> epics = Util.castList(Epic.class, epicQuery.list());
            for (Epic dbEpic : epics) {
                if (dbEpic.getTitle().toLowerCase().equals(epicTitle.toLowerCase())) {
                    epic = dbEpic;
                    break;
                }
            }

            if (epic == null && autoCreate) {
                //New epic was specified
                epic = new Epic();
                epic.setTitle(epicTitle);
                epic.setArea(area);

                //Set prio for epic
                Query epicQuery2 = session.createQuery("from Epic where area like ? and archived=false order by prio desc");
                epicQuery2.setParameter(0, area);
                List<Epic> epicList = Util.castList(Epic.class, epicQuery2.list());

                if (epicList.isEmpty()) {
                    epic.setPrio(1);
                } else {
                    int lastPrio = epicList.get(0).getPrio();
                    epic.setPrio(lastPrio + 1);
                }

                session.save(epic);
            }
        }
        return epic;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneStory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Story cloneStory(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) throws Exception {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Story clone = null;
        NewStoryContainer storyToPush = null;
        try {
            tx = session.beginTransaction();

            Story storyToClone = (Story) session.get(Story.class, id);
            if (!storyToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }
            clone = storyToClone.copy(withChildren);
            clone.setPrio(storyToClone.getPrio() + 1);

            //Move down all stories in the current epic
            Epic parentEpic = clone.getEpic();
            if (parentEpic != null) {
                for (Story story : parentEpic.getChildren()) {
                    if (story.getPrioInEpic() > storyToClone.getPrioInEpic()) {
                        story.setPrioInEpic(story.getPrioInEpic() + 1);
                    }
                }
                clone.setPrioInEpic(storyToClone.getPrioInEpic() + 1);
            }

            //Move down all stories under this story
            Query query = session.createQuery("from Story where prio > ? and area.name like ? and archived=false");
            query.setParameter(0, storyToClone.getPrio());
            query.setParameter(1, areaName);
            List<Story> storyList = Util.castList(Story.class, query.list());

            for (Story story : storyList) {
                story.setPrio(story.getPrio() + 1);
            }

            clonedId = (Integer) session.save(clone);
            clone.setTitle("Clone " + clonedId + " " + clone.getTitle());

            Set<Task> clonedChildren = clone.getChildren();
            for (Task task : clonedChildren) {
                session.save(task);
            }

            ListItem lastItem = new ListItem();
            lastItem.setId(storyToClone.getId());
            storyToPush = new NewStoryContainer();
            storyToPush.fromStory(clone);
            storyToPush.setLastItem(lastItem);
            List<String> messages = new ArrayList<String>();
            messages.add(getJsonStringExclChildren(Story.class, storyToPush, EPIC_STORY_VIEW));
            messages.add(getJsonStringInclChildren(Story.class.getSimpleName(), storyToPush, STORY_TASK_VIEW));
            tx.commit();
            
            AtmosphereHandler.pushJsonMessages(areaName, messages);
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return storyToPush;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneEpic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Epic cloneEpic(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) throws Exception {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Epic clone = null;
        NewEpicContainer epicToPush = null;
        try {
            tx = session.beginTransaction();

            Epic epicToClone = (Epic) session.get(Epic.class, id);
            if (!epicToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            clone = epicToClone.copy(withChildren);
            clone.setPrio(epicToClone.getPrio() + 1);

            //Move down all epics in the current theme
            Theme parentTheme = clone.getTheme();
            if (parentTheme != null) {
                for (Epic epic : parentTheme.getChildren()) {
                    if (epic.getPrioInTheme() > epicToClone.getPrioInTheme()) {
                        epic.setPrioInTheme(epic.getPrioInTheme() + 1);
                    }
                }
                clone.setPrioInTheme(epicToClone.getPrioInTheme() + 1);
            }

            //Move down all epics under this epic
            Query query = session.createQuery("from Epic where prio > ? and area.name like ? and archived=false");
            query.setParameter(0, epicToClone.getPrio());
            query.setParameter(1, areaName);
            List<Epic> epicList = Util.castList(Epic.class, query.list());

            for (Epic epic : epicList) {
                epic.setPrio(epic.getPrio() + 1);
            }

            clonedId = (Integer) session.save(clone);
            clone.setTitle("Clone " + clonedId + " " + clone.getTitle());

            Set<Story> clonedChildren = clone.getChildren();
            for (Story story : clonedChildren) {
                session.save(story);
            }

            ListItem lastItem = new ListItem();
            lastItem.setId(epicToClone.getId());
            epicToPush = new NewEpicContainer();
            epicToPush.fromEpic(clone);
            epicToPush.setLastItem(lastItem);

            List<String> messages = new ArrayList<String>();

            messages.add(getJsonStringExclChildren(Epic.class, epicToPush, THEME_EPIC_VIEW));
            messages.add(getJsonStringInclChildren(Epic.class.getSimpleName(), epicToPush, EPIC_STORY_VIEW));
            tx.commit();

            AtmosphereHandler.pushJsonMessages(areaName, messages);
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return epicToPush;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneTheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Theme cloneTheme(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) throws Exception {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Theme clone = null;
        NewThemeContainer themeToPush = null;
        try {
            tx = session.beginTransaction();

            Theme themeToClone = (Theme) session.get(Theme.class, id);
            if (!themeToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            clone = themeToClone.copy(withChildren);
            clone.setPrio(themeToClone.getPrio() + 1);

            //Move down all themes under this theme
            Query query = session.createQuery("from Theme where prio > ? and area.name like ? and archived=false");
            query.setParameter(0, themeToClone.getPrio());
            query.setParameter(1, areaName);
            List<Theme> themeList = Util.castList(Theme.class, query.list());

            for (Theme theme : themeList) {
                theme.setPrio(theme.getPrio() + 1);
            }

            clonedId = (Integer) session.save(clone);
            clone.setTitle("Clone " + clonedId + " " + clone.getTitle());

            Set<Epic> clonedChildren = clone.getChildren();
            for (Epic epic : clonedChildren) {
                session.save(epic);
            }

            ListItem lastItem = new ListItem();
            lastItem.setId(themeToClone.getId());
            themeToPush = new NewThemeContainer();
            themeToPush.fromTheme(clone);
            themeToPush.setLastItem(lastItem);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(Theme.class.getSimpleName(), themeToPush, THEME_EPIC_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return themeToPush;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deletenote/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteNote(@PathVariable String areaName, @RequestBody int noteId) throws JsonGenerationException, JsonMappingException, IOException {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Note noteToRemove = (Note) session.get(Note.class, noteId);
            Story parentStory =  (Story) session.get(Story.class, noteToRemove.getStoryId());
            if (parentStory == null || !parentStory.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            parentStory.getNotes().remove(noteToRemove);
            session.delete(noteToRemove);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(PUSH_ACTION_DELETE, noteId, STORY_TASK_VIEW));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return true;
    }

    /**
     * Used when deleting a story.
     * @param storyId id of the story to remove
     * @return true if everything was ok
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deletestory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteStory(@PathVariable String areaName, @RequestBody int storyId) throws JsonGenerationException, JsonMappingException, IOException {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Story storyToRemove = (Story) session.get(Story.class, storyId);
            if (!storyToRemove.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Set<Task> tasksInStory = storyToRemove.getChildren();
            Set<Note> notesInStory = storyToRemove.getNotes();

            //Move up all stories under this story
            Query query = session.createQuery("from Story where prio > ? and area.name like ? and archived=false");
            query.setParameter(0, storyToRemove.getPrio());
            query.setParameter(1, areaName);
            List<Story> storyList = Util.castList(Story.class, query.list());

            for (Story story : storyList) {
                story.setPrio(story.getPrio() - 1);
            }

            for (Task taskToRemove : tasksInStory) {
                session.delete(taskToRemove);
            }

            for (Note noteToRemove : notesInStory) {
                session.delete(noteToRemove);
            }
            Epic parentEpic = storyToRemove.getEpic();
            if (parentEpic != null) {
                parentEpic.getChildren().remove(storyToRemove);
            }
            session.delete(storyToRemove);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(PUSH_ACTION_DELETE, storyId, ALL_VIEWS));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return true;
    }

    /**
     * Used when deleting a epic.
     * @param epicId id of the story to remove
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deleteepic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteEpic(@PathVariable String areaName, @RequestBody int epicId) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Epic epicToRemove = (Epic) session.get(Epic.class, epicId);
            if (!epicToRemove.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            //Move up all epics under this epic
            Query query = session.createQuery("from Epic where prio > ? and area.name like ? and archived=false");
            query.setParameter(0, epicToRemove.getPrio());
            query.setParameter(1, areaName);
            List<Epic> epicList = Util.castList(Epic.class, query.list());
            for (Epic epic : epicList) {
                epic.setPrio(epic.getPrio() - 1);
            }

            Set<Story> storiesInEpic = epicToRemove.getChildren();

            for (Story storyToEdit : storiesInEpic) {
                storyToEdit.setEpic(null);
            }
            session.delete(epicToRemove);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(PUSH_ACTION_DELETE, epicId, ALL_VIEWS));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return true;
    }

    /**
     * Used when deleting a theme.
     * @param themeId id of the theme to remove
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deletetheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteTheme(@PathVariable String areaName, @RequestBody int themeId) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Theme themeToRemove = (Theme) session.get(Theme.class, themeId);
            if (!themeToRemove.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            //Move up all themes under this theme
            Query query1 = session.createQuery("from Theme where prio > ? and area.name like ?");
            query1.setParameter(0, themeToRemove.getPrio());
            query1.setParameter(1, areaName);
            List<Theme> themeList = Util.castList(Theme.class, query1.list());

            for (Theme theme : themeList) {
                theme.setPrio(theme.getPrio() - 1);
            }

            Query query2 = session.createQuery("from Story where area.name like ? and theme = ?");
            query2.setParameter(0, areaName);
            query2.setParameter(1, themeToRemove);
            List<Story> storyList = Util.castList(Story.class, query2.list());

            for (Story storyToEdit : storyList) {
                storyToEdit.setTheme(null);
            }

            Set<Epic> epicsInTheme = themeToRemove.getChildren();

            for (Epic epicToEdit : epicsInTheme) {
                epicToEdit.setTheme(null);
                //TODO: Fix that this could potentially make two Epics with
                //same name exist without a theme
            }
            session.delete(themeToRemove);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(PUSH_ACTION_DELETE, themeId, ALL_VIEWS));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return true;
    }

    /**
     * Used when deleting a task.
     * @param taskId id of the task to remove
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deletetask/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteTask(@PathVariable String areaName, @RequestBody int taskId) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Task taskToRemove = (Task) session.get(Task.class, taskId);
            Story parentStory =  (Story) session.get(Story.class, taskToRemove.getParentId());

            if (!parentStory.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            //Move up tasks under the removed task.
            Set<Task> tasksInParent = parentStory.getChildren();
            for (Task task : tasksInParent) {
                if (task.getPrioInStory() > taskToRemove.getPrioInStory()) {
                    task.setPrioInStory(task.getPrioInStory() - 1);
                }
            }

            parentStory.getChildren().remove(taskToRemove);
            session.delete(taskToRemove);

            tx.commit();
            AtmosphereHandler.push(areaName, getJsonStringInclChildren(PUSH_ACTION_DELETE, taskId, ALL_VIEWS));
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return true;
    }

    /**
     * Used when moving stories between areas.
     * @param areaName area to move from
     * @param storyIds ids of the stories to move
     * @param newAreaName target area
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/moveToArea/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean moveToArea(@PathVariable String areaName,
            @RequestBody int[] storyIds, @RequestParam String newAreaName) {
        String username = getUserName();

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area oldArea = (Area) session.get(Area.class, areaName);
            Area newArea = (Area) session.get(Area.class, newAreaName);
            User user = (User) session.get(User.class, username);

            List<String> pushMsgsOldArea = new ArrayList<String>();
            List<String> pushMsgsNewArea = new ArrayList<String>();
            //Check that the user has rights for the new area as well
            if ((newArea != null && (newArea.isAdmin(username) || newArea.isEditor(username)))
                    || (user != null && user.isMasterAdmin())) {
                List<Story> storiesToMove = new ArrayList<Story>(); 

                //Get all the stories to move
                for (int id : storyIds) {
                    Query storyQuery = session.createQuery("from Story where area like ? and id=?");
                    storyQuery.setParameter(0, oldArea);
                    storyQuery.setParameter(1, id);
                    Story story = (Story) storyQuery.uniqueResult();
                    storiesToMove.add(story);
                }

                Collections.sort(storiesToMove, new Comparator<Story>() {
                    @Override
                    public int compare(Story o1, Story o2) {
                        return o1.getPrio() - o2.getPrio();
                    }
                });

                for (Story story : storiesToMove) {
                    //Set new rank
                    int newPrio = -1;
                    if (!story.isArchived()) {
                        newPrio = Util.getNextPrio(BacklogType.STORY, newArea, session);
                    }
                    story.setPrio(newPrio);
                    story.setArea(newArea);

                    //Change all story attribute options
                    AttributeOption newOpt1 = getAttrAfterMove(story.getStoryAttr1(), newArea.getStoryAttr1().getOptions(), session);
                    AttributeOption newOpt2 = getAttrAfterMove(story.getStoryAttr2(), newArea.getStoryAttr2().getOptions(), session);
                    AttributeOption newOpt3 = getAttrAfterMove(story.getStoryAttr3(), newArea.getStoryAttr3().getOptions(), session);
                    story.setStoryAttr1(newOpt1);
                    story.setStoryAttr2(newOpt2);
                    story.setStoryAttr3(newOpt3);

                    //Change all task attribute options
                    for (Task task : story.getChildren()) {
                        AttributeOption taskOpt1 = getAttrAfterMove(task.getTaskAttr1(), newArea.getTaskAttr1().getOptions(), session);
                        task.setTaskAttr1(taskOpt1);
                    }

                    //Handle move of theme and epic
                    if (story.getEpic() != null && story.getTheme() != null) {
                        //Both theme and epic exists.
                        //Firstly, look for a matching theme
                        Theme newTheme = getThemeAfterMove(story, newArea, session);
                        story.setTheme(newTheme);

                        //Look for a matching epic
                        Epic newEpic = null;
                        boolean foundMatch = false;
                        for (Epic epic : newTheme.getChildren()) {
                            if (epic.getTitle().equals(story.getEpic().getTitle())) {
                                newEpic = epic;
                                foundMatch = true;
                                break;
                            } 
                        }
                        if (!foundMatch) {
                            //Create new epic in the theme.
                            newEpic = story.getEpic().copy(false);
                            int prio = -1;
                            if (!newEpic.isArchived()) {
                                prio = Util.getNextPrio(BacklogType.EPIC, newArea, session);                                
                            }
                            newEpic.setPrio(prio);

                            newEpic.setArea(newArea);

                            //Set correct prioInTheme
                            int prioInTheme = newTheme.getChildren().size() + 1;
                            newEpic.setPrioInTheme(prioInTheme);

                            session.save(newEpic);
                            newTheme.getChildren().add(newEpic);
                        }
                        story.getEpic().getChildren().remove(story);
                        story.setEpic(newEpic);
                        newEpic.getChildren().add(story);
                        newEpic.setTheme(newTheme);

                    } else if (story.getTheme() != null) {
                        Theme newTheme = getThemeAfterMove(story, newArea, session);
                        story.setTheme(newTheme);
                    } else if (story.getEpic() != null) {
                        Query epicQuery1 = session.createQuery("from Epic where area like ? and title like ?");
                        epicQuery1.setParameter(0, newArea);
                        epicQuery1.setParameter(1, story.getEpic().getTitle());
                        Epic newEpic = (Epic) epicQuery1.uniqueResult();
                        if (newEpic == null) {
                            //Create new epic
                            newEpic = story.getEpic().copy(false);

                            //Set correct prio
                            int prio = -1;
                            if (!newEpic.isArchived()) {
                                prio = Util.getNextPrio(BacklogType.EPIC, newArea, session);                                
                            }
                            newEpic.setPrio(prio);
                            newEpic.setArea(newArea);
                            session.save(newEpic);
                        }
                        //Set correct prioInEpic
                        int prioInEpic = newEpic.getChildren().size() + 1;
                        story.setPrioInEpic(prioInEpic);

                        story.getEpic().getChildren().remove(story);
                        story.getEpic().rebuildChildrenOrder();

                        story.setEpic(newEpic);
                        newEpic.getChildren().add(story);
                        story.setTheme(newEpic.getTheme());
                    }
                    String noteMsg = String.format("User %s moved the story to area %s", username, newArea.getName());
                    Note note = Note.createSystemNote(noteMsg, story, session);
                    pushMsgsNewArea.add(getJsonStringInclChildren(Note.class.getSimpleName(), note, STORY_TASK_VIEW));

                    pushMsgsNewArea.add(getJsonStringExclChildren(Story.class, story, STORY_TASK_VIEW));
                    pushMsgsOldArea.add(getJsonStringInclChildren(PUSH_ACTION_DELETE, story.getId(), STORY_TASK_VIEW + "|" + EPIC_STORY_VIEW));
                }
                Util.rebuildRanks(BacklogType.STORY, oldArea, session);

                //Push out all affected themes and epics
                Set<Epic> updatedEpics = new HashSet<Epic>();
                Set<Theme> updatedThemes = new HashSet<Theme>();
                for (Story story : storiesToMove) {
                    Theme theme = story.getTheme();
                    Epic epic = story.getEpic();
                    if (theme != null && updatedThemes.add(theme)) {
                        pushMsgsNewArea.add(getJsonStringExclChildren(Theme.class, theme, THEME_EPIC_VIEW));
                    }
                    if (epic != null && updatedEpics.add(epic)) {
                        pushMsgsNewArea.add(getJsonStringExclChildren(Epic.class, epic, EPIC_STORY_VIEW));
                    }
                    List<Note> notes = story.getTenNewestNotes();
                    Collections.reverse(notes);
                    for (Note n : notes) {
                        pushMsgsNewArea.add(getJsonStringInclChildren(Note.class.getSimpleName(), n, STORY_TASK_VIEW));
                    }
                }
            }
            AtmosphereHandler.pushJsonMessages(areaName, pushMsgsOldArea);
            AtmosphereHandler.pushJsonMessages(newAreaName, pushMsgsNewArea);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return true;
    }

    /**
     * Helper for moveToArea. Finds a matching attribute after moving a story to a new area.
     * Creates a new attribute if no match was found.
     * @param currentOption the selected option in the old area
     * @param targetOptions the available options in the new area
     * @param session hibernate session
     * @return matched attribute (or new attribute if no match)
     */
    private AttributeOption getAttrAfterMove(AttributeOption currentOption,
            Set<AttributeOption> targetOptions, Session session) {
        if (currentOption != null) {
            for (AttributeOption newOption : targetOptions) {
                if (newOption.getName().equals(currentOption.getName())) {
                    return newOption;
                }
            }
            //No matching attribute found; copy the attribute.
            AttributeOption newOption = currentOption.copy();
            Set<AttributeOption> attributeOptions = targetOptions;
            newOption.setCompareValue(attributeOptions.size() + 1);
            session.save(newOption);
            attributeOptions.add(newOption);
            return newOption;
        }
        return null;
    }

    /**
     * Helper for moveToArea. Finds a matching theme after moving a story to a new area.
     * Creates a new theme if no match was found.
     * @param storyToMove the story thatäs being moved
     * @param newArea target area
     * @param session hibernate session
     * @return matched theme (or new attribute if no match)
     */
    private Theme getThemeAfterMove(Story storyToMove, Area newArea, Session session) {
        Query themeQuery = session.createQuery("from Theme where area like ? and title like ?");
        themeQuery.setParameter(0, newArea);
        themeQuery.setParameter(1, storyToMove.getTheme().getTitle());
        Theme newTheme = (Theme) themeQuery.uniqueResult();
        if (newTheme == null) {
            //Create new theme
            newTheme = storyToMove.getTheme().copy(false);

            //Set prio for theme
            int prio = -1;
            if (!newTheme.isArchived()) {
                prio = Util.getNextPrio(BacklogType.THEME, newArea, session);
            }
            newTheme.setPrio(prio);

            newTheme.setArea(newArea);
            session.save(newTheme);
        }
        return newTheme;
    } 

    /**
     * Used when creating an area
     * @return new area name if everything was ok
     */
    @RequestMapping(value="/createArea", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody String createArea(@RequestBody String areaName) {
        //Removing all invalid characters:
        areaName = areaName.replaceAll("\\<.*?>","").replaceAll("[\"/\\\\.?;#%\u20AC]", "");        
        areaName = areaName.trim();

        String username = getUserName();
        if (username == null) {
            throw new Error("Trying to create area without being logged in");
        }

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area sameNameArea = (Area) session.get(Area.class, areaName);

            //Only create if it was a valid area name, and the area does not already exist
            //and the user is logged in
            if (!areaName.isEmpty() && sameNameArea == null
                    && isLoggedIn() && areaName.length() <= 50) {
                Area area = new Area(areaName, session);
                area.makeAdmin(username);
                session.save(area);
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
        return areaName;
    }

    /**
     * Used when changing name of an area.
     * @return new area name if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/changeAreaName/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody String changeAreaName(@PathVariable String areaName, @RequestBody String newName) {        
        //Removing all invalid characters:
        newName = newName.replaceAll("\\<.*?>","").replaceAll("[\"/\\\\.?;#%\u20AC]", "");        
        newName = newName.trim();

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            //Only create if it was a valid area name, and the area does not already exist
            Area sameNameArea = (Area) session.get(Area.class, newName);
            if (!newName.isEmpty() && sameNameArea == null && newName.length() <= 50) {
                //Since the area name is primary key in DB, we need to create
                //a new area with the new name and move all existing items there.
                Area oldArea = (Area) session.get(Area.class, areaName);
                Area newArea = new Area();
                newArea.setName(newName);
                session.save(newArea);

                newArea.setStoryAttr1(oldArea.getStoryAttr1());
                newArea.setStoryAttr2(oldArea.getStoryAttr2());
                newArea.setStoryAttr3(oldArea.getStoryAttr3());
                newArea.setTaskAttr1(oldArea.getTaskAttr1());
                newArea.setAdmins(oldArea.getAdmins());
                oldArea.setAdmins(null);
                newArea.setEditors(oldArea.getEditors());
                oldArea.setEditors(null);

                Query storyQuery = session.createQuery("from Story where area like ?");
                storyQuery.setParameter(0, oldArea);
                List<Story> stories = Util.castList(Story.class, storyQuery.list());
                for (Story story : stories) {
                    story.setArea(newArea);
                }

                Query epicQuery = session.createQuery("from Epic where area like ?");
                epicQuery.setParameter(0, oldArea);
                List<Epic> epics = Util.castList(Epic.class, epicQuery.list());
                for (Epic epic : epics) {
                    epic.setArea(newArea);
                }

                Query themeQuery = session.createQuery("from Theme where area like ?");
                themeQuery.setParameter(0, oldArea);
                List<Theme> themes = Util.castList(Theme.class, themeQuery.list());
                for (Theme theme : themes) {
                    theme.setArea(newArea);
                }

                session.delete(oldArea);
            } else {
                newName = null;
            }
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
            newName = null;
        } finally {
            session.close();
        }
        return newName;
    }


    /**
     * Used when deleting an area
     * @return true if everything was ok
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonGenerationException 
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/deleteArea/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteArea(@PathVariable String areaName) throws JsonGenerationException, JsonMappingException, IOException {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            //Firstly, unlink elements from each other
            Query noteQuery = session.createQuery("from Note where story.area.name like ?");
            noteQuery.setParameter(0, areaName);
            List<Note> notes = Util.castList(Note.class, noteQuery.list());
            for (Note note : notes) {
                note.setStory(null);
            }

            Query taskQuery = session.createQuery("from Task where story.area.name like ?");
            taskQuery.setParameter(0, areaName);
            List<Task> tasks = Util.castList(Task.class, taskQuery.list());
            for (Task task : tasks) {
                task.setStory(null);
            }

            Query storyQuery = session.createQuery("from Story where area.name like ?");
            storyQuery.setParameter(0, areaName);
            List<Story> stories = Util.castList(Story.class, storyQuery.list());
            for (Story story : stories) {
                story.setTheme(null);
                story.setEpic(null);
            }

            Query epicQuery = session.createQuery("from Epic where area.name like ?");
            epicQuery.setParameter(0, areaName);
            List<Epic> epics = Util.castList(Epic.class, epicQuery.list());
            for (Epic epic : epics) {
                epic.setTheme(null);
                epic.setChildren(null);
            }

            Query themeQuery = session.createQuery("from Theme where area.name like ?");
            themeQuery.setParameter(0, areaName);
            List<Theme> themes = Util.castList(Theme.class, themeQuery.list());
            for (Theme theme : themes) {
                theme.setChildren(null);
            }

            tx.commit();
            tx = session.beginTransaction();

            //Secondly, remove all elements
            for (Note note : notes) {
                session.delete(note);
            }

            for (Task task : tasks) {
                session.delete(task);
            }
            for (Story story : stories) {
                session.delete(story);
            }
            for (Epic epic : epics) {
                session.delete(epic);
            }
            for (Theme theme : themes) {
                session.delete(theme);
            }

            Area area = (Area) session.get(Area.class, areaName);

            for (AttributeOption option : area.getStoryAttr1().getOptions()) {
                session.delete(option);
            }
            for (AttributeOption option : area.getStoryAttr2().getOptions()) {
                session.delete(option);
            }
            for (AttributeOption option : area.getStoryAttr3().getOptions()) {
                session.delete(option);
            }
            for (AttributeOption option : area.getTaskAttr1().getOptions()) {
                session.delete(option);
            }
            session.delete(area.getStoryAttr1());
            session.delete(area.getStoryAttr2());
            session.delete(area.getStoryAttr3());
            session.delete(area.getTaskAttr1());

            session.delete(area);

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        AtmosphereHandler.push(areaName, getJsonStringInclChildren("AreaDelete", "{}", ALL_VIEWS));
        return true;
    }

    /**
     * Used when adding an admin
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/addAdmin/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean addAdmin(@PathVariable String areaName, @RequestBody String username) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            username = username.trim();
            username = StringEscapeUtils.escapeHtml(username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                session.close();
                throw new NullPointerException("area is null");
            }

            if (!username.isEmpty()) {
                area.makeAdmin(username);
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

        return true;
    }

    /**
     * Used when removing an admin
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/removeAdmin/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean removeAdmin(@PathVariable String areaName, @RequestBody String username) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            username = username.trim();
            username = StringEscapeUtils.escapeHtml(username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                session.close();
                throw new NullPointerException("area is null");
            }

            if (!username.isEmpty()) {
                area.removeAdmin(username);
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

        return true;
    }

    /**
     * Used when adding an editor
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/addEditor/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean addEditor(@PathVariable String areaName, @RequestBody String username) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            username = username.trim();
            username = StringEscapeUtils.escapeHtml(username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                session.close();
                throw new NullPointerException("area is null");
            }

            if (!username.isEmpty()) {
                area.makeEditor(username);
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

        return true;
    }

    /**
     * Used when removing an editor
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/removeEditor/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean removeEditor(@PathVariable String areaName, @RequestBody String username) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            username = username.trim();
            username = StringEscapeUtils.escapeHtml(username);

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                session.close();
                throw new NullPointerException("area is null");
            }

            if (!username.isEmpty()) {
                area.removeEditor(username);
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

        return true;
    }

    /**
     * Used when updating story attributes for an area.
     * @param areaName
     * @param updatedAttribute
     * @return true if everything was ok
     * @throws Exception
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/updateAttribute/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean updateAttribute(@PathVariable String areaName,
            @RequestBody Attribute updatedAttribute) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area area = (Area) session.get(Area.class, areaName);
            Attribute dbAttribute = (Attribute) session.get(Attribute.class, updatedAttribute.getId());
            dbAttribute.setName(updatedAttribute.getName());

            //Check that the updateOption really belongs to the area specified by client.
            if (area.getStoryAttr1() != dbAttribute
                    && area.getStoryAttr2() != dbAttribute
                    && area.getStoryAttr3() != dbAttribute
                    && area.getTaskAttr1() != dbAttribute) {
                throw new Exception("Trying to modify unauthorized object");
            }

            Set<AttributeOption> dbOptions = dbAttribute.getOptions();
            Set<AttributeOption> updatedOptions = updatedAttribute.getOptions();

            if (updatedOptions.size() > 1500) {
                throw new Exception("Too many attribute options");
            }

            //Build Hashmap to update..
            Map<Integer,AttributeOption> dbOptionsMap = new HashMap<Integer, AttributeOption>();
            Iterator<AttributeOption> itr = dbOptions.iterator();
            while (itr.hasNext()) {
                AttributeOption option = itr.next();
                if (updatedOptions.contains(option)) {
                    dbOptionsMap.put(option.getId(), option);
                } else {
                    itr.remove();

                    //Option was removed; reset all stories that have this option
                    Query storyQuery = session.createQuery("from Story where area.name like ?");
                    storyQuery.setParameter(0, areaName);
                    List<Story> stories = Util.castList(Story.class, storyQuery.list());
                    for (Story story : stories) {
                        if (story.getStoryAttr1() == option) {
                            story.setStoryAttr1(null);
                        }
                        if (story.getStoryAttr2() == option) {
                            story.setStoryAttr2(null);
                        }
                        if (story.getStoryAttr3() == option) {
                            story.setStoryAttr3(null);
                        }
                    }
                    session.delete(option);

                    //If it was a task attribute, reset all tasks with that attribute
                    Query taskQuery = session.createQuery("from Task where story.area.name like ?");
                    taskQuery.setParameter(0, areaName);
                    List<Task> tasks = Util.castList(Task.class, taskQuery.list());
                    for (Task task : tasks) {
                        if (task.getTaskAttr1() == option) {
                            task.setTaskAttr1(null);
                        }
                    }
                }
            }

            //Finally update
            for (AttributeOption updatedOption : updatedAttribute.getOptions()) {

                if (updatedOption.getId() <= -1) {
                    //New option
                    session.save(updatedOption);
                    dbAttribute.addOption(updatedOption);
                } else {
                    AttributeOption dbOption = dbOptionsMap.get(updatedOption.getId());
                    dbOption.setColor(updatedOption.getColor());
                    dbOption.setIconEnabled(updatedOption.isIconEnabled());
                    dbOption.setIcon(updatedOption.getIcon());
                    dbOption.setName(updatedOption.getName());
                    dbOption.setCompareValue(updatedOption.getCompareValue());
                    dbOption.setSeriesIncrement(updatedOption.getSeriesIncrement());
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

        return true;
    }

    /**
     * Used when reading info about an area.
     * @return Area
     */
    @RequestMapping(value="/readArea/{areaName}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody Area readArea(@PathVariable String areaName) {
        return Util.getArea(areaName, sessionFactory);
    }

    /**
     * Generates a JSON-string from the specified data, but does <b>not</b> include children
     * @param clazz The class (e.g. Task.class)
     * @param data The object-data
     * @param viewParam The views that this data is intended for
     * @return A String in JSON-format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> String getJsonStringExclChildren(Class<T> clazz, Object data, String viewParam) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().addMixInAnnotations(clazz, ChildrenExcluder.class);
        return generateJsonString(mapper, clazz.getSimpleName(), data, viewParam);
    }

    /**
     * Generates a JSON-string from the specified data, and includes children
     * @param type The type of event (e.g. "Delete")
     * @param data The object-data
     * @param viewParam The views that this data is intended for
     * @return A String in JSON-format
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static <T> String getJsonStringInclChildren(String type, Object data, String viewParam) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        return generateJsonString(mapper, type, data, viewParam);
    }

    private static <T> String generateJsonString(ObjectMapper mapper, String type, Object data, String viewParam) throws JsonGenerationException, JsonMappingException, IOException {
        HashMap<String, Object> typeMapper = new HashMap<String, Object>();
        typeMapper.put("type", type);
        typeMapper.put("data", data);
        typeMapper.put("views", viewParam);
        return mapper.writeValueAsString(typeMapper);
    }

    /**
     * Returns the new attribute-value if this is different from the current
     * one, otherwise null
     * 
     * @param currentAttr
     *            The current attribute option
     * @param newAttr
     *            The new attribute option
     * @return The value of the new attribute option, or null if is doesn't
     *         differ from the current
     */
    private String getUpdatedAttrValue(AttributeOption currentAttr,
            AttributeOption newAttr) {
        String newAttrValue = "nothing", currAttrValue = "nothing";
        if (currentAttr != null) {
            currAttrValue = currentAttr.getName();
        }
        if (newAttr != null) {
            newAttrValue = newAttr.getName();
        }

        if (currAttrValue.equals(newAttrValue)) {
            return null;
        }
        return newAttrValue;
    }

    /**
     * Used by clients to register themselves for push-notifications for a certain area
     * @param event
     * @param areaName
     */
    @RequestMapping(value = "/register/{areaName}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody void registerForArea(final AtmosphereResource event, @PathVariable String areaName) {
//        System.out.println("=== INFO === registerForArea() with areaName " + areaName);
        AtmosphereHandler.suspendClient(event, areaName);
    }

}