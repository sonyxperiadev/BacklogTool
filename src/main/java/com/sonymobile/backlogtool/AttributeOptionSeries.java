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
 * AttributeOptionSeries extends AttributeOption with information
 * about the start and end of the series.
 * 
 * @author Fredrik Persson &lt;fredrik6.persson@sonymobile.com&gt;
 */
public class AttributeOptionSeries extends AttributeOption {
    private int seriesStart;
    private int seriesEnd;
    
    public AttributeOptionSeries() {}
    
    public AttributeOptionSeries(int id, String name, String icon, boolean iconEnabled, int compareValue, 
            int seriesStart, int seriesEnd, int seriesIncrement) {
        super(name, icon, compareValue);
        this.seriesStart = seriesStart;
        this.seriesEnd = seriesEnd;
        setSeriesIncrement(seriesIncrement);
        setId(id);
        setIconEnabled(iconEnabled);
    }

    public Integer getSeriesStart() {
        return seriesStart;
    }

    public void setSeriesStart(Integer seriesStart) {
        this.seriesStart = seriesStart;
    }

    public Integer getSeriesEnd() {
        return seriesEnd;
    }

    public void setSeriesEnd(Integer seriesEnd) {
        this.seriesEnd = seriesEnd;
    }

    public void setSeriesStart(int seriesStart) {
        this.seriesStart = seriesStart;
    }

    public void setSeriesEnd(int seriesEnd) {
        this.seriesEnd = seriesEnd;
    }

}
