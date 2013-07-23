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
    <title>Backlog Tool</title>
    
    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/styles.css?v=${versionNoDots}" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.10.3.custom.min.css" />"></link>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-2.0.1.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-ui-1.10.3.custom.min.js" />"></script>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/fff-silk.min.css" />"></link>
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>

    <script type="text/javascript">
    
        google.load("feeds", "1");
    
        function initializeChangelog() {
            var feed = new google.feeds.Feed("https://github.com/sonyxperiadev/BacklogTool/releases.atom");
            feed.load(function(result) {
                if (!result.error) {
                    for (var i = 0; i < 2; i++) {
                        var entry = result.feed.entries[i];
                        $("#releases").append("<a href=https://github.com" + entry.link + "><h3>" + entry.title + "</h3></a>");
                        $("#releases").append("<p>" + entry.publishedDate.slice(0, -14) + "</p>");
                        $("#releases").append(entry.content);
                    }
                }
            });
        }

        var isLoggedIn = "${isLoggedIn}" == "true" ? true : false;

        $(document).ready(function() {

            initializeChangelog();

            $(".home-link").css("color", "#1c94c4");

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
    <div id="wrap">
        <div id="header">
            <h1>
                <a href="${pageContext.request.contextPath}/${area.name}">
                    <p id="topic" class="textstyle inline">Backlog tool</p>
                </a>
            </h1>
            <div id="login-out-container">
                <c:if test="${isLoggedIn == true}">
                    <p class="headerText textstyle inline">${loggedInUser}</p>
                    <a id="login-out" class="fff inline" href="auth/logout">Log out</a>
                    <script>
                        $("#login-out").button({
                            text: false,
                            icons: {
                                primary: 'silk-icon-door-out'
                            }
                        });
                    </script>
                </c:if>
                <c:if test="${isLoggedIn == false}">
                    <a id="login-out" class="fff inline" href="auth/logout">&nbsp Log in</a>
                    <script>
                        $("#login-out").button({
                            icons: {
                                primary: 'silk-icon-door-in'
                            }
                        });
                    </script>
                </c:if>
            </div>
            <br style="clear: both" /> 
        </div>
        <div id="main">
            <div id="list-container-div">
                <div id="area-container-div" class="inline">
                    <div class="well">
                        <h3>Areas</h3>
                        <c:if test="${!adminAreas.isEmpty()}">
                            <p>With admin permissions</p>
                        </c:if>
                        <c:forEach var="area" items="${adminAreas}">
                            <a class="area-links" href="story-task/${area}">
                                ${area} 
                            </a>
                            <a>
                                <img
                                    src="resources/css/ui-lightness/images/delete.png"
                                    class="deletebutton" id="${area}"
                                    title="Delete area" alt="Delete area" />
                            </a>
                            <a href="areaedit/${area}">
                                <img
                                    src="resources/css/ui-lightness/images/pencil_go.png"
                                    class="editbutton" title="Edit area" alt="Edit area" /></a>
                            <br />
                        </c:forEach>
                        <br>
                        <c:if test="${!nonAdminAreas.isEmpty()}">
                            <p>With viewing permissions</p>
                        </c:if>
                        <c:forEach var="area" items="${nonAdminAreas}">
                            <a class="area-links" href="story-task/${area}">
                                ${area} </a>
                            <br />
                        </c:forEach>
                    </div>
                    <div class="well">
                        <h3>Create new area</h3>
                        <input type="text"
                            class="text ui-corner-all"
                            id="area-name" 
                            size="33"
                            placeholder="Name" 
                            maxlength="50"> 
                        <a title="Create area" id="create-area">Create area</a>
                    </div>
                </div>
                <div id="right-div" class="inline">
                    <div id="info-container-div" class="well">
                        <h3>About</h3>
                        <p>This tool keeps track of the backlog for software
                            development teams. Backlog items can be inserted as <b>themes</b>, <b>epics</b>,
                            <b>stories</b> and <b>tasks</b> as a tree structure in
                            that order.
                        </p>
                        <br>
                        <p>Three different views are available when managing the
                            backlog, namely theme-epic, epic-story and story-task.
                            In these views, double click on the backlog
                            items in order to edit them.
                        </p>
                        <br>
                        <p>
                            Currently only <b>Chrome</b> and <b>Firefox</b> browsers
                            are supported. Thanks to famfamfam.com for the icons!
                        </p>
                        <br>
                        <p>
                            <a href="https://github.com/sonyxperiadev/BacklogTool" target="blank">
                                Read more...
                            </a>
                        </p>
                    </div>
                    <div id="releases" class="well"></div>
                </div>
            </div>
        </div>
    </div>

    <div id="footer">
        <c:import url="footer.jsp" />
    </div>

    <div id="delete-area" title="Delete area">
        <p>
            <span style="float: left; margin: 0 7px 20px 0;"></span> 
            Do you want to remove this area?
        </p>
    </div>
</body>

</html>
