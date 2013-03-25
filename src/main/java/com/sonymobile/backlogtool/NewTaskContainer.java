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
 * Container used when creating a new task.
 *
 * @author Fredrik Persson &lt;fredrik5.persson@sonymobile.com&gt;
 * @author Nicklas Nilsson &lt;nicklas4.persson@sonymobile.com&gt;
 */
public class NewTaskContainer extends Task {
    
    /**
     * Keeps track of which backlog item that was selected most recently,
     * so that the new item can be placed after that item.
     */
    private ListItem lastItem;

    private int parentId;

    private String taskAttr1Id;
    
    
    public ListItem getLastItem() {
        return lastItem;
    }

    public void setLastItem(ListItem lastItem) {
        this.lastItem = lastItem;
    }
    

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    @Override
    public int getParentId() {
        return parentId;
    }

    public String getTaskAttr1Id() {
        return taskAttr1Id;
    }

    public void setTaskAttr1Id(String taskAttr1Id) {
        this.taskAttr1Id = taskAttr1Id;
    }

}
