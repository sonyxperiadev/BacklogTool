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
<title>Backlog tool - ${area.name}</title>
<script type="text/javascript">
    var areaName = "${area.name}";
    var view = "${view}";
    var disableEditsBoolean = "${disableEdits}" == "true" ? true : false;
</script>
<link rel="shortcut icon"
    href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.8.21.custom.css" />"></link>
<link rel="stylesheet" type="text/css"
    href="<c:url value="/resources/css/styles.css?v=1" />"></link>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-1.7.2.min.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery.blockUI.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery-ui-1.8.21.custom.min.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery.autosize-min.js" />"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/resources/js/scripts.js?v=2" />"></script>
<script type="text/javascript" src="code.icepush"></script>
<script type="text/javascript"
    src="<c:url value="/resources/js/jquery.truncator.js" />"></script>
</head>

<body>
    <c:import url="header.jsp"></c:import>
    <div id="list-container-div">
        <ul class="parent-child-list" id="list-container"></ul>
        <ul class="parent-child-list" id="archived-list-container"></ul>
    </div>
    <c:import url="delete.jsp"></c:import>
    <br>
    <br>
    <br>
    <br>
</body>

</html>