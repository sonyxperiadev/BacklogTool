<li class="childLi task ui-state-default editTask ui-hidden" parentId="${task.story.id}" id="${task.id}">
    <!-- TASKTITLE START -->
    <!-- TYPE MARK START -->
    <p class="marginLeft typeMark">Task ${task.id}</p>
    <!-- TYPE MARK END -->
    <div class="taskTitle ${task.id}">
        <p class="taskInfo">${task.titleWithLinksAndLineBreaks}</p>
    </div> 
    <textarea id="taskTitle${task.id}"
        class="taskInfo bindChange taskTitle hidden-edit ${task.id}"
        maxlength="500">${task.title}</textarea> 
    <!-- TASKTITLE END -->
    <!-- TASKOWNER START -->
    <div class="taskOwner ${task.id}" id="taskOwner ${task.id}">
        <p class="taskHeading">
            Owner:
        </p>
        <p class="taskInfo">
            ${task.owner}
        </p>
    </div> 
    <textarea id="taskOwner${task.id}"
        class="taskInfo bindChange taskOwner hidden-edit ${task.id}"
        maxlength="50">${task.owner}</textarea>
    <!-- TASKOWNER END -->
    <!-- STATUS FIELD START -->
    <div class="taskStatus ${task.id}" id="taskTitle${task.id}">
        <p class="taskHeading">
            ${area.taskAttr1.name}:
        </p>
        <p class="taskInfo ${task.id}">
            <c:if test='${task.taskAttr1 != null && task.taskAttr1.iconEnabled}'>
                <img src="../resources/image/${task.taskAttr1.icon}"
                    title="${task.taskAttr1.name}" /> 
            </c:if>
            ${task.taskAttr1.name}
        </p>
    </div> 
    <select id="taskAttr1${task.id}"
        class="bindChange taskInfo taskStatus hidden-edit ${task.id} text ui-widget-content ui-corner-all">
        <option value=""></option>
        <c:forEach var="option" items="${area.taskAttr1.options}">
            <option value="${option.id}">${option.name}</option>
        </c:forEach>
    </select>
    <!-- STATUS FIELD END -->
    <!-- CALULATEDTIME START -->
    <div class="calculatedTime ${task.id}" id="calculatedTime${task.id}">
        <p class="taskHeading">
            Estimated time:
        </p>
        <p class="taskInfo">
            ${task.calculatedTime}
        </p>
    </div> 
    <select id="calculatedTime${task.id}"
        class="taskInfo bindChange calculatedTime hidden-edit ${task.id} text ui-widget-content ui-corner-all">
        <option value="0.5">0.5</option>
        <option value="1">1</option>
        <option value="1.5">1.5</option>
        <option value="2">2</option>
    </select> 
    <!-- CALCULATEDTIME END -->
    <button class="save-button hidden-edit ${task.id}" title="Save">Save</button>
    <button class="cancelButton hidden-edit ${task.id}" title="Cancel">Cancel</button>
    <a id="${task.id}" title="Remove task" class="icon deleteItem delete-icon"></a> 
    <br style="clear: both" />
</li>