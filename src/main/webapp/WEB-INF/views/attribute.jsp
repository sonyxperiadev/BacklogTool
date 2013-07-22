<ul id="ul${attribute.id}">
    <c:forEach items="${attribute.options}" var="option">
        <c:if test="${option.getClass().simpleName == 'AttributeOption'}">
            <li id="${option.id}">
                <span class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                <div class="inline-block icon-container">
                    <input id="iconEnabled${option.id}"
                        class="checkbox inline-block"
                        type="checkbox" title="Display icon"
                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                        
                    <img class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                        id="icon${option.id}"
                        src="../resources/image/${option.icon}" 
                        icon="${option.icon}" />

                </div>
                <input id="name${option.id}" value="${option.name}"
                    maxlength="15" 
                    class="inline-block attrOptionTitle ui-corner-all">
                <img class="removeOption" src="../resources/image/delete.png" />
            </li>
        </c:if>
        <c:if test="${option.getClass().simpleName == 'AttributeOptionSeries'}">
            <li id="${option.id}" class="series">
                <span class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                <div class="inline-block icon-container">
                    <input id="iconEnabled${option.id}"
                        class="checkbox inline-block"
                        type="checkbox" 
                        title="Display icon"
                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                        
                        <img class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                            id="icon${option.id}"
                            src="../resources/image/${option.icon}" 
                            icon="${option.icon}" />
                        
                </div>
                <input id="name${option.id}" value="${option.name}"
                    title="Optional name" 
                    maxlength="15"
                    class="inline-block attrOptionTitle ui-corner-all">
                <input id="seriesStart${option.id}"
                    title="Series start" 
                    value="${option.seriesStart}"
                    maxlength="3" 
                    type="number"
                    class="inline-block attrOptionSeriesBox ui-corner-all">
                - 
                <input id="seriesEnd${option.id}"
                    title="Series end" value="${option.seriesEnd}"
                    maxlength="3" type="number"
                    class="inline-block attrOptionSeriesBox ui-corner-all">
                <input id="seriesIncrement${option.id}"
                    title="Series increment"
                    value="${option.seriesIncrement}" 
                    maxlength="3"
                    type="number"
                    class="inline-block attrOptionSeriesBox ui-corner-all">
                <img class="removeOption" src="../resources/image/delete.png" />     
            </li>
        </c:if>
    </c:forEach>
</ul>
<button id="${attribute.id}" class="addOption fff">&nbsp Add single element</button>
<button id="${attribute.id}" class="addOptionSeries fff">&nbsp Add series</button>