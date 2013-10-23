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
    var MAX_NOTES = 10;
    var TIMER_FILTER = 0;
    var TIMER_BIND_EVENTS = 1;
    var TIMER_SCROLL_SORTABLE = 2;
    var TIMER_RETRUNCATE = 3;

    var allExpanded = false;

    $("#header-buttons").removeClass("elem-hidden-children elem-loading");

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

    /**
     * Returns a date on the format YYYY-MM-dd HH:mm, or an empty
     * string if the argument was null
     * @param milliseconds The milliseconds to turn into a string-representation
     */
    var getFullDateAndTime = function(milliseconds) {
        if (milliseconds == null) {
            return "";
        } else {
            var date = new Date(milliseconds);
            var h = date.getHours();
            var mm = date.getMinutes();
            return date.yyyymmdd() + " " + prependZero(h) + ":" + prependZero(mm);
        }
    };

    /**
     * Prepends a zero to the argument if it is less than 10
     * @param num The number to prepend a zero to if necessary
     * @returns The argument with a prepended zero if arg < 10
     */
    var prependZero = function(num) {
        if (num < 10) {
            num = '0' + num;
        }
        return num;
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
        return '<img src="../resources/image/'+storyAttr.icon+'"/> ';
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

    if (archivedView) {
        $("#active").click(function () {
            window.location = '?archived-view=false';
        });
    } else {
        $("#archive").click(function () {
            window.location = '?archived-view=true';
        });
    }

    /**
     * Adding line breaks and <a> tags for the param text.
     */
    var addLinksAndLineBreaks = function(text) {
        return text.replace(/((https|http):\/\/[^\s]+)/gi, '<a href="$1">$1</a>')
                .replace(/\n/g, '<br />');
    };

    /**
     * Escapes html from param text
     */
    var escapeHtml = function(text) {
        if (text == null) {
            return "";
        }
        return $("<div/>").html(text).text();
    };

    var getNameIfExists = function (object) {
        if (object == null) {
            return '&nbsp;';
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
            if (archivedView == false) {
                var data = response.responseBody;

                //Data is on format {msg1},{msg2},
                //make it into a valid json array:
                data = "[" + data.substring(0, data.length - 1) + "]";

                var jsonObj = {};
                try {
                    jsonObj = JSON.parse(data);
                } catch (error) {
                    alert("Error: Invalid JSON-message from the server, check console log " + error);
                }

                for (var i=0; i<jsonObj.length; i++) {
                    if (jsonObj[i].views.indexOf(view) > -1 || jsonObj[i].views == "*") {
                        processPushData(jsonObj[i]);
                    }
                }
                addZebraStripesToParents();
            }
        };

        if (request.logLevel == "debug") {
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
     * Try to interpret the data as JSON, and forward the data
     * to corresponding method
     */
    var processPushData = function processPushData(jsonObj) {
        var data = jsonObj.data;
        var childData = new Array();
        if (typeof data.children !== "undefined" && data.children != null) {
            childData = data.children;
            data.children = new Array();
        }
        if (data) {
            /* Store current offset and focus in order to be able to restore
             * these after the push-message has been processed */ 
            var offset = null, offsetObj = null, inputFocus = null;
            if (editingItems.length > 0) {
                offsetObj = editingItems[editingItems.length - 1]; // fetch the latest item
                inputFocus = $(document.activeElement);
                offset = $("li#" + offsetObj.id + "." + offsetObj.type).offset();
                if (offset == null) { // no matched elements => probably a note
                    offset = $("#" + offsetObj.id).offset();
                }
            }

            if (jsonObj.type == "Story") {
                if (data.latestNonSystemNote != null && getNotes(data.id).length == 0) {
                    getNotes(data.id).push(data.latestNonSystemNote);
                }
                updateStoryLi(data, !allExpanded);
                for (var i = 0; i < childData.length; i++) {
                    updateTaskLi(childData[i]);
                }
                updateTypeMarkWidth();
            } else if (jsonObj.type == "Task") {
                updateTaskLi(data);
            } else if (jsonObj.type == "Epic") {
                updateEpicLi(data, !allExpanded);
                for (var i = 0; i < childData.length; i++) {
                    updateStoryLi(childData[i], !allExpanded);
                }
            } else if (jsonObj.type == "Theme") {
                updateThemeLi(data, !allExpanded);
                for (var i = 0; i < childData.length; i++) {
                    updateEpicLi(childData[i], !allExpanded);
                }
            } else if (jsonObj.type == "Delete") {
                removeItem(data);
            } else if (jsonObj.type == "childMove" || jsonObj.type == "parentMove") {
                handleMovePush(jsonObj.type, data);
            } else if (jsonObj.type == "Note") {
                updateNoteLi(data);
            } else if (jsonObj.type == "AreaDelete") {
                displayAreaRemovedDialog();
            }

            if (offset !== null) {
                // Restore offset
                var newOffset = $("li#" + offsetObj.id + "." + offsetObj.type).offset();
                if (newOffset == null) {
                    newOffset = $("#" + offsetObj.id).offset();
                }
                if (typeof newOffset !== "undefined") {
                    if (newOffset.top != offset.top) {
                        // Restore scroll
                        var newPos = $(window).scrollTop() + (newOffset.top - offset.top);
                        $(window).scrollTop(newPos);
                    }
                    inputFocus[0].focus(); // Restore focus
                }
                offset = null;
            }
        } else {
            window.console && console.log("No json-data in push-message");
        }
    };

    /**
     * Process and handle a move-push-event
     */
    var handleMovePush = function(moveType, dataObj) {
        var objList = dataObj.objects;
        var childAttr = "prioInStory";
        if (view == "epic-story") {
            childAttr = "prioInEpic";
        } else if (view == "theme-epic") {
            childAttr = "prioInTheme";
        }
        var ulObj = $('ul#list-container');

        if (moveType == "parentMove") {
            for (var id in objList) {
                var p = getParent(id);
                p.prio = objList[id];
            }
        } else { // child-move
            for (var i = 0; i < objList.length; i++) {
                var p = objList[i];
                var children = p.children;
                // The children should be sorted in correct prio-order in the parent
                children.sort(function(a, b) {
                    if (a[childAttr] > b[childAttr]) {
                        return 1;
                    } else if (a[childAttr] < b[childAttr]){
                        return -1;
                    } else {
                        return 0;
                    }
                });
                var parentObj = getParent(p.id);
                if (parentObj == null) {
                    // Only happens when an Epic or Story is "created" via a Story
                    parentObj = p;
                    parentObj.children = new Array();
                    if (view == "epic-story") {
                        updateEpicLi(parentObj);
                    } else if (view == "theme-epic") {
                        updateThemeLi(parentObj);
                    }
                }

                var childIdArr = new Array();
                var parentLi = $("li#" + p.id + ".parentLi");
                for (var j = 0; j < children.length; j++) {
                    var childId = children[j].id;
                    var childLi = $("li#" + childId + ".childLi");
                    if (childLi.length == 0) {
                        if (view == "epic-story") {
                            updateStoryLi(children[j]);
                        } else if (view == "theme-epic") {
                            updateEpicLi(children[j]);
                        }
                    } else if (view == "epic-story") {
                        updateStoryLiContent(children[j]); // Keeping Theme-box up-to-date
                    }
                    childLi.attr('parentid', p.id);

                    // If the prioIn* is 1, it should be placed immediately after it's parent
                    if (children[j][childAttr] == 1) {
                        parentLi.after(childLi);
                    } else {
                        $("li#" + children[j-1].id + ".childLi").after(childLi);
                    }
                    childIdArr.push("li#" + childId);
                }
                var childrenLis = $(childIdArr.toString());
                toggleExpandBtn(parentLi, childrenLis, children);

                parentObj.children = children;
            }
        }
        sortList(ulObj);
    };

    var displayAreaRemovedDialog = function() {
        var areaErrorDialog = $(document.createElement('div'));
        $(areaErrorDialog).attr('title', 'Area removed');
        $(areaErrorDialog).html('<p>The current area has been removed by another user. You will be redirected to the start page.</p>');
        areaErrorDialog.dialog({
            modal: true,
            width: 325,
            resizable: false,
            minHeight: 0,
            buttons: {
                Ok: function() {
                    $( this ).dialog( "close" );
                }
            },
            close: function(event, ui) {
                window.location.replace("../");
            }
        });
    };

    var displayUpdateMsg = function () {
        $.blockUI({
            message: '<h1>Updating...</h1>',
            fadeIn:  0,
            overlayCSS: { backgroundColor: '#808080', cursor: null},
            fadeOut:  350});
    };

    /**
     * Exclude the scrollbar for the specified element.
     * What it does is to disable 'sortable' when the
     * cursor is positioned in the x-interval
     * [element.width - 20px, element.width]
     * @param element The jQuery-element
     */
    var exclScrollBarFromSortable = function(element) {
        element.mousemove(function(e) {
            var elemWith = $(this).width();
            var dragAreaWidth = elemWith - 20;
            var offsetX = e.offsetX;
            if (offsetX == null) {
                // Fix for Firefox
                offsetX = e.clientX - $(e.target).offset().left + window.pageXOffset; 
            }

            if (offsetX <= elemWith && offsetX >= dragAreaWidth) {
                $("#list-container").sortable("option", "disabled", true);
                delay(function() { // to not make sortable off 'permanent'
                    if (!preventSortable()) {
                        $("#list-container").sortable("option", "disabled", false);
                    }
                }, 2000, TIMER_SCROLL_SORTABLE );
            } else {
                if (!preventSortable()) {
                    $("#list-container").sortable("option", "disabled", false);
                }
            }
        });
    };

    /**
     * Checks whether sortable should be disabled
     * @returns True if sortable should be disabled, otherwise false
     */
    var preventSortable = function() {
        return (isFilterActive() || disableEditsBoolean || $("#order-by").val() != "prio" || archivedView === true);
    };

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
    
    /**
     * Get the parent that has the specified id
     * @param id The id of the parent to get
     * @returns The parent if found, otherwise null
     */
    var getParent = function(id) {
        var p = parentsMap[id];
        if (typeof p !== "undefined" && p != null) {
            if (typeof p.children === "undefined") {
                p.children = new Array();
            }
        } else {
            p = null;
        }
        return p;
    };

    /**
     * Get the parent that evaluates to true in the
     * specified function
     * 
     * @param comp
     *            A function that accepts one argument, the
     *            parent. The function must evaluate to true
     *            to indicate that the parent matches
     * @returns The parent, if found, otherwise null
     */
    var getParentBy = function(comp) {
        for ( var key in parentsMap) {
            if (parentsMap.hasOwnProperty(key)) {
                var p = parentsMap[key];
                if (comp(p)) {
                    return p;
                }
            }
        }
        return null;
    };
    
    var putParent = function(id, parent) {
        parentsMap[id] = parent;
    };
    
    /**
     * Replaces the parent with the specified id with the specified newParent. 
     * The children of the replaced parent will be set as the the parent's
     * children
     * @param id The id of the parent to replace
     * @param newParent The new parent to replace with
     */
    var replaceParent = function(id, newParent) {
        var p = getParent(id);
        if (typeof p !== "undefined") {
            var children = p.children;
            newParent.children = children;
            putParent(id, newParent);
        }
    };

    /**
     * Replaces the parent or child with the specified id, with the specified
     * newObject. 
     * @param id The id of the parent or child to replace
     * @param newObject The object to replace with
     */
    var replaceParentOrChild = function(id, newObject) {
        var p = parentsMap[id];
        if (typeof p !== "undefined") {
            var children = p.children;
            newObject.children = children;
            putParent(id, newObject);
        } else {
            findChild(id, function(child, parent, pos) {
                parent.children[pos] = newObject;
            });
        }
    };

    /**
     * Get the child with the specified id. Returns null if no child
     * with that id is found.
     * @param id The id of the child to get
     * @returns The child, or null if it wasn't found
     */
    var getChild = function (id) {
        var ch = null;
        findChild(id, function(child, parent, pos) {
            ch = child;
        });
        return ch;
    };
    
    /**
     * Searches for a child with the specified id, and when a match is found
     * the specified function will be called with arguments (childObj, parentObj, posInParent)
     * @param id The id of the child to find
     * @param resFunc The callback when a match is found 
     */
    var findChild = function(id, resFunc) {
        for (var key in parentsMap) {
            if (parentsMap.hasOwnProperty(key) && typeof parentsMap[key].children !== "undefined") {
                for (var i = 0; i < parentsMap[key].children.length; i++) {
                    var child = parentsMap[key].children[i];
                    if (child.id == id) {
                        resFunc(child, parentsMap[key], i);
                        return;
                    }
                }
            }
        }
    };
    
    /**
     * Remove the parent with the specified id
     * @param id The id of the parent to find
     * @returns The removed parent
     */
    var removeParent = function(id) {
        var delItem = parentsMap[id];
        delete parentsMap[id];
        return delItem;
    };
    
    /**
     * Replace the child with the specified id
     * @param id The id to replace
     * @param newChild The new child to replace with
     */
    var replaceChild = function (id, newChild) {
        findChild(id, function(child, parent, pos) {
            parent.children[pos] = newChild;
        });
    };

    /**
     * Increase the prio-attribute by one for all parents whose
     * current prio is greater or equal to the specified prio
     * @param fromPrio The lowest prio the parent must have to get it's prio increased
     */
    var incrPrioForParents = function(fromPrio) {
        for (var key in parentsMap) {
            if (parentsMap.hasOwnProperty(key)) {
                var p = parentsMap[key];
                if (p.prio >= fromPrio) {
                    p.prio++;
                }
            }
        }
    };
    
    /**
     * Decrease the prio-attribute by one for all parents whose
     * current prio is greater or equal to the specified prio
     * @param fromPrio The lowest prio the parent must have to get it's prio decreased
     */
    var decrPrioForParents = function(fromPrio) {
        for (var key in parentsMap) {
            if (parentsMap.hasOwnProperty(key)) {
                var p = parentsMap[key];
                if (p.prio >= fromPrio) {
                    p.prio--;
                }
            }
        }
    };
    
    /**
     * Iterate all parents, and for each parent, the the specified
     * function is called
     * @param cbForEachParent Function called with (p) for each parent
     */
    var iterAllParents = function(cbForEachParent) {
        for (var key in parentsMap) {
            if (parentsMap.hasOwnProperty(key)) {
                cbForEachParent(parentsMap[key]);
            }
        }
    };

    /**
     * Get the Note-array corresponding to the
     * specified id
     * @param id The id of the Story whose Notes to get
     */
    var getNotes = function(id) {
        if (typeof notesMap === "undefined") {
            notesMap = {};
        }
        id = id.toString();
        var notes = notesMap[id];
        if (notes == null) {
            notesMap[id] = new Array();
            notes = notesMap[id];
        }
        return notes;
    };

    /**
     * Set the list of notes for the specified id
     * @param id The id of the story
     * @param notesList The (sorted) list of notes
     */
    var setNotesList = function(id, notesList) {
        if (typeof notesMap === "undefined") {
            notesMap = {};
        }
        id = id.toString();
        notesMap[id] = notesList;
    };

    /**
     * Remove the note with the specified id
     * @param noteId The id of the note to remove
     * @returns The removed note
     */
    var removeNote = function(noteId) {
        if(typeof notesMap !== "undefined") {
            for (var storyId in notesMap) {
                if (notesMap.hasOwnProperty(storyId)) {
                    var notes = notesMap[storyId];
                    for (var i = 0; i < notes.length; i++) {
                        if (notes[i].id == noteId) {
                            var retElem = notes[i];
                            notes.remove({id:noteId});
                            return retElem;
                        }
                    }
                }
            }
        }
    };

    /**
     * Add the child to the parent if it does not already exist,
     * and update the specified attribute accordingly (e.g. the prioInTheme)
     * @param parentObj The parent to add the child to
     * @param childObj The child to add
     * @param The name of the child's attribute to update (e.g. prioInTheme)
     */
    var addChildToParent = function(parentObj, childObj, attr) {
        var children = parentObj.children;
        if (typeof children === "undefined") {
            children = new Array();
            parentObj.children = children; 
        }
        var alreadyExists = false;
        for (var i=0; i<children.length; i++) {
            if (children[i].id == childObj.id) {
                alreadyExists = true;
            }
        }
        if (!alreadyExists) {
            if (typeof childObj[attr] === "undefined") {
                childObj[attr] = children.length + 1; // Put it as the last element
            }
            /* Insert the child-object (Task/Story/Epic) */ 
            children.splice(childObj[attr] - 1, 0, childObj);
            for (var i = 0; i < children.length; i++) { // Update the prio for all children
                children[i][attr] = i + 1;
            }
        }
    };

    /**
     * Sort the parent-list-items in the specified list, and
     * then append the corresponding children to each parent
     * @param ulObj The ul-object
     */
    var sortList = function(ulObj) {
        console.log("Sort called");

        var comp = null;
        if (archivedView == true) {
            comp = getComparatorFor("dateArchived");
        } else {
            var orderBy = $("#order-by").val();
            if (orderBy == "prio") {
                comp = prioComparator;
            } else {
                comp = getComparatorFor(orderBy);
            }
        }

        ulObj.children('li.parentLi').sort(comp).appendTo(ulObj);

        iterAllParents(function(parent) {
            if (typeof parent.children !== "undefined") {
                var idArr = new Array();
                for (var i = 0; i < parent.children.length; i++) {
                    idArr.push('li#' + parent.children[i].id);
                }

                if (idArr.length > 0) {
                    $('li#' + parent.id + '.parentLi').after($(idArr.toString()));
                }
            }
        });
    };

    var isNumeric = function(val) {
        return val !== null && !isNaN(val);
    };

    /**
     * Returns a function func(a, b) that works as a comparator
     * when e.g. sorting
     * @param attr The name of the object attribute to compare when sorting
     * @returns A comparator-function
     */
    var getComparatorFor = function(attr) {
      return function(a, b) {
          return baseComparator(a, b, attr);
      };
    };
    
    /**
     * Compares the attribute attr of the two object a and b
     * and returns a number depending on the outcome
     * @param a Object one
     * @param b Object two
     * @param attr The attribute of the objects to compare
     * @returns A number < 0 if a < b, > 0 if a > b or 0 if a == b 
     */
    var baseComparator = function(a, b, attr) {
        var v1 = getParent(a.id)[attr];
        var v2 = getParent(b.id)[attr];

        if (attr == "storyAttr1" || attr == "storyAttr2" || attr == "storyAttr3") {
            if (v1 !== null) {
                v1 = v1.compareValue;
            }
            if (v2 !== null) {
                v2 = v2.compareValue;
            }
        }

        if (isNumeric(v1) && isNumeric(v2)) {
            return (attr == "dateArchived") ? v2 - v1 : v1 - v2;
        }
        
        // For e.g. empty descriptions, these should be further down in the list
        if (v1 === "") {
            v1 = null;
        }
        if (v2 === "") {
            v2 = null;
        }
        
        if (v1 == null) {
            return 1;
        } else if (v2 == null) {
            return -1;
        } else {
            return v1.localeCompare(v2);
        }
    };

    var prioComparator = function(a, b) {
        var p1 = getParent(a.id);
        var p2 = getParent(b.id);
        
        if (p1.prio > p2.prio) {
            return 1;
        } else if (p2.prio > p1.prio) {
            return -1;
        } else {
            return 0;
        }
    };

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

    /**
     * Function to trigger when a long description is expanded.
     */
    var expandText = function (item) {
        expandedItems.push(item.attr("class"));
    };

    /**
     * Function to trigger when a long description is collapsed.
     */
    var collapseText = function (item) {
        expandedItems.remove(item.attr("class"));
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
        paragraph.html(addLinksAndLineBreaks(newText));
        return paragraph;
    };

    /**
     * Truncate the specified paragraph
     */
    var trunc = function(paragraph, afterElement) {
        paragraph.dotdotdot({
            ellipsis: '...',
            wrap: 'letter',
            watch: 'window'
        });
    };

    var isElementTruncated = function(element) {
        if (element != null && element.length > 0) {
            return element.triggerHandler("isTruncated.dot");
        }
        return false;
    };

    var focusAndSelectText = function(id) {
        $("#"+id).focus();
        $("#"+id).select();
    };

    $('#order-by').bind('change', function() {
        displayUpdateMsg();

        //Save offset
        var offset = null, offsetObj = null, inputFocus = null;
        if (editingItems.length > 0) {
            offsetObj = editingItems[editingItems.length - 1]; // fetch the latest item
            inputFocus = $(document.activeElement);
            offset = $("li#" + offsetObj.id + "." + offsetObj.type).offset();
            if (offset == null) { // no matched elements => probably a note
                offset = $("#" + offsetObj.id).offset();
            }
        }

        var orderBy = $("#order-by").val();
        sortList($('ul#list-container'));
        addZebraStripesToParents();

        if (offset !== null) {
            // Restore offset
            var newOffset = $("li#" + offsetObj.id + "." + offsetObj.type).offset();
            if (newOffset == null) {
                newOffset = $("#" + offsetObj.id).offset();
            }
            if (newOffset.top != offset.top) {
                // Restore focus
                var newPos = $(window).scrollTop() + (newOffset.top - offset.top);
                $(window).scrollTop(newPos);
            }
            inputFocus[0].focus();
            offset = null;
        }

        $.unblockUI();        

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
        toggleChildren($(e.target));
        e.stopPropagation();
    };

    var toggleChildren = function(toggleBtn) {
        if (toggleBtn.attr("class").indexOf("ui-icon-triangle-1-s") != -1) {
            toggleBtn.removeClass("ui-icon-triangle-1-s");
            toggleBtn.addClass("ui-icon-triangle-1-e");
        } else if (toggleBtn.attr("class").indexOf("ui-icon-triangle-1-e") != -1) {
            toggleBtn.removeClass("ui-icon-triangle-1-e");
            toggleBtn.addClass("ui-icon-triangle-1-s");
        }
        var parent = getParent(toggleBtn.closest('li').attr("id"));
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
    };

    var expandedItems = new Array();

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
                        var parent = getParentBy(function(p) {
                            return p.themeId == cookieItem.id;
                        });
                        if (parent != null) {
                            var selectedItem = new Object();
                            selectedItem.id = parent.id;
                            selectedItem.type = "parent";
                            if (!contains(selectedItems, selectedItem)) {
                                selectedItems.push(selectedItem);
                            }
                        }
                    } else if (cookieItem.type == "epic") {
                        //Add all stories that are in this epic
                        var parent = getParentBy(function(p) {
                            return p.epicId == cookieItem.id;
                        });
                        if (parent != null) {
                            var selectedItem = new Object();
                            selectedItem.id = parent.id;
                            selectedItem.type = "parent";
                            if (!contains(selectedItems, selectedItem)) {
                                selectedItems.push(selectedItem);
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
                        var parent = getParentBy(function(p) {
                            return p.themeId == cookieItem.id;
                        });
                        if (parent != null) {
                            var selectedItem = new Object();
                            selectedItem.id = parent.id;
                            selectedItem.type = "parent";
                            if (!contains(selectedItems, selectedItem)) {
                                selectedItems.push(selectedItem);
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

    var selectItem = function(selectObj) {
        $("li#" + selectObj.id).addClass("ui-selected");
        selectedItems.push(selectObj);
    };

    var unselectItem = function(id) {
        $("li#" + id).removeClass("ui-selected");
        selectedItems.remove({id:id});
    };

    var unselectAll = function() {
        for (var i = 0; i < selectedItems.length; i++) {
            $('li[id|=' + selectedItems[i].id + ']').removeClass("ui-selected over");
        }
        selectedItems = new Array();
    };

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
            unselectAll();
        }

        if (pressed.attr("class").indexOf("parent") != -1) {
            //Parent was selected
            if (pressed.attr("class").indexOf("ui-selected") != -1) {
                //Already selected
                unselectItem(pressed.attr("id"));
            } else {
                //Not already selected
                selectItem({id:pressed.attr("id"), type:"parent"});
            }
        } else {
            //Child was selected
            if (pressed.attr("class").indexOf("ui-selected") != -1) {
                //Already selected
                unselectItem(pressed.attr("id"));
            } else {
                //Not already selected
                selectItem({id:pressed.attr("id"), type:"child"});
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
            unselectAll();
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
            while (i--) {
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
        var task = new Object();
        task.parentId = event.target.id;
        task.lastItem = getLastSelected("child");

        $.ajax({
            url : "../json/createtask/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(task),
            contentType : "application/json; charset=utf-8",
            success : function(newTask) {
                if (newTask != null) {
                    newTask.lastItem = task.lastItem;
                    updateTaskLi(newTask);
                    expandParentForChild(newTask.id);
                    visible[newTask.id] = true;

                    unselectAll();
                    selectItem({id : newTask.id, type : "child"});
                    updateCookie();

                    $.unblockUI();
                    editTask(newTask.id);
                    scrollTo(newTask.id);
                    focusAndSelectText("taskTitle"+newTask.id);
                    updateTypeMarkWidth();
                }
            },
            error : function(request, status, error) {
                $.unblockUI();
                alert(error);
            }
        });
        event.stopPropagation();
    };

    var createStory = function(event) {
        displayUpdateMsg();
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
            success : function(newStory) {
                if (newStory != null) {
                    newStory.lastItem = storyContainer.lastItem;
                    updateStoryLi(newStory);
                    $("li#" + newStory.id).addClass("ui-selected");
                    $.unblockUI();

                    unselectAll();
                    if (view == "story-task") {
                        selectItem({id : newStory.id, type : "parent"});
                    } else if (view == "epic-story") {
                        selectItem({id : newStory.id, type : "child"});
                    }
                    updateCookie();

                    expandParentForChild(newStory.id);
                    visible[newStory.id] = true;
                    editStory(newStory.id);
                    scrollTo(newStory.id);
                    focusAndSelectText("title"+newStory.id);
                    updateTypeMarkWidth();
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
            }
        });
        if (event != null) {
            event.stopPropagation();
        }
    };

    var createEpic = function(event) {
        displayUpdateMsg();
        var epicContainer = new Object();
        
        var theme = null;
        if (view == "theme-epic") {
            newEpicThemeID = event.target.id;
            theme = getParent(newEpicThemeID);
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
            success : function(newEpic) {
                if (newEpic != null) {
                    newEpic.lastItem = epicContainer.lastItem;
                    updateEpicLi(newEpic);

                    unselectAll();
                    if (view == "epic-story") {
                        selectItem({id : newEpic.id, type : "parent"});
                    } else if (view == "theme-epic") {
                        selectItem({id : newEpic.id, type : "child"});
                    }
                    updateCookie();

                    expandParentForChild(newEpic.id);
                    visible[newEpic.id] = true;
                    editEpic(newEpic.id);
                    $.unblockUI();
                    scrollTo(newEpic.id);
                    focusAndSelectText("epicTitle" + newEpic.id);
                    updateTypeMarkWidth();
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
            }
        });
        if (event != null) {
            event.stopPropagation();
        }
    };

    var createTheme = function(event) {
        displayUpdateMsg();
        var themeContainer = new Object();
        themeContainer.lastItem = getParentOfLastSelected();

        $.ajax({
            url : "../json/createtheme/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(themeContainer),
            contentType : "application/json; charset=utf-8",
            success : function(newTheme) {
                if (newTheme != null) {
                    newTheme.lastItem = themeContainer.lastItem;
                    updateThemeLi(newTheme);

                    unselectAll();
                    selectItem({id : newTheme.id, type : "parent"});
                    updateCookie();

                    $.unblockUI();
                    expandParentForChild(newTheme.id);
                    visible[newTheme.id] = true;
                    editTheme(newTheme.id);
                    scrollTo(newTheme.id);
                    focusAndSelectText("themeTitle" + newTheme.id);
                    updateTypeMarkWidth();
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
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
            success : function(newObj) {
                if (newObj != null) {
                    visible[newObj.id] = true;
                    unselectAll();
                    selectItem({id : newObj.id, type : familyMember});
                    updateCookie();
                }
                $.unblockUI();
                
                var children = newObj.children;
                if (type == "Story") {
                    updateStoryLi(newObj);
                    if (children.length > 0) {
                        for (var i = 0; i < children.length; i++) {
                            updateTaskLi(children[i]);
                        }
                    }
                    editStory(newObj.id);
                    scrollTo(newObj.id);
                    focusAndSelectText("title"+newObj.id);
                } else if (type == "Epic") {
                    updateEpicLi(newObj);
                    if (children.length > 0) {
                        for (var i = 0; i < children.length; i++) {
                            updateStoryLi(children[i]);
                        }
                    }
                    editEpic(newObj.id);
                    scrollTo(newObj.id);
                    focusAndSelectText("epicTitle" + newObj.id);
                } else if (type == "Theme") {
                    updateThemeLi(newObj);
                    if (children.length > 0) {
                        for (var i = 0; i < children.length; i++) {
                            updateEpicLi(children[i]);
                        }
                    }
                    editTheme(newObj.id);
                    scrollTo(newObj.id);
                    focusAndSelectText("themeTitle" + newObj.id);
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
            }
        });
    };

    var deleteItem = function (event) {
        itemId = event.target.id;
        var item = $(event.target).closest('li');
        var itemType = null;
        if (item.hasClass("task")) {
            itemType = "task";
        } else if (item.hasClass("story")) {
            itemType = "story";
        } else if (item.hasClass("epic")) {
            itemType = "epic";
        } else if (item.hasClass("theme")) {
            itemType = "theme";
        } else if (item.hasClass("note")) {
            itemType = "note";
            var splitStr = itemId.split("-"); // as note-id:s are on the form "note-#"
            itemId = parseInt(splitStr[splitStr.length - 1]);
        };

        $('#delete-item').attr("title","Delete "+itemType);
        $("#deleteDescription").html("Are you sure you want to delete this " + itemType + "?");
        $('#delete-item').dialog({
            resizable: false,
            minHeight: 0,
            modal: true,
            buttons: {
                Delete: function() {
                    displayUpdateMsg();
                    $.ajax({
                        url: "../json/delete" + itemType + "/" + areaName,
                        type: 'POST',
                        dataType: 'json',
                        data: JSON.stringify(parseInt(itemId)),
                        contentType: "application/json; charset=utf-8",
                        success: function (data) {
                            $.unblockUI();
                            removeItem(itemId);
                            unselectAll();
                            if (archivedView) {
                                reBuildArchivedList();
                            }
                        },
                        error: function (request, status, error) {
                            $.unblockUI();
                            alert(error);
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
     * Remove the object and the corresponding li-element, and if a parent, all the corresponding li-children
     * from the page
     * @param itemId The id of the item to remove
     */
    var removeItem = function(itemId) {
        var itemLi = $("li#" + itemId);
        if (itemLi.length == 0) { // no match - try note
            itemLi = $("#note-" + itemId);
        }

        if (itemLi.hasClass('parentLi')) { // A parent was removed
            var obj = removeParent(itemLi.attr('id'));
            if (typeof obj !== "undefined") {
                $('li[parentid="' + itemId + '"]').remove();
                itemLi.remove();
                decrPrioForParents(obj.prio);
            }
        } else if (itemLi.hasClass("note")) { // a removed note
            var note = removeNote(itemId);
            itemLi.remove();
            var storyId = parseInt(note.storyId);
            var showSingleNote = (expandedItems.indexOf(storyId) == -1);
            if (showSingleNote) {
                var singleNote = getSingleNote(getNotes(storyId));
                if (singleNote != null) {
                    updateNoteLi(singleNote); // A note was removed with the list collapsed
                }
            }
        } else if (itemLi.length > 0) { // A child was removed
            var parentLi = $("li#" + itemLi.attr("parentid"));
            var parent = getParent(itemLi.attr("parentid"));
            var children = parent.children;
            var position = -1;
            for (var i = 0; i < children.length; i++) {
                if (children[i].id == itemId) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                children.splice(position, 1);
                itemLi.remove();
                toggleExpandBtn(parentLi, itemLi, children);

                var attr = "prioInStory";
                if (view == "epic-story") {
                    attr = "prioInEpic";
                } else if (view == "theme-epic") {
                    attr = "prioInTheme";
                }

                for (var i = 0; i < children.length; i++) { // Update the prio for all children
                    children[i][attr] = i + 1;
                }
            }
        } else {
            removeNote(itemId); // test to see if it's a hidden note
        }
        if (typeof itemLi !== "undefined") {
            editingItems.remove({id:itemId});
        }
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
        for (var i = 0; i < editingItems.length; i++) {
            var bindEvent = null;
            if (editingItems[i].type == "story") {
                bindEvent = editStory;
            } else if (editingItems[i].type == "task") {
                bindEvent = editTask;
            } else if (editingItems[i].type == "epic") {
                bindEvent = editEpic;
            } else if (editingItems[i].type == "theme") {
                bindEvent = editTheme;
            }

            if (bindEvent != null) {
                $("." + editingItems[i].id).toggleClass('hidden-edit');
                $('li#'+editingItems[i].id).dblclick(bindEvent);
            }
        }
        editingItems = new Array();
        updateWhenItemsClosed();
    };

    /**
     * Save current editing of parents/children.
     */
    var bulkSave = function() {
        displayUpdateMsg();
        while (editingItems.length > 0) {
            var lastElement = $(editingItems).last()[0];
            var id = lastElement.id;
            if(lastElement.type != "note") {
                id = eval(lastElement.id);
            }
            editingItems.remove({id:id});

            if (lastElement.type == "task") {
                saveTask(id);
            } else if (lastElement.type == "story") {
                saveStory(id);
            } else if (lastElement.type == "epic") {
                saveEpic(id);
            } else if (lastElement.type == "theme") {
                saveTheme(id);
            }
        }
        editingItems = new Array();
        $.unblockUI();
    };

    /**
     * Returns true if you are going into edit mode on a parent/child.
     */
    var isGoingIntoEdit = function isGoingIntoEdit(editId){
        for (var i = 0; editingItems.length > i; i++) {
            if (editingItems[i].id == editId) {
                return false;
            }
        }
        return true;
    };

    /**
     * Toggles the Notes-list for the specified story-element
     * @param storyLi The story-li-element
     * @param open True to open/expand, false to close/collapse
     */
    var toggleNotesList = function(storyLi, open) {
        var moreNotesP = storyLi.find(".more-notes-loader-p");
        var notesFormDiv = $("#notes-form-" + storyLi.attr("id"));
        var expanded = false;

        if (open) {
            moreNotesP.removeClass("ui-hidden");
            if (loggedIn === true) {
                notesFormDiv.removeClass("ui-hidden");
                notesFormDiv.find("textarea").autosize('');
            }
        } else {
            moreNotesP.addClass("ui-hidden");
            notesFormDiv.addClass("ui-hidden");
            expanded = true;
        }
        
        if (expanded) { // list open; close
            collapseNotesList(storyLi);
        } else { // list closed; open
            var showSysMsgs = storyLi.find(".show-sys-msgs").prop('checked');
            expandNotesList(storyLi, showSysMsgs);
        }

    };

    /**
     * Collapse the notes-list belonging to the
     * specified story-li-element
     * @param storyLi The li-element for the Story
     */
    var collapseNotesList = function(storyLi) {
        var notes = getNotes(storyLi.attr("id"));
        var ul = storyLi.find("div.notes-container ul");
        ul.empty();
        var singleNote = getSingleNote(notes);
        if (singleNote != null) {
            var noteLi = updateNoteLi(singleNote);
            noteLi.find("div.note").addClass("single-note");
        } else {
            storyLi.find("div.notes-container").addClass("ui-hidden");
        }
    };

    /**
     * Get the 'single-note' (i.e. the newest non-system-generated note)
     * from the specified notes
     * @param notes The notes
     * @returns A note, or null if no single-note found
     */
    var getSingleNote = function(notes) {
        var singleNote = null;
        for (var i = 0; i < notes.length; i++) {
            if (notes[i].systemGenerated === false) {
                singleNote = notes[i];
                break;
            }
        }
        return singleNote;
    };

    /**
     * Expand the notes-list belonging to the specified
     * story-li-element
     * 
     * @param storyLi
     *            The li-element for the story
     * @param inclSysMsgs
     *            True to display system-generated messages as
     *            well, or false to hide them
     */
    var expandNotesList = function(storyLi, inclSysMsgs) {
        var notes = getNotes(storyLi.attr("id"));
        var ul = storyLi.find("div.notes-container ul");
        ul.empty();

        for (var i = notes.length - 1; i >= 0; i--) {
            var note = notes[i];
            if (inclSysMsgs || note.systemGenerated === false) {
                updateNoteLi(note);
            }
        }
    };

    var editStory = function(event) {
        var storyId = null;
        if (typeof event == "number") {
            storyId = event;
        } else {
            storyId = parseInt($(this).attr('id'));
        }

        if ($("li#" + storyId).hasClass("oneline-li")) {
            expandOneline(storyId);
        }

        if (view == "story-task") {
            var story = getParent(storyId);
        } else {
            story = getChild(storyId);
        }
        if (isGoingIntoEdit(storyId)) {
            if (expandedItems.indexOf(storyId) == -1) {
                // Expand the story
                var eventObj = jQuery.Event('click');
                eventObj.target = $("li#" + storyId);
                toggleExpandableItem(eventObj);
            }
            $("li#"+storyId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:storyId, type:"story"});
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

            if (story.storyAttr1 == null) {
                $("select#storyAttr1"+storyId).val('');
            } else {
                $("select#storyAttr1"+storyId).val(story.storyAttr1.id);
            }
            if (story.storyAttr2 == null) {
                $("select#storyAttr2"+storyId).val('');
            } else {
                $("select#storyAttr2"+storyId).val(story.storyAttr2.id);
            }
            if (story.storyAttr3 == null) {
                $("select#storyAttr3"+storyId).val('');
            } else {
                $("select#storyAttr3"+storyId).val(story.storyAttr3.id);
            }
            $("input#customer"+storyId).val(escapeHtml(story.customer));
            $("input#contributor"+storyId).val(escapeHtml(story.contributor));
            $("select#customerSite"+storyId).val(story.customerSite);
            $("select#contributorSite"+storyId).val(story.contributorSite);
            $("archiveStory"+storyId).val(story.archived);

            $("textarea#title" + storyId).val(escapeHtml(story.title));
            $("textarea#description" + storyId).val(escapeHtml(story.description));
            $('#archiveStory' + storyId).prop('checked', story.archived);

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

                    $('#save-all').button("option", "disabled", false);
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

                    $('#save-all').button("option", "disabled", false);
                }
            });

            $("textarea#theme"+storyId).focus(function() {
                $("textarea#theme"+storyId).autocomplete("search", $("textarea#theme"+storyId).val());
            });

            $("textarea#epic"+storyId).focus(function() {
                $("textarea#epic"+storyId).autocomplete("search", $("textarea#epic"+storyId).val());
            });

            $("textarea#epic"+storyId).val(escapeHtml(story.epicTitle));
            $("textarea#theme"+storyId).val(escapeHtml(story.themeTitle));
            
            //auto resize the textareas to fit the text
            $("textarea."+storyId).autosize('').trigger("autosize.resize");
        } else {
            editingItems.remove({id:storyId});
            $("."+storyId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
        }
    };

    var saveStory = function(event) {
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
            url: "../json/updatestory/"+areaName,
            type: 'POST',
            dataType: 'json',
            async: false,
            data: JSON.stringify(story),
            contentType: "application/json; charset=utf-8",
            success: function (updatedStory) {
                if (view == "story-task") {
                    if (getParent(storyId).archived != updatedStory.archived) { // we changed archived status
                        removeItem(storyId);
                    } else {
                        updateStoryLi(updatedStory);
                    }
                    if (archivedView && !updatedStory.archived) {
                        //Story was hidden from the current view;
                        //we need to add a new element
                        reBuildArchivedList();
                    }
                }
                exitEditMode(storyId);
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editStory);
    };

    /**
     * Adds the specified parent to the list of parents, as well as
     * puts it in the correct position in the specified list
     * @param lastItemObj An object with the id of the last selected element
     * @param newItemLi The new li-object to put in the list
     * @param newItemObj The object corresponding to the li-object
     * @param ulObj The ul-list-element to add the element to
     */
    var handleNewParentItem = function(lastItemObj, newItemLi, newItemObj, ulObj) {
        incrPrioForParents(newItemObj.prio);
        putParent(newItemObj.id, newItemObj);

        putInCorrPrioPos(ulObj, lastItemObj, newItemLi);

        if ($('#order-by').val() != "prio") {
            sortList(ulObj);
        } 
    };

    /**
     * Put the specified li-element (and, if any, children) in the correct position 
     * in the specified ul-list. If an lastItemObj isn't specified the li-element 
     * will simply be put last in the list
     * @param ulObj The ul-element to put the li-element in
     * @param lastItemObj An optional object with an id of the element after which to put the li-element
     * @param liObj The li-element to put in the list
     */
    var putInCorrPrioPos = function(ulObj, lastItemObj, liObj) {
        var parent = $('li.parentLi#' + liObj.attr("id"));
        var children = $('li.childLi[parentId="'+ liObj.attr("id") +'"]');
        if (parent.length == 0) {
            parent = liObj;
        }
        if (parent != null) {
            if (typeof lastItemObj !== "undefined" && lastItemObj != null) {
                var putAfter = getParentOrLastChildElement(lastItemObj.id);

                if (putAfter != null) {
                    putAfter.after(children);
                    putAfter.after(parent);
                } else {
                    ulObj.append(parent);
                    ulObj.append(children);
                }
            } else if ($('li.parentLi#' + liObj.attr("id")).length == 0) {
                ulObj.append(parent);
                ulObj.append(children);
            }
        }
    };

    /**
     * Get the last element belonging to the specified id, i.e. either the
     * parent if the id belongs to a child-less parent, or the last child
     * of the parent if the parent has children. If the id belongs to a 
     * child, the last child of the same parent will be returned.
     * @param itemId The id of a parent or child
     * @returns The last element that corresponds to the specified id
     */
    var getParentOrLastChildElement = function(itemId) {
        var elem = getParent(itemId);
        if (elem == null) {
            findChild(itemId, function(childObj, parentObj, posInParent) {
                elem = parentObj;
            });
        }
        if (elem != null) {
            var children = elem.children;
            if (typeof children !== "undefined" && children.length > 0) {
                return $('li#' + children[children.length - 1].id);
            } else {
                return $('li#' + elem.id);
            }
        }
        return null;
    };
    
    /**
     * Finds and updates (or creates) a Note-li with new values
     * @param updatedNote The new Note-object
     * @param appendFirst True to prepend, rather than the default append
     * @returns The updated or created li-object
     */
    var updateNoteLi = function(updatedNote, appendFirst) {
        var noteId = updatedNote.id;
        var notesArray = getNotes(updatedNote.storyId);
        var notePos = -1;

        for (var i = 0; i < notesArray.length; i++) {
            if (notesArray[i].id == noteId) {
                notePos = i;
                break;
            }
        }

        var lastElem = null;
        var offset = null;

        if (notePos < 0) { // new note
            notesArray.unshift(updatedNote);
        } else {
            notesArray[notePos] = updatedNote;
        }

        var ulList = $("li#" + updatedNote.storyId).find(".notes-container ul");
        var noteItem = $("#note-" + noteId);
        var singleMode = false;
        var scrollTop = false;
        if (noteItem.length == 0) { // No html-element exists for the Note
            var divItem = $('div#note-placeholder').clone();
            var htmlStr = divItem.html();
            htmlStr = htmlStr.replace(/-1/g, noteId); // Replace all occurences of -1
            noteItem = $(htmlStr);
            noteItem.addClass("ui-hidden");

            var showSysMsgs = $("li#" + updatedNote.storyId).find(".show-sys-msgs").prop('checked');
            var sysGen = updatedNote.systemGenerated;
            var insertItem = (!sysGen || sysGen && showSysMsgs);
            if (expandedItems.indexOf(updatedNote.storyId) == -1) {
                // Only showing one note
                singleMode = true;
                insertItem = !sysGen; // only non-system-msgs are shown in single-mode
                if (insertItem) {
                    ulList.empty();
                }
            } else if (ulList.scrollTop() > 40) {
                // Several notes in list, but not scrolled to top - save current list-position
                // (40 px marginal)
                lastElem = ulList.children().last();
                offset = lastElem.offset();
                noteItem.find("div.note").removeClass("single-note");
            } else {
                // Several notes and scrolled to bottom - scroll along with new elements
                scrollTop = true;
                noteItem.find("div.note").removeClass("single-note");
            }

            if (insertItem) {
                if (appendFirst === true) {
                    ulList.append(noteItem);
                } else {
                    ulList.prepend(noteItem);
                }
            }
        }

        if (!disableEditsBoolean) {
            $("a.deleteItem", noteItem).unbind('click');
            $("a.deleteItem", noteItem).click(deleteItem);
        } else {
            $("a.deleteItem", noteItem).addClass('disabled');
        }

        $("div.single-note", noteItem).unbind();
        $("div.single-note", noteItem).click(toggleExpandableItem);

        noteItem.find("span.user").text(updatedNote.user);
        noteItem.find("span.message").html(addLinksAndLineBreaks(updatedNote.message));
        noteItem.find("span.date").text(getFullDateAndTime(updatedNote.createdDate));
        noteItem.fadeIn("slow");
        if (singleMode) {
            var noteDiv = noteItem.find("div.note.single-note");
            trunc(noteDiv.find("p.user-msg"), null);
        }

        if (lastElem !== null && offset !== null) {
            var newOffset = lastElem.offset();
            if (typeof newOffset !== "undefined") {
                if (newOffset.top != offset.top) {
                    // Restore scroll
                    var newPos = ulList.scrollTop() + (newOffset.top - offset.top);
                    ulList.scrollTop(newPos);
                }
            }
        }

        if (scrollTop) {
            ulList.scrollTop(0);
        }
        return noteItem;
    };

    /**
     * Finds and updates a story li-element with new values.
     * @param updatedStory JSON data for the updated story
     * @param oneline set to true if it should be rendered in oneline mode
     */
    var updateStoryLi = function(updatedStory, oneline) {
        var storyId = updatedStory.id;

        var ulObj = null;
        if (updatedStory.archived && archivedView) {
            ulObj = $('ul#archived-list-container');
        } else if (!updatedStory.archived && !archivedView) {
            ulObj = $('ul#list-container');
        }

        if ($('li#' + storyId + '.story').length == 0) { //Was a new story
            var divItem = null;
            if (oneline) {
                divItem = $('div#story-oneline-placeholder').clone();
            } else {
                divItem = $('div#story-placeholder').clone();
            }
            var htmlStr = divItem.html();
            htmlStr = htmlStr.replace(/-1/g, storyId); // Replace all occurences of -1

            var newItem = $(htmlStr);

            if (view == "story-task") {
                handleNewParentItem(updatedStory.lastItem, newItem, updatedStory, ulObj);

                var notes = getNotes(storyId);
                var singleNote = getSingleNote(notes);
                if (singleNote != null) {
                    var noteLi = updateNoteLi(singleNote);
                    noteLi.find("div.note").addClass("single-note");
                } else {
                    newItem.find("div.notes-container").addClass("ui-hidden");
                }
                
                if (updatedStory.hasMoreNotes === true) {
                    setLoadNotesBtnState(newItem.find("a.more-notes-loader"), 'more-to-load');
                }
            } else if (view == "epic-story") {
                newItem.attr('parentid', updatedStory.epicId);
                var parent = getParent(updatedStory.epicId);
                var attr = 'prioInEpic';

                if (parent != null) {

                    if (updatedStory[attr] > 1) {
                        $('li#' + (parent.children[updatedStory[attr] - 2].id)).after(newItem);
                    } else {
                        $('li#' + parent.id).after(newItem);
                    }
                    addChildToParent(parent, updatedStory, attr);
                    toggleExpandBtn($('li#' + parent.id), newItem, parent.children);
                }
            }

            bindEventsToItem(newItem);
        } else {
            replaceParentOrChild(storyId, updatedStory);
            if (view == "story-task") {
                if (typeof updatedStory.children !== "undefined" && updatedStory.children.length > 0) {
                    $("li#" + storyId + " div.icon").addClass("expand-icon ui-icon ui-icon-triangle-1-e");
                }

                if (ulObj != null) {
                    putInCorrPrioPos(ulObj, updatedStory.lastItem, $('li#' + storyId + '.story'));
                    if ($("#order-by").val() != "prio") {
                        sortList(ulObj);
                    }
                }
            }
        }
        updateStoryLiContent(updatedStory);
    };

    var updateStoryLiContent = function(story) {
        var storyId = story.id;
        var storyLi = $("li#" + storyId);
        storyLi.find('.titleText').html(story.title);
        storyLi.find('.theme').html((story.themeTitle != undefined) ? story.themeTitle : "");
        storyLi.find('.epic').html((story.epicTitle != undefined) ? story.epicTitle : "");

        //Re-add truncate on the description paragraph
        var descriptionParagraph = storyLi.find('.story-description');
        if (descriptionParagraph.length > 0) {
            descriptionParagraph.html(addLinksAndLineBreaks(story.description));

            descriptionParagraph.removeClass("untrunc-description trunc-description");
            if (expandedItems.indexOf(storyId) > -1) {
                descriptionParagraph.addClass("untrunc-description");
            } else {
                descriptionParagraph.addClass("trunc-description");
                trunc(descriptionParagraph, null);
            }

            if (!isElementTruncated(descriptionParagraph) 
                    && view != "story-task" && expandedItems.indexOf(storyId) == -1) {
                storyLi.find("p.expand-item-p").addClass("ui-hidden");
            } else {
                storyLi.find("p.expand-item-p").removeClass("ui-hidden");
            }
        }

        storyLi.find('p.customerSite').html(getSiteImage(story.customerSite));
        storyLi.find('p.customer').html(story.customer);

        storyLi.find('p.contributorSite').html(getSiteImage(story.contributorSite));
        storyLi.find('p.contributor').html(story.contributor);

        storyLi.find('p.added').html(getDate(story.added));
        storyLi.find('p.deadline').html(getDate(story.deadline));

        storyLi.find('p.story-attr1').html(getAttrImage(story.storyAttr1)+getNameIfExists(story.storyAttr1));
        storyLi.find('p.story-attr2').html(getAttrImage(story.storyAttr2)+getNameIfExists(story.storyAttr2));
        storyLi.find('p.story-attr3').html(getAttrImage(story.storyAttr3)+getNameIfExists(story.storyAttr3));

        if (story.archived == true) {
            $('#archived-text' + storyId).text("Archived");
            $('#date-archived' + storyId).html(getDate(story.dateArchived));
            $('#archiveStory' + storyId).attr('checked', true);
        } else {
            $('#archived-text' + storyId).text("");
            $('#date-archived' + storyId).text("");
            $('#archiveStory' + storyId).attr('checked', false);
        }

        if (story.hasMoreNotes === false) {
            setLoadNotesBtnState($("#" + storyId + " a.more-notes-loader"), 'all-loaded');
        }

        var oneline = $("li#" + storyId + ".oneline-li");
        oneline.find(".title-span").html(story.title);
        var storyAttr1 = oneline.find('.story-attr1');
        var storyAttr2 = oneline.find('.story-attr2');
        var storyAttr3 = oneline.find('.story-attr3');
        var deadLine = oneline.find('.deadline');
        var dateArchived = oneline.find('.date-archived');

        storyAttr1.html(getAttrImage(story.storyAttr1)+getNameIfExists(story.storyAttr1));
        storyAttr2.html(getAttrImage(story.storyAttr2)+getNameIfExists(story.storyAttr2));
        storyAttr3.html(getAttrImage(story.storyAttr3)+getNameIfExists(story.storyAttr3));
        deadLine.html(getDate(story.deadline));
        dateArchived.html(getDate(story.dateArchived));

        //Add hover titles to the p tag since the header only displays
        //explanations for the parents
        if (story.storyAttr1 == null) {
            storyAttr1.attr("title", "");
        } else {
            storyAttr1.attr("title", area.storyAttr1.name);
        }
        if (story.storyAttr2 == null) {
            storyAttr2.attr("title", "");
        } else {
            storyAttr2.attr("title", area.storyAttr2.name);
        }
        if (story.storyAttr3 == null) {
            storyAttr3.attr("title", "");
        } else {
            storyAttr3.attr("title", area.storyAttr3.name);
        }
        if (story.deadline == null) {
            deadLine.attr("title", "");
        } else {
            deadLine.attr("title", "Deadline");
        }
        if (story.dateArchived == null) {
            dateArchived.attr("title", "");
        } else {
            dateArchived.attr("title", "Date archived");
        }

    };

    /**
     * Exit edit mode on a backlog item
     */
    var exitEditMode = function(id) {
        $("."+id).toggleClass('hidden-edit');
        editingItems.remove({id:id});
        updateWhenItemsClosed();
    };

    var saveTask = function(event) {
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
            url: "../json/updatetask/"+areaName,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(task),
            contentType: "application/json; charset=utf-8",
            success: function (updatedTask) {
                //Set the updated values
                updateTaskLi(updatedTask);
                exitEditMode(taskId);
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editTask);
    };
    
    var toggleExpandBtn = function(parentLi, childLi, childrenArr) {
        var iconDiv = parentLi.find('div.icon');
        $('.expand-icon', parentLi).unbind('click', expandClick);

        var visibleChild;
        if (iconDiv.hasClass("ui-icon-triangle-1-s")) {
            visibleChild = true;
            childLi.css('display', 'list-item');
            childLi.removeClass("ui-hidden");
        } else {
            visibleChild = false;
            childLi.css('display', 'none');
            childLi.addClass("ui-hidden");
        }

        childLi.each(function() {
            visible[$(this).attr("id")] = visibleChild;
        });

        iconDiv.removeClass('ui-icon expand-icon ui-icon-triangle-1-s ui-icon-triangle-1-e');

        if (childrenArr.length > 0) {
            if (visibleChild) {
                iconDiv.addClass('ui-icon-triangle-1-s');
            } else {
                iconDiv.addClass('ui-icon-triangle-1-e');
            }
            iconDiv.addClass('expand-icon ui-icon');
            $('.expand-icon', parentLi).bind('click', expandClick);
        }
    };
    
    /**
     * Expand the parent that the specified childId belongs to
     * @param childId The id of the child whose parent to expand
     */
    var expandParentForChild = function(childId) {
        findChild(childId, function(childObj, parentObj, posInParent) {
            var iconDiv = $("li#" + parentObj.id).find("div.icon");
            if (iconDiv.hasClass("ui-icon-triangle-1-e")) {
                toggleChildren(iconDiv);
            }
        });
    };

    /**
     * Finds and updates a task li-element with new values.
     */
    var updateTaskLi = function(updatedTask) {
        var taskId = updatedTask.id;

        if ($('li#' + taskId + '.task').length == 0) {
            var divItem = $('div#task-placeholder').clone();
            var htmlStr = divItem.html();
            var parentLi = $('li#' + updatedTask.parentId + '.story');
            htmlStr = htmlStr.replace(/-1/g, taskId); // Replace all occurences of -1

            newItem = $(htmlStr);            
            newItem.attr('id', taskId);
            newItem.attr('parentid', updatedTask.parentId);

            var parent = getParent(updatedTask.parentId);
            var children = parent.children;

            if (updatedTask.prioInStory > 1) {
                $('li#' + (children[updatedTask.prioInStory - 2].id) + '.task').after(newItem);
            } else {
                $('li#' + updatedTask.parentId + '.story').after(newItem);
            }
            addChildToParent(parent, updatedTask, 'prioInStory');
            toggleExpandBtn(parentLi, newItem, children);

            bindEventsToItem(newItem);
        } else {
            replaceChild(taskId, updatedTask);
        }

        $(".taskOwner."+taskId).find("p.taskInfo").html(updatedTask.owner);
        $(".calculatedTime."+taskId).find("p.taskInfo").html(updatedTask.calculatedTime);
        $(".taskStatus."+taskId).find("p.taskInfo").empty().append(getAttrImage(updatedTask.taskAttr1)).append(getNameIfExists(updatedTask.taskAttr1));

        //Re-add truncate on the title paragraph
        var titleParagraph = $(".taskTitle."+taskId).find("p.taskInfo");
        titleParagraph = untruncate(titleParagraph, updatedTask.title);
        titleParagraph.truncate(
                $.extend({}, truncateOptions, {className: 'truncate'+taskId, max_length: 90})
        );
        if (expandedItems.indexOf('truncate'+taskId) != -1) {
            $('a.truncate'+taskId, titleParagraph.parent()).click();
        }
    };

    var editTask = function(event) {
        var taskId = null;
        if (typeof event == "number") {
            taskId = event;
        }
        else {
            taskId = parseInt($(this).closest('li').attr('id'));
        }
        var task = getChild(taskId);
        if (isGoingIntoEdit(taskId)) {
            $("li#"+taskId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:taskId, type:"task"});
            $('button.'+taskId).button();
            $('button.'+taskId).unbind();
            $('.cancelButton.'+taskId).click({id: taskId},cancel);
            $('.save-button.'+taskId).click(function() {
                saveTask(parseInt(taskId), true);
            });
            $("."+taskId).toggleClass('hidden-edit');
            //sets values for all edit fields
            $("textarea#taskTitle" + taskId).val(escapeHtml(task.title));
            $("textarea#taskDescription" + taskId).val(escapeHtml(task.description));
            $("select#calculatedTime" + taskId).val(task.calculatedTime);

            if (task.taskAttr1 == null) {
                $("select#taskAttr1" + taskId).val('');
            } else {
                $("select#taskAttr1" + taskId).val(task.taskAttr1.id);
            }

            //auto resize the textareas to fit the text
            $("textarea."+taskId).autosize('').trigger("autosize.resize");
        } else {
            editingItems.remove({id:taskId});
            $("."+taskId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
            //Slide toggle fix
            $('#'+taskId).css("height", $('#'+taskId).height());
        }
    };

    var saveEpic = function(event) {
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
        epic.archived = $('#archiveEpic' + epicId).is(':checked');

        var themeTitle = $("textarea#epicTheme"+epicId).val();
        // In the Theme-Epic-view there is no textarea for Theme, so
        // we have to get it from the map instead
        if (themeTitle == null) {
            findChild(epicId, function(childObj, parentObj, posInParent) {
                themeTitle = childObj.themeTitle;
            });
        }
        epic.themeTitle = themeTitle;

        $.ajax({
            url: "../json/updateepic/"+areaName,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(epic),
            contentType: "application/json; charset=utf-8",
            success: function (updatedEpic) {
                if (view == "epic-story") {
                    if (getParent(epicId).archived != updatedEpic.archived) {
                        removeItem(epicId);
                    } else {
                        updateEpicLi(updatedEpic);
                        exitEditMode(epicId);
                    }
                } else {
                    replaceChild(epicId, updatedEpic);
                    exitEditMode(epicId);
                }
                if (archivedView && !updatedEpic.archived) {
                    //Epic was hidden from the current view;
                    //we need to add a new element
                    reBuildArchivedList();
                }
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editEpic);
    };

    /**
     * Finds and updates an epic li-element with new values.
     * @param updatedStory JSON data for the updated epic
     * @param oneline set to true if it should be rendered in oneline mode
     */
    var updateEpicLi = function(updatedEpic, oneline) {
        var epicId = updatedEpic.id;

        var ulObj = null;
        if (updatedEpic.archived) {
            ulObj = $("ul#archived-list-container");
        } else {
            ulObj = $("ul#list-container");
        }

        if ($('li#' + epicId + '.epic').length == 0) {
            var divItem = null;
            if (oneline) {
                divItem = $('div#epic-oneline-placeholder').clone();
            } else {
                divItem = $('div#epic-placeholder').clone();
            }
            var htmlStr = divItem.html();
            htmlStr = htmlStr.replace(/-1/g, epicId); // Replace all occurences of -1
            
            newItem = $(htmlStr);            
            newItem.attr('id', epicId);
            if (view == "theme-epic") {
                newItem.attr('parentid', updatedEpic.themeId);
            }

            if (view == "epic-story") {
                handleNewParentItem(updatedEpic.lastItem, newItem, updatedEpic, ulObj);
            } else {
                var parent = getParent(updatedEpic.themeId);
                var attr = 'prioInTheme';
                if (parent != null) {
                    
                    if (updatedEpic[attr] > 1) {
                        $('li#' + (parent.children[updatedEpic[attr] - 2].id)).after(newItem);
                    } else {
                        $('li#' + parent.id).after(newItem);
                    }

                    addChildToParent(parent, updatedEpic, attr);
                    toggleExpandBtn($('li#' + parent.id), newItem, parent.children);
                }
            }
            bindEventsToItem(newItem);
        } else {
            replaceParentOrChild(epicId, updatedEpic);
            if (view == "epic-story") {
                // If the update has meant a change of archive-status...
                putInCorrPrioPos(ulObj, updatedEpic.lastItem, $('li#' + epicId + '.epic'));
                if ($("#order-by").val() != "prio") {
                    sortList(ulObj);
                }
            }
        }

        $('.titles-epic-story, .titles-theme-epic').find('p.theme.'+epicId).html((updatedEpic.themeTitle != undefined) ? updatedEpic.themeTitle : "");
        $('.titles-epic-story, .titles-theme-epic').find('p.titleText.'+epicId).html(updatedEpic.title);

        var descriptionParagraph = $('.titles-epic-story, .titles-theme-epic').find('p.description.'+epicId);
        if (descriptionParagraph.length > 0) {
            descriptionParagraph.html(addLinksAndLineBreaks(updatedEpic.description));

            descriptionParagraph.removeClass("untrunc-description trunc-description");
            if (expandedItems.indexOf(epicId) > -1) {
                descriptionParagraph.addClass("untrunc-description");
            } else {
                descriptionParagraph.addClass("trunc-description");
                trunc(descriptionParagraph, null);
            }

            if (!isElementTruncated(descriptionParagraph) && expandedItems.indexOf(epicId) == -1) {
                $("li#" + epicId).find("p.expand-item-p").addClass("ui-hidden");
            } else {
                $("li#" + epicId).find("p.expand-item-p").removeClass("ui-hidden");
            }
        }

        if (updatedEpic.archived == true) {
            $('p#archived-text' + epicId).text("Archived");
            $('p#date-archived' + epicId).html(getDate(updatedEpic.dateArchived));
            $('#archiveEpic' + epicId).attr('checked', true);
        } else {
            $('p#archived-text' + epicId).text("");
            $('p#date-archived' + epicId).text("");
            $('#archiveEpic' + epicId).attr('checked', false);
        }

        var oneline = $("li#" + epicId + ".oneline-li");
        oneline.find(".title-span").html(updatedEpic.title);
        var dateArchived = oneline.find('.date-archived');
        dateArchived.html(getDate(updatedEpic.dateArchived));

        if (updatedEpic.dateArchived == null) {
            dateArchived.attr("title", "");
        } else {
            dateArchived.attr("title", "Date archived");
        }
    };

    var editEpic = function(event) {
        var epicId = null;
        if (typeof event == "number") {
            epicId = event;
        }
        else {
            epicId = parseInt($(this).closest('li').attr('id'));
        }

        if ($("li#" + epicId).hasClass("oneline-li")) {
            expandOneline(epicId);
        }

        if (view == "epic-story") {
            var epic = getParent(epicId);
        } else {
            epic = getChild(epicId);
        }

        if (isGoingIntoEdit(epicId)) {
            $("li#"+epicId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:epicId, type:"epic"});

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

                    $('#save-all').button("option", "disabled", false);
                }
            });
            
            $("textarea#epicTheme"+epicId).focus(function() {
                $("textarea#epicTheme"+epicId).autocomplete("search", $("textarea#epicTheme"+epicId).val());
            });
            
            $("textarea#epicTitle" + epicId).val(escapeHtml(epic.title));
            $("textarea#epicTheme"+epicId).val(escapeHtml(epic.themeTitle));
            $("textarea#epicDescription" + epicId).val(escapeHtml(epic.description));
            $('#archiveEpic' + epicId).prop('checked', epic.archived);
            
            //auto resize the textareas to fit the text
            $('textarea.'+epicId).autosize('').trigger("autosize.resize");
        } else {
            editingItems.remove({id:epicId});
            $("."+epicId).toggleClass('hidden-edit');
            updateWhenItemsClosed();
        }
    };

    var saveTheme = function(event) {
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
            url: "../json/updatetheme/"+areaName,
            type: 'POST',
            async: false,
            dataType: 'json',
            data: JSON.stringify(theme),
            contentType: "application/json; charset=utf-8",
            success: function (updatedTheme) {
                //if theme was moved from or to archive
                if (getParent(themeId).archived != updatedTheme.archived) {
                    removeItem(themeId);
                } else {
                    updateThemeLi(updatedTheme);
                    exitEditMode(themeId);
                }
                if (archivedView && !updatedTheme.archived) {
                    //Theme was hidden from the current view;
                    //we need to add a new element
                    reBuildArchivedList();
                }
            },
            error: function (request, status, error) {
                alert(error);
            }
        });
        li.dblclick(editTheme);
    };

    /**
     * Finds and updates a theme li-element with new values.
     * @param updatedStory JSON data for the updated theme
     * @param oneline set to true if it should be rendered in oneline mode
     */
    var updateThemeLi = function(updatedTheme, oneline) {
        var themeId = updatedTheme.id;

        var ulObj = null;
        if (updatedTheme.archived) {
            ulObj = $("ul#archived-list-container");
        } else {
            ulObj = $("ul#list-container");
        }

        if ($('li#' + themeId + '.theme').length == 0 && view == "theme-epic") {
            var divItem = null;
            if (oneline) {
                divItem = $('div#theme-oneline-placeholder').clone();
            } else {
                divItem = $('div#theme-placeholder').clone();
            }
            var htmlStr = divItem.html();
            htmlStr = htmlStr.replace(/-1/g, themeId); // Replace all occurences of -1

            newItem = $(htmlStr);            
            newItem.attr('id', themeId);
            handleNewParentItem(updatedTheme.lastItem, newItem, updatedTheme, ulObj);
            bindEventsToItem(newItem);
        } else {
            replaceParentOrChild(themeId, updatedTheme);

            // If the update has meant a change of archive-status...
            putInCorrPrioPos(ulObj, updatedTheme.lastItem, $('li#' + themeId + '.theme'));
            if ($("#order-by").val() != "prio") {
                sortList(ulObj);
            }
        }

        $('.titles-theme-epic').find('p.titleText.'+themeId).html(updatedTheme.title);

        var descriptionParagraph = $('.titles-theme-epic').find('p.description.'+themeId);
        if (descriptionParagraph.length > 0) {
            descriptionParagraph.html(addLinksAndLineBreaks(updatedTheme.description));

            descriptionParagraph.removeClass("untrunc-description trunc-description");
            if (expandedItems.indexOf(themeId) > -1) {
                descriptionParagraph.addClass("untrunc-description");
            } else {
                descriptionParagraph.addClass("trunc-description");
                trunc(descriptionParagraph, null);
            }

            if (!isElementTruncated(descriptionParagraph) && expandedItems.indexOf(themeId) == -1) {
                $("li#" + themeId).find("p.expand-item-p").addClass("ui-hidden");
            } else {
                $("li#" + themeId).find("p.expand-item-p").removeClass("ui-hidden");
            }
        }
        
        if (updatedTheme.archived == true) {
            $('p#archived-text' + themeId).text("Archived");
            $('p#date-archived' + themeId).html(getDate(updatedTheme.dateArchived));
            $('#archiveTheme' + themeId).attr('checked', true);
        } else {
            $('p#archived-text' + themeId).text("");
            $('p#date-archived' + themeId).text("");
            $('#archiveTheme' + themeId).attr('checked', false);
        }

        var oneline = $("li#" + themeId + ".oneline-li");
        oneline.find(".title-span").html(updatedTheme.title);
        var dateArchived = oneline.find('.date-archived');
        dateArchived.html(getDate(updatedTheme.dateArchived));

        if (updatedTheme.dateArchived == null) {
            dateArchived.attr("title", "");
        } else {
            dateArchived.attr("title", "Date archived");
        }

    };

    var editTheme = function(event) {
        var themeId = null;
        if (typeof event == "number") {
            themeId = event;
        } else {
            themeId = parseInt($(this).closest('li').attr('id'));
        }

        if ($("li#" + themeId).hasClass("oneline-li")) {
            expandOneline(themeId);
        }

        if (view == "theme-epic") {
            var theme = getParent(themeId);
        } else {
            theme = getChild(themeId);
        }
        if (isGoingIntoEdit(themeId)) {
            $("li#"+themeId).unbind("dblclick"); //Only the cancel button closes again
            editingItems.push({id:themeId, type:"theme"});

            $('button.'+themeId).button();
            $('button.'+themeId).unbind();
            $('.cancelButton.'+themeId).click({id: themeId},cancel);
            $('.save-button.'+themeId).click(function() {
                saveTheme(parseInt(themeId), true);
            });
            $("."+themeId).toggleClass('hidden-edit');
            
            $('textarea#themeTitle' + themeId).val(escapeHtml(theme.title));
            $('textarea#themeDescription' + themeId).val(escapeHtml(theme.description));
            $('#archiveTheme' + themeId).prop('checked', theme.archived);

            //auto resize the textareas to fit the text
            $('textarea.'+themeId).autosize('').trigger("autosize.resize");
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
                                for (var i = 0; i < storiesToMove.length; i++) {
                                    removeItem(storiesToMove[i]);
                                }
                                unselectAll();
                                $.unblockUI();
                            },
                            error: function (request, status, error) {
                                $.unblockUI();
                                alert(error);
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
     * Set the state of the specified "load-notes"-button
     * @param btn The button-element
     * @param state 'loading', 'more-to-load' or 'all-loaded'
     */
    var setLoadNotesBtnState = function(btn, state) {
        btn.unbind('click');
        btn.removeClass('ui-state-disabled');
        state = state.toLowerCase();
        if (state === 'loading') {
            btn.text('[ LOADING ]');
        } else if (state === 'more-to-load') {
            btn.text('Load older notes');
            btn.click(fetchMoreNotesFromServer);
        } else if (state === 'all-loaded') {
            btn.addClass('ui-state-disabled');
            btn.text('All notes loaded');
        }
    };

    /**
     * Used when posting/creating a new Note
     * @param storyId The id of the Story to post the Note to
     * @param message The message of the Note
     */
    var postNote = function(storyId, message) {
        displayUpdateMsg();

        var noteContainer = new Object();
        noteContainer.id = -1;
        noteContainer.storyId = storyId;
        noteContainer.message = message;
        $.ajax({
            url : "../json/createnote/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : JSON.stringify(noteContainer),
            contentType : "application/json; charset=utf-8",
            success : function(newNote) {
                if (newNote != null) {
                    $.unblockUI();
                    $("#notes-textarea-" + storyId).val("").trigger('autosize.resize');
                    updateNoteLi(newNote);

                    var newElem = $('#note-' + newNote.id);
                    var ulList = newElem.closest('ul');
                    ulList.animate({
                        // scroll up to the new note
                        scrollTop: 0},
                        200);
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
            }
        });
    };

    /**
     * Used to get more Notes from the server and put them in the
     * corresponding note-list
     */
    var fetchMoreNotesFromServer = function(event) {
        var item = $(event.target);
        setLoadNotesBtnState(item, 'loading');
        var story = item.closest('li');
        var storyId = story.attr("id");

        var nbrOfNotes = getNotes(storyId).length;
        var part = Math.floor(nbrOfNotes / MAX_NOTES) + 1;
        $.ajax({
            url : "../json/read-notes/" + storyId + "/" + part,
            type : 'GET',
            dataType : 'json',
            contentType : "application/json; charset=utf-8",
            success : function(jsonResp) {
                if (jsonResp != null) {
                    var moreNotesAvail = jsonResp.moreNotesAvailable;
                    var notesList = jsonResp.notesList;
                    var newList = getNotes(storyId).concat(notesList);
                    setNotesList(storyId, newList);
                    $("#notes-textarea-" + storyId).val("");
                    var showSysMsgs = story.find(".show-sys-msgs").prop('checked');
                    for (var i = 0; i < notesList.length; i++) {
                        if (showSysMsgs || notesList[i].systemGenerated === false) {
                            updateNoteLi(notesList[i], true);
                        }
                    }

                    if (moreNotesAvail == true) {
                        setLoadNotesBtnState(item, 'more-to-load');
                    } else {
                        setLoadNotesBtnState(item, 'all-loaded');
                    }
                }
            },
            error : function(error) {
                $.unblockUI();
                alert(error);
            }
        });
        if (event != null) {
            event.stopPropagation();
        }
    };

    /**
     * Show or hide system-notes depending
     * on the checkbox-status
     */
    var toggleSystemNotes = function(event) {
        var checkbox = $(event.target);
        var story = checkbox.closest('li.parentLi');

        var offset = checkbox.offset().top;
        expandNotesList(story, checkbox.prop('checked'));
        var newOffset = checkbox.offset().top;
        if (offset != newOffset) {
            $(window).scrollTop($(window).scrollTop() + (newOffset - offset));
        }
    };

    /**
     * Updates save all button when the last editing item is going out of edit mode.
     */
    var updateWhenItemsClosed = function() {
        if (editingItems.length == 0) {
            $('#save-all').button("option", "disabled", true);
        }
        updateAllExpandBtns();
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
     * Populates the archived-list with backlog items belonging
     * to argument page number.
     */
    var buildArchivedList = function(pageNbr) {
        var type = null;
        if (view == "story-task") {
            type = "Story";
        } else if (view == "epic-story") {
            type = "Epic";
        } else if (view == "theme-epic") {
            type = "Theme";
        }
        $("#pagination").pagination('destroy');

        var url = "../json/read-archived/" + areaName + "?type=" + type + "&page=" + pageNbr;
        if (isFilterActive()) {
            url += "&ids=" + $("#filter").val();
        }
        $.ajax({
            url: url,
            dataType: 'json',
            error: function (request, status, error) {
                alert(error);
            },
            success: function (data) {
                $("#pagination").pagination({
                    pages: data.nbrOfPages,
                    currentPage: pageNbr,
                    cssStyle: 'light-theme',
                    onPageClick: function(pageNumber, event) {
                        $('#archived-list-container').empty();
                        buildArchivedList(pageNumber);
                        return false; //Makes sure that no hashtag is appended to URL
                    }
                });
                if (view == "story-task") {
                    notesMap = data.notesMap;
                }
                var archivedItems = data.archivedItems;
                for (var i=0; i<archivedItems.length; i++) {
                    var childData = archivedItems[i].children;
                    archivedItems[i].children = new Array();
                    if (type == "Story") {
                        updateStoryLi(archivedItems[i],true);
                        for (var k = 0; k < childData.length; k++) {
                            updateTaskLi(childData[k]);
                        }
                    } else if (type == "Epic") {
                        updateEpicLi(archivedItems[i], true);
                        for (var k = 0; k < childData.length; k++) {
                            updateStoryLi(childData[k], true);
                        }
                    } else if (type == "Theme") {
                        updateThemeLi(archivedItems[i], true);
                        for (var k = 0; k < childData.length; k++) {
                            updateEpicLi(childData[k], true);
                        }
                    }
                    //TODO: Make sure that sort is not called several times
                }

                updateTypeMarkWidth();
            }
        });
    };

    /**
     * Rebuilds the archived list.
     * This means adding missing items and updating the ones that 
     * already exist.
     */
    var reBuildArchivedList = function() {
        var currentPage = $("#pagination").pagination('getCurrentPage');
        buildArchivedList(currentPage);
    };

    if (archivedView) {
        buildArchivedList(1);
    }

    /**
     * Builds the visible html list using the JSON data
     */
    buildVisibleList = function () {
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
                $(this).css("display", "none");
            }
        });

        //Truncating long description texts
        $("li").each(function() {
            var currentId = $(this).attr("id");
            $('p.taskInfo', this).truncate(
                    $.extend(truncateOptions, {className: 'truncate'+currentId, max_length: 90})
            );
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

                $('#save-all').button("option", "disabled", false);
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

                $('#save-all').button("option", "disabled", false);
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

        if (typeof loggedIn !== "undefined" && loggedIn === true) {
            $(".note-textarea").keyup(function (e) {
                if (e.keyCode == KEYCODE_ENTER && !e.shiftKey) {
                    var storyId = $(this).closest("li.story").attr("id");

                    if ($(this).val().length > 0) { // prevent empty notes
                        editingItems.remove({id:$(this).attr("id"), type:"note"});
                        postNote(parseInt(storyId), $(this).val());
                    }
                    e.stopPropagation();
                }
            });

            $(".note-textarea").focus(function(event) {
                editingItems.push({id:$(this).attr("id"), type:"note"});
            });
            $(".note-textarea").blur(function(event) {
                editingItems.remove({id:$(this).attr("id"), type:"note"});
            });
        } else {
            $("div.notes-form").addClass("ui-hidden");
        }
        $(".more-notes-loader").click(fetchMoreNotesFromServer);
        $(".show-sys-msgs").click(toggleSystemNotes);

        $(".expand-item-btn").click(toggleExpandableItem);

        if (disableEditsBoolean) {
            disableEdits();
        }

        $(".oneline.title-span").click(expandOneline);

        updateTypeMarkWidth();

        if (isFilterActive() || disableEditsBoolean || $("#order-by").val() != "prio") {
            $("#list-container").sortable("option", "disabled", true);
        }
        firstBuild = false;
        addZebraStripesToParents();
    };

    /**
     * Sets the same width for all typemarks (for example "Story 42")
     */
    var updateTypeMarkWidth = function() {
        $(".typeMark").css("width", "auto");
        var maxWidth = 0;
        $(".typeMark").each(function (index) {
            var width = $(this).width();
            if (width == 0) {
                //The item is probably in a hidden container;
                //clone it and get the width.
                var clone = $(this).clone();
                clone.attr("style", "position: absolute !important; top: -100px !important;");
                clone.appendTo("body");
                width = clone.width();
                clone.remove();
            }
            if (width > maxWidth) {
                maxWidth = width;
            }
        });
        maxWidth = Math.ceil(maxWidth*1.1);
        $(".typeMark, .header-id").width(maxWidth);
    };

    /**
     * Expands a backlogitem from oneline mode.
     */
    var expandOneline = function(event, ignoreEffects) {
        var li = null;
        var id = null;

        if (typeof event == "number") {
            id = event;
            li = $("li#" + id);
        } else {
            li = $(this).closest("li");
            id = li.attr("id");
        }

        var lastId = li.prev("li").attr("id");

        var jsonData = getParent(id);
        if (jsonData == null) {
            jsonData = getChild(id);
        } else {
            var lastItem = new Object();
            lastItem.id = lastId;
            jsonData.lastItem = lastItem;
        }
        li.remove();
        if (li.hasClass("story")) {
            updateStoryLi(jsonData);
        } else if (li.hasClass("epic")) {
            updateEpicLi(jsonData);
        } else if (li.hasClass("theme")) {
            updateThemeLi(jsonData);
        }

        li = $("li#" + id);
        if (lastId == null) {
            //Move the item to top.
            li.prependTo(li.parent());
            var children = $('li.childLi[parentId="'+ id +'"]');
            li.after(children);
        }

        if (!ignoreEffects) {
            var height = li.height();
            li.height("20px");

            li.animate({height: height}, 200, function(){
                li.css("height", "auto");
            });
            unselectAll();
            if (li.hasClass("parentLi")) {
                selectItem({id:id, type:"parent"});
            } else if (li.hasClass("childLi")) {
                selectItem({id:id, type:"child"});
            }
            updateCookie();
            addZebraStripesToParents();
        }

        if (jsonData.children != null && jsonData.children.length > 0) {
            var iconDiv = li.find('div.icon');
            if (visible[jsonData.children[0].id]) {
                iconDiv.addClass('ui-icon-triangle-1-s');
            } else {
                iconDiv.addClass('ui-icon-triangle-1-e');
            }
            iconDiv.addClass('expand-icon ui-icon');
            $('.expand-icon', li).bind('click', expandClick);
        }
    };

    /**
     * Collapses a backlog item to oneline mode.
     */
    var collapseOneline = function() {
        var li = $(this).closest("li");
        var id = li.attr("id");
        var lastId = li.prev("li").attr("id");

        var jsonData = getParent(id);
        if (jsonData == null) {
            jsonData = getChild(id);
        } else {
            var lastItem = new Object();
            lastItem.id = lastId;
            jsonData.lastItem = lastItem;
        }
        expandedItems.remove(id);

        li.animate({height: 20}, 200, function(){
            li.remove();
            if (li.hasClass("story")) {
                updateStoryLi(jsonData, true);
            } else if (li.hasClass("epic")) {
                updateEpicLi(jsonData, true);
            } else if (li.hasClass("theme")) {
                updateThemeLi(jsonData, true);
            }
            li = $("li#" + id);

            if (lastId == null) {
                //Move the item to top.
                li.prependTo(li.parent());
                var children = $('li.childLi[parentId="'+ id +'"]');
                li.after(children);
            }

            if (jsonData.children != null && jsonData.children.length > 0) {
                var iconDiv = li.find('div.icon');
                if (visible[jsonData.children[0].id]) {
                    iconDiv.addClass('ui-icon-triangle-1-s');
                } else {
                    iconDiv.addClass('ui-icon-triangle-1-e');
                }
                iconDiv.addClass('expand-icon ui-icon');
                $('.expand-icon', li).bind('click', expandClick);
            }

            unselectAll();
            if (li.hasClass("parentLi")) {
                selectItem({id:id, type:"parent"});
            } else if (li.hasClass("childLi")) {
                selectItem({id:id, type:"child"});
            }
            updateCookie();
            addZebraStripesToParents();
        });
    };

    bindEventsToItem = function (elem) {
        var elemId = elem.attr("id");

        //Call refresh when excessive calls for bindEventsToItem has stopped
        delay(function() {
            $("#list-container").sortable("refresh");
        }, 250, TIMER_BIND_EVENTS );

        elem.click(liClick);
        elem.mousedown(function () {
            $(this).addClass("over");
        });
        elem.mouseup(function () {
            $(".parent-child-list").children("li").removeClass("over");
        });

        $(".bindChange", elem).change( function(event){
            $('#save-all').button( "option", "disabled", false );
        });

        $(".bindChange", elem).bind('input propertychange', function(event) {
            $("#save-all").button( "option", "disabled", false );
        });

        $("a.createEpic", elem).click(createEpic);
        $("a.createStory", elem).click(createStory);
        $("a.createTask", elem).click(createTask);
        $("a.deleteItem", elem).click(deleteItem);
        $("a.cloneItem", elem).click(function() {
            cloneItem($(this), false);
        });
        $("a.cloneItem-with-children", elem).click(function() {
            cloneItem($(this), true);
        });

        elem.unbind("dblclick");

        if (elem.hasClass("editStory")) {
            elem.dblclick(editStory);
        } else if (elem.hasClass("editTask")) {
            elem.dblclick(editTask);
        } else if (elem.hasClass("editEpic")) {
            elem.dblclick(editEpic);
        } else if (elem.hasClass("editTheme")) {
            elem.dblclick(editTheme);
        }
        //This avoids exiting edit mode if an element inside a theme, epic, story or task is double clicked.
        $(".bindChange", elem).dblclick(function(event) {
            event.stopPropagation();
        });

        $( "#storyTheme,#epicTheme", elem ).autocomplete({
            minLength: 0,
            source: "../json/autocompletethemes/" + areaName,
            change: function() {
                $( "#storyEpic", elem ).attr("value", "");
            },
            select: function (event, data) {
                $( "#storyEpic", elem ).attr("value", "");
                //Used for deselecting the input field.
                $(this).autocomplete('disable');
                $(this).autocomplete('enable');

                $('#save-all').button("option", "disabled", false);
            }
        });

        $("#storyTheme", elem).focus(function() {
            $("#storyTheme", elem).autocomplete("search", $("#storyTheme", elem).val());
        });

        $("textarea#epicTheme", elem).focus(function() {
            $("textarea#epicTheme", elem).autocomplete("search", $("#epicTheme", elem).val());
        });

        $( "#storyEpic", elem ).autocomplete({
            minLength: 0,
            select: function (event, data) {
                //Used for deselecting the input field.
                $(this).autocomplete('disable');
                $(this).autocomplete('enable');

                $('#save-all').button("option", "disabled", false);
            },
            search: function() {
                var themeName = $("#storyTheme", elem).val();
                $( "#storyEpic", elem ).autocomplete({
                    source: "../json/autocompleteepics/" + areaName + "?theme=" + themeName
                });
            }
        }).focus(function() {
            $(this).autocomplete("search", "");
        }).blur(function(){
            $(this).autocomplete('enable');
        });

        if (typeof loggedIn !== "undefined"  && loggedIn === true) {
            $(".note-textarea", elem).keyup(function (e) {
                if (e.keyCode == KEYCODE_ENTER && !e.shiftKey) {
                    var storyId = $(this).closest("li.story").attr("id");

                    if ($(this).val().length > 0) {
                        editingItems.remove({id:$(this).attr("id"), type:"note"});
                        postNote(parseInt(storyId), $(this).val());
                    }
                    e.stopPropagation();
                }
            });
            $(".note-textarea", elem).focus(function(event) {
                editingItems.push({id:$(this).attr("id"), type:"note"});
            });
            $(".note-textarea", elem).blur(function(event) {
                editingItems.remove({id:$(this).attr("id"), type:"note"});
            });
        } else {
            $("div.notes-form", elem).addClass("ui-hidden");
        }
        $(".more-notes-loader", elem).click(fetchMoreNotesFromServer);
        $(".show-sys-msgs", elem).click(toggleSystemNotes);

        $(".expand-item-btn", elem).click(toggleExpandableItem);

        //Stops textarea from making a new line when trying to save changes.
        $("textarea", elem).keydown(function(e){
            if (e.keyCode == KEYCODE_ENTER && !e.shiftKey) {
                e.preventDefault();
            }
        });

        if (disableEditsBoolean) {
            disableEdits();
        }

        $("p.titleText", elem).click(collapseOneline);
        $(".oneline.title-span", elem).click(expandOneline);
    };

    /**
     * Expands or collapses the clicked Story/Theme/Epic-element
     */
    var toggleExpandableItem = function(event) {
        var li = $(event.target).closest("li.story, li.epic, li.theme");
        var id = parseInt(li.attr("id"));
        var descr = li.find("p.story-description, p.epic-description, p.theme-description");
        var isParentStory = (li.hasClass("parentLi") && li.hasClass("story"));

        if (expandedItems.indexOf(id) == -1) { // collapsed; open
            descr.trigger("destroy.dot");
            descr.removeClass("trunc-description").addClass("untrunc-description");
            expandedItems.push(id);

            exclScrollBarFromSortable($("p.story-description, p.epic-description, p.theme-description"));
            exclScrollBarFromSortable($("div.notes-container ul"));
            exclScrollBarFromSortable($("div.titles textarea.description, div.titles-theme-epic textarea.description"));

            if (isParentStory) {
                li.find("div.notes-container").removeClass("ui-hidden").addClass("in-edit");
                toggleNotesList(li, true);
                li.find("div.notes-form textarea").focus();
            }
        } else { // already open; collapse
            descr.removeClass("untrunc-description").addClass("trunc-description");
            trunc(descr, null);
            expandedItems.remove(id);

            if (!isGoingIntoEdit(id)) { // currently editing
                var event = jQuery.Event('click');
                event.data = {id:id};
                $(".cancelButton", li).trigger(event); // cancel editing
            }

            if (isParentStory) {
                li.find("div.notes-container").removeClass("in-edit");
                toggleNotesList(li, false);
            }
        }
    };

    var setHeightAndMargin = function (value) {
        $("#list-container-div").css("margin", value+"px auto");
    };

    /**
     * Iterates all non-oneline-li:s and checks if the elements'
     * description-paragrah is truncated or not, and based on this
     * hides or shows the expand-button
     */
    var updateAllExpandBtns = function() {
        var elements = $("#list-container li:not(.oneline-li)");
        if (archivedView === true) {
            elements = $("#archived-list-container li:not(.oneline-li)");
        }

        elements.each(function(index) {
            var paragraph = $(this).find("p.story-description, p.epic-description, p.theme-description");
            paragraph.trigger("update.dot");
            var expandBtn = $(this).find("p.expand-item-p");
            if (!isElementTruncated(paragraph) && view != "story-task" || expandedItems.indexOf($(this).attr("id")) > -1) {
                expandBtn.addClass("ui-hidden");
            } else {
                expandBtn.removeClass("ui-hidden");
            }
        });
    };

    $(window).resize(function() {
        $("#header").css("height", "auto");
        setHeightAndMargin($("#header").height());
        // Delay to let dotdotdot truncate before we check
        delay(updateAllExpandBtns, 10, TIMER_RETRUNCATE);
        updateTypeMarkWidth();
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
        var moveType = null;
        if (selectedItems.length > 0) {
            moveType = (selectedItems[0].type == "child") ? "childMove" : "parentMove";
        }

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
                if (data != null) {
                    if (moveType == "childMove") {
                        var dataObj = new Object();
                        dataObj.objects = data;
                        dataObj.view = view;
                        handleMovePush(moveType, dataObj);

                        expandParentForChild(selectedItems[0].id);
                    }
                }

                $.unblockUI();
            },
            error: function (request, status, error) {
                $.unblockUI();
                alert(error);
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

            /* A child-element must not be positioned in the beginning of the list, as there
             * is no parent */
            if (ui.item.hasClass('childLi') && ui.item.index() == 0) {
                $(this).sortable('cancel');
            } else {
                displayUpdateMsg();
                sendMovedItems();
            }
            var pressed = $(ui.item);
            pressed.removeClass("moving");
            $('.ui-selected').removeClass("ui-hidden");
            lastPressed = null;
            $(".over").removeClass("over");
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
        allExpanded = true;
        displayUpdateMsg();
        setTimeout(function() {
            //setTimeout is used in order to let the browser show the updating message
            //before running heavy operations.
            $("li.oneline-li").each(function() {
                var id = parseInt($(this).attr("id"));
                if (id != -1) {
                    expandOneline(id, true);
                }
            });

            iterAllParents(function(parent) {
                var expBtn = $("li#" + parent.id).find("div.icon");
                if (expBtn.hasClass("ui-icon-triangle-1-e")) {
                    toggleChildren(expBtn);
                }
            });

            for (var i = 0; i < selectedItems.length; ++i) {
                $('li[id|=' + selectedItems[i].id + ']').addClass("ui-selected");
            }
            $( "#list-container" ).sortable("refresh");

            addZebraStripesToParents();
            $.unblockUI();
        },50);
    });
    $("#collapse-all").click(function() {
        location.reload();
    });

    $(".showArchive").buttonset();

    /**
     * Sets timeout for a function, to be reset after each call on delay
     * for the same timer-group ( TIMER_FILTER / TIMER_BIND_EVENT /
     *  TIMER_SCROLL_SORTABLE ).
     * Can for example be used to trigger a function after
     * typing has stopped in a textbox.
     * Usage:
     * delay(function() {
            //To be called after timeout
        }, *delayInMs*, TIMER_* ); 
     */
    var delay = (function() {
        var timer = [0, 0, 0, 0];
        return function(callback, ms, index) {
            clearTimeout (timer[index]);
            timer[index] = setTimeout(callback, ms);
        };
    })();

    $('#filter').keyup(function() {
        delay(function() {
            updateFilter();
        }, 500, TIMER_FILTER );
    });

    /*
     * Update view when the filter is changed.
     * Also disable drag and drop if filter is active.
     */
    var updateFilter = function() {
        if (archivedView) {
            $('#archived-list-container').empty();
            buildArchivedList(1);
        } else {
            iterAllParents(function(parent) {
                var pLi = $("li#" + parent.id);
                var cLi = $('[parentid="' + parent.id + '"]');
                var visibleChildren = true;
                if (isFiltered(parent.id) || !isFilterActive()) {
                    pLi.removeClass("ui-hidden");
                    pLi.css("display", "list-item");
                    if (pLi.find("div.icon").hasClass("ui-icon-triangle-1-s")) {
                        cLi.css("display", "list-item");
                        cLi.removeClass("ui-hidden");
                    }
                } else {
                    visibleChildren = false;
                    pLi.addClass("ui-hidden");
                    pLi.css("display", "none");
                    if (typeof parent.children !== "undefined") {
                        cLi.css("display", "none");
                        cLi.addClass("ui-hidden");
                    }
                }

                if (typeof parent.children !== "undefined") {
                    for (var i = 0; i < parent.children.length; i++) {
                        visible[parent.children[i]] = visibleChildren;
                    }
                }
            });
        }

        if (isFilterActive()) {
            $("html, body").animate({ scrollTop: 0 }, "fast");
            var paramString = "?";
            if (archivedView) {
                paramString = "?archived-view=true&";
            }
            paramString += 'ids=' + $("#filter").val();
            window.history.replaceState( {} , document.title, paramString );
            $("#list-container").sortable("option", "disabled", true);

            //Remove selected items if they are invisible after changing filter
            selectedItems = $.grep(selectedItems, function (selected, i) {
                return isFiltered(selected.id);
            });
            updateCookie(); //Necessary if selected items changed
        } else {
            var paramString = "?";
            if (archivedView) {
                paramString += "archived-view=true";
            }
            window.history.replaceState( {} , document.title, paramString);
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
            location.reload();
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
    
    $("#header").css("height", "auto");
    setHeightAndMargin($("#header").height());

});
