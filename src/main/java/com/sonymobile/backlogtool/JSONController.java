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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.icepush.PushContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Handles Ajax application requests with JSON data.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Controller
@RequestMapping(value="/json")
public class JSONController {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ServletContext context;

    //        @RequestMapping(value="/emptyDB", method=RequestMethod.GET)
    //        @Transactional
    //        public @ResponseBody void emptyDB() {
    //            Session session = sessionFactory.getCurrentSession();
    //
    //            session.createSQLQuery("DROP TABLE stories CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE tasks CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE users CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE areas CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE area_editors CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE area_admins CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE area_adminldapgroups CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE area_editorldapgroups CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE themes CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE epics CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE attributes CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE attributeoptions CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP TABLE attributes_attributeoptions CASCADE").executeUpdate();
    //            session.createSQLQuery("DROP SEQUENCE hibernate_sequence").executeUpdate();
    //
    //            System.out.println("DB was dropped");
    //        }

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
                String queryString1 = "from Story where area.name like ? order by " + order + ".compareValue";
                Query query1 = session.createQuery(queryString1);
                query1.setParameter(0, areaName);

                //When sorting by compareValues, all stories with null as this attribute are excluded and therefore
                //have to be added in a separate request:
                String queryString2 = "from Story where area.name like ? and " + order + " is null";
                Query query2 = session.createQuery(queryString2);
                query2.setParameter(0, areaName);

                list = Util.castList(Story.class, query1.list());
                list.addAll(Util.castList(Story.class, query2.list()));
            } else if (order.equals("prio")) {
                //Since the archived stories don't have any prio, we order them by their date archived.
                String nonArchivedQueryString = "from Story where area.name like ? and archived=false order by prio";
                String archivedQueryString = "from Story where area.name like ? and archived=true order by dateArchived desc";

                Query nonArchivedQuery = session.createQuery(nonArchivedQueryString);
                Query archivedQuery = session.createQuery(archivedQueryString);

                archivedQuery.setParameter(0, areaName);
                nonArchivedQuery.setParameter(0, areaName);

                list = Util.castList(Story.class, nonArchivedQuery.list());
                list.addAll(Util.castList(Story.class, archivedQuery.list()));
            } else {
                String queryString = "from Story where area.name like ? order by " + order;
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
                String queryString1 = "from Epic where area.name like ? and archived=false order by prio";
                Query query1 = session.createQuery(queryString1);
                query1.setParameter(0, areaName);

                String queryString2 = "from Epic where area.name like ? and archived=true order by dateArchived desc";
                Query query2 = session.createQuery(queryString2);
                query2.setParameter(0, areaName);

                epics = Util.castList(Epic.class, query1.list());
                epics.addAll(Util.castList(Epic.class, query2.list()));
            } else {

                String queryString = "from Epic where area.name like ? order by " + order;
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
                String queryString1 = "from Theme where area.name like ? and archived=false order by prio";
                Query query1 = session.createQuery(queryString1);
                query1.setParameter(0, areaName);

                String queryString2 = "from Theme where area.name like ? and archived=true order by dateArchived desc";
                Query query2 = session.createQuery(queryString2);
                query2.setParameter(0, areaName);

                themes = Util.castList(Theme.class, query1.list());
                themes.addAll(Util.castList(Theme.class, query2.list()));
            } else {
                String queryString = "from Theme where area.name like ? order by " + order;
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

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createtask/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Integer createTask(@PathVariable String areaName, @RequestBody NewTaskContainer newTask) {
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
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return newTask.getId();
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createstory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Integer createStory(@PathVariable String areaName, @RequestBody NewStoryContainer newStory) throws Exception {
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

            if (epic != null) {
                epic.getChildren().add(newStory);
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

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return newStory.getId();
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createepic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Integer createEpic(@PathVariable String areaName, @RequestBody NewEpicContainer newEpic) throws Exception {
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

            PushContext pushContext = PushContext.getInstance(context);
            pushContext.push(areaName);

        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return newEpic.getId();
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/createtheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Integer createTheme(@PathVariable String areaName, @RequestBody NewThemeContainer newTheme) throws Exception {
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

            PushContext pushContext = PushContext.getInstance(context);
            pushContext.push(areaName);

        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return newTheme.getId();
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatetask/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Task updateTask(@PathVariable String areaName,
            @RequestBody NewTaskContainer updatedTask, @RequestParam boolean pushUpdate) {
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
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        if (pushUpdate) {
            PushContext pushContext = PushContext.getInstance(context);
            pushContext.push(areaName);
        }
        return task;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatestory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Story updateStory(@PathVariable String areaName,
            @RequestBody NewStoryContainer updatedStory, @RequestParam boolean pushUpdate) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Story story = null;
        try {
            tx = session.beginTransaction();

            story = (Story) session.get(Story.class, updatedStory.getId());
            if (!story.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            boolean createIfDoesNotExist = true;
            Theme theme = getTheme(updatedStory.getThemeTitle(), story.getArea(), session, createIfDoesNotExist);
            Epic newEpic = getEpic(updatedStory.getEpicTitle(), theme, story.getArea(), session, createIfDoesNotExist);

            //Move story from old epic if it was changed
            if (updatedStory.getEpicTitle() != null) {
                Epic oldEpic = story.getEpic();
                if (oldEpic != newEpic) {
                    if (oldEpic != null) {
                        oldEpic.getChildren().remove(story);
                        oldEpic.rebuildChildrenOrder();
                    }
                    if (newEpic != null) {
                        newEpic.getChildren().add(story);
                        story.setPrioInEpic(Integer.MAX_VALUE); //The prio gets rebuilt on newEpic.rebuildChildrenOrder().
                        newEpic.rebuildChildrenOrder();
                    }
                }
            }

            if (updatedStory.isArchived() && !story.isArchived()) {
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

            if (updatedStory.getThemeTitle() != null) {
                story.setTheme(theme);
            }
            if (updatedStory.getEpicTitle() != null) {
                story.setEpic(newEpic);
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
        if (pushUpdate) {
            PushContext pushContext = PushContext.getInstance(context);
            pushContext.push(areaName);
        }
        return story;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updateepic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Epic updateEpic(@PathVariable String areaName,
            @RequestBody NewEpicContainer updatedEpic, @RequestParam boolean pushUpdate) throws Exception {
        boolean success = false;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Epic epic = null;
        try {
            tx = session.beginTransaction();

            epic = (Epic) session.get(Epic.class, updatedEpic.getId());
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

            //Only make changes if the title does not already exist on another object
            if (sameNameEpic == null || sameNameEpic == epic) {

                if (updatedEpic.getThemeTitle() != null) {
                    epic.setTheme(theme);
                    for (Story story : epic.getChildren()) {
                        story.setTheme(theme);
                    }
                }

                if (updatedEpic.isArchived() && !epic.isArchived()) {
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
                tx.commit();
                if (pushUpdate) {
                    PushContext pushContext = PushContext.getInstance(context);
                    pushContext.push(areaName);
                }
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return epic;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/updatetheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Theme updateTheme(@PathVariable String areaName,
            @RequestBody Theme updatedTheme, @RequestParam boolean pushUpdate) throws Exception {
        boolean success = false;

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Theme theme = null;
        try {
            tx = session.beginTransaction();

            theme = (Theme) session.get(Theme.class, updatedTheme.getId());
            if (!theme.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Area area = (Area) session.get(Area.class, areaName);
            if (area == null) {
                throw new Exception("Could not find area!");
            }

            Theme sameNameTheme = getTheme(updatedTheme.getTitle(), area, session, false);
            if (sameNameTheme == null || sameNameTheme == theme) {
                if (updatedTheme.isArchived() && !theme.isArchived()) {
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
                tx.commit();
                if (pushUpdate) {
                    PushContext pushContext = PushContext.getInstance(context);
                    pushContext.push(areaName);
                }
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return theme;
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
                if (theme != null) {
                    theme.getChildren().add(epic);
                    epic.setPrioInTheme(Integer.MAX_VALUE); //This changes in theme.rebuildChildrenOrder().
                    theme.rebuildChildrenOrder();
                    epic.setTheme(theme);
                }
            }
        }
        return epic;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneStory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody int cloneStory(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Story storyToClone = (Story) session.get(Story.class, id);
            if (!storyToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Story clone = storyToClone.copy(withChildren);
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

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return clonedId;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneEpic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody int cloneEpic(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Epic epicToClone = (Epic) session.get(Epic.class, id);
            if (!epicToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Epic clone = epicToClone.copy(withChildren);
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

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return clonedId;
    }

    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/cloneTheme/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody int cloneTheme(@PathVariable String areaName, @RequestParam int id, @RequestParam boolean withChildren) {
        int clonedId = -1;
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Theme themeToClone = (Theme) session.get(Theme.class, id);
            if (!themeToClone.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Theme clone = themeToClone.copy(withChildren);
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

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return clonedId;
    }


    /**
     * Used when deleting a story.
     * @param storyId id of the story to remove
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/deletestory/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteStory(@PathVariable String areaName, @RequestBody int storyId) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Story storyToRemove = (Story) session.get(Story.class, storyId);
            if (!storyToRemove.getArea().getName().equals(areaName)) {
                throw new Error("Trying to modify unauthorized object");
            }

            Set<Task> tasksInStory = storyToRemove.getChildren();

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
            Epic parentEpic = storyToRemove.getEpic();
            if (parentEpic != null) {
                parentEpic.getChildren().remove(storyToRemove);
            }
            session.delete(storyToRemove);

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
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
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
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
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
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
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
        return true;
    }

    /**
     * Used when creating an area
     * @return new area name if everything was ok
     */
    @RequestMapping(value="/createArea", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody String createArea(@RequestParam String areaName) {
        //Replacing all invalid characters:
        areaName = areaName.replaceAll("\\<.*?>","").replaceAll("\"", "").replaceAll("/", "");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            Area sameNameArea = (Area) session.get(Area.class, areaName);

            //Only create if it was a valid area name, and the area does not already exist
            //and the user is logged in
            if (!areaName.isEmpty() && sameNameArea == null && isLoggedIn()) {
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
    public @ResponseBody String changeAreaName(@PathVariable String areaName, @RequestParam String newName) {        
        //Replacing all invalid characters:
        newName = newName.replaceAll("\\<.*?>","").replaceAll("\"", "").replaceAll("/", "");

        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            //Only create if it was a valid area name, and the area does not already exist
            Area sameNameArea = (Area) session.get(Area.class, newName);
            if (!newName.isEmpty() && sameNameArea == null) {
                String hql="update Area set name=? where name=? ";
                Query query=session.createQuery(hql);
                query.setString(0, newName);
                query.setString(1, areaName);
                query.executeUpdate();
            } else {
                newName = null;
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
        return newName;
    }


    /**
     * Used when deleting an area
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isAdmin')")
    @RequestMapping(value="/deleteArea/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody boolean deleteArea(@PathVariable String areaName) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            //Firstly, unlink elements from each other
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

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);
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

        PushContext pushContext = PushContext.getInstance(context);
        pushContext.push(areaName);

        return true;
    }

    /**
     * Used when reading info about an area.
     * @return Area
     */
    @RequestMapping(value="/readArea/{areaName}", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody Area readArea(@PathVariable String areaName) {
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

    private boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GrantedAuthority anonymous = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
        return !auth.getAuthorities().contains(anonymous);
    }

}
