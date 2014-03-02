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

function appendTask(task) {
    
}

$(document).ready(function () {
    if ($('.status-td:first').width() < 100) {
        $('.status-td').width("150px");
        $('#status-table').width("auto");
        $('#main').css("overflow-x","scroll");
    }
    
    
    
    $( ".status-list" ).sortable({
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
    
    $(".expand-icon").click(function(e) {
        var story = getParent(this.parentNode.id);
        var storyLi = $(this.parentNode);
        
        if ($(this).attr("class").indexOf("ui-icon-triangle-1-s") != -1) { //clicked to expand
            var options = area.taskAttr1.options.slice(0);
            options.push({id:"null", name:"UNCATEGORIZED"});
            storyLi.append('<ul class="child-container"></ul>');
            var storyUl = storyLi.children('ul').first();
            options.forEach(function(status) {
                storyUl.append('<li id="' + status.id + '" class="status-child-header">' + status.name + '</li>');
            });
            story.children.forEach(function(task) {
                var taskStatus = null;
                if (task.taskAttr1 != null) {
                    taskStatus = task.taskAttr1.id;
                }
                storyUl.children('#' + taskStatus).first().after('<li class="board-task" id="' + task.id  + '">' + task.title + '</li>');
            });
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
            
        } else if ($(this).attr("class").indexOf("ui-icon-triangle-1-e") != -1) { //clicked to collapse
            $(this).siblings('ul').first().remove();
        }

    });
    
});
