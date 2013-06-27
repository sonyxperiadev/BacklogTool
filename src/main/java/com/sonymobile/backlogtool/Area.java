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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

/**
 * An area can be looked upon as a backlog instance where all themes, epics,
 * stories and tasks belong. Each area has its own setup of story status
 * alternatives, icons and admins/editors.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Cache(usage=READ_WRITE)
@Entity
@Table(name="Areas")
public class Area {

    @Id
    @Column(length=250)
    private String name;

    @ElementCollection
    private Set<String> editors = new HashSet<String>();

    @ElementCollection
    private Set<String> admins = new HashSet<String>();

    @ElementCollection
    private Set<String> editorLDAPGroups = new HashSet<String>();

    @ElementCollection
    private Set<String> adminLDAPGroups = new HashSet<String>();

    @OneToOne(fetch=FetchType.LAZY)
    private Attribute storyAttr1;

    @OneToOne(fetch=FetchType.LAZY)
    private Attribute storyAttr2;

    @OneToOne(fetch=FetchType.LAZY)
    private Attribute storyAttr3;

    @OneToOne(fetch=FetchType.LAZY)
    private Attribute taskAttr1;

    public Area() {}

    /**
     * Constructor for Area. Creates the area with default attribute options
     * (status, effort etc.).
     * @param name the area name to create
     * @param session the Hibernate session to use for creating the area.
     */
    public Area(String name, Session session) {
        this.name = name;

        storyAttr1 = new Attribute("Status");
        storyAttr2 = new Attribute("Effort");
        storyAttr3 = new Attribute("Prio");
        taskAttr1 = new Attribute("Status");

        session.save(storyAttr1);
        session.save(storyAttr2);
        session.save(storyAttr3);
        session.save(taskAttr1);

        AttributeOption attributeValue1 = new AttributeOption("NOT STARTED", "time.png", 1);
        AttributeOption attributeValue2 = new AttributeOption("ONGOING", "hourglass.png", 2);
        AttributeOption attributeValue3 = new AttributeOption("FINISHED", "tick.png", 3);

        AttributeOption attributeValue4 = new AttributeOption("HIGH", "exclamation.png", 1);
        AttributeOption attributeValue5 = new AttributeOption("MEDIUM", "asterisk_orange.png", 2);
        AttributeOption attributeValue6 = new AttributeOption("LOW", "arrow_down.png", 3 );

        AttributeOption attributeValue7 = new AttributeOption("HIGH", "exclamation.png", 1);
        AttributeOption attributeValue8 = new AttributeOption("MEDIUM", "asterisk_orange.png", 2);
        AttributeOption attributeValue9 = new AttributeOption("LOW", "arrow_down.png", 3);

        AttributeOption attributeValue10 = new AttributeOption("NOT STARTED", "time.png", 1);
        AttributeOption attributeValue11 = new AttributeOption("ONGOING", "hourglass.png", 2);
        AttributeOption attributeValue12 = new AttributeOption("FINISHED", "tick.png", 3);

        session.save(attributeValue1);
        session.save(attributeValue2);
        session.save(attributeValue3);
        session.save(attributeValue4);
        session.save(attributeValue5);
        session.save(attributeValue6);
        session.save(attributeValue7);
        session.save(attributeValue8);
        session.save(attributeValue9);
        session.save(attributeValue10);
        session.save(attributeValue11);
        session.save(attributeValue12);

        storyAttr1.addOption(attributeValue1);
        storyAttr1.addOption(attributeValue2);
        storyAttr1.addOption(attributeValue3);

        storyAttr2.addOption(attributeValue4);
        storyAttr2.addOption(attributeValue5);
        storyAttr2.addOption(attributeValue6);

        storyAttr3.addOption(attributeValue7);
        storyAttr3.addOption(attributeValue8);
        storyAttr3.addOption(attributeValue9);

        taskAttr1.addOption(attributeValue10);
        taskAttr1.addOption(attributeValue11);
        taskAttr1.addOption(attributeValue12);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public boolean isAdmin(String username) {
        return admins.contains(username);
    }

    public boolean isEditor(String username) {
        return editors.contains(username);
    }

    public boolean makeAdmin(String username) {
        return admins.add(username);
    }

    public boolean removeAdmin(String username) {
        return admins.remove(username);
    }

    public boolean makeEditor(String username) {
        return editors.add(username);
    }

    public boolean removeEditor(String username) {
        return editors.remove(username);
    }

    @JsonIgnore
    public Set<String> getEditors() {
        return editors;
    }

    @JsonIgnore
    public Set<String> getAdmins() {
        return admins;
    }

    public Attribute getStoryAttr1() {
        return storyAttr1;
    }

    public void setStoryAttr1(Attribute storyAttr1) {
        this.storyAttr1 = storyAttr1;
    }

    public Attribute getStoryAttr2() {
        return storyAttr2;
    }

    public void setStoryAttr2(Attribute storyAttr2) {
        this.storyAttr2 = storyAttr2;
    }

    public Attribute getStoryAttr3() {
        return storyAttr3;
    }

    public void setStoryAttr3(Attribute storyAttr3) {
        this.storyAttr3 = storyAttr3;
    }

    public Attribute getTaskAttr1() {
        return taskAttr1;
    }

    public void setTaskAttr1(Attribute taskAttr1) {
        this.taskAttr1 = taskAttr1;
    }

    public void setEditors(Set<String> editors) {
        this.editors = editors;
    }

    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }
    
}
