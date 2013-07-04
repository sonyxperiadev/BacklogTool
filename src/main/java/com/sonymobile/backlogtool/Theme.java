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
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
@Table(name="Themes")
public class Theme {
    
    public static final int DESCRIPTION_LENGTH = 100000;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    @Column(length=100)
    private String title;

    @Column(length=1000)
    private String description = "";

    private int prio;

    private boolean archived;

    private Date dateArchived;

    @ManyToOne
    private Area area;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="theme")
    @OrderBy("prioInTheme")
    private Set<Epic> children = new HashSet<Epic>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return StringEscapeUtils.escapeHtml(title);
    }

    public void setTitle(String title) {
        this.title = StringEscapeUtils.unescapeHtml(title).trim();
    }

    public String getDescription() {
        return StringEscapeUtils.escapeHtml(description);
    }

    public void setDescription(String description) {
        this.description = StringEscapeUtils.unescapeHtml(description);
    }

    /**
     * @return description where <a>-tags have been added around URLs
     * and newline-chars have been replaced with <br />.
     */
    @JsonIgnore
    public String getDescriptionWithLinksAndLineBreaks() {
        return Util.textAsHtmlLinksAndLineBreaks(getDescription());
    }

    public int getPrio() {
        return prio;
    }

    public void setPrio(int prio) {
        this.prio = prio;
    }

    public Set<Epic> getChildren() {
        return children;
    }

    public void setChildren(Set<Epic> children) {
        this.children = children;
    }

    @JsonIgnore
    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    /**
     * Rebuilds the priority order of the children
     * in case one or more children were removed.
     */
    public void rebuildChildrenOrder() {
        List<Epic> epicList = new ArrayList<Epic>();
        epicList.addAll(children);
        Collections.sort(epicList, new Comparator<Epic>() {

            @Override
            public int compare(Epic epic1, Epic epic2) {
                Integer value1 = new Integer(epic1.getPrioInTheme());
                Integer value2 = new Integer(epic2.getPrioInTheme());
                return value1.compareTo(value2);
            }
        });

        int prioCounter = 1;
        for (Epic epic : epicList) {
            epic.setPrioInTheme(prioCounter++);
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

    public Theme copy(boolean withChildren) {
        Theme copy = new Theme();
        copy.setArchived(archived);
        copy.setArea(area);
        copy.setDateArchived(dateArchived);
        copy.setDescription(description);
        copy.setTitle(title);
        return copy;
    }

}
