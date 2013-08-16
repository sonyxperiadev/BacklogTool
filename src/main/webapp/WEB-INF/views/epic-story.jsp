<!--
The MIT License

Copyright 2012 Sony Mobile Communications AB. All rights reserved.

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%> <%-- Library used in included placeholder-files --%>
<%@ page session="false"%>
<!DOCTYPE html>
<html>

<head>
    <title>Backlog Tool - ${area.name}</title>

    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.10.3.custom.min.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/styles.css?v=${versionNoDots}" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/jquery.dropdown.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/fff-silk.min.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/simplePagination.css" />"></link>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-2.0.1.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.blockUI-2.61.0.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-ui-1.10.3.custom.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.autosize-min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/scripts.js?v=${versionNoDots}" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.truncator.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.dropdown.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.atmosphere-min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.simplePagination.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.dotdotdot.min.js" />"></script>

    <script type="text/javascript">
        var loggedIn = "${isLoggedIn}" == "true" ? true : false;
        var areaName = "${area.name}";
        var view = "${view}";
        var archivedView = "${archivedView}" == "true" ? true : false;
        var disableEditsBoolean = "${disableEdits}" == "true" ? true : false;
        var parentsMap = ${jsonDataNonArchivedEpics};
        var area = ${jsonAreaData};
    </script>
</head>

<body>
    <div id="wrap">
        <div id="header" class="initial-fixed-height">
            <c:import url="header.jsp" />
        </div>

        <div id="main">
            <div id="epic-placeholder" class="placeholder" >
                <c:set var="epic" value="${placeholderEpic}"/>
                <%@ include file="/WEB-INF/views/placeholders/epic.jsp"%>
            </div>
            <div id="story-placeholder" class="placeholder" >
                <c:set var="story" value="${placeholderStory}"/>
                <%@ include file="/WEB-INF/views/placeholders/story.jsp"%>
            </div>
            <div id="story-oneline-placeholder" class="placeholder">
                <c:set var="story" value="${placeholderStory}" />
                <%@ include file="/WEB-INF/views/placeholders/story-oneline.jsp"%>
            </div>
            <div id="epic-oneline-placeholder" class="placeholder">
                <c:set var="epic" value="${placeholderEpic}" />
                <%@ include file="/WEB-INF/views/placeholders/epic-oneline.jsp"%>
            </div>

            <div id="list-container-div">

                <div id="table-header">
                    <p class="oneline header-item header-id">ID</p>
                    <p class="oneline header-item header-title">Title</p>
                    <c:if test="${archivedView}">
                        <p class="oneline header-item small-item">Archived</p>
                    </c:if>
                </div>

                <ul class="parent-child-list" id="list-container">

                    <c:forEach var="epic" items="${nonArchivedEpics}">
                        <c:set var="hidden" value="${filterIds != null && !filterIds.contains(epic.id)}"/>
                        <%@ include file="/WEB-INF/views/placeholders/epic-oneline.jsp" %>
                        <c:set var="hidden" value="${true}"/>
                        <c:forEach var="story" items="${epic.children}">
                            <%@ include file="/WEB-INF/views/placeholders/story-oneline.jsp" %>
                        </c:forEach>
                    </c:forEach>
                </ul>
                <ul class="parent-child-list" id="archived-list-container"></ul>
            </div>
            <c:if test="${archivedView}">
                <div id="pagination"></div>
            </c:if>
        </div>

    </div>

    <div id="footer">
        <c:import url="footer.jsp" />
    </div>

    <c:import url="delete.jsp"></c:import>
</body>

</html>