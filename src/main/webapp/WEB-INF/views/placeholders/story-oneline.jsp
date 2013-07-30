<li class="story ui-state-default oneline-li parentLi" id="${story.id}">

    <div title="Show tasks"
            class="oneline icon <c:if test="${story.children.size() > 0}">expand-icon ui-icon ui-icon-triangle-1-e</c:if>">
    </div>

    <p class="typeMark oneline">Story ${story.id}</p>
    <p class="title-text oneline">${story.title}</p>

    <p class="oneline attr-text story-attr1">
        <c:if test='${story.storyAttr1 != null && story.storyAttr1.iconEnabled}'>
            <img class="attr-icon" src="../resources/image/${story.storyAttr1.icon}"
                title="${story.storyAttr1.name}" /></c:if> 
                ${story.storyAttr1.name}
    </p>

    <p class="oneline attr-text story-attr2">
        <c:if test='${story.storyAttr2 != null && story.storyAttr2.iconEnabled}'>
            <img class="attr-icon" src="../resources/image/${story.storyAttr2.icon}"
                title="${story.storyAttr2.name}" /></c:if> 
            ${story.storyAttr2.name}
    </p>


    <p class="oneline attr-text story-attr3">
        <c:if test='${story.storyAttr3 != null && story.storyAttr3.iconEnabled}'>
            <img class="attr-icon" src="../resources/image/${story.storyAttr3.icon}"
                title="${story.storyAttr3.name}" /></c:if> 
            ${story.storyAttr3.name}
    </p>

    <p class="oneline date-text">
        <fmt:formatDate value="${story.deadline}" pattern="yyyy-MM-dd" />
    </p>
    <c:if test='${story.archived}'>
        <p class="title oneline">Archived:</p>
    </c:if>
    <p class="oneline">
        <fmt:formatDate value="${story.dateArchived}" pattern="yyyy-MM-dd" />
    </p>
</li>
