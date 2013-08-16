<li class="parentLi theme ui-state-default editTheme 
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    " 
    id="${theme.id}"
>
    <div id="icons">
        <div title="Show epics"
        class="icon 
        <c:if test="${theme.children.size() > 0}">
            expand-icon ui-icon ui-icon-triangle-1-e
        </c:if>
        ">
    </div>
    <a id="${theme.id}" title="Create new epic"
        class="icon createEpic add-child-icon"></a>
    <br> 
    <a id="${theme.id}" title="Clone this theme excluding children"
        class="cloneItem theme icon">
        <img src="../resources/image/page_white_copy.png">
    </a>
    </div>
    <!-- TITLE FIELDS -->
    <div class="titles-theme-epic">
        <!-- TYPE MARK START -->
        <p class="typeMark">
            Theme ${theme.id}
        </p>
        <!-- TYPE MARK END --> 
        <br style="clear: both" /> 
        <!-- TITLE START -->
        <p class="titleText ${theme.id}">
            ${theme.title}
        </p>
        <textarea placeholder="Title" id="themeTitle${theme.id}"
            class="bindChange titleText hidden-edit title ${theme.id}"
            rows="1" maxlength="100">${theme.title}</textarea>
        <!-- TITLE END --> 
        <!-- DESCRIPTION START -->
        <p class="description theme-description ${theme.id}">
            ${theme.descriptionWithLinksAndLineBreaks}
        </p>
        <textarea placeholder="Description"
            id="themeDescription${theme.id}"
            class="bindChange hidden-edit description ${theme.id}"
            rows="2" maxlength="100000">${theme.description}</textarea>
        <!-- DESCRIPTION END -->
    </div>
    <!-- TITLE FIELDS END --> 
    <a id="${theme.id}" title="Remove theme"
        class="icon deleteItem delete-icon"></a> 
    <input type="checkbox"
        class="marginTopBig inline bindChange hidden-edit ${theme.id}"
        id="archiveTheme${theme.id}"
        <c:if test='${theme.archived}'>
            checked="checked"
        </c:if>
    >
    <p class="title inline hidden-edit ${theme.id}">
        Archive theme
    </p>
    <br>
    <button class="save-button hidden-edit ${theme.id}" title="Save">Save</button>
    <button class="cancelButton hidden-edit ${theme.id}" title="Cancel">Cancel</button>
    <p id="archived-text${theme.id}" class="title ${theme.id}">
        <c:if test='${theme.archived}'>
                Archived
        </c:if>
    </p>
    <p id="date-archived${theme.id}" class="description ${theme.id}">
        <fmt:formatDate value="${theme.dateArchived}" pattern="yyyy-MM-dd" />
    </p>
    <br style="clear: both" />
    <p class="expand-item-p"><a class="expand-item-btn">. . .</a></p>
</li>