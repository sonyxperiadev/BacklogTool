<%--
    The MIT License

    Copyright 2014 Sony Mobile Communications AB. All rights reserved.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
 --%>
<li id="${story.id}" class="ui-state-default status-item story parentLi">

    <c:choose>
        <c:when test="${fn:length(story.children) == 0}">
            <div class="oneline board-expand-icon ui-icon ui-icon-blank"></div>
        </c:when>
        <c:otherwise>
            <div title="Show tabsks"
                class="oneline board-expand-icon ui-icon ui-icon-triangle-1-e"></div>
        </c:otherwise>
    </c:choose>
    <a href="../story-task/${area.name}?ids=${story.id}" target="_blank" class="board-story board-title">
        ${story.title}
    </a>
</li>