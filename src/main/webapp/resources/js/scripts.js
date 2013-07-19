/*
 *  The MIT License
 *
 *  Copyright 2012 Sony Mobile Communications AB. All rights reserved.
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

/**
 * Overrides the default outerWidth and outerHeight functions
 * since they are very slow for items with display: none.
 * The methods are called a lot by the jQuery.sortable() function,
 * making drag and drop very slow without these lines.
 */
(function ($) {
    var outerWidth = $.fn.outerWidth;
    $.fn.outerWidth = function() {
        if ($(this).css('display') === 'none') {
            return 0;
        }
        return outerWidth.call(this);
    };
    var outerHeight = $.fn.outerHeight;
    $.fn.outerHeight = function() {
        if ($(this).css('display') === 'none') {
            return 0;
        }
        return outerHeight.call(this);
    };
})(jQuery);

/**
 * Creates a cookie and saves locally.
 * 
 * @param name
 *            what to call it
 * @param value
 *            what value to store
 * @param days
 *            how many days until it gets destroyed
 */
function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    } else
        var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

/**
 * Reads a locally stored cookie.
 * 
 * @param name
 *            the name of the cookie to read
 * @returns value if it existed, otherwise null
 */
function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for ( var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0)
            return c.substring(nameEQ.length, c.length);
    }
    return null;
}

/**
 * Deletes a locally stored cookie.
 * 
 * @param name
 *            the cookie name to delete
 */
function deleteCookie(name) {
    createCookie(name, "", -1);
}

/**
 * This method alternate the color of parent li's.
 * For a readability purpose
 */
var addZebraStripesToParents = function() {
    $("#list-container>li.zebra-stripes").removeClass("zebra-stripes");
    $( "#list-container .parentLi" ).each(function(index) {
        if (index % 2 == 0) {
            $(this).addClass("zebra-stripes");
            $(this).nextUntil(".parentLi").addClass("zebra-stripes");
        }
    });
};

/**
 * Checks if array a contains an object with same id as argument object
 * 
 * @param a
 *            array to search in
 * @param obj
 *            object with the id to search for
 * @returns {Boolean} if it existed
 */
function contains(a, obj) {
    var i = a.length;
    while (i--) {
        if (a[i].id == obj.id) {
            return true;
        }
    }
    return false;
}

/**
 * Checks if argument id has been entered in the filter textbox
 * 
 * @param id
 */
function isFiltered(id) {
    var filterString = $("#filter").val();
    var filterArray = filterString.split(",");
    for ( var i = 0; i < filterArray.length; i++) {
        if (filterArray[i].trim() == id) {
            return true;
        }
    }
    return false;
}

/**
 * Gets URL parameter
 * 
 * @param name
 *            parameter name
 * @returns value or null if it doesn't exist
 */
function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '='
            + '([^&;]+?)(&|#|;|$)').exec(location.search) || [ , "" ])[1]
    .replace(/\+/g, '%20'))
    || null;
}

/**
 * Checks if something has been typed in the filter textbox
 * 
 * @returns {Boolean} true if information has been typed
 */
function isFilterActive() {
    return $("#filter").val().length > 0;
}

$(document).ready(function () {
    var KEYCODE_ENTER = 13;
    var KEYCODE_ESC = 27;
    var KEYCODE_CTRL = 17;

    /**
     * Formats a date as string
     * @returns {String} date string
     */
    Date.prototype.yyyymmdd = function() {
        var yyyy = this.getFullYear().toString();
        var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
        var dd  = this.getDate().toString();
        return yyyy + "-" + (mm[1]?mm:"0" + mm[0]) + "-" + (dd[1]?dd:"0"+dd[0]);
    };

    /**
     * Removes spaces from a string.
     * @returns string without spaces
     */
    String.prototype.trim = function() {
        return this.replace(/^\s+|\s+$/g, "");
    };

    /**
     * Returns date as a formatted string if it existed was not null,
     * otherwise an empty string gets returned.
     * @param dateString string to format
     */
    var getDate = function(dateString) {
        if (dateString == null) {
            return "";
        } else {
            return new Date(dateString).yyyymmdd();
        }
    };

    var getSiteImage = function(site) {
        if (site == "NONE") {
            return "";
        } else {
            return '<img src="../resources/css/ui-lightness/images/'+site+'.png" title="'+site+'" alt="'+site+'"/>';
        }
    };

    var getAttrImage = function(storyAttr) {
        if (storyAttr == null || storyAttr.iconEnabled == false) {
            return '';
        }
        return '<img src="../resources/image/'+storyAttr.icon+'" title="' + getNameIfExists(storyAttr) + '"/> ';
    };

    /**
     * Used to tick checkboxes on archived parent's.
     */
    var getArchived = function(archived) {
        if (archived) {
            return 'checked="checked"';
        } else {
            return '';
        }
    };

    //fix for trim
    if (typeof String.prototype.trim !== 'function') {
        String.prototype.trim = function () {
            return this.replace(/^\s+|\s+$/g, '');
        };
    }

    //fix for indexof
    Array.prototype.indexOf = function (obj, start) {
        for (var i = (start || 0), j = this.length; i < j; i++) {
            if (this[i] === obj) {
                return i;
            }
        }
        return -1;
    };

    var enableEdits = function () {
        $(".editTheme").dblclick(editTheme);
        $(".editEpic").dblclick(editEpic);
        $(".editStory").dblclick(editStory);
        $(".editTask").dblclick(editTask);
    };

    var disableEdits = function() {
        $( "#create-parent" ).button( "option", "disabled", true );
        $(".add-child-icon").addClass('disabled');
        $(".deleteItem").addClass('disabled');
        $(".editTheme").unbind("dblclick");
        $(".editEpic").unbind("dblclick");
        $(".editStory").unbind("dblclick");
        $(".editTask").unbind("dblclick");
    };


    $('#archived-checkbox').change(function () {
        $('#archived-list-container').toggle($('#archived-checkbox').is(":checked"));
        var dispArchived = $("#archived-checkbox").prop("checked");
        if (dispArchived) {
            $("#archived-list-container").empty();
            buildVisibleList(true);

            //Unbind all items and bind them again including archived.
            $(".editTheme").unbind("dblclick");
            $(".editEpic").unbind("dblclick");
            $(".editStory").unbind("dblclick");
            $(".editTask").unbind("dblclick");
            if (!disableEditsBoolean) {
                enableEdits();
            }
        } else {
            $("#list-divider").hide();
        }
        var cookieStr = (dispArchived) ? "checked" : "unchecked";
        createCookie("backlogtool-disparchived", cookieStr, 60);
    });

    /**
     * Adding line breaks and <a> tags for the param text.
     */
    var addLinksAndLineBreaks = function(text) {
        return text.replace(/\n/g, '<br />').replace( /(http:\/\/[^\s]+)/gi , '<a href="$1">$1</a>' );
    };

    var replaceNullWithEmpty = function (object) {
        if (object == null) {
            return '';
        } else {
            return object;
        }
    };

    var getNameIfExists = function (object) {
        if (object == null) {
            return '';
        } else {
            return object.name;
        }
    };

    var scrollTo = function(id) {
        var offset = $('#'+id).offset().top - $(window).scrollTop();
        if (offset > window.innerHeight) {
            $('html, body').animate({
                scrollTop: $('#' + id).offset().top
            }, 2000);
        }
    };

    /**
     * Generates a string containing all selected ids.
     */
    var selectedToString = function() {
        var ids = "";
        for (var i = 0; i < selectedItems.length; i++) {
            ids += (selectedItems[i].id);
            if (i != selectedItems.length-1) {
                ids += ',';
            }
        }
        return ids;
    };

    var printStories = function() {
        var url = "../print-stories/" + areaName + "?ids=" + selectedToString();
        window.open(url, "_blank");
    };

    /**
     * Updates the cookie with information about which elements that are selected.
     * Triggered when an item is selected or unselected.
     */
    var updateCookie = function updateCookie() {
        var parentName = null;
        var childName = null;
        if (view == "story-task") {
            parentName = "story";
            childName = "task";
        } else if (view == "epic-story") {
            parentName = "epic";
            childName = "story";
        } else if (view == "theme-epic") {
            parentName = "theme";
            childName = "epic";
        }
        deleteCookie("backlogtool-selectedItems");
        var selectedCookie = new Array();
        for (var i=0; i<selectedItems.length; ++i) {
            var currentItem = new Object();
            currentItem.id = selectedItems[i].id;
            if (selectedItems[i].type == "parent") {
                currentItem.type = parentName;
            } else if (selectedItems[i].type == "child") {
                currentItem.type = childName;
            }
            selectedCookie.push(currentItem);
        }
        createCookie("backlogtool-selectedItems", JSON.stringify(selectedCookie), 1);
    };

    var reload = function reload() {
        readData();
        $(".parent-child-list").empty();
        buildVisibleList();
        $.unblockUI();
    };

    var ignorePush;
    var socket;
    /**
     * Registers for push-notifications for the current area
     */
    var connectPush = function connectPush() {
        socket = $.atmosphere;
        var request = new $.atmosphere.AtmosphereRequest();
        request.url = '../json/register/' + areaName;
        request.contentType = "application/json";
        request.transport = 'websocket';
        request.fallbackTransport = 'long-polling';
//      request.logLevel = 'debug';

        request.onMessage = function(response) {
            if (!ignorePush) {
                var message = response.responseBody;
                processPushData(message);
                // TODO: Let processPushData handle all pushes, i.e. don't reload everything
                reload();
            }
        };

        if(request.logLevel == "debug") {
            request.onError = function(response) {
                window.console && console.log("No json-data in push-message");
            };

            request.onOpen = function(response) {
                window.console && console.log("onOpen: " + response.responseBody);
            };

            request.onClose = function(response) {
                window.console && console.log("onClose: " + response.responseBody);
            };

            request.onReconnect = function(response) {
                window.console && console.log("onReconnect: " + response.responseBody);
            };

            request.onTransportFailure = function(errorMsg, response) {
                window.console && console.log("onTransportFailure: " + errorMsg + " || " + response.responseBody);
            };
        }

        socket.subscribe(request);
    };
    connectPush();
    
    /**
     * Variable to ignore push-messages in e.g. edit-mode
     */
    ignorePush = false;
    
    /**
     * Try to interpret the data as JSON, and forward the data
     * to corresponding method
     */
    var processPushData = function processPushData(dataString) {
        var jsonObj = {};
        try {
            jsonObj = JSON.parse(dataString);
        } catch (error) {
            alert("Error: Invalid JSON-message from the server");
        }
        var data = jsonObj.data;
        if(data) {
            if(jsonObj.type == "Story") {
                updateStoryLi(data);
            } else if(jsonObj.type == "Task") {
                updateTaskLi(data);
            } else if(jsonObj.type == "Epic") {
                updateEpicLi(data);
            } else if(jsonObj.type == "Theme") {
                updateThemeLi(data);
            }
        } else {
            window.console && console.log("No json-data in push-message");
        }
    };

    var displayUpdateMsg = function () {
        $.blockUI({
            message: '<h1>Updating...</h1>',
            fadeIn:  0,
            overlayCSS: { backgroundColor: '#808080', cursor: null},
            fadeOut:  350});
    };

    var parents = null;
    var area = null;
    var storyAttr1Options = null;

    //Apply filter from URL if it exists
    var filterIdsParam = getURLParameter('ids');
    if (filterIdsParam != null) {
        $('#filter').val(filterIdsParam);
        $('#filter').focus();
    }

    //Load sorting from cookie if it exists
    var sorting = readCookie("backlogtool-orderby");
    if (sorting != null) {
        $("#order-by").val(sorting);
        if ($("#order-by").val() == null) {
            //Failed to set the cookievalue, revert to prio
            $("#order-by").val("prio");
        }
    }

    // Load checkbox-status for the Archived-checkbox
    var dispArchived = readCookie("backlogtool-disparchived");
    if(dispArchived != null) {
        $('#archived-checkbox').prop('checked', (dispArchived == "checked"));
    }

    var readData = function readData() {
        $.ajax({
            url: "../json/read" + view + "/" + areaName + "?order=" + $("#order-by").val(),
            dataType: 'json',
            async: false,
            success: function (data) {
                parents = data;
            }
        });
        $.ajax({
            url: "../json/readArea/" + areaName,
            dataType: 'json',
            async: false,
            success: function (data) {
                area = data;
            }
        });
        storyAttr1Options = "";
        for (var i=0; i<area.storyAttr1.options.length; ++i) {
            storyAttr1Options += '<option value="'+ area.storyAttr1.options[i].id+ '">'
            + area.storyAttr1.options[i].name + '</option>';
        }
        storyAttr2Options = "";
        for (var i=0; i<area.storyAttr2.options.length; ++i) {
            storyAttr2Options += '<option value="'+ area.storyAttr2.options[i].id+ '">'
            + area.storyAttr2.options[i].name + '</option>';
        }
        storyAttr3Options = "";
        for (var i=0; i<area.storyAttr3.options.length; ++i) {
            storyAttr3Options += '<option value="'+ area.storyAttr3.options[i].id+ '">'
            + area.storyAttr3.options[i].name + '</option>';
        }
        taskAttr1Options = "";
        for (var i=0; i<area.taskAttr1.options.length; ++i) {
            taskAttr1Options += '<option value="'+ area.taskAttr1.options[i].id+ '">'
            + area.taskAttr1.options[i].name + '</option>';
        }
        var emptyOption = '<option value=""></option>';

        $("#storyAttr1").empty().append(emptyOption);
        $("#storyAttr1").append(storyAttr1Options);

        $("#storyAttr2").empty().append(emptyOption);
        $("#storyAttr2").append(storyAttr2Options);

        $("#storyAttr3").empty().append(emptyOption);
        $("#storyAttr3").append(storyAttr3Options);

        $("#taskAttr1").empty().append(emptyOption);
        $("#taskAttr1").append(taskAttr1Options);

    };

    readData();

    Array.prototype.remove = function (value) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == value ||
                    (this[i].id != null && (this[i].id == value.id) || this[i].id == value)) {
                this.splice(i, 1);
                break;
            }
        }
    };

    var visible = new Object();

    var getParent = function (id) {
        for (var i = 0; i < parents.length; ++i) {
            if (parents[i].id == id) {
                return parents[i];
            }
        }
    };

    var replaceParent = function (id, newParent) {
        for (var i = 0; i < parents.length; ++i) {
            if (parents[i].id == id) {
                parents[i] = newParent;
                break;
            }
        }
    };

    var getChild = function (id) {
        for (var i = 0; i < parents.length; ++i) {
            for (var k = 0; k < parents[i].children.length; ++k) {
                if (parents[i].children[k].id == id) {
                    return parents[i].children[k];
                };
            }
        }
    };

    var replaceChild = function (id, newChild) {
        for (var i = 0; i < parents.length; ++i) {
            for (var k = 0; k < parents[i].children.length; ++k) {
                if (parents[i].children[k].id == id) {
                    parents[i].children[k] = newChild;
                    break;
                };
            }
        }
    };

    /**
     * Function to trigger when a long description is expanded.
     */
    var expandText = function (item) {
        extendedDescriptions.push(item.attr("class"));
    };

    /**
     * Function to trigger when a long description is collapsed.
     */
    var collapseText = function (item) {
        extendedDescriptions.remove(item.attr("class"));
    };

    var truncateOptions = {
            max_length: 140,
            more: '...',
            less: 'less',
            onExpand: expandText,
            onCollapse: collapseText
    };

    /**
     * Untruncates a paragraph item and sets a new text in it. 
     */
    var untruncate = function (paragraph, newText) {
        if (paragraph.length == 2) {
            $(paragraph[0]).remove();
            paragraph = $(paragraph[1]);
        }
        paragraph.css({display: "block"});
        paragraph.html(newText);
        return paragraph;
    };

    var focusAndSelectText = function(id) {
        $("#"+id).focus();
        $("#"+id).select();
    };

    $('#order-by').bind('change', function() {
        displayUpdateMsg();
        reload();
        var orderBy = $("#order-by").val();
        if (orderBy == "prio") {
            $("#list-container").sortable("option", "disabled", false);
        } else {
            $("#list-container").sortable("option", "disabled", true);
        }
        createCookie("backlogtool-orderby", orderBy, 60);
    });

    $('#toArea').bind('change', function() {
        var toArea = $("#toArea").val();
        if (toArea != '') {
            moveToArea();
        }
        $("#toArea").val(''); //Reset to default value
    }); 

    var expandClick = function (e) {
        if ($(this).attr("class").indexOf("ui-icon-triangle-1-s") != -1) {
            $(this).removeClass("ui-icon-triangle-1-s");
            $(this).addClass("ui-icon-triangle-1-e");
        } else if ($(this).attr("class").indexOf("ui-icon-triangle-1-e") != -1) {
            $(this).removeClass("ui-icon-triangle-1-e");
            $(this).addClass("ui-icon-triangle-1-s");
        }
        var parent = getParent($(this).closest('li').attr("id"));
        var children = new Array();
        for (var k = 0; k < parent.children.length; k++) {
            var currentChildId = parent.children[k].id;
            children.push(document.getElementById(currentChildId));
            if (visible[currentChildId]) {
                visible[currentChildId] = false;
            } else {
                visible[currentChildId] = true;
            }
        }
        $(children).slideToggle();
        e.stopPropagation();
    };

    var extendedDescriptions = new Array();

    //Read cookie with selected items..
    var selectedItems = new Array();
    try {
        var selectedItemsCookie = JSON.parse(readCookie("backlogtool-selectedItems"));
    } catch(error) {
        selectedItemsCookie = new Array();
    }
    if (selectedItemsCookie != null) {
        for (var i=0; i<selectedItemsCookie.length; ++i) {
            var cookieItem = selectedItemsCookie[i];
            if (!isFilterActive() || isFiltered(cookieItem.id)) {
                if (view == "story-task") {
                    if (cookieItem.type == "story") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "parent";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    } else if (cookieItem.type == "task") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "child";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    } else if (cookieItem.type == "theme") {
                        //Add all stories that are in this theme
                        for (var k=0; k<parents.length; ++k) {
                            var parent = parents[k];
                            if (parent.themeId == cookieItem.id) {
                                var selectedItem = new Object();
                                selectedItem.id = parent.id;
                                selectedItem.type = "parent";
                                if (!contains(selectedItems, selectedItem)) {
                                    selectedItems.push(selectedItem);
                                }
                            }
                        }
                    } else if (cookieItem.type == "epic") {
                        //Add all stories that are in this epic
                        for (var k=0; k<parents.length; ++k) {
                            var parent = parents[k];
                            if (parent.epicId == cookieItem.id) {
                                var selectedItem = new Object();
                                selectedItem.id = parent.id;
                                selectedItem.type = "parent";
                                if (!contains(selectedItems, selectedItem)) {
                                    selectedItems.push(selectedItem);
                                }
                            }
                        }
                    }
                } else if (view == "epic-story") {
                    if (cookieItem.type == "epic") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "parent";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    } else if (cookieItem.type == "story") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "child";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    } else if (cookieItem.type == "theme") {
                        //Add all epics that are in this theme
                        for (var k=0; k<parents.length; ++k) {
                            var parent = parents[k];
                            if (parent.themeId == cookieItem.id) {
                                var selectedItem = new Object();
                                selectedItem.id = parent.id;
                                selectedItem.type = "parent";
                                if (!contains(selectedItems, selectedItem)) {
                                    selectedItems.push(selectedItem);
                                }
                            }
                        }
                    }
                } else if (view == "theme-epic") {
                    if (cookieItem.type == "theme") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "parent";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    } else if (cookieItem.type == "epic") {
                        var selectedItem = new Object();
                        selectedItem.id = cookieItem.id;
                        selectedItem.type = "child";
                        if (!contains(selectedItems, selectedItem)) {
                            selectedItems.push(selectedItem);
                        }
                    }
                }
            }
        }
    }

    var editingItems = new Array();
    var lastPressed = null;

    var liClick = function (pressed) {
        if (pressed.type != null) {
            //If the method was triggered by an event,
            //then use $(this) as the pressed element
            pressed = $(this);
        }

        if (isShift && isCtrl) {
            isCtrl = false;
        }

        if (!isCtrl ||
                (selectedItems[0] != null && pressed.attr("class").indexOf(selectedItems[0].type) == -1)) {
            //If Ctrl was not held down or the type pressed was not same as last time,
            //then reset all selected items.
            deleteCookie("backlogtool-selectedItems");
            selectedItems = new Array();
            $(".parent-child-list").children("li").removeClass("ui-selected");
        }

        if (pressed.attr("class").indexOf("parent") != -1) {
            //Parent was selected
            if (pressed.attr("class").indexOf("ui-selected") != -1) {
                //Already selected
                pressed.removeClass("ui-selected");
                selectedItems.remove({id:pressed.attr("id")});
            } else {
                //Not already selected
                pressed.addClass("ui-selected");
                selectedItems.push({id:pressed.attr("id"), type:"parent"});
            }
        } else {
            //Child was selected
            if (pressed.attr("class").indexOf("ui-selected") != -1) {
                //Already selected
                pressed.removeClass("ui-selected");
                selectedItems.remove({id:pressed.attr("id")});
            } else {
                //Not already selected
                pressed.addClass("ui-selected");
                selectedItems.push({id:pressed.attr("id"), type:"child"});
            }
        }
        lastPressed = pressed;
        updateCookie();
    };

    /**
     * The following click event handler unselects all items if the user presses
     * outside both the li elements and the important parts of the header.
     */
    $(document).click(function(event) {
        if ($(event.target).closest('li, a, input, button, select').length == 0) {
            selectedItems = new Array();
            $("ul > li.ui-selected").removeClass("ui-selected");
            updateCookie();
        }
    });

    /**
     * Gets the most recently selected item.
     * Optional argument is type specification ("child" or "parent").
     */
    var getLastSelected = function(type) {
        var i = selectedItems.length;
        if (i > 0) {
            if (arguments.length == 0) {
                return selectedItems[i-1];
            }
            while(i--) {
                if (selectedItems[i].type == type) {
                    return selectedItems[i];
                }
            }
        }
        return null;
    };

    /**
     * Gets the last selected item and returns it directly if it's a parent.
     * If it's a child, then it returns the parent of that item.
     */
    var getParentOfLastSelected = function() {
        var lastSelected = getLastSelected();
        if (lastSelected != null && lastSelected.type == "child") {
            //Find the parent of the last selected child
            var lastChild = getChild(lastSelected.id);
            if (lastChild != null) {
                var lastParentId = (lastChild.parentId
                        || lastChild.epicId || lastChild.themeId);
                lastSelected = new Object();
                lastSelected.type = "parent";
                lastSelected.id = lastParentId; 
            }
        }
        return lastSelected;
    };

    var createTask = function(event) {
        displayUpdateMsg();
        ignorePush = true;
        var task = new Object();
        task.parentId = event.target.id;
        task.lastItem = getLastSelected("child");

        $.ajax({
            url : "../json/createtask/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(task),
            contentType : "application/json; charset=utf-8",
            success : function(newId) {
                if (newId != null) {
                    visible[newId] = true;
                    selectedItems = new Array();
                    selectedItems.push({id : newId, type : "child"});
                    updateCookie();
                }
                reload();
                editTask(newId);
                scrollTo(newId);
                focusAndSelectText("taskTitle"+newId);
            },
            error : function(request, status, error) {
                alert(error);
                reload();
            }
        });
        event.stopPropagation();
    };

    var createStory = function(event) {
        displayUpdateMsg();
        ignorePush = true;
        var storyContainer = new Object();
        storyContainer.added = new Date();

        if (view == "epic-story") {
            newStoryEpicID = event.target.id;
            var epic = getParent(newStoryEpicID);
            storyContainer.epicTitle = epic.title;
            storyContainer.themeTitle = epic.themeTitle;
            storyContainer.lastItem = getLastSelected("child");
        } else {
            storyContainer.lastItem = getParentOfLastSelected();
        }
        $.ajax({
            url : "../json/createstory/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(storyContainer),
            contentType : "application/json; charset=utf-8",
            success : function(newId) {
                if (newId != null) {
                    visible[newId] = true;
                    selectedItems = new Array();
                    if (view == "story-task") {
                        selectedItems.push({id : newId, type : "parent"});
                    } else if (view == "epic-story") {
                        selectedItems.push({id : newId, type : "child"});
                    }
                    updateCookie();
                }
                reload();
                editStory(newId);
                scrollTo(newId);
                focusAndSelectText("title"+newId);
            },
            error : function(error) {
                alert(error);
                reload();
            }
        });
        if (event != null) {
            event.stopPropagation();
        }
    };

    var createEpic = function(event) {
        displayUpdateMsg();
        ignorePush = true;
        var epicContainer = new Object();

        if (view == "theme-epic") {
            newEpicThemeID = event.target.id;
            var theme = getParent(newEpicThemeID);
            epicContainer.themeTitle = theme.title;
            epicContainer.lastItem = getLastSelected("child");
        } else {
            epicContainer.lastItem = getParentOfLastSelected();
        }

        $.ajax({
            url : "../json/createepic/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(epicContainer),
            contentType : "application/json; charset=utf-8",
            success : function(newId) {
                if (newId != null) {
                    visible[newId] = true;
                    selectedItems = new Array();
                    if (view == "epic-story") {
                        selectedItems.push({id : newId, type : "parent"});
                    } else if (view == "theme-epic") {
                        selectedItems.push({id : newId, type : "child"});
                    }
                    updateCookie();
                }
                reload();
                editEpic(newId);
                scrollTo(newId);
                focusAndSelectText("epicTitle" + newId);
            },
            error : function(error) {
                alert(error);
                reload();
            }
        });
        if (event != null) {
            event.stopPropagation();
        }
    };

    var createTheme = function(event) {
        displayUpdateMsg();
        ignorePush = true;
        var themeContainer = new Object();
        themeContainer.lastItem = getParentOfLastSelected();

        $.ajax({
            url : "../json/createtheme/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(themeContainer),
            contentType : "application/json; charset=utf-8",
            success : function(newId) {
                if (newId != null) {
                    visible[newId] = true;
                    selectedItems = new Array();
                    selectedItems.push({id : newId, type : "parent"});
                    updateCookie();
                }
                reload();
                editTheme(newId);
                scrollTo(newId);
                focusAndSelectText("themeTitle" + newId);
            },
            error : function(error) {
                alert(error);
                reload();
            }
        });
    };

    /**
     * Used when pressing the clone icon
     */
    var cloneItem = function(clickedElement, withChildren) {
        displayUpdateMsg();
        var id = clickedElement.attr("id");

        var type = '';
        if (clickedElement.hasClass('story')) {
            type = 'Story';
        } else if (clickedElement.hasClass('epic')) {
            type = 'Epic';
        } else if (clickedElement.hasClass('theme')) {
            type = 'Theme';
        }

        var familyMember = '';
        if (clickedElement.closest("li").hasClass("parentLi")) {
            familyMember = 'parent';
        } else if (clickedElement.closest("li").hasClass("childLi")) {
            familyMember = 'child';
        }

        var clonedItem = new Object();
        clonedItem.id = id;
        clonedItem.withChildren = withChildren;

        $.ajax({
            url : "../json/clone" + type + "/" + areaName,
            type : 'POST',
            data: clonedItem,
            success : function(newId) {
                if (newId != null) {
                    visible[newId] = true;
                    selectedItems = new Array();
                    selectedItems.push({id : newId, type : familyMember});
                    updateCookie();
                }
                reload();
                editStory(newId);
                scrollTo(newId);
            },
            error : function(error) {
                alert(error);
                reload();
            }
        });
    };

    var deleteItem = function (event) {
        itemId = event.target.id;
        var item = $(event.target).closest('li');
        if (item.hasClass("task")) {
            item = "task";
        } else if (item.hasClass("story")) {
            item = "story";
        } else if (item.hasClass("epic")) {
            item = "epic";
        } else if (item.hasClass("theme")) {
            item = "theme";
        };

        $('#delete-item').attr("title","Delete "+item);
        $("#deleteDescription").html("Are you sure you want to delete this " + item + "?");
        $('#delete-item').dialog({
            resizable: false,
            minHeight: 0,
            modal: true,
            buttons: {
                Delete: function() {
                    displayUpdateMsg();
                    $.ajax({
                        url: "../json/delete" + item + "/" + areaName,
                        type: 'POST',
                        dataType: 'json',
                        data: JSON.stringify(parseInt(itemId)),
                        contentType: "application/json; charset=utf-8",
                        success: function (data) {
                            reload();
                        },
                        error: function (request, status, error) {
                            alert(error);
                            reload();
                        }
                    });
                    $(this).dialog("close");
                },
                Cancel: function() {
                    $(this).dialog("close");
                }
            }
        });
        event.stopPropagation();
    };

    /**
     * Cancel current editing of parents/children.
     */
    var cancel = function(event) {
        var id = event.data.id;
        editingItems.remove({id:id});
        $("."+id).toggleClass('hidden-edit');

        //Re-activate double click
        var li = $("li#"+id);
        if (li.hasClass("task")) {
            li.dblclick(editTask);
        } else if (li.hasClass("story")) {
            li.dblclick(editStory);
        } else if (li.hasClass("epic")) {
            li.dblclick(editEpic);
        } else if (li.hasClass("theme")) {
            li.dblclick(editTheme);
        }       
        updateWhenItemsClosed();
    };

    /**
     * Cancel current editing of parents/children.
     */
    var bulkCancel = function() {            
        editingItems = new Array();
        updateWhenItemsClosed();
    };

    /**
     * Save current editing of parents/children.
     */
    var bulkSave = function() {
        displayUpdateMsg();
        var pushUpdate = false;
        while (editingItems.length > 0) {
            //If last item, we want to trigger a push update
            if (editingItems.length == 1) {               
                pushUpdate = true;
            }
            var lastElement = $(editingItems).last()[0];
            var id = eval(lastElement.id);

            if (lastElement.type == "task") {
                saveTask(id, pushUpdate);
            } else if (lastElement.type == "story") {
                saveStory(id, pushUpdate);
            } else if (lastElement.type == "epic") {
                saveEpic(id, pushUpdate);
            } else if (lastElement.type == "theme") {
                saveTheme(id, pushUpdate);
            }

        }
        editingItems = new Array();
        reload();
        ignorePush = false;
    };

    /**
     * Returns true if you are going into edit mode on a parent/child.
     */
    var isGoingIntoEdit = function isGoingIntoEdit(editId){
        for(var i = 0; editingItems.length > i; i++) {
            if (editingItems[i].id == editId) {
                return false;
            }
        }
        return true;
    };

    var editStory = function(event) {
        var storyId = null;
        if (typeof event == "number") {
            storyId = event;
        } else {
            storyId = $(this).attr('id');
        }
        if (view == "story-task") {
            var story = getParent(storyId);
        } else {
            story = getChild(storyId);
        }
        if (isGoingIntoEdit(storyId)) {
            $("li#"+storyId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:storyId, type:"story"});
            ignorePush = true;
            $('button.'+storyId).button();
            $('button.'+storyId).unbind();
            //$('.save-button.'+storyId).button( "option", "disabled", true );

            $('.cancelButton.'+storyId).click({id: storyId},cancel);
            $('.save-button.'+storyId).click( function() {
                saveStory(parseInt(storyId), true);
            });

            //Sets values for all edit fields
            $("#deadline"+storyId).datepicker({ showWeek: true, firstDay: 1, dateFormat: "yy-mm-dd" });
            $("#added"+storyId).datepicker({ showWeek: true, firstDay: 1, dateFormat: "yy-mm-dd" });

            if (story.deadline == null) {
                $("#deadline"+storyId).val("");
            } else {
                $("#deadline"+storyId).datepicker('setDate', new Date(story.deadline));
            }
            if (story.added == null) {
                $("#added"+storyId).val("");
            } else {
                $("#added"+storyId).datepicker('setDate', new Date(story.added));
            }

            $("."+storyId).toggleClass('hidden-edit');

            if (story.storyAttr1 != null) {
                $("select#storyAttr1"+storyId).val(story.storyAttr1.id);
            }
            if (story.storyAttr2 != null) {
                $("select#storyAttr2"+storyId).val(story.storyAttr2.id);
            }
            if (story.storyAttr3 != null) {
                $("select#storyAttr3"+storyId).val(story.storyAttr3.id);
            }
            $("select#customerSite"+storyId).val(story.customerSite);
            $("select#contributorSite"+storyId).val(story.contributorSite);
            $("archiveStory"+storyId).val(story.archived);

            $("textarea#theme"+storyId).autocomplete({
                minLength: 0,
                source: "../json/autocompletethemes/" + areaName,
                change: function() {
                    $("textarea#epic"+storyId).attr("value", "");
                },
                select: function (event, data) {
                    //$('.save-button').button( "option", "disabled", false );
                    $("textarea#epic"+storyId).attr("value", "");
                    //Used for deselecting the input field.
                    $(this).autocomplete('disable');
                    $(this).autocomplete('enable');
                }
            });
            $("textarea#epic"+storyId).autocomplete({
                minLength: 0,
                search: function() {
                    var themeName = $("textarea#theme"+storyId).val();
                    $("textarea#epic"+storyId).autocomplete({source: "../json/autocompleteepics/" + areaName + "?theme=" + themeName});
                },
                select: function (event, data) {
                    //$('.save-button').button( "option", "disabled", false );
                    $("textarea#epic"+storyId).attr("value", "");

                    //Used for deselecting the input field.
                    $(this).autocomplete('disable');
                    $(this).autocomplete('enable');
                }
            });

            $("textarea#theme"+storyId).focus(function() {
                $("textarea#theme"+storyId).autocomplete("search", $("textarea#theme"+storyId).val());
            });

            $("textarea#epic"+storyId).focus(function() {
                $("textarea#epic"+storyId).autocomplete("search", $("textarea#epic"+storyId).val());
            });

            //auto resize the textareas to fit the text
            $('textarea'+"."+storyId).autosize('');
        } else {
            editingItems.remove({id:storyId});
            $("."+storyId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
        }
    };

    var saveStory = function(event, pushUpdate) {
        var storyId = null;
        if (typeof event == "number") {
            storyId = event;
        } else {
            storyId = event.data.storyId;
        }
        var li = $('#'+storyId);

        //Creates a new story and sets all updated values
        var story = new Object();
        story.id = eval(storyId);
        story.title = $('#title' + storyId).val();
        story.description = $('#description' + storyId).val();
        story.customerSite = $('#customerSite' + storyId).val();
        story.contributorSite = $('#contributorSite' + storyId).val();
        story.customer = $('#customer' + storyId).val();
        story.contributor = $('#contributor' + storyId).val();
        story.epicTitle = $('#epic' + storyId).val();
        story.themeTitle = $('#theme' + storyId).val();
        story.added = new Date($('#added' + storyId).val());
        story.archived = $('#archiveStory' + storyId).is(':checked');
        story.deadline = new Date($('#deadline' + storyId).val());
        story.storyAttr1Id = $('#storyAttr1' + storyId).val();
        story.storyAttr2Id = $('#storyAttr2' + storyId).val();
        story.storyAttr3Id = $('#storyAttr3' + storyId).val();

        $.ajax({
            url: "../json/updatestory/"+areaName+"?pushUpdate="+pushUpdate,
            type: 'POST',
            dataType: 'json',
            async: false,
            data: JSON.stringify(story),
            contentType: "application/json; charset=utf-8",
            success: function (updatedStory) {
                //Set updated values, we prefer to not reload the whole page.
                updateStoryLi(updatedStory);

                if (view == "story-task") {
                    //If story was moved,
                    //check if this story is in list-container or in archived.list-container and moves it.
                    if (getParent(storyId).archived != updatedStory.archived) {
                        li.fadeOut("normal", function() {
                            if (li.parent('#list-container').length) {
                                //move all children and parent
                                $('#list-container > [parentId="'+storyId+'"]').prependTo('#archived-list-container');
                                li.prependTo('#archived-list-container').fadeIn();
                            } else {
                                //move all children and parent
                                li.appendTo('#list-container').fadeIn();
                                $('#archived-list-container > [parentId="'+storyId+'"]').appendTo('#list-container');
                            }
                            exitEditMode(storyId);
                            addZebraStripesToParents();
                        });
                    } else {
                        exitEditMode(storyId);
                    }
                    //Replacing story with a new one
                    replaceParent(storyId, updatedStory);
                } else if (updatedStory.archived) {
                    //Save this for issue #44
                    //li.fadeOut("normal", function() {
                    //replaceChild(storyId, updatedStory);
                    exitEditMode(storyId);
                    //});
                } else {
                    exitEditMode(storyId);
                }
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editStory);
    };

    /**
     * Finds and updates a story li-element with new values.
     */
    var updateStoryLi = function(updatedStory) {
        var storyId = updatedStory.id;
        
        var storyLi = $('li#' + storyId + ".story");
        if(storyLi.length == 0) {
            window.console && console.log("Clone up a new story-li");
            // clone up placeholder
        }
        
        $('.titles, .titles-epic-story').find('p.titleText.'+storyId).html(updatedStory.title);
        $('.titles, .titles-epic-story').find('p.theme.'+storyId).html((updatedStory.themeTitle != undefined) ? updatedStory.themeTitle : "");
        $('.titles, .titles-epic-story').find('p.epic.'+storyId).html((updatedStory.epicTitle != undefined) ? updatedStory.epicTitle : "");

        //Re-add truncate on the description paragraph
        var descriptionParagraph = $('.titles, .titles-epic-story, .titles-theme-epic').find('p.description.'+storyId);
        descriptionParagraph = untruncate(descriptionParagraph, updatedStory.description);
        descriptionParagraph.truncate(
                $.extend({}, truncateOptions, {className: 'truncate'+storyId})
        );
        if (extendedDescriptions.indexOf('truncate'+storyId) != -1) {
            $('a.truncate'+storyId, descriptionParagraph.parent()).click();
        }

        $('.stakeholders').find('p.customerSite.'+storyId).empty().append(getSiteImage(updatedStory.customerSite));
        $('.stakeholders').find('p.customer.'+storyId).html(updatedStory.customer);

        $('.stakeholders').find('p.contributorSite.'+storyId).empty().append(getSiteImage(updatedStory.contributorSite));
        $('.stakeholders').find('p.contributor.'+storyId).html(updatedStory.contributor);

        $('.times').find('p.added.' + storyId).html(getDate(updatedStory.added));
        $('.times').find('p.deadline.' + storyId).html(getDate(updatedStory.deadline));

        $('.story-attr1-2').find('p.story-attr1.' + storyId).empty().append(getAttrImage(updatedStory.storyAttr1)).append(getNameIfExists(updatedStory.storyAttr1));
        $('.story-attr1-2').find('p.story-attr2.' + storyId).empty().append(getAttrImage(updatedStory.storyAttr2)).append(getNameIfExists(updatedStory.storyAttr2));
        $('.story-attr3').find('p.story-attr3.' + storyId).empty().append(getAttrImage(updatedStory.storyAttr3)).append(getNameIfExists(updatedStory.storyAttr3));
    };

    /**
     * Exit edit mode on a backlog item
     */
    var exitEditMode = function(id) {
        $("."+id).toggleClass('hidden-edit');
        editingItems.remove({id:id});
        updateWhenItemsClosed();
    };

    var saveTask = function(event, pushUpdate) {
        var taskId;
        if (typeof event == "number") {
            taskId = event;
        }
        else {
            taskId = event.data.taskId;
        }
        var li = $('#'+taskId);

        //Creates a new task and sets all updated values
        var task = new Object();
        task.id = taskId;
        task.title = $("textarea#taskTitle"+taskId).val();
        task.owner = $("textarea#taskOwner"+taskId).val();
        task.calculatedTime = $("select#calculatedTime"+taskId).val();

        task.taskAttr1Id = $('#taskAttr1' + taskId).val();

        $.ajax({
            url: "../json/updatetask/"+areaName+"?pushUpdate="+pushUpdate,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(task),
            contentType: "application/json; charset=utf-8",
            success: function (updatedTask) {
                //Set the updated values
                updateTaskLi(updatedTask);

                replaceChild(taskId, updatedTask);
                exitEditMode(taskId);
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editTask);
    };

    /**
     * Finds and updates a task li-element with new values.
     */
    var updateTaskLi = function(updatedTask) {
        var taskId = updatedTask.id;
        $(".taskOwner."+taskId).find("p.taskInfo").html(updatedTask.owner);
        $(".calculatedTime."+taskId).find("p.taskInfo").html(updatedTask.calculatedTime);
        $(".taskStatus."+taskId).find("p.taskInfo").empty().append(getAttrImage(updatedTask.taskAttr1)).append(getNameIfExists(updatedTask.taskAttr1));

        //Re-add truncate on the title paragraph
        var titleParagraph = $(".taskTitle."+taskId).find("p.taskInfo");
        titleParagraph = untruncate(titleParagraph, updatedTask.title);
        titleParagraph.truncate(
                $.extend({}, truncateOptions, {className: 'truncate'+taskId, max_length: 90})
        );
        if (extendedDescriptions.indexOf('truncate'+taskId) != -1) {
            $('a.truncate'+taskId, titleParagraph.parent()).click();
        }
    };

    var editTask = function(event) {
        var taskId = null;
        if (typeof event == "number") {
            taskId = event;
        }
        else {
            taskId = $(this).closest('li').attr('id');
        }
        var task = getChild(taskId);
        if (isGoingIntoEdit(taskId)) {
            $("li#"+taskId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:taskId, type:"task"});
            ignorePush = true;
            $('button.'+taskId).button();
            $('button.'+taskId).unbind();
            $('.cancelButton.'+taskId).click({id: taskId},cancel);
            $('.save-button.'+taskId).click(function() {
                saveTask(parseInt(taskId), true);
            });
            $("."+taskId).toggleClass('hidden-edit');
            //sets values for all edit fields
            $("textarea#taskTitle" + taskId).html(task.title);
            $("textarea#taskDescription" + taskId).html(task.description);
            $("select#calculatedTime" + taskId).val(task.calculatedTime);

            if (task.taskAttr1 != null) {
                $("select#taskAttr1" + taskId).val(task.taskAttr1.id);
            }

            //auto resize the textareas to fit the text
            $('textarea'+"."+taskId).autosize('');
        } else {
            editingItems.remove({id:taskId});
            $("."+taskId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
            //Slide toggle fix
            $('#'+taskId).css("height", $('#'+taskId).height());
        }
    };

    var saveEpic = function(event, pushUpdate) {
        var epicId;
        if (typeof event == "number") {
            epicId = event;
        }
        else {
            epicId = event.data.epicId;
        }
        var li = $('#'+epicId);

        //Creates a new epic and sets all updated values
        var epic = new Object();
        epic.id = eval(epicId);
        epic.title = $("textarea#epicTitle"+epicId).val();
        epic.description = $("textarea#epicDescription"+epicId).val();
        epic.themeTitle = $("textarea#epicTheme"+epicId).val();
        epic.archived = $('#archiveEpic' + epicId).is(':checked');

        $.ajax({
            url: "../json/updateepic/"+areaName+"?pushUpdate="+pushUpdate,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(epic),
            contentType: "application/json; charset=utf-8",
            success: function (updatedEpic) {

                updateEpicLi(updatedEpic);

                if (view == "epic-story") {
                    //If epic was moved,
                    //check if this epic is in list-container or in archived.list-container and moves it.
                    if (getParent(epicId).archived != updatedEpic.archived) {
                        li.fadeOut("normal", function() {
                            if (li.parent('#list-container').length) {
                                //move all children and parent
                                $('#list-container > [parentId="'+epicId+'"]').prependTo('#archived-list-container');
                                li.prependTo('#archived-list-container').fadeIn();
                            } else {
                                //move all children and parent
                                li.appendTo('#list-container').fadeIn();
                                $('#archived-list-container > [parentId="'+epicId+'"]').appendTo('#list-container');
                            }
                            exitEditMode(epicId);
                            addZebraStripesToParents();
                        });
                    } else {
                        exitEditMode(epicId);
                    }
                    //Replacing story with a new one
                    replaceParent(epicId, updatedEpic);
                } else if (updatedEpic.archived) {
                    //Save this for issue #44
                    //li.fadeOut("normal", function() {
                    exitEditMode(epicId);
                    //});
                } else {
                    exitEditMode(epicId);
                }
                replaceChild(epicId, updatedEpic);
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editEpic);
    };

    /**
     * Finds and updates a epic li-element with new values.
     */
    var updateEpicLi = function(updatedEpic) {
        var epicId = updatedEpic.id;
        $('.titles-epic-story, .titles-theme-epic').find('p.theme.'+epicId).html((updatedEpic.themeTitle != undefined) ? updatedEpic.themeTitle : "");
        $('.titles-epic-story, .titles-theme-epic').find('p.titleText.'+epicId).html(updatedEpic.title);

        //Re-add truncate on the description paragraph
        var descriptionParagraph = $('.titles-epic-story, .titles-theme-epic').find('p.description.'+epicId);
        descriptionParagraph = untruncate(descriptionParagraph, updatedEpic.description);
        descriptionParagraph.truncate(
                $.extend({}, truncateOptions, {className: 'truncate'+epicId, max_length: 90})
        );
        if (extendedDescriptions.indexOf('truncate'+epicId) != -1) {
            $('a.truncate'+epicId, descriptionParagraph.parent()).click();
        }
    };

    var editEpic = function(event) {
        var epicId = null;
        if (typeof event == "number") {
            epicId = event;
        }
        else {
            epicId = $(this).closest('li').attr('id');
        }
        if (view == "epic-story") {
            var epic = getParent(epicId);
        } else {
            epic = getChild(epicId);
        }

        if (isGoingIntoEdit(epicId)) {
            $("li#"+epicId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:epicId, type:"epic"});
            ignorePush = true;

            $('button.'+epicId).button();
            $('button.'+epicId).unbind();
            $('.cancelButton.'+epicId).click({id: epicId},cancel);
            $('.save-button.'+epicId).click(function() {
                saveEpic(parseInt(epicId), true);
            });
            $("."+epicId).toggleClass('hidden-edit');

            $("textarea#epicTheme"+epicId).autocomplete({
                minLength: 0,
                source: "../json/autocompletethemes/" + areaName,
                select: function (event, data) {
                    //$('.save-button').button( "option", "disabled", false );
                    //Used for deselecting the input field.
                    $(this).autocomplete('disable');
                    $(this).autocomplete('enable');
                }
            });

            $("textarea#epicTheme"+epicId).focus(function() {
                $("textarea#epicTheme"+epicId).autocomplete("search", $("textarea#epicTheme"+epicId).val());
            });

            //auto resize the textareas to fit the text
            $('textarea'+"."+epicId).autosize('');
        } else {
            editingItems.remove({id:epicId});
            $("."+epicId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
        }
    };

    var saveTheme = function(event, pushUpdate) {
        var themeId;
        if (typeof event == "number") {
            themeId = event;
        }
        else {
            themeId = event.data.themeId;
        }
        var li = $('#'+themeId);

        //Creates a new epic and sets all updated values
        var theme = new Object();
        theme.id = eval(themeId);
        theme.title = $("textarea#themeTitle"+themeId).val();
        theme.description = $("textarea#themeDescription"+themeId).val();
        theme.archived = $('#archiveTheme' + themeId).is(':checked');

        $.ajax({
            url: "../json/updatetheme/"+areaName+"?pushUpdate="+pushUpdate,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(theme),
            contentType: "application/json; charset=utf-8",
            success: function (updatedTheme) {

                updateThemeLi(updatedTheme);

                //if theme was moved from or to archive
                if (getParent(themeId).archived != updatedTheme.archived) {
                    //Checks if this theme is in list-container or in archived.list-container and moves it.
                    li.fadeOut("normal", function() {
                        if (li.parent('#list-container').length) {
                            //move all children and parent
                            $('#list-container > [parentId="'+themeId+'"]').prependTo('#archived-list-container');
                            li.prependTo('#archived-list-container').fadeIn();
                        } else {
                            //move all children and parent
                            li.appendTo('#list-container').fadeIn();
                            $('#archived-list-container > [parentId="'+themeId+'"]').appendTo('#list-container');
                        }
                        exitEditMode(themeId);
                        addZebraStripesToParents();
                    });
                } else {
                    exitEditMode(themeId);
                }
                //Replacing theme with a new one
                replaceParent(themeId, updatedTheme);
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editTheme);
    };

    /**
     * Finds and updates a theme li-element with new values.
     */
    var updateThemeLi = function(updatedTheme) {
        var themeId = updatedTheme.id;
        $('.titles-theme-epic').find('p.titleText.'+themeId).html(updatedTheme.title);

        //Re-add truncate on the description paragraph
        var descriptionParagraph = $('.titles-theme-epic').find('p.description.'+themeId);
        descriptionParagraph = untruncate(descriptionParagraph, updatedTheme.description);
        descriptionParagraph.truncate(
                $.extend({}, truncateOptions, {className: 'truncate'+themeId, max_length: 90})
        );
        if (extendedDescriptions.indexOf('truncate'+themeId) != -1) {
            $('a.truncate'+themeId, descriptionParagraph.parent()).click();
        }
    };

    var editTheme = function(event) {
        var themeId = null;
        if (typeof event == "number") {
            themeId = event;
        } else {
            themeId = $(this).closest('li').attr('id');
        }
        if (view == "theme-epic") {
            var theme = getParent(themeId);
        } else {
            theme = getChild(themeId);
        }
        if (isGoingIntoEdit(themeId)) {
            $("li#"+themeId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:themeId, type:"theme"});
            ignorePush = true;

            $('button.'+themeId).button();
            $('button.'+themeId).unbind();
            $('.cancelButton.'+themeId).click({id: themeId},cancel);
            $('.save-button.'+themeId).click(function() {
                saveTheme(parseInt(themeId), true);
            });
            $("."+themeId).toggleClass('hidden-edit');

            //auto resize the textareas to fit the text
            $('textarea'+"."+themeId).autosize('');
        } else {
            editingItems.remove({id:themeId});
            $("."+themeId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
        }
    };

    /**
     * Used when moving stories to another area.
     */
    var moveToArea = function() {
        $('#settings').dropdown('hide');
        var newArea = $("#toArea").val();

        var storiesToMove = new Array();
        for (var i = 0; i < selectedItems.length; i++) {
            if (selectedItems[i].type == "parent") {
                storiesToMove.push(selectedItems[i].id);
            }
        }
        if (storiesToMove.length > 0) {
            var moveStoryDialog = $(document.createElement('div'));
            $(moveStoryDialog).attr('title', 'Move stories to area');
            $(moveStoryDialog).html('<p>Do you want to move the ' + storiesToMove.length
                    + ' selected item(s) to ' + newArea + '?</p>');
            $(moveStoryDialog).dialog({
                resizable : false,
                minHeight : 0,
                modal : true,
                buttons : {
                    "Move stories" : function() {
                        $.ajax({
                            url: "../json/moveToArea" + "/" + areaName + "?newAreaName="+newArea,
                            type: 'POST',
                            dataType: 'json',
                            data: JSON.stringify(storiesToMove),
                            contentType: "application/json; charset=utf-8",
                            success: function (data) {
                                reload();
                                selectedItems = new Array();
                            },
                            error: function (request, status, error) {
                                alert(error);
                                reload();
                            }
                        });
                        $(this).dialog("close");
                    },
                    Cancel : function() {
                        $(this).dialog("close");
                    }
                }
            });
        } else {
            var noStoriesDialog = $(document.createElement('div'));
            $(noStoriesDialog).attr('title', 'No stories selected');
            $(noStoriesDialog).html('<p>Please select stories to move before using this option!</p>');
            noStoriesDialog.dialog({
                modal: true,
                width: 325,
                minHeight: 0,
                buttons: {
                    Ok: function() {
                        $( this ).dialog( "close" );
                    }
                }
            });
        }
    }; 

    /**
     * Updates save all button and enable ranking etc, when the last editing item is going out of edit mode.
     */
    var updateWhenItemsClosed = function() {
        if (editingItems.length == 0) {
            displayUpdateMsg();
            $("#list-container").sortable( "option", "disabled", false );
            //$('.save-button').button( "option", "disabled", true );
            reload();
            ignorePush = false;
        }
    };

    /**
     * Prints the topic for archived parent's when the lists is generated.
     */
    var getArchivedTopic = function(archived, id) {
        if (archived) {
            return '<p class="title ' + id + '">Archived</p>';
        } else return '';
    };

    /**
     * A list that is appended to the site is being generated.
     * @param boolean archived true if list with archived parents is generated
     * @return string newContainer a string that holds the list
     */
    var generateList = function(archived) {
        newContainer = '';
        for (var k = 0; k < parents.length; ++k) {
            currentParent = parents[k];
            if (!isFilterActive() || isFiltered(currentParent.id)) {
                if (currentParent.archived == archived) {

                    //Check if at least one child is visible
                    var oneVisible = false;
                    for (var i = 0; i < currentParent.children.length; ++i) {
                        if (visible[currentParent.children[i].id] == true) {
                            oneVisible = true;
                            break;
                        }
                    }
                    //Sets all children of same group as visible if at least one was visible
                    if (oneVisible) {
                        for (var i = 0; i < currentParent.children.length; ++i) {
                            visible[currentParent.children[i].id] = true;
                        }
                    }

                    var icon = '';

                    if (currentParent.children.length > 0) {
                        if (oneVisible) {
                            icon = 'expand-icon ui-icon ui-icon-triangle-1-s';
                        } else {
                            icon = 'expand-icon ui-icon ui-icon-triangle-1-e';
                        }
                    }
                    if (view == "story-task") {
                        var list = '<div id="icons">'
                            +'<div title="Show tasks" class="icon ' + icon + '">'
                            +'</div>'
                            +'<a id="' + currentParent.id + '" title="Create new task" class="icon createTask add-child-icon"></a><br>'
                            +'<a id="' + currentParent.id + '" title="Clone this story excluding tasks" class="cloneItem story"><img src="../resources/image/page_white_copy.png"></a>'
                            +'<a id="' + currentParent.id + '" title="Clone this story including tasks" class="cloneItem-with-children story"><img src="../resources/image/page_white_stack.png"></a>'
                            +'</div>'
                            //TITLE FIELDS
                            +'<div class="titles">'
                            //TYPE MARK START
                            +'<p class="typeMark">Story ' + currentParent.id + '</p>'
                            //TYPE MARK END
                            //THEME START
                            +'<p class="theme ' + currentParent.id + '">' + replaceNullWithEmpty(currentParent.themeTitle) + '</p>'
                            +'<textarea placeholder="Theme" id="theme'+currentParent.id+'" class="bindChange theme hidden-edit ' + currentParent.id + '" rows="1"maxlength="100">' + replaceNullWithEmpty(currentParent.themeTitle) + '</textarea>'
                            //THEME END
                            //EPIC START
                            +'<p class="epic ' + currentParent.id + '">' + replaceNullWithEmpty(currentParent.epicTitle) + '</p>'
                            +'<textarea placeholder="Epic" id="epic'+currentParent.id+'" class="bindChange epic hidden-edit ' + currentParent.id + '" rows="1" maxlength="100">' + replaceNullWithEmpty(currentParent.epicTitle) + '</textarea>'
                            //EPIC END
                            +'<br style="clear:both" />'
                            //STORY TITLE START
                            +'<p class="titleText ' + currentParent.id + '">' + currentParent.title + '</p>'
                            +'<textarea placeholder="Title" id="title'+currentParent.id+'" class="bindChange titleText hidden-edit title ' + currentParent.id + '" rows="1" maxlength="100">' + currentParent.title + '</textarea>'
                            //STORY TITLE END
                            //STORYDESCRIPTION START
                            +'<p class="description story-description ' + currentParent.id + '">' + addLinksAndLineBreaks(currentParent.description) + '</p>'
                            +'<textarea placeholder="Description" id="description'+currentParent.id+'" class="bindChange hidden-edit description ' + currentParent.id + '" rows="2" maxlength="100000">' + currentParent.description + '</textarea>'
                            //STORYDESCRIPTION END
                            +'</div>'
                            //TITLE FIELDS END
                            //STAKEHOLDER DIV START
                            +'<div class="stakeholders">'
                            //CUSTOMER FIELD START
                            +'<p class="title">Customer </p>'
                            +'<p class="customerSite ' + currentParent.id + '">'+getSiteImage(currentParent.customerSite)+'</p>'
                            +'<p class="' + currentParent.id + ' customer description">' + currentParent.customer + '</p>'
                            +'<select id="customerSite'+currentParent.id+'" class="bindChange customerSite hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value="NONE"></option>'
                            +'<option value="Beijing">Beijing</option>'
                            +'<option value="Tokyo">Tokyo</option>'
                            +'<option value="Lund">Lund</option>'
                            +'</select>'
                            +'<input placeholder="Department" id="customer'+currentParent.id+'" class="bindChange customer hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all" maxlength="50" value="'+currentParent.customer+'"></input>'
                            //CUSTOMER FIELD END
                            //CONTRIBUTOR FIELD START
                            +'<p class="title">Contributor </p>'
                            +'<p id="'+currentParent.id+'" class="contributorSite ' + currentParent.id + '">'+getSiteImage(currentParent.contributorSite)+'</p>'
                            +'<p class="' + currentParent.id + ' contributor description">' + currentParent.contributor + '</p>'
                            +'<select id="contributorSite'+currentParent.id+'" class="bindChange contributorSite hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value="NONE"></option>'
                            +'<option value="Beijing">Beijing</option>'
                            +'<option value="Tokyo">Tokyo</option>'
                            +'<option value="Lund">Lund</option>'
                            +'</select>'
                            +'<input placeholder="Department" id="contributor'+currentParent.id+'" class="bindChange contributor hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all" maxlength="50" value="'+currentParent.contributor+'"></input>'
                            //CONTRIBUTOR FIELD END
                            +'</div>'
                            //STAKEHOLDER DIV END
                            //TIME FIELDS START
                            +'<div class="times">'
                            +'<p class="title">Deadline </p>'
                            +'<p class="deadline description ' + currentParent.id + '">' + getDate(currentParent.deadline) + '</p>'
                            +'<input id="deadline'+currentParent.id+'" type="text" class="bindChange deadline hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<p class="title">Added </p>'
                            +'<p class="added description ' + currentParent.id + '">' + getDate(currentParent.added) + '</p>'
                            +'<input id="added'+currentParent.id+'" type="text" class="bindChange added hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'</div>'
                            //TIME FIELDS END
                            //ATTR1 AND ATTR2 DIV START
                            +'<div class="story-attr1-2">'
                            //ATTR1 FIELD START
                            +'<p class="title">' + area.storyAttr1.name + '</p>'
                            +'<p class="description story-attr1 ' + currentParent.id + '">' + getAttrImage(currentParent.storyAttr1) + getNameIfExists(currentParent.storyAttr1) + '</p>'
                            +'<select id="storyAttr1'+currentParent.id+'" class="bindChange story-attr1 hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr1Options
                            +'</select>'
                            //ATTR1 FIELD END
                            //ATTR2 FIELD START
                            +'<p class="title">' + area.storyAttr2.name + '</p>'
                            +'<p class="description story-attr2 ' + currentParent.id + '">' + getAttrImage(currentParent.storyAttr2) + getNameIfExists(currentParent.storyAttr2) + '</p>'
                            +'<select id="storyAttr2'+currentParent.id+'" class="bindChange story-attr2 hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr2Options
                            +'</select>'
                            //ATTR2 FIELD END
                            +'</div>'
                            //ATTR1 AND ATTR2 DIV END
                            //ATTR3 DIV START
                            +'<div class="story-attr3">'
                            +'<p class="title">' + area.storyAttr3.name + '</p>'
                            +'<p class="description story-attr3 ' + currentParent.id + '">' + getAttrImage(currentParent.storyAttr3) + getNameIfExists(currentParent.storyAttr3) + '</p>'
                            +'<select id="storyAttr3' + currentParent.id +'" class="bindChange story-attr3 hidden-edit ' + currentParent.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr3Options
                            +'</select>'
                            +'<input type="checkbox" class="inline bindChange hidden-edit ' + currentParent.id + '" id="archiveStory' + currentParent.id + '"' + getArchived(currentParent.archived) + '><p class="title inline hidden-edit ' + currentParent.id + '">Archive story</p></input>'
                            +'<button class="inline marginTop save-button hidden-edit ' + currentParent.id + '" title="Save">Save</button>'
                            +'<button class="inline marginTop cancelButton hidden-edit ' + currentParent.id + '" title="Cancel">Cancel</button>'
                            + getArchivedTopic(archived, currentParent.id)
                            +'<p class="description ' + currentParent.id + '">' + getDate(currentParent.dateArchived) + '</p>'
                            +'</div>'
                            //ATTR3 FIELD END
                            +'<a id=' + currentParent.id + ' title="Remove story" class="icon deleteItem delete-icon"></a>'
                            +'<br style="clear:both" />';
                        newContainer += '<li class="parentLi story ui-state-default editStory" id="' + currentParent.id + '">' + list +'</li>';


                        for (var i=0; i<currentParent.children.length; ++i) {
                            var currentChild = currentParent.children[i];

                            newContainer += '<li class="childLi task ui-state-default editTask" parentId="' + currentParent.id + '"' + 'id="' + currentChild.id + '">'
                            //TASKTITLE START
                            //TYPE MARK START
	                        +'<p class="marginLeft typeMark">Task ' + currentChild.id + '</p>'
                            //TYPE MARK END
                            +'<div class="taskTitle ' + currentChild.id + '">'
                            +'<p class="taskInfo">'+ addLinksAndLineBreaks(currentChild.title) +'</p>'
                            +'</div>'
                            +'<textarea id="taskTitle' + currentChild.id + '" class="taskInfo bindChange taskTitle hidden-edit ' + currentChild.id + '" maxlength="500">' + currentChild.title + '</textarea>'
                            //TASKTITLE END
                            //TASKOWNER START
                            +'<div class="taskOwner ' + currentChild.id + '" id="taskOwner' + currentChild.id + '"><p class="taskHeading">Owner: </p><p class="taskInfo">'+ currentChild.owner +'</p></div>'
                            +'<textarea id="taskOwner' + currentChild.id + '" class="taskInfo bindChange taskOwner hidden-edit ' + currentChild.id + '" maxlength="50">' + currentChild.owner + '</textarea>'
                            //TASKOWNER END
                            //STATUS FIELD START
                            +'<div class="taskStatus ' + currentChild.id + '" id="taskTitle' + currentChild.id + '"><p class="taskHeading">' + area.taskAttr1.name + ': </p><p class="taskInfo ' + currentChild.id + '">' + getAttrImage(currentChild.taskAttr1) + getNameIfExists(currentChild.taskAttr1) + '</p></div>'
                            +'<select id="taskAttr1' + currentChild.id + '" class="bindChange taskInfo taskStatus hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + taskAttr1Options
                            +'</select>'
                            //STATUS FIELD END
                            //CALULATEDTIME START
                            +'<div class="calculatedTime ' + currentChild.id + '" id="calculatedTime' + currentChild.id + '"><p class="taskHeading">Estimated time: </p><p class="taskInfo">'+ currentChild.calculatedTime +'</p></div>'
                            +'<select id="calculatedTime' + currentChild.id + '" class="taskInfo bindChange calculatedTime hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value="0.5">0.5</option>'
                            +'<option value="1">1</option>'
                            +'<option value="1.5">1.5</option>'
                            +'<option value="2">2</option>'
                            +'</select>'
                            //CALCULATEDTIME END
                            +'<button class="save-button hidden-edit ' + currentChild.id + '" title="Save">Save</button>'
                            +'<button class="cancelButton hidden-edit ' + currentChild.id + '" title="Cancel">Cancel</button>'
                            +'<a id=' + currentChild.id + ' title="Remove task" class="icon deleteItem delete-icon"></a>'
                            +'<br style="clear:both" />'
                            +'</li>';
                        }
                    } else if (view == "epic-story") {
                        var list = '<div id="icons">'
                            +'<div title="Show tasks" class="icon ' + icon + '">'
                            +'</div>'
                            +'<a id="' + currentParent.id + '" title="Create new story" class="icon createStory add-child-icon"></a>'
                            +'<a id="' + currentParent.id + '" title="Clone this epic excluding children" class="cloneItem epic"><img src="../resources/image/page_white_copy.png"></a>'
                            +'</div>'
                            //TITLE FIELDS
                            +'<div class="titles-theme-epic">'
                            //TYPE MARK START
                            +'<p class="typeMark">Epic ' + currentParent.id + '</p>'
                            //TYPE MARK END
                            //THEME START
                            +'<p class="theme ' + currentParent.id + '">' + replaceNullWithEmpty(currentParent.themeTitle) + '</p>'
                            +'<textarea placeholder="Theme" id="epicTheme'+currentParent.id+'" class="bindChange theme hidden-edit ' + currentParent.id + '" rows="1" maxlength="100">' + replaceNullWithEmpty(currentParent.themeTitle) + '</textarea>'
                            //THEME END
                            +'<br style="clear:both" />'
                            //EPIC TITLE START
                            +'<p class="titleText ' + currentParent.id + '">' + currentParent.title + '</p>'
                            +'<textarea placeholder="Title" id="epicTitle'+currentParent.id+'" class="bindChange titleText hidden-edit title ' + currentParent.id + '" rows="1" maxlength="100">' + currentParent.title + '</textarea>'
                            //EPIC TITLE END
                            //EPIC DESCRIPTION START
                            +'<p class="description epic-description ' + currentParent.id + '">' + addLinksAndLineBreaks(currentParent.description) + '</p>'
                            +'<textarea placeholder="Description" id="epicDescription'+currentParent.id+'" class="bindChange hidden-edit description ' + currentParent.id + '" rows="2" maxlength="100000">' + currentParent.description + '</textarea>'
                            //EPIC DESCRIPTION END
                            +'</div>'
                            //TITLE FIELDS END
                            +'<a id=' + currentParent.id + ' title="Remove epic" class="icon deleteItem delete-icon"></a>'
                            +'<input type="checkbox" class="marginTopBig inline bindChange hidden-edit ' + currentParent.id + '" id="archiveEpic' + currentParent.id + '"' + getArchived(currentParent.archived) + '><p class="title inline hidden-edit ' + currentParent.id + '">Archive epic</p></input><br>'
                            +'<button class="save-button hidden-edit ' + currentParent.id + '" title="Save">Save</button>'
                            +'<button class="cancelButton hidden-edit ' + currentParent.id + '" title="Cancel">Cancel</button>'
                            + getArchivedTopic(archived, currentParent.id)
                            +'<p class="description ' + currentParent.id + '">' + getDate(currentParent.dateArchived) + '</p>'
                            +'</div>'
                            +'<br style="clear:both" />';

                        newContainer += '<li class="parentLi epic ui-state-default editEpic" id="' + currentParent.id + '">' + list +'</li>';


                        for (var i=0; i<currentParent.children.length; ++i) {
                            var currentChild = currentParent.children[i];
                            newContainer += '<li class="childLi story ui-state-default editStory" parentId="' + currentParent.id + '"' + 'id="' + currentChild.id + '">'
                            +'<div id="icons">'
                            +'<a id="' + currentChild.id + '" title="Clone this story excluding tasks" class="cloneItem story"><img src="../resources/image/page_white_copy.png"></a>'
                            +'</div>'
                            //TITLE FIELDS
                            +'<div class="padding-left titles-epic-story">'
                            //TYPE MARK START
	                        +'<p class="typeMark">Story ' + currentChild.id + '</p>'
                            //TYPE MARK END
                            //THEME START
                            +'<p class="theme ' + currentChild.id + '">' + replaceNullWithEmpty(currentChild.themeTitle) + '</p>'
                            +'<textarea placeholder="Theme" id="theme'+currentChild.id+'" class="bindChange theme hidden-edit ' + currentChild.id + '" rows="1"maxlength="100">' + replaceNullWithEmpty(currentChild.themeTitle) + '</textarea>'
                            //THEME END
                            //EPIC START
                            +'<p class="epic ' + currentChild.id + '">' + replaceNullWithEmpty(currentChild.epicTitle) + '</p>'
                            +'<textarea placeholder="Epic" id="epic'+currentChild.id+'" class="bindChange epic hidden-edit ' + currentChild.id + '" rows="1" maxlength="100">' + replaceNullWithEmpty(currentChild.epicTitle) + '</textarea>'
                            //EPIC END
                            +'<br style="clear:both" />'
                            //STORY TITLE START
                            +'<p class="titleText ' + currentChild.id + '">' + currentChild.title + '</p>'
                            +'<textarea placeholder="Title" id="title'+currentChild.id+'" class="bindChange titleText hidden-edit title ' + currentChild.id + '" rows="1" maxlength="100">' + currentChild.title + '</textarea>'
                            //STORY TITLE END
                            //STORYDESCRIPTION START
                            +'<p class="description story-description ' + currentChild.id + '">' + addLinksAndLineBreaks(currentChild.description) + '</p>'
                            +'<textarea placeholder="Description" id="description'+currentChild.id+'" class="bindChange hidden-edit description ' + currentChild.id + '" rows="2" maxlength="100000">' + currentChild.description + '</textarea>'
                            //STORYDESCRIPTION END
                            +'</div>'
                            //TITLE FIELDS END
                            //STAKEHOLDER DIV START
                            +'<div class="stakeholders">'
                            //CUSTOMER FIELD START
                            +'<p class="title">Customer </p>'
                            +'<p class="customerSite ' + currentChild.id + '">'+getSiteImage(currentChild.customerSite)+'</p>'
                            +'<p class="' + currentChild.id + ' customer description">' + currentChild.customer + '</p>'
                            +'<select id="customerSite'+currentChild.id+'" class="bindChange customerSite hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value="NONE"></option>'
                            +'<option value="Beijing">Beijing</option>'
                            +'<option value="Tokyo">Tokyo</option>'
                            +'<option value="Lund">Lund</option>'
                            +'</select>'
                            +'<input placeholder="Department" id="customer'+currentChild.id+'" class="bindChange customer hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all" maxlength="50" value="'+currentChild.customer+'"></input>'
                            //CUSTOMER FIELD END
                            //CONTRIBUTOR FIELD START
                            +'<p class="title">Contributor </p>'
                            +'<p id="'+currentChild.id+'" class="contributorSite ' + currentChild.id + '">'+getSiteImage(currentChild.contributorSite)+'</p>'
                            +'<p class="' + currentChild.id + ' contributor description">' + currentChild.contributor + '</p>'
                            +'<select id="contributorSite'+currentChild.id+'" class="bindChange contributorSite hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value="NONE"></option>'
                            +'<option value="Beijing">Beijing</option>'
                            +'<option value="Tokyo">Tokyo</option>'
                            +'<option value="Lund">Lund</option>'
                            +'</select>'
                            +'<input placeholder="Department" id="contributor'+currentChild.id+'" class="bindChange contributor hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all" maxlength="50" value="'+currentChild.contributor+'"></input>'
                            //CONTRIBUTOR FIELD END
                            +'</div>'
                            //STAKEHOLDER DIV END
                            //TIME FIELDS START
                            +'<div class="times">'
                            +'<p class="title">Deadline </p>'
                            +'<p class="deadline description ' + currentChild.id + '">' + getDate(currentChild.deadline) + '</p>'
                            +'<input id="deadline'+currentChild.id+'" type="text" class="bindChange deadline hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<p class="title">Added </p>'
                            +'<p class="added description ' + currentChild.id + '">' + getDate(currentChild.added) + '</p>'
                            +'<input id="added'+currentChild.id+'" type="text" class="bindChange added hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'</div>'
                            //TIME FIELDS END
                            //ATTR 1 AND 2 DIV START
                            +'<div class="story-attr1-2">'
                            //ATTR1 FIELD START
                            +'<p class="title">' + area.storyAttr1.name + '</p>'
                            +'<p class="description story-attr1 ' + currentChild.id + '">' + getAttrImage(currentChild.storyAttr1) + getNameIfExists(currentChild.storyAttr1) + '</p>'
                            +'<select id="storyAttr1'+currentChild.id+'" class="bindChange story-attr1 hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr1Options
                            +'</select>'
                            //ATTR1 FIELD END
                            //ATTR2 FIELD START
                            +'<p class="title">' + area.storyAttr2.name + '</p>'
                            +'<p class="description story-attr2 ' + currentChild.id + '">' + getAttrImage(currentChild.storyAttr2) + getNameIfExists(currentChild.storyAttr2) + '</p>'
                            +'<select id="storyAttr2'+currentChild.id+'" class="bindChange story-attr2 hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr2Options
                            +'</select>'
                            //ATTR2 FIELD END
                            +'</div>'
                            //ATTR1 AND ATTR2 DIV END
                            //ATTR3 DIV START
                            +'<div class="story-attr3">'
                            +'<p class="title">' + area.storyAttr3.name + '</p>'
                            +'<p class="description story-attr3 ' + currentChild.id + '">' + getAttrImage(currentChild.storyAttr3) + getNameIfExists(currentChild.storyAttr3) + '</p>'
                            +'<select id="storyAttr3' + currentChild.id +'" class="bindChange story-attr3 hidden-edit ' + currentChild.id + ' text ui-widget-content ui-corner-all">'
                            +'<option value=""></option>'
                            + storyAttr3Options
                            +'</select>'
                            +'<input type="checkbox" class="inline bindChange hidden-edit ' + currentChild.id + '" id="archiveStory' + currentChild.id + '"' + getArchived(currentChild.archived) + '><p class="title inline hidden-edit ' + currentChild.id + '">Archive story</p></input>'
                            +'<button class="inline marginTop save-button hidden-edit ' + currentChild.id + '" title="Save">Save</button>'
                            +'<button class="inline marginTop cancelButton hidden-edit ' + currentChild.id + '" title="Cancel">Cancel</button>'
                            + getArchivedTopic(currentChild.archived, currentChild.id)
                            +'<p class="description ' + currentChild.id + '">' + getDate(currentChild.dateArchived) + '</p>'
                            +'</div>'
                            //ATTR3 DIV END
                            +'<a id=' + currentChild.id + ' title="Remove story" class="icon deleteItem delete-icon"></a>'
                            +'<br style="clear:both" />'
                            +'</li>';
                        }
                    } else if (view == "theme-epic") {
                        var list = '<div id="icons">'
                            +'<div title="Show epics" class="icon ' + icon + '">'
                            +'</div>'
                            +'<a id="' + currentParent.id + '" title="Create new epic" class="icon createEpic add-child-icon"></a><br>'
                            +'<a id="' + currentParent.id + '" title="Clone this theme excluding children" class="cloneItem theme icon"><img src="../resources/image/page_white_copy.png"></a>'
                            +'</div>'
                            //TITLE FIELDS
                            +'<div class="titles-theme-epic">'
                            //TYPE MARK START
                            +'<p class="typeMark">Theme ' + currentParent.id + '</p>'
                            //TYPE MARK END
                            +'<br style="clear:both" />'
                            //TITLE START
                            +'<p class="titleText ' + currentParent.id + '">' + currentParent.title + '</p>'
                            +'<textarea placeholder="Title" id="themeTitle'+currentParent.id+'" class="bindChange titleText hidden-edit title ' + currentParent.id + '" rows="1" maxlength="100">' + currentParent.title + '</textarea>'
                            //TITLE END
                            //DESCRIPTION START
                            +'<p class="description theme-description ' + currentParent.id + '">' + addLinksAndLineBreaks(currentParent.description) + '</p>'
                            +'<textarea placeholder="Description" id="themeDescription'+currentParent.id+'" class="bindChange hidden-edit description ' + currentParent.id + '" rows="2" maxlength="100000">' + currentParent.description + '</textarea>'
                            //DESCRIPTION END
                            +'</div>'
                            //TITLE FIELDS END
                            +'<a id=' + currentParent.id + ' title="Remove theme" class="icon deleteItem delete-icon"></a>'
                            +'<input type="checkbox" class="marginTopBig inline bindChange hidden-edit ' + currentParent.id + '" id="archiveTheme' + currentParent.id + '"' + getArchived(currentParent.archived) + '><p class="title inline hidden-edit ' + currentParent.id + '">Archive theme</p></input><br>'
                            +'<button class="save-button hidden-edit ' + currentParent.id + '" title="Save">Save</button>'
                            +'<button class="cancelButton hidden-edit ' + currentParent.id + '" title="Cancel">Cancel</button>'
                            + getArchivedTopic(archived, currentParent.id)
                            +'<p class="description ' + currentParent.id + '">' + getDate(currentParent.dateArchived) + '</p>'
                            +'</div>'
                            +'<br style="clear:both" />';

                        newContainer += '<li class="parentLi theme ui-state-default editTheme" id="' + currentParent.id + '">' + list +'</li>';


                        for (var i = 0; i<currentParent.children.length; ++i) {
                            var currentChild = currentParent.children[i];
                            newContainer += '<li class="childLi epic ui-state-default editEpic" parentId="' + currentParent.id + '"' + 'id="' + currentChild.id + '">'
	                        +'<div id="icons">'
                            +'<a id="' + currentChild.id + '" title="Clone this epic excluding children" class="cloneItem epic icon"><img src="../resources/image/page_white_copy.png"></a>'
	                        +'</div>'
                            //TITLE FIELDS
                            +'<div class="padding-left titles-theme-epic">'
                            //TYPE MARK START
	                        +'<p class="typeMark">Epic ' + currentChild.id + '</p>'
                            //TYPE MARK END
                            +'<br style="clear:both" />'
                            //TITLE START
                            +'<p class="titleText ' + currentChild.id + '">' + currentChild.title + '</p>'
                            +'<textarea placeholder="Title" id="epicTitle'+currentChild.id+'" class="bindChange titleText hidden-edit title ' + currentChild.id + '" rows="1" maxlength="100">' + currentChild.title + '</textarea>'
                            //TITLE END
                            //DESCRIPTION START
                            +'<p class="description epic-description ' + currentChild.id + '">' + addLinksAndLineBreaks(currentChild.description) + '</p>'
                            +'<textarea placeholder="Description" id="epicDescription'+currentChild.id+'" class="bindChange hidden-edit description ' + currentChild.id + '" rows="2" maxlength="100000">' + currentChild.description + '</textarea>'
                            //DESCRIPTION END
                            +'</div>'
                            +'<a id=' + currentChild.id + ' title="Remove epic" class="icon deleteItem delete-icon"></a>'
                            +'<input type="checkbox" class="marginTopBig inline bindChange hidden-edit ' + currentChild.id + '" id="archiveEpic' + currentChild.id + '"' + getArchived(currentChild.archived) + '><p class="title inline hidden-edit ' + currentChild.id + '">Archive epic</p></input><br>'
                            +'<button class="save-button hidden-edit ' + currentChild.id + '" title="Save">Save</button>'
                            +'<button class="cancelButton hidden-edit ' + currentChild.id + '" title="Cancel">Cancel</button>'
                            +'<br style="clear:both" />'
                            +'</li>';
                        }
                    }
                }
            }
        }
        return newContainer;
    };

    var firstBuild = true;

    /**
     * Builds the visible html list using the JSON data
     */
    buildVisibleList = function (archived) {
        if ($("#archived-checkbox").prop("checked")) {
            $("#list-divider").show();
            $("#archived-list-container").append(generateList(true)).show();
        }
    	if (archived != true && !firstBuild) {
            $('#list-container').append(generateList(false));    	    
        }
        editingItems =  new Array();
        for (var i = 0; i < selectedItems.length; ++i) {
            $('li[id|=' + selectedItems[i].id + ']').addClass("ui-selected");
        }
        $( "#list-container" ).sortable("refresh");

        //Make sure all items that should be invisible are invisible
        $(".childLi").each(function () {
            var currentId = $(this).attr("id");
            if (visible[currentId] != true) {
                $(this).addClass("ui-hidden");
            }
        });

        //Truncating long description texts
        $("li").each(function() {
            var currentId = $(this).attr("id");
            $('p.story-description', this).truncate(
                    $.extend(truncateOptions, {className: 'truncate'+currentId})
            );
            $('p.epic-description, p.theme-description', this).truncate(
                    $.extend(truncateOptions, {className: 'truncate'+currentId, max_length: 200})
            );
            $('p.taskInfo', this).truncate(
                    $.extend(truncateOptions, {className: 'truncate'+currentId, max_length: 90})
            );

            if (extendedDescriptions.indexOf('truncate'+currentId) != -1) {
                $('a.truncate'+currentId, this).click();
            }
        });

        $('.expand-icon').click(expandClick);
        $(".parent-child-list").children("li").click(liClick);

        $(".parent-child-list").children("li").mousedown(function () {
            $(this).addClass("over");
        });

        $(".parent-child-list").children("li").mouseup(function () {
            $(".parent-child-list").children("li").removeClass("over");
        });
        //$('.save-button').button().attr("disabled", true);

        $("a.createEpic").click(createEpic);
        $("a.createStory").click(createStory);
        $("a.createTask").click(createTask);
        $("a.deleteItem").click(deleteItem);
        $("a.cloneItem").click(function() {
            cloneItem($(this), false);
        });

        $("a.cloneItem-with-children").click(function() {
            cloneItem($(this), true);
        });

        enableEdits();

        //This avoids exiting edit mode if an element inside a theme, epic, story or task is double clicked.
        $(".bindChange").dblclick(function(event) {
            event.stopPropagation();
        });

        $( "#storyTheme,#epicTheme" ).autocomplete({
            minLength: 0,
            source: "../json/autocompletethemes/" + areaName,
            change: function() {
                $( "#storyEpic" ).attr("value", "");
            },
            select: function (event, data) {
                $( "#storyEpic" ).attr("value", "");
                //Used for deselecting the input field.
                $(this).autocomplete('disable');
                $(this).autocomplete('enable');
            }
        });

        $("#storyTheme").focus(function() {
            $("#storyTheme").autocomplete("search", $("#storyTheme").val());
        });

        $("textarea#epicTheme").focus(function() {
            $("textarea#epicTheme").autocomplete("search", $("#epicTheme").val());
        });

        $( "#storyEpic" ).autocomplete({
            minLength: 0,
            select: function (event, data) {
                //Used for deselecting the input field.
                $(this).autocomplete('disable');
                $(this).autocomplete('enable');
            },
            search: function() {
                var themeName = $("#storyTheme").val();
                $( "#storyEpic" ).autocomplete({
                    source: "../json/autocompleteepics/" + areaName + "?theme=" + themeName
                });
            }
        }).focus(function() {
            $(this).autocomplete("search", "");
        }).blur(function(){
            $(this).autocomplete('enable');
        });

        //Stops textarea from making a new line when trying to save changes.
        $("textarea").keydown(function(e){
            if (e.keyCode == KEYCODE_ENTER && !e.shiftKey) {
                e.preventDefault();
            }
        });

        $(".bindChange").change( function(event){
            var id = ($(event.target)).closest('li').attr('id');
            $('#save-all').button( "option", "disabled", false );
            //$(".save-button."+id).button( "option", "disabled", false );
        });

        $(".bindChange").bind('input propertychange', function(event) {
            var id = ($(event.target)).closest('li').attr('id');
            $("#save-all").button( "option", "disabled", false );
            //$(".save-button."+id).button( "option", "disabled", false );
        });

        if (disableEditsBoolean) {
            disableEdits();
        }

        if (isFilterActive() || disableEditsBoolean || $("#order-by").val() != "prio") {
            $("#list-container").sortable("option", "disabled", true);
        }
        firstBuild = false;
        addZebraStripesToParents();
    };

    var setHeightAndMargin = function (value) {
        $("#list-container-div").css("margin", value+"px auto");
    };

    $(window).resize(function() {
        setHeightAndMargin($("#header").height());
    });

    /*
     * Changing the create parent button based on what view you're on
     * Also changing the view description text and the color of the view links
     */
    if (view == "story-task") {     
        $("#print-stories").click(function() {
            printStories();
        });

        $("#create-parent").button({
            label: 'CREATE STORY'
        }).click(function() {
            createStory();
        });
        $(".story-task-link").css("color", "#1c94c4");
        $("#topic").text("BACKLOG TOOL / ");
        $("#topic-area").text(areaName);
    } else if (view == "epic-story") {
        $("#print-stories-li").remove();
        $("#move-li").remove();
        $( "#create-parent" ).button({ label: "CREATE EPIC" }).click(function() {
            createEpic();
        });
        $(".epic-story-link").css("color", "#1c94c4");
        $("#topic").text("BACKLOG TOOL / ");
        $("#topic-area").text(areaName);
    } else if (view == "theme-epic") {
        $("#print-stories-li").remove();
        $("#move-li").remove();
        $( "#create-parent" ).button({ label: "CREATE THEME" }).click(function() {
            createTheme();
        });
        $(".theme-epic-link").css("color", "#1c94c4");
        $("#topic").text("BACKLOG TOOL / ");
        $("#topic-area").text(areaName);
    } else if (view == "home") {
        $("#print-stories-li").remove();
        $("create-parent").remove();
        $("#move-li").remove();
        $(".home-link").css("color", "#1c94c4");
        $("#topic-area").text("BACKLOG TOOL");
    }

    $('#settings').button({
        text: false,
        icons: {
            primary: 'silk-icon-wrench'
        }
    });

    var sendMovedItems = function () {
        var moveContainer = new Object();
        var lastItem = new Object();
        moveContainer.lastItem = lastItem;

        $(".parent-child-list").children("li").each(function (index) {
            if ($(this).attr("class").indexOf("moving") != -1) {
                //Current was the grabbed item, break
                return false;
            } else if ($(this).attr("class").indexOf("ui-selected") == -1) {
                //If current not selected
                if ($(this).attr("class").indexOf("parent") != -1) {
                    moveContainer.lastItem.id = $(this).attr("id");
                    moveContainer.lastItem.type = "parent";
                    //Current was parent. If it has children that are invisible,
                    //then jump to the last one.
                    var belongingChildren = getParent($(this).attr("id")).children;
                    if (belongingChildren.length > 0) {
                        var lastChildId = belongingChildren[belongingChildren.length-1].id;
                        if (visible[lastChildId] != true) {
                            moveContainer.lastItem.id = lastChildId;
                            moveContainer.lastItem.type = "child";
                        }
                    }
                } else if ($(this).attr("class").indexOf("child") != -1
                        && visible[$(this).attr("id")] == true) {
                    moveContainer.lastItem.id = $(this).attr("id");
                    moveContainer.lastItem.type = "child";
                }
            }
        });
        moveContainer.movedItems = selectedItems;

        if (moveContainer.lastItem.id != null) {
            moveContainer.lastItem.id = eval(moveContainer.lastItem.id);
        }

        $.ajax({
            url: "../json/move" + view + "/"+areaName,
            type: 'POST',
            dataType: 'json',
            data: JSON.stringify(moveContainer),
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                reload();
            },
            error: function (request, status, error) {
                alert(error);
                reload();
            }
        });
    };

    $("#list-container").sortable({
        tolerance: 'pointer',
        cursor: 'pointer',
        revert: false, //Animation
        placeholder: "ui-state-highlight",
        opacity: 0.50,
        axis: 'y',
        scroll: true,
        scrollSpeed: 35,
        distance: 5,
        helper: function () {
            var pressed = $(".over");
            var container = $('<div/>').attr('id', 'draggingContainer');

            if (pressed.attr("class").indexOf("ui-selected") == -1) {
                liClick(pressed);
            }
            container.append($(".ui-selected").clone());

            return container;
        },
        start: function (event, ui) {
            ignorePush = true;
            var pressed = $(ui.item);
            pressed.addClass("moving");

            $('.ui-selected').addClass("ui-hidden");
            $("#draggingContainer").children("li").removeClass("ui-hidden");

            //Sort selected list
            var currentOrder = $('.parent-child-list').sortable('toArray');
            selectedItems.sort(function sortfunction(a, b) {
                return currentOrder.indexOf(a.id) - currentOrder.indexOf(b.id);
            });

        },
        stop: function (event, ui) {
            displayUpdateMsg();
            sendMovedItems();

            var pressed = $(ui.item);
            pressed.removeClass("moving");

            lastPressed = null;
            ignorePush = false;
        }

    });

    buildVisibleList();

    $('#filter-button').button().click(function() {
        $('#filter').val(selectedToString());
        $('#filter').focus();
        updateFilter();
    });
    $('#save-all').button({
        text: false,
        icons: {
            primary: 'silk-icon-disk'
        }
    }).click(bulkSave);
    $("#expand-all").click(function() {
        for (var i=0; i<parents.length; ++i) {
            for (var j=0; j<parents[i].children.length; ++j) {
                var currentChildId = parents[i].children[j].id;
                visible[currentChildId] = true;
            }
        }
        $(".parent-child-list").empty();
        buildVisibleList();
    });
    $("#collapse-all").click(function() {
        for (var i=0; i<parents.length; ++i) {
            for (var j=0; j<parents[i].children.length; ++j) {
                var currentChildId = parents[i].children[j].id;
                visible[currentChildId] = false;
            }
        }
        $(".parent-child-list").empty();
        buildVisibleList();
    });

    /**
     * Sets timeout for a function, to be reset after each call on delay.
     * Can for example be used to trigger a function after
     * typing has stopped in a textbox.
     * Usage:
     * delay(function() {
            //To be called after timeout
        }, *delayInMs* ); 
     */
    var delay = (function() {
        var timer = 0;
        return function(callback, ms) {
            clearTimeout (timer);
            timer = setTimeout(callback, ms);
        };
    })();

    $('#filter').keyup(function() {
        delay(function() {
            updateFilter();
        }, 500 );
    });

    /*
     * Update view when the filter is changed.
     * Also disable drag and drop if filter is active.
     */
    var updateFilter = function() {
        $(".parent-child-list").empty();
        buildVisibleList();

        if (isFilterActive()) {
            $("html, body").animate({ scrollTop: 0 }, "fast");

            var filterString = '?ids=' + $("#filter").val();
            window.history.replaceState( {} , document.title, filterString );
            $("#list-container").sortable("option", "disabled", true);

            //Remove selected items if they are invisible after changing filter
            selectedItems = $.grep(selectedItems, function (selected, i) {
                return isFiltered(selected.id);
            });
            updateCookie(); //Necessary if selected items changed
        } else {
            window.history.replaceState( {} , document.title, '?' );
            if (!disableEditsBoolean) {
                $("#list-container").sortable("option", "disabled", false);
            }
        }
    };

    var isCtrl = false;
    var isShift = false;

    $(window).keyup(function (e) {
        if (e.keyCode == KEYCODE_ENTER && !e.shiftKey) {
            bulkSave();
        }
        if (e.keyCode == KEYCODE_ESC) {
            bulkCancel();
        }
        if (e.which == KEYCODE_CTRL) {
            isCtrl = false;
        }// else if (e.which == 16) {
        //   isShift = false;
        //  }
    });

    $(window).keydown(function (e) {
        if (e.which == KEYCODE_CTRL) {
            isCtrl = true;
        } //else if (e.which == 16) {
        //   isShift = true;
        // }
    });

    setHeightAndMargin($("#header").height());

});
