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
<%@ page session="false"%>
<!DOCTYPE html>
<html>

<head>
<link rel="shortcut icon"
    href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
<title>Backlog-tool</title>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/styles.css?v=1" />"></link>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.8.21.custom.css" />"></link>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-1.7.2.min.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-ui-1.8.21.custom.min.js" />"></script>
<script type="text/javascript">
    var lastArea = "${lastArea}";
    var isLoggedIn = "${isLoggedIn}" == "true" ? true : false;

    $(document).ready(function() {
        $(".home-link").css("color", "#1c94c4");
        $('#login-out').button();

        $("#create-area").button().click(function() {
            $.ajax({
                url : "json/createArea",
                type : 'POST',
                data: $("#area-name").val(),
                contentType : "application/json; charset=utf-8",
                success : function(response) {
                    if (response == "") {
                        location.reload();
                    } else {
                        window.location.href = "story-task/" + response;
                    }
                },
                error : function(request, status, error) {
                    alert(error);
                    location.reload();
                }
            });
        });

        if (!isLoggedIn) {
            $("#create-area").button("option", "disabled", true);
            $("#area-name").attr('disabled', 'disabled');
        }

        var deleteArea = function(event) {
            $('#delete-area').dialog({
                resizable : false,
                height : 180,
                modal : true,
                buttons : {
                    "Delete this area" : function() {
                        $.ajax({
                            url : "json/deleteArea/" + event.target.id,
                            type : 'POST',
                            dataType : 'json',
                            data : JSON.stringify(event.target.id),
                            contentType : "application/json; charset=utf-8",
                            success : function(data) {
                                window.location.href = "${pageContext.request.contextPath}";
                            },
                            error : function(request, status, error) {
                                alert(error);
                                location.reload();
                            }
                        });
                        $(this).dialog("close");
                    },
                    Cancel : function() {
                        $(this).dialog("close");
                    }
                }
            });
        };
        $(".deletebutton").click(deleteArea);
    });
</script>

</head>

<body>
    <header>
        <h1>
            <a href="${pageContext.request.contextPath}/${area.name}">
                <p id="topic" class="textstyle inline">Backlog tool</p>
            </a>
        </h1>
        <br style="clear: both" /> <a id="login-out" href="auth/logout"><c:if
                test="${isLoggedIn == true}">LOG OUT</c:if> <c:if
                test="${isLoggedIn == false}">LOG IN</c:if></a>
        <c:if test="${lastArea != null}">
            <a title="STORY TASK VIEW"
                class="story-task-link navigation-link"
                href="story-task/${lastArea}">STORY TASK </a>
            <a title="EPIC STORY VIEW"
                class="epic-story-link navigation-link"
                href="epic-story/${lastArea}">EPIC STORY /&nbsp</a>
            <a title="THEME EPIC VIEW"
                class="theme-epic-link navigation-link"
                href="theme-epic/${lastArea}">THEME EPIC /&nbsp</a>
            <a title="AREA VIEW" class="home-link navigation-link"
                href="${lastArea}">AREA /&nbsp</a>
        </c:if>
    </header>
    <div id="list-container-div">
        <div id="area-container-div" class="inline">
            <p style="margin-top: 15px;">Areas:</p>
            <c:forEach var="area" items="${adminAreas}">
                <c:set var="style" value="" />
                <c:if test="${area == lastArea}">
                    <c:set var="style" value='style="color: #1C94C4;"' />
                </c:if>
                <a class="area-links" href="story-task/${area}" ${style}>
                    ${area} </a>
                <a><img
                    src="resources/css/ui-lightness/images/delete.png"
                    class="deletebutton" id="${area}"
                    title="Delete area" alt="Delete area" /></a>
                <a href="areaedit/${area}"><img
                    src="resources/css/ui-lightness/images/pencil_go.png"
                    class="editbutton" title="Edit area" alt="Edit area" /></a>
                <br />
            </c:forEach>
            <br>
            <c:forEach var="area" items="${nonAdminAreas}">
                <c:set var="style" value="" />
                <c:if test="${area == lastArea}">
                    <c:set var="style" value='style="color: #1C94C4;"' />
                </c:if>
                <a class="area-links" href="story-task/${area}" ${style}>
                    ${area} </a>
                <br />
            </c:forEach>
            <br> <br>
            <p>Create new area</p>
            <input type="text"
                class="text ui-widget-content ui-corner-all"
                id="area-name" size="33" maxlength="50"> <a
                title="Create area" id="create-area">Create area</a>
        </div>

        <div id="info-container-div" class="inline">
            <p>This tool keeps track of the backlog for software
                development teams.</p>
            <p>
                Backlog items can be inserted as <b>themes</b>, <b>epics</b>,
                <b>stories</b> and <b>tasks</b> as a tree structure in
                that order.
            </p>
            <p>Three different views are available when managing the
                backlog, namely theme-epic, epic-story and story-task.
                In these views, double click on the backlog
                items in order to edit them.</p>
            <br>
            <p>
                Currently only <b>Chrome</b> and <b>Firefox</b> browsers
                are supported.
            <p>Thanks to famfamfam.com for the icons!</p>
        </div>
    </div>
    <div id="delete-area" title="Delete area">
        <p>
            <span style="float: left; margin: 0 7px 20px 0;"></span> Do
            you want to remove this area?
        </p>
    </div>
</body>

</html>
