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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
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
@Table(name="Tasks")
public class Task {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    @Column(length=500)
    private String title;

    private int prioInStory;

    private boolean archived;

    private Date dateArchived;

    @Column(length=50)
    private String owner = "";

    private double calculatedTime = 0.5;

    @JoinColumn (name="storyId")
    @ManyToOne
    private Story story;

    @ManyToOne
    private AttributeOption taskAttr1;

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Task)) {
            return false;
        }
        return this.id == ((Task) other).getId();
    }

    /**
     * @return the title.
     */
    public String getTitle() {
        return StringEscapeUtils.escapeHtml(title);
    }

    /**
     * Sets the title for this task.
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = StringEscapeUtils.unescapeHtml(title);
    }

    /**
     * @return title where <a>-tags have been added around URLs
     * and newline-chars have been replaced with <br />.
     */
    @JsonIgnore
    public String getTitleWithLinksAndLineBreaks() {
        return Util.textAsHtmlLinksAndLineBreaks(getTitle());
    }

    /**
     * Sets the id for this task. The server uses this one.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the priority of this task.
     * Returns this task's current prio.
     */
    public int getPrioInStory() {
        return prioInStory;
    }

    /**
     * Sets this task's prio.
     * @param prio the new priority of this task
     */
    public void setPrioInStory(int prioInStory) {
        this.prioInStory = prioInStory;
    }

    /**
     * Sets this task's story.
     * @param storyId the storyId to set for this task
     */
    public void setStory(Story story) {
        this.story = story;
    }

    /**
     * Returns the parent id for this task.
     * @return returns the tasks parent id
     */
    public int getParentId() {
        return story.getId();
    }

    /**
     * Returns this task's id.
     * @return id the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns this tasks owner.
     * @return owner the owner of this task
     */
    public String getOwner() {
        return StringEscapeUtils.escapeHtml(owner);
    }

    /**
     * Sets a owner for this task.
     * @Param owner the owner to set for this task
     */
    public void setOwner(String owner) {
        this.owner = StringEscapeUtils.unescapeHtml(owner);
    }

    /**
     * Returns the calculated time for this task.
     * @return calculatedTime the calculated time for this task
     */
    public double getCalculatedTime() {
        return calculatedTime;
    }

    /**
     * Sets the calculated time for this task
     * @Param calculatedTime the calculated time for this task.
     */
    public void setCalculatedTime(double calculatedTime) {
        this.calculatedTime = calculatedTime;
    }

    /**
     * Adds an integer to the prioInStory value of this task.
     * @param toAdd what to add
     */
    public void addPrioInStory (int toAdd) {
        this.prioInStory = prioInStory + toAdd;
    }

    public AttributeOption getTaskAttr1() {
        return taskAttr1;
    }

    public void setTaskAttr1(AttributeOption taskAttr1) {
        this.taskAttr1 = taskAttr1;
    }

    public Task copy() {
        Task copy = new Task();
        copy.setArchived(archived);
        copy.setCalculatedTime(calculatedTime);
        copy.setDateArchived(dateArchived);
        copy.setOwner(owner);
        copy.setPrioInStory(prioInStory);
        copy.setStory(story);
        copy.setTaskAttr1(taskAttr1);
        copy.setTitle(title);
        return copy;
    }

    @JsonIgnore
    public Story getStory() {
        return story;
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

}