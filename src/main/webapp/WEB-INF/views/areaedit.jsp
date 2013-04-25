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
<title>Backlog tool - Edit area</title>
<link rel="shortcut icon"
    href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
<title>Backlog-tool</title>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.8.21.custom.css " />"></link>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-1.7.2.min.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-ui-1.8.21.custom.min.js" />"></script>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/styles.css?v=1" />"></link>

<script type="text/javascript"
    src="<c:url value="/resources/js/scripts-areaedit.js?v=1" />"></script>

<script type="text/javascript">
    var areaName = "${area.name}";
    var storyAttr1Id = "${area.storyAttr1.id}";
    var storyAttr2Id = "${area.storyAttr2.id}";
    var storyAttr3Id = "${area.storyAttr3.id}";
    var taskAttr1Id = "${area.taskAttr1.id}";
</script>

</head>
<body>
    <header>
        <h1>
            <a href="${pageContext.request.contextPath}/${area.name}">
                <p id="topic" class="textstyle inline">Backlog tool
                    /&nbsp</p>
            </a>
            <p id="topic-area" class="textstyle inline">Edit area ${area.name}</p>
        </h1>
        <br style="clear: both" /> <a title="Log out" id="login-out"
            href="../auth/logout">
            <c:if test="${isLoggedIn}">
            LOG OUT
            </c:if>
            <c:if test="${!isLoggedIn}">
            LOG IN
            </c:if>
            </a>
    </header>

    <div id="list-container-div">
    	<div id="namechange-div">
	        <h4>Area name</h4>
	        <p>Change area name</p>
	        <input type="text"
	                class="ui-corner-all"
	                id="area-name" size="33" maxlength="50" value="${area.name}">
	        <input id="name-button" class="ui-corner-all areaedit-button"
	                type="submit" value="Change">
    	</div>

        <h4>Permissions</h4>
        <table>
            <tr>
                <td style="width: 280px">
                    <p>Current admins</p> <c:forEach var="admin"
                        items="${area.admins}">
                        <b>${admin}</b>
                        <a><img
                            src="../resources/css/ui-lightness/images/delete.png"
                            class="deleteAdminButton" id="${admin}"
                            title="Remove admin" alt="Remove admin" /></a>
                        <br />
                    </c:forEach> <br />
                    <p>Add new admin</p> <input id="admin-username"
                    class="ui-corner-all" placeholder="Username">
                    <input id="add-admin" class="ui-corner-all areaedit-button"
                    type="submit" value="Add"> <br> <br>
                </td>
                <td style="width: 280px">
                    <p>Current editors</p> <c:forEach var="editor"
                        items="${area.editors}">
                        <b>${editor}</b>
                        <a><img
                            src="../resources/css/ui-lightness/images/delete.png"
                            class="deleteEditorButton" id="${editor}"
                            title="Remove editor" alt="Remove editor" /></a>
                        <br />
                    </c:forEach> <br />
                    <p>Add new editor</p> <input id="editor-username"
                    class="ui-corner-all" placeholder="Username">
                    <input id="add-editor" class="ui-corner-all areaedit-button"
                    type="submit" value="Add"> <br> <br>
                </td>
            </tr>
        </table>

        <h4>Dynamic fields</h4>
        <h5>Stories</h5>
        <table>
            <tr>
                <td style="width: 280px"><input
                    id="${area.storyAttr1.id}"
                    class="ui-corner-all attrTitle"
                    value="${area.storyAttr1.name}" maxlength="15"></td>
                <td style="width: 280px"><input
                    id="${area.storyAttr2.id}"
                    class="ui-corner-all attrTitle"
                    value="${area.storyAttr2.name}" maxlength="15"></td>
                <td style="width: 280px"><input
                    id="${area.storyAttr3.id}"
                    class="ui-corner-all attrTitle"
                    value="${area.storyAttr3.name}" maxlength="15"></td>
            </tr>
            <tr>
                <td VALIGN="top">
                    <ul id="ul${area.storyAttr1.id}">
                        <c:forEach items="${area.storyAttr1.options}"
                            var="option">
                            <li id="${option.id}"><span
                                class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                                <div class="inline-block icon-container">
                                    <input id="iconEnabled${option.id}"
                                        class="checkbox inline-block"
                                        type="checkbox"
                                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                                    <img
                                        class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                                        id="icon${option.id}"
                                        src="../resources/image/${option.icon}"
                                        icon="${option.icon}" />
                                </div> <input id="name${option.id}"
                                value="${option.name}" maxlength="15"
                                class="inline-block attrOptionTitle ui-corner-all">
                                <img class="removeOption"
                                src="../resources/image/delete.png" />
                            </li>
                        </c:forEach>
                    </ul>
                    <p id="${area.storyAttr1.id}" class="addOption"
                        style="text-indent: 35px;">
                        <img id="${area.storyAttr1.id}"
                            class="addOption"
                            src="../resources/image/add.png" /> Add new
                    </p>
                </td>
                <td VALIGN="top">
                    <ul id="ul${area.storyAttr2.id}">
                        <c:forEach items="${area.storyAttr2.options}"
                            var="option">
                            <li id="${option.id}"><span
                                class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                                <div class="inline-block icon-container">
                                    <input id="iconEnabled${option.id}"
                                        class="checkbox inline-block"
                                        type="checkbox"
                                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                                    <img
                                        class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                                        id="icon${option.id}"
                                        src="../resources/image/${option.icon}"
                                        icon="${option.icon}" />
                                </div> <input id="name${option.id}"
                                value="${option.name}" maxlength="15"
                                class="inline-block attrOptionTitle ui-corner-all">
                                <img class="removeOption"
                                src="../resources/image/delete.png" />
                            </li>
                        </c:forEach>
                    </ul>
                    <p id="${area.storyAttr2.id}" class="addOption"
                        style="text-indent: 35px;">
                        <img id="${area.storyAttr2.id}"
                            class="addOption"
                            src="../resources/image/add.png" /> Add new
                    </p>
                </td>
                <td VALIGN="top">
                    <ul id="ul${area.storyAttr3.id}">
                        <c:forEach items="${area.storyAttr3.options}"
                            var="option">
                            <li id="${option.id}"><span
                                class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                                <div class="inline-block icon-container">
                                    <input id="iconEnabled${option.id}"
                                        class="checkbox inline-block"
                                        type="checkbox"
                                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                                    <img
                                        class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                                        id="icon${option.id}"
                                        src="../resources/image/${option.icon}"
                                        icon="${option.icon}" />
                                </div> <input id="name${option.id}"
                                value="${option.name}" maxlength="15"
                                class="inline-block attrOptionTitle ui-corner-all">
                                <img class="removeOption"
                                src="../resources/image/delete.png" />
                            </li>
                        </c:forEach>
                    </ul>
                    <p id="${area.storyAttr3.id}" class="addOption"
                        style="text-indent: 35px;">
                        <img id="${area.storyAttr3.id}"
                            class="addOption"
                            src="../resources/image/add.png" /> Add new
                    </p>
                </td>
            </tr>
        </table>
        <h5>Tasks</h5>
        <table>
            <tr>
                <td style="width: 280px"><input
                    id="${area.taskAttr1.id}"
                    class="attrTitle ui-corner-all" maxlength="15"
                    value="${area.taskAttr1.name}"></td>
            </tr>
            <tr>
                <td VALIGN="top">
                    <ul id="ul${area.taskAttr1.id}">
                        <c:forEach items="${area.taskAttr1.options}"
                            var="option">
                            <li id="${option.id}"><span
                                class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>
                                <div class="inline-block icon-container">
                                    <input id="iconEnabled${option.id}"
                                        class="checkbox inline-block"
                                        type="checkbox"
                                        <c:if test="${option.iconEnabled==true}">checked="checked"</c:if> />
                                    <img
                                        class="attrIcon <c:if test="${option.iconEnabled==false}">icon-hidden</c:if>"
                                        id="icon${option.id}"
                                        src="../resources/image/${option.icon}"
                                        icon="${option.icon}" />
                                </div> <input id="name${option.id}"
                                value="${option.name}" maxlength="15"
                                class="inline-block attrOptionTitle ui-corner-all">
                                <img class="removeOption"
                                src="../resources/image/delete.png" />
                            </li>
                        </c:forEach>
                    </ul>
                    <p id="${area.taskAttr1.id}" class="addOption"
                        style="text-indent: 35px;">
                        <img id="${area.taskAttr1.id}" class="addOption"
                            src="../resources/image/add.png" /> Add new
                    </p>
                </td>
            </tr>
        </table>

        <input id="save" class="ui-corner-all areaedit-button"
                    type="submit" value="Save">

        <div id="image_container">
            <c:forEach items="${icons}" var="icon">
                <img id="${icon}" src="../resources/image/${icon}" />
            </c:forEach>
        </div>


        <div id="delete-admin" title="Delete admin">
            <p>
                <span style="float: left; margin: 0 7px 20px 0;"></span>
                Do you want to remove this admin?
            </p>
        </div>

        <div id="delete-editor" title="Delete editor">
            <p>
                <span style="float: left; margin: 0 7px 20px 0;"></span>
                Do you want to remove this editor?
            </p>
        </div>
    </div>
</body>

</html>
