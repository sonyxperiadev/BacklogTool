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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.codehaus.jackson.annotate.JsonContentClass;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Class for handling the drag and drop functionality in the ranked lists.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Controller
@RequestMapping(value="/json")
public class MoveController {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    ServletContext context;

    /**
     * Handles drag and drop in priolist.
     * @param moveContainer sent data from client.
     * Contains which elements that were moved and which element they were placed after.
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/movestory-task/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Set<Story> moveStory(@PathVariable String areaName, @RequestBody MoveContainer moveContainer) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<Story> affectedStories = new ArrayList<Story>();
        Set<Story> parentsToPush = new HashSet<Story>();
        try {
            tx = session.beginTransaction();

            Story lastParent = null;
            Task lastChild = null;
            ListItem lastItem = moveContainer.getLastItem();

            String itemTypes = getTypes(moveContainer);

            if (lastItem.getType() != null) {
                if (lastItem.getType().equals("child")) {
                    lastChild = (Task) session.get(Task.class, lastItem.getId());
                    lastParent = (Story) session.get(Story.class, lastChild.getParentId());
                } else if (lastItem.getType().equals("parent")) {
                    lastParent = (Story) session.get(Story.class, lastItem.getId());
                }
            }
            // Maps a parents id to its prio
            HashMap<Integer, Integer> movedParentsPrio = new HashMap<Integer, Integer>();
            if (itemTypes.equals("child")) {
                if (lastParent == null) {
                    //The children were placed first in list, don't do anything
                    return parentsToPush;
                }

                //Get all moved children from db.
                List<Task> movedChildren = new ArrayList<Task>();
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Task movedChild = (Task) session.get(Task.class, movedItem.getId());
                    if (!movedChild.getStory().getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedChildren.add(movedChild);
                }

                Set<Task> existingChildren = lastParent.getChildren();
                int prioCounter = 1;

                if (lastChild == null) {
                    //The moved children will be placed first in the
                    //parent and all other children moved down
                    for (Task child : movedChildren) {
                        child.setPrioInStory(prioCounter++);
                    }
                    for (Task child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInStory(prioCounter++);
                        }
                    }
                } else {
                    //The moved children will get a specific position
                    //in the parent (after lastChild)
                    for (Task child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInStory(prioCounter++);
                        }
                        if (child == lastChild) {
                            for (Task movedChild : movedChildren) {
                                movedChild.setPrioInStory(prioCounter++);
                            }
                        }
                    }
                }

                Set<Story> oldParents = new HashSet<Story>();
                parentsToPush.add(lastParent);
                affectedStories.add(lastParent);
                //Move all tasks to the new parent
                for (Task child : movedChildren) {
                    Story oldParent = child.getStory();
                    oldParent.getChildren().remove(child);
                    lastParent.getChildren().add(child);
                    child.setStory(lastParent);
                    oldParents.add(oldParent);
                    parentsToPush.add(oldParent);
                }

                for (Story oldStory : oldParents) {
                    oldStory.rebuildChildrenOrder();
                }

            } else if (itemTypes.equals("parent")) {
                //Get all moved parents from db.
                List<Story> movedParents = new ArrayList<Story>();
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Story movedParent = (Story) session.get(Story.class, movedItem.getId());
                    if (!movedParent.getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedParents.add(movedParent);
                }

                //Get all parents
                Query query = session.createQuery("from Story where area.name like ? and archived=false order by prio");
                query.setParameter(0, areaName);
                List<Story> allParents = Util.castList(Story.class, query.list());

                int prioCounter = 1;

                if (lastParent == null) {
                    //The moved parents will be placed first and all other moved down
                    for (Story parent : movedParents) {
                        parent.setPrio(prioCounter++);
                    }
                    for (Story parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                    }
                } else {
                    //The moved parents will be placed after lastParent
                    for (Story parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                        if (parent == lastParent) {
                            for (Story movedParent : movedParents) {
                                movedParent.setPrio(prioCounter++);
                            }
                        }
                    }
                }

                for (Story parent : allParents) {
                    movedParentsPrio.put(parent.getId(), parent.getPrio());
                }

            }
            HashMap<String, Object> moveActionMap = new HashMap<String, Object>();
            moveActionMap.put("lastItem", lastItem);
            if (itemTypes.equals("child")) {
                moveActionMap.put("objects", parentsToPush);
            } else {
                moveActionMap.put("objects", movedParentsPrio);
            }
            String jsonString = JSONController.getJsonStringInclChildren(itemTypes + "Move", moveActionMap, JSONController.STORY_TASK_VIEW);

            tx.commit();
            AtmosphereHandler.push(areaName, jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }

        return parentsToPush;
    }

    /**
     * Handles drag and drop in priolist.
     * @param moveContainer sent data from client.
     * Contains which elements that were moved and which element they were placed after.
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/moveepic-story/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Set<Epic> moveEpic(@PathVariable String areaName, @RequestBody MoveContainer moveContainer) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Set<Epic> parentsToPush = new HashSet<Epic>();
        try {
            tx = session.beginTransaction();

            Epic lastParent = null;
            Story lastChild = null;
            ListItem lastItem = moveContainer.getLastItem();

            String itemTypes = getTypes(moveContainer);

            if (lastItem.getType() != null) {
                if (lastItem.getType().equals("child")) {
                    lastChild = (Story) session.get(Story.class, lastItem.getId());
                    lastParent = (Epic) session.get(Epic.class, lastChild.getEpicId());
                } else if (lastItem.getType().equals("parent")) {
                    lastParent = (Epic) session.get(Epic.class, lastItem.getId());
                }
            }
            // Maps a parents id to its prio
            HashMap<Integer, Integer> movedParentsPrio = new HashMap<Integer, Integer>();
            List<Story> movedChildren = new ArrayList<Story>();
            if (itemTypes.equals("child")) {
                if (lastParent == null) {
                    //The children were placed first in list, don't do anything
                    return parentsToPush;
                }

                //Get all moved children from db.
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Story movedChild = (Story) session.get(Story.class, movedItem.getId());
                    if (!movedChild.getEpic().getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedChildren.add(movedChild);
                }

                Set<Story> existingChildren = lastParent.getChildren();
                int prioCounter = 1;

                if (lastChild == null) {
                    //The moved children will be placed first in the
                    //parent and all other children moved down
                    for (Story child : movedChildren) {
                        child.setPrioInEpic(prioCounter++);
                    }
                    for (Story child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInEpic(prioCounter++);
                        }
                    }
                } else {
                    //The moved children will get a specific position
                    //in the parent (after lastChild)
                    for (Story child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInEpic(prioCounter++);
                        }
                        if (child == lastChild) {
                            for (Story movedChild : movedChildren) {
                                movedChild.setPrioInEpic(prioCounter++);
                            }
                        }
                    }
                }

                Set<Epic> oldParents = new HashSet<Epic>();
                parentsToPush.add(lastParent);
                //Move all tasks to the new parent
                for (Story child : movedChildren) {
                    Epic oldParent = child.getEpic();
                    oldParent.getChildren().remove(child);
                    lastParent.getChildren().add(child);
                    child.setEpic(lastParent);
                    child.setTheme(lastParent.getTheme());
                    oldParents.add(oldParent);
                    parentsToPush.add(oldParent);
                }

                for (Epic oldEpic : oldParents) {
                    oldEpic.rebuildChildrenOrder();
                }

            } else if (itemTypes.equals("parent")) {
                //Get all moved parents from db.
                List<Epic> movedParents = new ArrayList<Epic>();
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Epic movedParent = (Epic) session.get(Epic.class, movedItem.getId());
                    if (!movedParent.getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedParents.add(movedParent);
                }

                //Get all parents
                Query query = session.createQuery("from Epic where area.name like ? and archived=false order by prio");
                query.setParameter(0, areaName);
                List<Epic> allParents = Util.castList(Epic.class, query.list());

                int prioCounter = 1;

                if (lastParent == null) {
                    //The moved parents will be placed first and all other moved down
                    for (Epic parent : movedParents) {
                        parent.setPrio(prioCounter++);
                    }
                    for (Epic parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                    }
                } else {
                    //The moved parents will be placed after lastParent
                    for (Epic parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                        if (parent == lastParent) {
                            for (Epic movedParent : movedParents) {
                                movedParent.setPrio(prioCounter++);
                            }
                        }
                    }
                }

                for (Epic parent : allParents) {
                    movedParentsPrio.put(parent.getId(), parent.getPrio());
                }
            }

            List<String> messages = new ArrayList<String>();
            HashMap<String, Object> moveActionMap = new HashMap<String, Object>();
            moveActionMap.put("lastItem", lastItem);
            if (itemTypes.equals("child")) {
                moveActionMap.put("objects", parentsToPush);
                
                for (Story s : movedChildren) {
                    messages.add(JSONController.getJsonStringExclChildren(Story.class, s, JSONController.STORY_TASK_VIEW));
                }
            } else {
                moveActionMap.put("objects", movedParentsPrio);
            }
            messages.add(JSONController.getJsonStringInclChildren(itemTypes + "Move", moveActionMap, JSONController.EPIC_STORY_VIEW));
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

        return parentsToPush;
    }

    /**
     * Handles drag and drop in priolist.
     * @param moveContainer sent data from client.
     * Contains which elements that were moved and which element they were placed after.
     * @return true if everything was ok
     */
    @PreAuthorize("hasPermission(#areaName, 'isEditor')")
    @RequestMapping(value="/movetheme-epic/{areaName}", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody Set<Theme> moveTheme(@PathVariable String areaName, @RequestBody MoveContainer moveContainer) throws Exception {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Set<Theme> parentsToPush = new HashSet<Theme>();
        try {
            tx = session.beginTransaction();

            Theme lastParent = null;
            Epic lastChild = null;
            ListItem lastItem = moveContainer.getLastItem();

            String itemTypes = getTypes(moveContainer);

            if (lastItem.getType() != null) {
                if (lastItem.getType().equals("child")) {
                    lastChild = (Epic) session.get(Epic.class, lastItem.getId());
                    lastParent = (Theme) session.get(Theme.class, lastChild.getThemeId());
                } else if (lastItem.getType().equals("parent")) {
                    lastParent = (Theme) session.get(Theme.class, lastItem.getId());
                }
            }
            // Maps a parents id to its prio
            HashMap<Integer, Integer> movedParentsPrio = new HashMap<Integer, Integer>();
            List<Epic> movedChildren = new ArrayList<Epic>();
            if (itemTypes.equals("child")) {
                if (lastParent == null) {
                    //The children were placed first in list, don't do anything
                    return parentsToPush;
                }

                //Get all moved children from db.
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Epic movedChild = (Epic) session.get(Epic.class, movedItem.getId());
                    if (!movedChild.getTheme().getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedChildren.add(movedChild);
                }

                Set<Epic> existingChildren = lastParent.getChildren();
                int prioCounter = 1;

                if (lastChild == null) {
                    //The moved children will be placed first in the
                    //parent and all other children moved down
                    for (Epic child : movedChildren) {
                        child.setPrioInTheme(prioCounter++);
                    }
                    for (Epic child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInTheme(prioCounter++);
                        }
                    }
                } else {
                    //The moved children will get a specific position
                    //in the parent (after lastChild)
                    for (Epic child : existingChildren) {
                        if (!movedChildren.contains(child)) {
                            child.setPrioInTheme(prioCounter++);
                        }
                        if (child == lastChild) {
                            for (Epic movedChild : movedChildren) {
                                movedChild.setPrioInTheme(prioCounter++);
                            }
                        }
                    }
                }

                Set<Theme> oldParents = new HashSet<Theme>();
                parentsToPush.add(lastParent);
                //Move all children to the new parent
                for (Epic child : movedChildren) {
                    Theme oldParent = child.getTheme();
                    oldParent.getChildren().remove(child);
                    lastParent.getChildren().add(child);
                    child.setTheme(lastParent);
                    for (Story grandChild : child.getChildren()) {
                        grandChild.setTheme(lastParent);
                    }
                    oldParents.add(oldParent);
                    parentsToPush.add(oldParent);
                }

                for (Theme oldTheme : oldParents) {
                    oldTheme.rebuildChildrenOrder();
                }

            } else if (itemTypes.equals("parent")) {
                //Get all moved parents from db.
                List<Theme> movedParents = new ArrayList<Theme>();
                for (ListItem movedItem : moveContainer.getMovedItems()) {
                    Theme movedParent = (Theme) session.get(Theme.class, movedItem.getId());
                    if (!movedParent.getArea().getName().equals(areaName)) {
                        throw new Exception("Trying to modify unauthorized object!");
                    }
                    movedParents.add(movedParent);
                }

                //Get all parents
                Query query = session.createQuery("from Theme where area.name like ? and archived=false order by prio");
                query.setParameter(0, areaName);
                List<Theme> allParents = Util.castList(Theme.class, query.list());

                int prioCounter = 1;

                if (lastParent == null) {
                    //The moved parents will be placed first and all other moved down
                    for (Theme parent : movedParents) {
                        parent.setPrio(prioCounter++);
                    }
                    for (Theme parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                    }
                } else {
                    //The moved parents will be placed after lastParent
                    for (Theme parent : allParents) {
                        if (!movedParents.contains(parent)) {
                            parent.setPrio(prioCounter++);
                        }
                        if (parent == lastParent) {
                            for (Theme movedParent : movedParents) {
                                movedParent.setPrio(prioCounter++);
                            }
                        }
                    }
                }

                for (Theme parent : allParents) {
                    movedParentsPrio.put(parent.getId(), parent.getPrio());
                }
            }

            List<String> messages = new ArrayList<String>();
            HashMap<String, Object> moveActionMap = new HashMap<String, Object>();
            moveActionMap.put("lastItem", lastItem);
            if (itemTypes.equals("child")) {
                moveActionMap.put("objects", parentsToPush);

                for (Epic e : movedChildren) {
                    messages.add(JSONController.getJsonStringExclChildren(Epic.class, e, JSONController.EPIC_STORY_VIEW));
                }
            } else {
                moveActionMap.put("objects", movedParentsPrio);
            }
            messages.add(JSONController.getJsonStringInclChildren(itemTypes + "Move", moveActionMap, JSONController.THEME_EPIC_VIEW));

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

        return parentsToPush;
    }

    /**
     * Checks that all items in moveContainer are of same type and returns the type.
     * @param moveContainer
     * @throws Exception if different types
     */
    public String getTypes(MoveContainer moveContainer) throws Exception {
        String itemTypes = moveContainer.getMovedItems().get(0).getType();
        for (ListItem item : moveContainer.getMovedItems()) {
            String currentType = item.getType();
            if (!currentType.equals(itemTypes)) {
                throw new Exception("Invalid moved items - cannot be several types!");
            }
        }
        return itemTypes;
    }

}