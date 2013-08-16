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

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.Cache;

/**
 * This class represents an backlog item. Backlog items have the following relation:
 * theme - epic - story - task, where theme is the widest item. Each theme can contain
 * several epics and each epic can contain several stories etc.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Cache(usage=READ_WRITE)
@Entity
@Table(name="Stories")
public class Story {
    
    public static final int DESCRIPTION_LENGTH = 100000;
    public static final int MAX_START_NOTES = 10;
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    @Column(length=100)
    private String title;

    @Column(length=DESCRIPTION_LENGTH)
    private String description = "";

    private int prio;

    private int prioInEpic;

    private boolean archived;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="story")
    @OrderBy("prioInStory")
    private Set<Task> children = new HashSet<Task>();
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy="story")
    @OrderBy("created DESC")
    private Set<Note> notes = new HashSet<Note>();
    
    @JoinColumn(name="themeId")
    @ManyToOne
    private Theme theme;

    @JoinColumn(name="epicId")
    @ManyToOne
    private Epic epic;

    private String customerSite = "NONE";

    @Column(length=50)
    private String customer = "";

    private String contributorSite = "NONE";

    @Column(length=50)
    private String contributor = "";

    private Date added;

    private Date deadline;

    private Date dateArchived;

    @ManyToOne
    private AttributeOption storyAttr1;

    @ManyToOne
    private AttributeOption storyAttr2;

    @ManyToOne
    private AttributeOption storyAttr3;

    @ManyToOne
    private Area area;

    /**
     * @return the title
     */
    public String getTitle() {
        return StringEscapeUtils.escapeHtml(title);
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = StringEscapeUtils.unescapeHtml(title);
    }

    /**
     * @return the title
     */
    public int getPrio() {
        return prio;
    }

    /**
     * @param prio the prio to set
     */
    public void setPrio(int prio) {
        this.prio = prio;
    }

    /**
     * Add task to this story.
     */
    public void addTask(Task task) {
        children.add(task);
    }

    /**
     * @return the tasks from this story
     */
    public Set<Task> getChildren() {
        return children;
    }

    /**
     * @param tasks the tasks to set
     */
    public void setChildren(Set<Task> children) {
        this.children = children;
    }

    /**
     * @return The Notes for this Story
     */
    @JsonIgnore
    public Set<Note> getNotes() {
        return notes;
    }

    /**
     * Get the ten newest/latest notes for this Story
     * @return A List with the 10 newest notes
     */
    @JsonIgnore
    public List<Note> getTenNewestNotes() {
        List<Note> list = new ArrayList<Note>();
        if (notes != null) {
            Iterator<Note> itr = notes.iterator();
            int count = 0;
            while (itr.hasNext() && count < MAX_START_NOTES) {
                list.add(itr.next());
                count++;
            }
        }
        return list;
    }

    /**
     * @param notes The Notes to set for this Story
     */
    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    /**
     * Get the last created non-system-generated Note for this Story
     * @return A Note, or null if none are found
     */
    public Note getLatestNonSystemNote() {
        Note latestNote = null;
        if (notes != null) {
            Iterator<Note> itr = notes.iterator();
            Note tmpNote = null;
            while (itr.hasNext() && latestNote == null) {
                tmpNote = itr.next();
                if (!tmpNote.isSystemGenerated()) {
                    latestNote = tmpNote;
                }
            }
        }
        return latestNote;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return StringEscapeUtils.escapeHtml(description);
    }

    /**
     * @return description where <a>-tags have been added around URLs
     * and newline-chars have been replaced with <br />.
     */
    @JsonIgnore
    public String getDescriptionWithLinksAndLineBreaks() {
        return Util.textAsHtmlLinksAndLineBreaks(getDescription());
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = StringEscapeUtils.unescapeHtml(description);
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id for this story. The server uses this one.
     */
    public void setId(int id) {
        this.id = id;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public String getContributorSite() {
        return contributorSite;
    }

    public void setContributorSite(String contributorSite) {
        this.contributorSite = contributorSite;
    }

    public String getCustomerSite() {
        return customerSite;
    }

    public void setCustomerSite(String customerSite) {
        this.customerSite = customerSite;
    }

    /**
     * Adds an integer to the prio value of this task.
     * @param toAdd what to add
     */
    public void addPrio (int toAdd) {
        this.prio = prio + toAdd;
    }

    /**
     * Copies this story to a new story with same attributes
     * @param whether to clone the tasks as well
     * @return copy
     */
    public Story copy(boolean includeTasks) {
        Story copy = new Story();
        copy.setDescription(description);
        copy.setTitle(title);
        copy.setAdded(added);
        copy.setContributorSite(contributorSite);
        copy.setCustomerSite(customerSite);
        copy.setContributor(contributor);
        copy.setCustomer(customer);
        copy.setDeadline(deadline);
        copy.setEpic(epic);
        copy.setTheme(theme);
        copy.setStoryAttr1(storyAttr1);
        copy.setStoryAttr2(storyAttr2);
        copy.setStoryAttr3(storyAttr3);
        copy.setArea(area);
        copy.setArchived(archived);
        copy.setDateArchived(dateArchived);

        if (includeTasks) {
            for (Task task : children) {
                Task copiedTask = task.copy();
                copiedTask.setStory(copy);
                copy.addTask(copiedTask);
            }
        }
        return copy;
    }

    public int getPrioInEpic() {
        return prioInEpic;
    }

    public void setPrioInEpic(int prioInEpic) {
        this.prioInEpic = prioInEpic;
    }

    @JsonIgnore
    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    @JsonIgnore
    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public Integer getEpicId() {
        if (epic == null) {
            return null;
        }
        return epic.getId();
    }

    public String getEpicTitle() {
        if (epic == null) {
            return null;
        }
        return epic.getTitle();
    }

    public Integer getThemeId() {
        if (theme == null) {
            return null;
        }
        return theme.getId();
    }

    public String getThemeTitle() {
        if (theme == null) {
            return null;
        }
        return theme.getTitle();
    }

    @JsonIgnore
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getCustomer() {
        return StringEscapeUtils.escapeHtml(customer);
    }

    public void setCustomer(String customer) {
        this.customer = StringEscapeUtils.unescapeHtml(customer);
    }

    public String getContributor() {
        return StringEscapeUtils.escapeHtml(contributor);
    }

    public void setContributor(String contributor) {
        this.contributor = StringEscapeUtils.unescapeHtml(contributor);
    }

    public AttributeOption getStoryAttr1() {
        return storyAttr1;
    }

    public void setStoryAttr1(AttributeOption storyAttr1) {
        this.storyAttr1 = storyAttr1;
    }

    public AttributeOption getStoryAttr2() {
        return storyAttr2;
    }

    public void setStoryAttr2(AttributeOption storyAttr2) {
        this.storyAttr2 = storyAttr2;
    }

    public AttributeOption getStoryAttr3() {
        return storyAttr3;
    }

    public void setStoryAttr3(AttributeOption storyAttr3) {
        this.storyAttr3 = storyAttr3;
    }

    /**
     * Rebuilds the priority order of the children
     * in case one or more tasks were removed.
     */
    public void rebuildChildrenOrder() {
        List<Task> taskList = new ArrayList<Task>();
        taskList.addAll(children);
        Collections.sort(taskList, new Comparator<Task>() {

            @Override
            public int compare(Task task1, Task task2) {
                Integer value1 = new Integer(task1.getPrioInStory());
                Integer value2 = new Integer(task2.getPrioInStory());
                return value1.compareTo(value2);
            }
        });

        int prioCounter = 1;
        for (Task task : taskList) {
            task.setPrioInStory(prioCounter++);
        }
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Date getDateArchived() {
        return dateArchived;
    }

    public void setDateArchived(Date dateArchived) {
        this.dateArchived = dateArchived;
    }

    /**
     * Returns true if this Story has more than ten notes in total
     * @return True if more than ten notes, otherwise false
     */
    @JsonSerialize
    public boolean hasMoreNotes() {
        return notes != null && notes.size() > MAX_START_NOTES;
    }

}