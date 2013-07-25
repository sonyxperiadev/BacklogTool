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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% pageContext.setAttribute("newLineChar", "\n"); %>
<% pageContext.setAttribute("newLineHtml", "<br/>"); %>
<%@ page session="false"%>
<!DOCTYPE html>
<html>

<head>
    <title>Backlog Tool</title>
    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <style type="text/css">
        body {
            font-family: arial, Helvetica, sans-serif;
            font-size: 8px;
            line-height: 1.6;
        }

        p {
            margin: 0.1em;
        }

        #story-container {
            border: 2px solid;
            width: 450px;
            height: 245px;
            padding: 5px;
        }

        #theme-epic {
            height: 40px;
        }

        #epic,#theme {
            float: left;
            display: inline-block;
            width: 170px;
            height: 40px;
            text-indent: 2px;
        }

        div.id-box {
            float: left;
            display: inline-block;
            margin-right: 20px;
            text-indent: 2px;
        }

        div.id-box p {
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 85px;
            display: block;
            white-space: nowrap;
        }

        #title,#effort-prio {
            float: left;
            border: 1px solid;
            display: inline-block;
            height: 85px;
            text-indent: 2px;
        }

        .dotted {
            border: 1px dashed #2F6FAB;
        }

        #title {
            width: 255px;
        }

        #effort-prio {
            border: 1px solid;
            width: 185px;
        }

        #effort {
            margin: 5px;
        }

        #prio {
            margin: 5px;
        }

        #task-story {
            font-size: 12px;
            overflow: hidden;
            max-height: 20px;
        }

        #task-id {
            float: right;
            padding-right: 10px;
        }

        #task-description {
            width: 440px;
            height: 170px;
            padding: 5px;
            border-top: 1px solid;
            overflow: hidden;
        }

        #task-footer {
            border: 1px solid;
        }

        #footer-owner {
            float: left;
        }

        #footer-time {
            float: right;
        }

        p {
            display: inline;
            word-wrap: break-word;
        }

        p.content {
            font-weight: bold;
        }

        p.title {
            font-size: 14px;
            font-weight: bold;
        }

        p.task {
            font-size: 12px;
            font-weight: bold;
        }

        td.timebox {
            border: 1px solid;
            width: 25px;
            height: 25px;
            font-size: 12px;
            font-weight: bold;
            padding: 2px;
        }

        p.task-story {
            font-weight: bold;
            font-size: 14px;
        }
    </style>
</head>

<body>
    <c:forEach var="story" items="${stories}" varStatus="rowCounter">
        <div id="story-container">
            <div id="theme-epic">
                <div id="story-id-box" class="id-box">
                    <p class="title">#${story.id}</p>
                </div>
                <div id="theme">
                    <p>Theme:</p>
                    <p class="content">${story.themeTitle}</p>
                </div>
                <div id="epic">
                    <p>Epic:</p>
                    <p class="content">${story.epicTitle}</p>
                </div>
            </div>
            <div id="title">
                <p class="title">${story.title}</p>
            </div>
            <div id="effort-prio">
                <div id="attr1">
                    <p>${area.storyAttr1.name}:</p>
                    <p class="content">${story.storyAttr1.name}</p>
                </div>
                <div id="attr2">
                    <p>${area.storyAttr2.name}:</p>
                    <p class="content">${story.storyAttr2.name}</p>
                </div>
                <div id="attr3">
                    <p>${area.storyAttr3.name}:</p>
                    <p class="content">${story.storyAttr3.name}</p>
                </div>
                <div>
                    <p>Work left:</p>
                </div>
                <table>
                    <tr>
                        <c:forEach var="i" begin="1" end="7" step="1">
                            <td class="timebox" />
                        </c:forEach>
                    </tr>
                </table>
            </div>
            <br style="clear: both" />
            <div id="description">
                <p>${fn:replace(story.description, newLineChar, newLineHtml)}</p>
            </div>
        </div>
        <br>
        <hr class='dotted' />
        <br>
        <c:choose>
            <c:when test="${rowCounter.count % 3 == 0}">
                <DIV style="page-break-after: always"></DIV>
            </c:when>
        </c:choose>
    </c:forEach>
    <c:forEach var="story" items="${stories}">
        <div style="page-break-after: always"></div>
        <c:forEach var="task" items="${story.children}"  varStatus="rowCounter">
            <div id="story-container">
                <div class="id-box">
                    <p class="title">#${task.id}</p>
                </div>
                <div id="task-story">
                    <p>Story:</p>
                    <p class="task-story">${story.title}</p>
                </div>
                <div id="task-description">
                    <p class="task">${fn:replace(task.title, newLineChar, newLineHtml)}</p>
                </div>
                <div id="task-footer">
                    <div id="footer-owner">
                        <p>Owner:</p>
                        <br />
                        <p class="task">${task.owner}</p>
                    </div>
                    <div id="footer-time">
                        <div>
                          <p>Work left:</p>
                        </div>
                        <table>
                            <tr>
                                <td class="timebox">${task.calculatedTime}</td>
                                <c:forEach var="i" begin="1" end="6" step="1">
                                    <td class="timebox" />
                                </c:forEach>
                            </tr>
                        </table>
                    </div>
                </div>
          </div>
      <br>
      <hr class='dotted' />
      <br>
      <c:choose>
          <c:when test="${rowCounter.count % 3 == 0}">
              <DIV style="page-break-after: always"></DIV>
          </c:when>
      </c:choose>
      </c:forEach>
    </c:forEach>

    <script>window.print();</script>

</body>

</html>
