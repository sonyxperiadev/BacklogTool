<li class="story ui-state-default editStory 
    <c:if test='${view.equals("story-task")}'>
        parentLi 
    </c:if>
    <c:if test='${view.equals("epic-story")}'>
        childLi 
    </c:if>
    <c:if test='${hidden}'>
        ui-hidden
    </c:if>
    " 
    id="${story.id}" 
    <c:if test='${view.equals("epic-story")}'>
        parentid="${story.epic.id}"
    </c:if>
>
    <div id="icons">
        <c:if test='${view.equals("story-task")}'>
            <div title="Show tasks"
                class="icon 
                <c:if test="${story.children.size() > 0}">
                    expand-icon ui-icon ui-icon-triangle-1-e
                </c:if>">
            </div>
            <a id="${story.id}" title="Create new task"
                class="icon createTask add-child-icon"></a>
            <br> 
        </c:if>
        <a id="${story.id}" title="Clone this story excluding tasks"
            class="cloneItem story"> 
            <img src="../resources/image/page_white_copy.png">
        </a> 
        <c:if test='${view.equals("story-task")}'>
            <a id="${story.id}" title="Clone this story including tasks"
                class="cloneItem-with-children story"> <img src="../resources/image/page_white_stack.png"></a>
        </c:if>
    </div>
    <!--  TITLE FIELDS -->
    <c:if test='${view.equals("story-task")}'>
        <div class="titles">
    </c:if>
    <c:if test='${view.equals("epic-story")}'>
        <div class="padding-left titles-epic-story">
    </c:if>
        <!-- TYPE MARK START -->
        <p class="typeMark">
            Story ${story.id}
        </p>
        <!-- TYPE MARK END -->
        <!-- THEME START -->
        <p class="theme ${story.id}">
            ${story.themeTitle}
        </p>
        <textarea placeholder="Theme" id="theme${story.id}"
            class="bindChange theme hidden-edit ${story.id}" rows="1"
            maxlength="100">${story.themeTitle}</textarea>
        <!-- THEME END -->
        <!-- EPIC START -->
        <p class="epic ${story.id}">
            ${story.epicTitle}
        </p>
        <textarea placeholder="Epic" id="epic${story.id}"
            class="bindChange epic hidden-edit ${story.id}" rows="1"
            maxlength="100">${story.epicTitle}</textarea>
        <!-- EPIC END -->
        <br style="clear: both" />
        <!-- STORY TITLE START -->
        <p class="titleText ${story.id}">
            ${story.title}
        </p>
        <textarea placeholder="Title" id="title${story.id}"
            class="bindChange titleText hidden-edit title ${story.id}"
            rows="1" maxlength="100">${story.title}</textarea>
        <!-- STORY TITLE END -->
        <!-- STORYDESCRIPTION START -->
        <p class="description story-description ${story.id}">
            ${story.descriptionWithLinksAndLineBreaks}
        </p>
        <textarea placeholder="Description" id="description${story.id}"
            class="bindChange hidden-edit description ${story.id}"
            rows="2" maxlength="100000">${story.description}</textarea>
        <!-- STORYDESCRIPTION END -->
    </div>
    <!-- TITLE FIELDS END -->

    <div class="story-right-content">
            <!-- STAKEHOLDER DIV START -->
        <div class="stakeholders">
            <!-- CUSTOMER FIELD START -->
            <p class="title">
                Customer
            </p>
            <p class="customerSite ${story.id}">
                <c:if test='${story.customerSite != null && !story.customerSite.equals("NONE")}'>
                    <img
                        src="../resources/css/ui-lightness/images/${story.customerSite}.png"
                        title="${story.customerSite}"
                        alt="${story.customerSite}" />
                </c:if>
            </p>
            <p class="${story.id} customer description">
                ${story.customer}&nbsp;<!-- &nbsp; Forces p-tag to have a height of one line -->
            </p>
            <select id="customerSite${story.id}"
                class="bindChange customerSite hidden-edit ${story.id} text ui-widget-content ui-corner-all">
                <option value="NONE"></option>
                <option value="Beijing">Beijing</option>
                <option value="Tokyo">Tokyo</option>
                <option value="Lund">Lund</option>
            </select> 
            <input placeholder="Department" id="customer${story.id}"
                class="bindChange customer hidden-edit ${story.id} text ui-widget-content ui-corner-all"
                maxlength="50" value="${story.customer}"></input>
            <!-- CUSTOMER FIELD END -->
            <!-- CONTRIBUTOR FIELD START -->
            <p class="title">
                Contributor
            </p>
            <p id="${story.id}" class="contributorSite ${story.id}">
                <c:if test='${story.contributorSite != null && !story.contributorSite.equals("NONE")}'>
                    <img
                        src="../resources/css/ui-lightness/images/${story.contributorSite}.png"
                        title="${story.contributorSite}"
                        alt="${story.contributorSite}" />
                </c:if>
            </p>
            <p class="${story.id} contributor description">${story.contributor}&nbsp;</p><!-- &nbsp; Forces p-tag to have a height of one line -->
            <select id="contributorSite${story.id}"
                class="bindChange contributorSite hidden-edit ${story.id} text ui-widget-content ui-corner-all">
                <option value="NONE"></option>
                <option value="Beijing">Beijing</option>
                <option value="Tokyo">Tokyo</option>
                <option value="Lund">Lund</option>
            </select> 
            <input placeholder="Department" id="contributor${story.id}"
                class="bindChange contributor hidden-edit ${story.id} text ui-widget-content ui-corner-all"
                maxlength="50" value="${story.contributor}"></input>
            <!-- CONTRIBUTOR FIELD END -->
        </div>
        <!-- STAKEHOLDER DIV END -->
        <!-- TIME FIELDS START -->
        <div class="times">
            <p class="title">
                Deadline
            </p>
            <p class="deadline description ${story.id}">
                <fmt:formatDate value="${story.deadline}" pattern="yyyy-MM-dd" />
            </p>
            <input id="deadline${story.id}" type="text"
                class="bindChange deadline hidden-edit ${story.id} text ui-widget-content ui-corner-all">
            <p class="title">Added</p>
            <p class="added description ${story.id}">
                <fmt:formatDate value="${story.added}" pattern="yyyy-MM-dd" />
            </p>
            <input id="added${story.id}" type="text"
                class="bindChange added hidden-edit ${story.id} text ui-widget-content ui-corner-all">
        </div>
        <!-- TIME FIELDS END -->
        <!-- ATTR1 AND ATTR2 DIV START -->
        <div class="story-attr1-2">
            <!-- ATTR1 FIELD START -->
            <p class="title">${area.storyAttr1.name}</p>
            <p class="description story-attr1 ${story.id}">
                <c:if test='${story.storyAttr1 != null && story.storyAttr1.iconEnabled}'>
                    <img src="../resources/image/${story.storyAttr1.icon}"
                        title="${story.storyAttr1.name}" /> 
                </c:if>
                ${story.storyAttr1.name}&nbsp;
            </p>
            <select id="storyAttr1${story.id}"
                class="bindChange story-attr1 hidden-edit ${story.id} text ui-widget-content ui-corner-all">
                <option value=""></option>
                <c:forEach var="option" items="${area.storyAttr1.options}">
                    <option value="${option.id}">${option.name}</option>
                </c:forEach>
            </select>
            <!-- ATTR1 FIELD END -->
            <!-- ATTR2 FIELD START -->
            <p class="title">
                ${area.storyAttr2.name}
            </p>
            <p class="description story-attr2 ${story.id}">
                <c:if test='${story.storyAttr2 != null && story.storyAttr2.iconEnabled}'>
                    <img src="../resources/image/${story.storyAttr2.icon}"
                        title="${story.storyAttr2.name}" /> 
                 </c:if>
                 ${story.storyAttr2.name}&nbsp;
            </p>
            <select id="storyAttr2${story.id}"
                class="bindChange story-attr2 hidden-edit ${story.id} text ui-widget-content ui-corner-all">
                <option value=""></option>
                <c:forEach var="option" items="${area.storyAttr2.options}">
                    <option value="${option.id}">${option.name}</option>
                </c:forEach>
            </select>
            <!-- ATTR2 FIELD END -->
        </div>
        <!-- ATTR1 AND ATTR2 DIV END -->
        <!-- ATTR3 DIV START -->
        <div class="story-attr3">
            <p class="title">${area.storyAttr3.name}</p>
            <p class="description story-attr3 ${story.id}">
                <c:if test='${story.storyAttr3 != null && story.storyAttr3.iconEnabled}'>
                    <img src="../resources/image/${story.storyAttr3.icon}"
                        title="${story.storyAttr3.name}" /> 
                </c:if>
                ${story.storyAttr3.name}&nbsp;
            </p>
            <select id="storyAttr3${story.id}"
                class="bindChange story-attr3 hidden-edit ${story.id} text ui-widget-content ui-corner-all">
                <option value=""></option>
                <c:forEach var="option" items="${area.storyAttr3.options}">
                    <option value="${option.id}">${option.name}</option>
                </c:forEach>
            </select> 
            <input type="checkbox"
                class="inline bindChange hidden-edit ${story.id}"
                id="archiveStory${story.id}"
                <c:if test='${story.archived}'>
                    checked="checked"
                </c:if>
            >
            <p class="title inline hidden-edit ${story.id}">
                Archive story
            </p>
            <button
                class="inline marginTop save-button hidden-edit ${story.id}"
                title="Save">Save</button>
            <button
                class="inline marginTop cancelButton hidden-edit ${story.id}"
                title="Cancel">Cancel</button>
            <p id="archived-text${story.id}" class="title ${story.id}">
                <c:if test='${story.isArchived()}'>
                    Archived
                </c:if>
            </p>
            <p id="date-archived${story.id}" class="description ${story.id}">
                <fmt:formatDate value="${story.dateArchived}" pattern="yyyy-MM-dd" />
            </p>
        </div>
        <br style="clear: both" />

        <c:if test='${view.equals("story-task")}'>
            <div class="notes-container">
                <div id="notes-form-${story.id}" class="notes-form ui-hidden">
                    <textarea placeholder="Note - Press enter to post" id="notes-textarea-${story.id}"
                    class="note-textarea"
                    rows="2" maxlength="1000"></textarea>
                    <br style="clear: both;" />
                </div>
                <ul></ul>
                <p class="more-notes-loader-p ui-hidden">
                    <a class="more-notes-loader note-link ui-state-disabled">Load older notes</a>
                    <label class="sys-msgs-label">
                        <span>Show system messages</span>
                        <input type="checkbox" class="show-sys-msgs" />
                    </label>
                </p>
            </div>
        </c:if>
    </div>
        <!-- ATTR3 FIELD END -->
        <a id="${story.id}" title="Remove story"
            class="icon deleteItem delete-icon" style="float: right;"></a> 
    <br style="clear: both" />
    <p class="expand-item-p"><a class="expand-item-btn">. . .</a></p>
</li>
