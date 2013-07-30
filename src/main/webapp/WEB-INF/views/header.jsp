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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<h1>
    <a href="../">
        <p id="topic" class="textstyle inline">Backlog tool</p>
    </a>
    <p id="topic-area" class="textstyle inline"></p>
</h1>

<div id="login-out-container">
    <c:if test="${isLoggedIn == true}">
        <p class="headerText textstyle inline">${loggedInUser}</p>
        <a id="login-out" class="fff inline" href="../auth/logout">Log out</a>
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
        <a id="login-out" class="fff inline" href="../auth/logout">&nbsp Log in</a>
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
<div id="header-buttons" class="elem-hidden-children elem-loading">
    <button id="settings" data-dropdown="#settings-div" class="fff" title="More options">#</button>
    <div id="settings-div" class="dropdown dropdown-tip dropdown-relative">
        <ul class="dropdown-menu">
            <li>
                <a id="expand-all" href="#" class="">EXPAND ALL</a>
            </li>
            <li>
                <a id="collapse-all" class="">COLLAPSE ALL</a>
            </li>
            <li id="print-stories-li">
                <hr class="menu-divider">
                <a id="print-stories" title="Print selected stories">PRINT SELECTED</a>
            </li>
            
            <c:if test="${isLoggedIn == true && adminAreas.size() > 0}">
                <li id ="move-li">
                    <hr class="menu-divider">
                    <div id="move-div">
                    <p>Move selected to area...</p>
                    <select id="toArea" autocomplete="off" class="text ui-widget-content ui-corner-all">
                        <option value=""></option>
                        <c:forEach var="currentArea" items="${adminAreas}">
                            <option value="${currentArea}">${currentArea}</option>
                        </c:forEach>
                    </select>
                    </div>
                </li>
            </c:if>
        </ul>
    </div>
    <button title="Save all changes" id="save-all" class="save-button fff" disabled>#</button>
    <div class="showArchive">
        <table>
            <tr>
                <td>
                    <input type="radio" id="active" name="radio" 
                        <c:if test="${!archivedView}">
                            checked="checked"
                        </c:if>
                    />
                    <label for="active">Active</label>
                </td>
                <td>
                    <input type="radio" id="archive" name="radio" 
                        <c:if test="${archivedView}">
                            checked="checked"
                        </c:if>
                    />
                <label for="archive">Archive</label>
                </td>
            </tr>
        </table>
    </div>
    <c:if test="${!archivedView}">
        <button title="Create a new story" id="create-parent" class="fff"></button>
    </c:if>
    <button id="filter-button" title="Filter selected items">FILTER SELECTED</button>
    <div class="filter">
        <p class="headerText textstyle">FILTER BY ID</p>
        <input title="Filter by ID, comma-separated" id="filter" class="filter"/>
    </div>
    <c:if test="${!archivedView}">
        <div class="order-by">
            <p class="headerText textstyle">ORDER BY</p>
            <select name="order-by" id="order-by"
                class="text ui-widget-content ui-corner-all">
                <option selected value="prio">Rank</option>
                <option value="title">Title</option>
                <option value="description">Description</option>
                <c:if test="${view == 'story-task'}">
                    <option value="contributor">Contributor</option>
                    <option value="customer">Customer</option>
                    <option value="contributorSite">Contributor site</option>
                    <option value="customerSite">Customer site</option>
                    <option value="storyAttr1">${area.storyAttr1.name}</option>
                    <option value="storyAttr2">${area.storyAttr2.name}</option>
                    <option value="storyAttr3">${area.storyAttr3.name}</option>
                </c:if>
            </select>
        </div>
    </c:if>
</div>
<div class="navigation-links">
    <a title="STORY TASK VIEW" class="story-task-link navigation-link" href="../story-task/${area.name}">STORY TASK </a> 
    <a title="EPIC STORY VIEW" class="epic-story-link navigation-link" href="../epic-story/${area.name}">EPIC STORY /&nbsp</a> 
    <a title="THEME EPIC VIEW" class="theme-epic-link navigation-link" href="../theme-epic/${area.name}">THEME EPIC /&nbsp</a> 
    <a title="AREA VIEW" class="home-link navigation-link" href="../">AREA /&nbsp</a> 
</div>