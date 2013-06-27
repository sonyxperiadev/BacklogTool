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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;

/**
 * An attribute option contains information about what the attribute option is called
 * and which icon it has. One example of an attribute option is "ongoing" as a story status.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
@Cache(usage=READ_WRITE)
@Entity
@Table(name = "AttributeOptions")
public class AttributeOption {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    private String name;
    private String icon;
    private String color;
    private int compareValue;
    private boolean iconEnabled = true;
    private Integer seriesIncrement;

    public AttributeOption() {}

    public AttributeOption(String name, String icon, int compareValue) {
        this.name = name;
        this.icon = icon;
        this.compareValue = compareValue;
    }

    public AttributeOption copy() {
        AttributeOption copy = new AttributeOption();
        copy.name = name;
        copy.icon = icon;
        copy.color = color;
        copy.iconEnabled = iconEnabled;
        copy.seriesIncrement = seriesIncrement;
        return copy;
    } 

    public String getName() {
        return StringEscapeUtils.escapeHtml(name);
    }
    
    /**
     * Gets the name without number if this attribute is part of a number series.
     * @return name
     */
    @JsonIgnore
    public String getNameNoNumber() {
        return getName().replaceAll(" [^ ]+$", "");
    }
    
    /**
     * Gets the number if this attribute is part of a number series.
     * @return number
     */
    @JsonIgnore
    public int getNumber() {
        String[] words = getName().split(" ");
        String lastWord = words[words.length-1];
        try {
            double numberDecimal = Double.parseDouble(lastWord);
            return (int) Math.round(numberDecimal);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public void setName(String name) {
        this.name = StringEscapeUtils.unescapeHtml(name);
    }
    public String getIcon() {
        return StringEscapeUtils.escapeHtml(icon);
    }
    public void setIcon(String icon) {
        this.icon = StringEscapeUtils.unescapeHtml(icon);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(int compareValue) {
        this.compareValue = compareValue;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AttributeOption)) {
            return false;
        }
        return id == ((AttributeOption) other).getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isIconEnabled() {
        return iconEnabled;
    }

    public void setIconEnabled(boolean iconEnabled) {
        this.iconEnabled = iconEnabled;
    }

    public Integer getSeriesIncrement() {
        return seriesIncrement;
    }

    public void setSeriesIncrement(Integer seriesIncrement) {
        this.seriesIncrement = seriesIncrement;
    }

}
