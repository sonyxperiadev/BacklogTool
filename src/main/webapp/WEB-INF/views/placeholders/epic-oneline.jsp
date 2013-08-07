<%--
    The MIT License

    Copyright 2013 Sony Mobile Communications AB. All rights reserved.

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
<li class="epic ui-state-default oneline-li editEpic
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    <c:if test='${view.equals("epic-story")}'>
        parentLi 
    </c:if>
    <c:if test='${view.equals("theme-epic")}'>
        childLi 
    </c:if>
    "
    id="${epic.id}"
    <c:if test='${view.equals("theme-epic")}'>
        parentid="${epic.theme.id}"
    </c:if>
    >

        <c:if test='${view.equals("epic-story")}'>
        <div title="Show stories"
                class="oneline icon <c:if test="${!epic.children.isEmpty()}">expand-icon ui-icon ui-icon-triangle-1-e</c:if>">
        </div>
    </c:if>
    <div 
        <c:if test='${view.equals("theme-epic")}'>
            class="padding-left"
        </c:if>
    >

        <p class="typeMark oneline">Epic ${epic.id}</p>

        <p class="title-text oneline">
            <span class="title-span oneline">
                ${epic.title}
            </span>
        </p>

        <p class="oneline date-text date-archived" 
            <c:if test='${epic.dateArchived != null}'>
                title="Date archived"
            </c:if>
        >
            <fmt:formatDate value="${epic.dateArchived}" pattern="yyyy-MM-dd" />
        </p>
    </div>
</li>
