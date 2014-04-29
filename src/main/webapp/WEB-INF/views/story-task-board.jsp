<!--
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
 -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page session="false"%>
<!DOCTYPE html>
<html>
<head>
    <title>Backlog Tool - ${area.name}</title>
    <script type="text/javascript">
    	var loggedIn = "${isLoggedIn}" == "true" ? true : false;
        var areaName = "${area.name}";
        var view = "${view}";
        var archivedView = "${archivedView}" == "true" ? true : false;
        var disableEditsBoolean = "${disableEdits}" == "true" ? true : false;
        var parentsMap = ${jsonDataNonArchivedStories};
        var area = ${jsonAreaData};
        var nbrOfColumns = ${statuses.size()};
    </script>
    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.10.3.custom.min.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/styles.css?v=${versionNoDots}" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/jquery.dropdown.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/simplePagination.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/fff-silk.min.css" />"></link>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-2.0.1.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.blockUI-2.61.0.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-ui-1.10.3.custom.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.autosize-min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/scripts.js?v=${versionNoDots}" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/board-scripts.js?v=${versionNoDots}" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.truncator.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.dropdown.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.atmosphere-2.1.4.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.simplePagination.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.dotdotdot.min.js" />"></script>
</head>
<body>
    <div id="wrap">
        <div id="header" class="initial-fixed-height">
            <c:import url="header.jsp" />
        </div>
        <div id="main">
            <div id="story-placeholder" class="placeholder" >
                <c:set var="story" value="${placeholderStory}"/>
                <%@ include file="/WEB-INF/views/placeholders/story-board.jsp"%>
            </div>
        
            <div id="table-container-div">
                <br>
                    <table id="status-table">
                        <tr id="status-names">
                            <c:forEach var="currentStatus" items="${statuses}">
                                <td class="status-td">
                                <div>
                                    <c:if test='${currentStatus.icon != null && currentStatus.iconEnabled}'>
                                        <img class="attr-icon left" src="../resources/image/${currentStatus.icon}" />
                                    </c:if> 
                                    <p class="left">&nbsp;${currentStatus.name}</p>
                                    <a class="right red-cross" title="Hide column">X</p>
                                </div>
                                </td>
                            </c:forEach>              
                        </tr>
                        <tr id="lists">
                            <c:forEach var="currentStatus" items="${statuses}">
                                <td>
                                    <ul id="${currentStatus.id > 0 ? currentStatus.id : 'null'}" class="status-list">
                                        <c:forEach var="story" items="${storiesByStatusId[currentStatus.id]}">
                                            <%@ include file="/WEB-INF/views/placeholders/story-board.jsp" %>
                                        </c:forEach>
                                    </ul>
                                </td>
                            </c:forEach>
                        </tr>
                    </table>
            </div>
        </div>

    </div>

    <div id="footer">
        <c:import url="footer.jsp" />
    </div>
</body>

</html>