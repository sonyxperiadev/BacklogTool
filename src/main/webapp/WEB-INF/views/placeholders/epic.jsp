<li class="epic ui-state-default editEpic 
    <c:if test='${view.equals("epic-story")}'>
        parentLi 
    </c:if>
    <c:if test='${view.equals("theme-epic")}'>
        childLi 
    </c:if>
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    "
    id="${epic.id}" 
    <c:if test='${view.equals("theme-epic")}'>
        parentid="${epic.theme.id}"
    </c:if>
>
    <div id="icons">
        <c:if test='${view.equals("epic-story")}'>
            <div title="Show stories"
            class="icon 
            <c:if test="${epic.children.size() > 0}">
                expand-icon ui-icon ui-icon-triangle-1-e
            </c:if>
            ">
            </div>
            <a id="${epic.id}" title="Create new story"
                class="icon createStory add-child-icon"></a> 
        </c:if>
        <a id="${epic.id}" title="Clone this epic excluding children" class="cloneItem epic">
            <img src="../resources/image/page_white_copy.png">
        </a>
    </div> 
    <!-- TITLE FIELDS -->
    <c:if test='${view.equals("epic-story")}'>
        <div class="titles-theme-epic">
    </c:if>
    <c:if test='${view.equals("theme-epic")}'>
        <div class="padding-left titles-theme-epic">
    </c:if>
        <!-- TYPE MARK START -->
        <p class="typeMark">
            Epic ${epic.id}
        </p>
        <!-- TYPE MARK END -->
        <!-- THEME START -->
        <c:if test='${view.equals("epic-story")}'>
            <p class="theme ${epic.id}">
                ${epic.themeTitle}
            </p>
            <textarea placeholder="Theme" id="epicTheme${epic.id}"
                class="bindChange theme hidden-edit ${epic.id}" rows="1"
                maxlength="100">${epic.themeTitle}</textarea>
        </c:if>
        <!-- THEME END -->
        <br style="clear: both" />
        <!-- EPIC TITLE START -->
        <p class="titleText ${epic.id}">
            ${epic.title}
        </p>
        <textarea placeholder="Title" id="epicTitle${epic.id}"
            class="bindChange titleText hidden-edit title ${epic.id}"
            rows="1" maxlength="100">${epic.title}</textarea>
        <!-- EPIC TITLE END -->
        <!-- EPIC DESCRIPTION START -->
        <p class="description epic-description ${epic.id}">${epic.descriptionWithLinksAndLineBreaks}</p>
        <textarea placeholder="Description"
            id="epicDescription${epic.id}"
            class="bindChange hidden-edit description ${epic.id}"
            rows="2" maxlength="100000">${epic.description}</textarea>
        <!-- EPIC DESCRIPTION END -->
    </div>
    <!-- TITLE FIELDS END --> 
    <a id="${epic.id}" title="Remove epic"
        class="icon deleteItem delete-icon"></a> 
    <input type="checkbox" class="marginTopBig inline bindChange hidden-edit ${epic.id}"
        id="archiveEpic${epic.id}" 
        <c:if test='${epic.archived}'>
            checked="checked"
        </c:if>
    >
    <p class="title inline hidden-edit ${epic.id}">
        Archive epic
    </p>
    <br>
    <button class="save-button hidden-edit ${epic.id}" title="Save">Save</button>
    <button class="cancelButton hidden-edit ${epic.id}" title="Cancel">Cancel</button>
    <p id="archived-text${epic.id}" class="title ${epic.id}">
        <c:if test='${epic.archived}'>
                Archived
        </c:if>
    </p>
    <p id="date-archived${epic.id}" class="description ${epic.id}">
        <fmt:formatDate value="${epic.dateArchived}" pattern="yyyy-MM-dd" />
    </p>
    <br style="clear: both" />
    <p class="expand-item-p"><a class="expand-item-btn">. . .</a></p>
</li>