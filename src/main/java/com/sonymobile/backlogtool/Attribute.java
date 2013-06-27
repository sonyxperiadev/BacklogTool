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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.annotations.Cache;

/**
 * An attribute contains information about what the attribute is called
 * and which options it has. One example of an attribute is the story status.
 * Its options can then be "not started" and "ongoing".
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Cache(usage=READ_WRITE)
@Entity
@Table(name = "Attributes")
public class Attribute {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    int id;

    String name;

    @OrderBy(value="compareValue")
    @OneToMany(fetch=FetchType.EAGER)
    Set<AttributeOption> options = new HashSet<AttributeOption>();

    public Attribute() {}

    public Attribute(String name) {
        this.name = name;
    }

    public String getName() {
        return StringEscapeUtils.escapeHtml(name);
    }

    public void setName(String name) {
        this.name = StringEscapeUtils.unescapeHtml(name);
    }

    public Set<AttributeOption> getOptions() {
        return options;
    }

    public void setOptions(Set<AttributeOption> options) {
        this.options = options;
    }

    public void addOption(AttributeOption option) {
        options.add(option);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}

