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

/**
 * Container used when creating a new theme.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
public class NewThemeContainer extends Theme {
    
    /**
     * Keeps track of which backlog item that was selected most recently,
     * so that the new item can be placed after that item.
     */
    private ListItem lastItem;
    
    
    public ListItem getLastItem() {
        return lastItem;
    }

    public void setLastItem(ListItem lastItem) {
        this.lastItem = lastItem;
    }

    /**
     * Copies all values from the specified Theme to this NewThemeContainer,
     * including IDs, priorities and children (and their IDs and priorities)
     * @param t The Theme to copy the values from
     */
    public void fromTheme(Theme t) {
        setArchived(t.isArchived());
        setArea(t.getArea());
        setDateArchived(t.getDateArchived());
        setDescription(t.getDescription());
        setTitle(t.getTitle());

        setPrio(t.getPrio());
        setId(t.getId());

        for (Epic e : t.getChildren()) {
            Epic copiedEpic = e.copy(false);
            copiedEpic.setTheme(this);
            copiedEpic.setPrioInTheme(e.getPrioInTheme());
            copiedEpic.setId(e.getId());
            getChildren().add(copiedEpic);
        }
    }
}
