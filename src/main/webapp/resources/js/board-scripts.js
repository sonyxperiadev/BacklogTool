/*
 *  The MIT License
 *
 *  Copyright 2014 Sony Mobile Communications AB. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

var MIN_COLUMN_WIDTH = 300;

/**
 * Function triggered when the expand buttons are pressed on a story to display tasks.
 */
function boardExpand(e) {
    var story = getParent(this.parentNode.id);
    var storyLi = $(this.parentNode);
    
    if ($(this).attr("class").indexOf("ui-icon-triangle-1-e") != -1) { //clicked to expand
        var options = area.taskAttr1.options.slice(0);
        options.push({id:"null", name:"UNCATEGORIZED"});
        storyLi.append('<ul></ul>');
        var storyUl = storyLi.children('ul').first();
        options.forEach(function(status) {
            storyUl.append('<li id="' + status.id + '" class="status-child-header">' + status.name + '</li>');
        });
        story.children.forEach(function(task) {
            var taskStatus = null;
            if (task.taskAttr1 != null) {
                taskStatus = task.taskAttr1.id;
            }
            storyUl.children('#' + taskStatus).first().after('<li class="board-task" id="' + task.id  + '"><p class="normal-text">' + task.title + '</p></li>');
            $('p', '#'+task.id).truncate({max_length: 45, more: '...', less: 'less',});
        });
        $(this).removeClass("ui-icon-triangle-1-e");
        $(this).addClass("ui-icon-triangle-1-s");
        
        if(loggedIn) {
            storyUl.sortable({
                cancel: '.status-child-header',
                axis: 'y',
                stop: function (event, ui) {
                    /* A child-element must not be positioned in the beginning of the list, as there
                     * is no status */
                    if (ui.item.index() == 0) {
                        $(this).sortable('cancel');
                    } else {
                        displayUpdateMsg();
                        var taskId = $(ui.item).attr("id");
                        var statusId = $(ui.item).prevAll(".status-child-header").first().attr("id");

                        $.ajax({
                            url : "../json/changeTaskAttr1/" + areaName + "?taskId=" + taskId,
                            type : 'POST',
                            data : statusId,
                            contentType : "application/json; charset=utf-8",
                            success : function(response) {
                                if (response != true) {
                                    alert('Failed to change status; reloading page');
                                    location.reload();
                                }
                                getChild(taskId).taskAttr1 = getTaskAttr(statusId);
                            },
                            error : function(request, status, error) {
                                alert(error);
                                location.reload();
                            }
                        });
                        
                        $.unblockUI();
                    }
                }
            });
        }
    } else if ($(this).attr("class").indexOf("ui-icon-triangle-1-s") != -1) { //clicked to collapse
        $(this).siblings('ul').first().remove();
        $(this).removeClass("ui-icon-triangle-1-s");
        $(this).addClass("ui-icon-triangle-1-e");
    }

}

function handleBoardPush(story) {
    var storyId = story.id;
    
    //If only the story itself was updated, its children are not pushed. 
    var storyWithChildren = getParent(storyId);
    story.children = storyWithChildren.children;
   
    var storyLi = $("li#" + storyId);
    var wasExpanded = storyLi.has(".ui-icon-triangle-1-s").length > 0;
    
    var divItem = $('div#story-placeholder').clone();
    var htmlStr = divItem.html();
    htmlStr = htmlStr.replace(/-1/g, storyId); // Replace all occurences of -1

    var newItem = $(htmlStr);
    var statusId = "null";
    if (story.storyAttr1 != null) {
        statusId = story.storyAttr1.id;
    }
    if (storyLi.length > 0 && storyLi.parents("ul").attr("id") == statusId) {
        //The element already exists in the correct status list;
        //put in same position as before
        storyLi.after(newItem);
    } else {
        $("ul#"+statusId).append(newItem);
    }
    storyLi.remove();
    
    newItem.children(".board-title").html(story.title);
    if (story.children != null && story.children.length > 0) {
        var expandIcon = newItem.children(".board-expand-icon");
        expandIcon.removeClass("ui-icon-blank").addClass("ui-icon-triangle-1-e");
        expandIcon.click(boardExpand);
        if (wasExpanded) {
            expandIcon.click();
        }
    }
    
    var statusList = newItem.parents(".status-list");
    
    statusList.sortable("refresh");
}

$(document).ready(function () {
    var changeColumnWidth = function() {
        var width = nbrOfColumns * MIN_COLUMN_WIDTH;
        $('body').css('min-width', Math.max(width,900) + 'px');
    };
    
    changeColumnWidth();
    
    if (loggedIn) {
        $(".status-list").sortable({
            connectWith: ".status-list",
            cursor: "move",
            placeholder: "ui-state-highlight",
            distance: 5,
            start: function( event, ui ) {
                if (ui.item.hasClass("story")) {
                    $('.ui-icon-triangle-1-s').click();//Collapses all open stories.
                }
                //Make sure the helper is correct height:
                ui.helper.height("auto");
                ui.placeholder.height(ui.helper.outerHeight());
            },
            receive: function( event, ui ) {
                displayUpdateMsg();
                var storyId = $(ui.item).attr("id");
                var statusId = event.target.id;
                
                var story = getParent(storyId);
                if (story.storyAttr1 == null) {
                    story.storyAttr1 = new Object();
                }
                story.storyAttr1.id = statusId;

                $.ajax({
                    url : "../json/changeStoryAttr1/" + areaName + "?storyId=" + storyId,
                    type : 'POST',
                    data : statusId,
                    contentType : "application/json; charset=utf-8",
                    success : function(response) {
                        if (response != true) {
                            alert('Failed to change status; reloading page');
                            location.reload();
                        }
                    },
                    error : function(request, status, error) {
                        alert(error);
                        location.reload();
                    }
                });
                $.unblockUI();
                
            },
          }).disableSelection();
    }
    
    $(".board-expand-icon").click(boardExpand);
    
    $(".red-cross").click(function() {
        var index = $(this).closest(".status-td").index() + 1;
        var column = $("#status-table td:nth-child(" + index + "), th:nth-child(" + index + ")");
        column.hide();
        nbrOfColumns--;
        changeColumnWidth();
        
        if ($('#header-buttons').text().trim() == '') {
            $('#header-buttons').append('Hidden columns: ');
        }
       
        var statusName = $(this).siblings().text();
        $('<a/>', {
            'class':'small-text',
            'text': statusName + ', ',
        }).on('click', function(){
            column.show();
            nbrOfColumns++;
            changeColumnWidth();
            $(this).remove();
        }).appendTo('#header-buttons');
        
    });
    
});
