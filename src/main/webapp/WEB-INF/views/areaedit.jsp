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
    <title>Backlog Tool - Edit area</title>

    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.10.3.custom.min.css" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/styles.css?v=${versionNoDots}" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/fff-silk.min.css" />"></link>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-2.0.1.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-ui-1.10.3.custom.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/scripts-areaedit.js?v=${versionNoDots}" />"></script>
    
    <script type="text/javascript">
        var areaName = "${area.name}";
        var storyAttr1Id = "${area.storyAttr1.id}";
        var storyAttr2Id = "${area.storyAttr2.id}";
        var storyAttr3Id = "${area.storyAttr3.id}";
        var taskAttr1Id = "${area.taskAttr1.id}";
    	var seriesIds = jQuery.parseJSON('${seriesIds}');
    </script>
    
    <style>
        #main {
            padding-top: 40px;
        }

        #right-panel > div {
            min-width: 435px;
        }

        li * {
            vertical-align: middle;
        }

        .new-attribute > div > .attrIcon {
            margin-left: 7px;
        }

        .new-attribute > .removeOption {
            margin-left: 7px;
        }

        .new-attribute > .icon-container {
            margin-left: 2px;
        }

        .new-attribute input {
            margin: 1px;
        }
        #save {
            margin-top: 30px;
        }
    </style>
</head>
<body>
    <div id="wrap">
        <div id="header">
            <h1>
                <a href="../">
                    <p id="topic" class="textstyle inline">Backlogtool /&nbsp</p>
                </a>
                <p id="topic-area" class="textstyle inline">Edit area ${area.name}</p>
            </h1>
            <div id="login-out-container">
                <p class="headerText textstyle inline">${loggedInUser}</p>
                <a id="login-out" class="fff inline" href="../auth/logout">Log out</a>
            </div>
            
        </div>
        <div id="main">
            <div id="left-panel">
                <div class="well">
                    <p>Area name</p>
                    <input type="text" 
                        class="ui-corner-all"
                        id="area-name" 
                        size="33" 
                        maxlength="50"
                        value="${area.name}"> 
                    <input id="name-button"
                        class="ui-corner-all areaedit-button"
                        type="submit" 
                        value="Change">
                </div>
                <div class="well">
                    <div id="admins-div">
                        <p>Admins</p> 
                        <input id="admin-username" 
                            class="inline ui-corner-all"
                            placeholder="Username">  
                        <button id="add-admin" type="submit" class="inline fff">Add</button>
                        <br/>
                        <ul>
                        <c:forEach var="admin" items="${area.admins}">
                            <li>
                                <b>${admin}</b>
                                <a>
                                    <img
                                        src="../resources/css/ui-lightness/images/delete.png"
                                        class="deleteAdminButton"
                                        id="${admin}" title="Remove admin"
                                        alt="Remove admin" />
                                </a>
                                <br />
                            </li>
                        </c:forEach>
                        </ul>
                    </div>
                    <div id="editors-div">
                        <p>Editors</p> 
                        <input id="editor-username" 
                            class="inline ui-corner-all"
                            placeholder="Username"> 
                        <button id="add-editor" type="submit" class="inline fff">Add</button> 
                        <ul>
                        <c:forEach var="editor" items="${area.editors}">
                            <li>
                                <b>${editor}</b>
                                <a>
                                    <img
                                        src="../resources/css/ui-lightness/images/delete.png"
                                        class="deleteEditorButton"
                                        id="${editor}" title="Remove editor"
                                        alt="Remove editor" />
                                </a>
                            </li>
                        </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
            <div id="right-panel" class="well">
                <br style="clear:both" />
                <div class="inline">
                    <p>Dynamic story field 1</p>
                    <input
                        id="${area.storyAttr1.id}"
                        class="ui-corner-all attrTitle"
                        value="${area.storyAttr1.name}"
                        maxlength="15">
                    <c:set var="attribute" value="${area.storyAttr1}" />
                    <%@ include file="/WEB-INF/views/attribute.jsp" %>
                </div>
                
                <div class="inline">
                    <p>Dynamic story field 2</p>
                    <input
                        id="${area.storyAttr2.id}"
                        class="ui-corner-all attrTitle"
                        value="${area.storyAttr2.name}"
                        maxlength="15">
                    <c:set var="attribute" value="${area.storyAttr2}" />
                    <%@ include file="/WEB-INF/views/attribute.jsp" %>
                </div>
                
                <br style="clear:both" />
                <br/>
                
                <div class="inline">
                    <p>Dynamic story field 3</p>
                    <input id="${area.storyAttr3.id}"
                        class="ui-corner-all attrTitle"
                        value="${area.storyAttr3.name}"
                        maxlength="15">
                
                    <c:set var="attribute" value="${area.storyAttr3}" />
                    <%@ include file="/WEB-INF/views/attribute.jsp" %>
                </div>
                
                <div class="inline">
                    <p>Dynamic task field</p>
                    <input id="${area.taskAttr1.id}"
                        class="ui-corner-all attrTitle"
                        value="${area.taskAttr1.name}"
                        maxlength="15">
                
                    <c:set var="attribute" value="${area.taskAttr1}" />
                    <%@ include file="/WEB-INF/views/attribute.jsp" %>
                </div>
                <br style="clear:both" />
                <button id="save" class="fff">&nbsp Save changes</button>
            </div>
        </div>
    </div>

    <div id="footer">
        <c:import url="footer.jsp" />
    </div>

    <div id="image_container">
        <c:forEach items="${icons}" var="icon">
            <img id="${icon}" src="../resources/image/${icon}" />
        </c:forEach>
    </div>


    <div id="delete-admin" title="Delete admin">
        <p>
            <span style="float: left; margin: 0 7px 20px 0;"></span> Do
            you want to remove this admin?
        </p>
    </div>

    <div id="delete-editor" title="Delete editor">
        <p>
            <span style="float: left; margin: 0 7px 20px 0;"></span> Do
            you want to remove this editor?
        </p>
    </div>
</body>

</html>
