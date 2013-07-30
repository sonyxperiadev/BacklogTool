<%--
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
--%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><c:if test="${dataString != null}">${dataString}</c:if><c:if test="${dataString == null}">
<!DOCTYPE html>
<html>
<head>
<title>Backlog Tool</title>
<style type="text/css">
body {
    font-family: arial, Helvetica, sans-serif;
    font-size: 15px;
}

p.link-box {
    border: 1px dotted #808080;
    padding: 10px;
    background-color: #f5f5f5;
    margin: 10px;
    font-family: Courier New;
}

p.error {
    border: 1px dotted red;
    padding: 10px;
    background-color: #f8dcdc;
    margin: 10px;
}
</style>
</head>
<body>
    <h1>Backlog Tool - Exporting data</h1>

    <c:if test="${errorStr != null}">
        <p class="error"><strong>Error:</strong> ${errorStr}</p>
    </c:if>

    <p>A comma-separated list with data from Backlog Tool can be
        retrieved by passing url-parameters to this page:</p>
    <p class="link-box">?fields={field1},{field2},{field3}&archived={true|false}</p>
    <p>
        where <i>archived</i> is optional
    </p>
    <p>Available fields are:
        <ul>
            <li>id</li>
            <li>title</li>
            <li>dateadded</li>
            <li>contributor</li>
            <li>contributorsite</li>
            <li>customer</li>
            <li>customersite</li>
            <li>datearchived</li>
            <li>storyattr1</li>
            <li>storyattr2</li>
            <li>storyattr3</li>
            <li>area</li>
            <li>deadline</li>
            <li>description</li>
        </ul>
    </p>
    <p>Example:</p>
    <p class="link-box">
        <a href="?fields=title,area,dateadded,datearchived&archived=true">?fields=title,area,dateadded,datearchived&archived=true</a>
    </p>
</body>
</html>
</c:if>