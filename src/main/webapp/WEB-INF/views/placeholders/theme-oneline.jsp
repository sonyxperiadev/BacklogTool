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
<li class="theme ui-state-default oneline-li editTheme parentLi
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    "
    id="${theme.id}">

    <div title="Show epics"
            class="oneline icon <c:if test="${!theme.children.isEmpty()}">expand-icon ui-icon ui-icon-triangle-1-e</c:if>">
    </div>

    <p class="typeMark oneline">Theme ${theme.id}</p>

    <p class="title-text oneline">
        <span class="title-span oneline">
            ${theme.title}
        </span>
    </p>

    <p class="oneline date-text date-archived" 
        <c:if test='${theme.dateArchived != null}'>
            title="Date archived"
        </c:if>
    >
        <fmt:formatDate value="${theme.dateArchived}" pattern="yyyy-MM-dd" />
    </p>
</li>
