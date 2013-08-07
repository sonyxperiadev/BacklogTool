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
<li class="story ui-state-default oneline-li editStory
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    <c:if test='${view.equals("story-task")}'>
        parentLi 
    </c:if>
    <c:if test='${view.equals("epic-story")}'>
        childLi 
    </c:if>
    "
    id="${story.id}"
    <c:if test='${view.equals("epic-story")}'>
        parentid="${story.epic.id}"
    </c:if>
    >

    <c:if test='${view.equals("story-task")}'>
        <div title="Show tasks"
                class="oneline icon <c:if test="${!story.children.isEmpty()}">expand-icon ui-icon ui-icon-triangle-1-e</c:if>">
        </div>
    </c:if>
    <div 
        <c:if test='${view.equals("epic-story")}'>
            class="padding-left"
        </c:if>
    >

        <p class="typeMark oneline">Story ${story.id}</p>

        <p class="title-text oneline">
            <span class="title-span oneline">
                ${story.title}
            </span>
        </p>

        <p class="oneline attr-text story-attr1" 
            <c:if test='${story.storyAttr1 != null}'>
                title="${area.storyAttr1.name}"
            </c:if>
        >
            <c:if test='${story.storyAttr1 != null && story.storyAttr1.iconEnabled}'>
                <img class="attr-icon" src="../resources/image/${story.storyAttr1.icon}" /></c:if> 
                ${story.storyAttr1.name}
        </p>

        <p class="oneline attr-text story-attr2" 
            <c:if test='${story.storyAttr2 != null}'>
                title="${area.storyAttr2.name}"
            </c:if>
        >
            <c:if test='${story.storyAttr2 != null && story.storyAttr2.iconEnabled}'>
                <img class="attr-icon" src="../resources/image/${story.storyAttr2.icon}" /></c:if> 
                ${story.storyAttr2.name}
        </p>

        <p class="oneline attr-text story-attr3" 
            <c:if test='${story.storyAttr3 != null}'>
                title="${area.storyAttr3.name}"
            </c:if>
        >
            <c:if test='${story.storyAttr3 != null && story.storyAttr3.iconEnabled}'>
                <img class="attr-icon" src="../resources/image/${story.storyAttr3.icon}" /></c:if> 
                ${story.storyAttr3.name}
        </p>

        <p class="oneline date-text deadline"
            <c:if test='${story.deadline != null}'>
                title="Deadline"
            </c:if>
        >
            <fmt:formatDate value="${story.deadline}" pattern="yyyy-MM-dd" />
        </p>

        <p class="oneline date-text date-archived" 
            <c:if test='${story.dateArchived != null}'>
                title="Date archived"
            </c:if>
        >
            <fmt:formatDate value="${story.dateArchived}" pattern="yyyy-MM-dd" />
        </p>
    </div>
</li>
