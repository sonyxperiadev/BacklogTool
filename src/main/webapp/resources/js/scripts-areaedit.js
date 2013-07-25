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
$(document).ready(function() {
    $('#login-out').button();
    $("#name-button").button();
    

	$("#login-out").button({
	    text: false,
	    icons: {
	        primary: 'silk-icon-door-out'
	    }
	});
	
	$('#add-admin, #add-editor').button({
	    text: false,
	    icons: {
	        primary: 'silk-icon-add'
	    }
	});
	
	$('.addOption, .addOptionSeries').button({
	    icons: {
	        primary: 'silk-icon-add'
	    }
	});
	
	$('#save').button({
	    icons: {
	        primary: 'silk-icon-disk'
	    }
	});
    
    $("#name-button").click(function() {
        $.ajax({
            url : "../json/changeAreaName/" + areaName,
            type : 'POST',
            data : $("#area-name").val(),
            contentType : "application/json; charset=utf-8",
            success : function(response) {
                if (response == "") {
                    location.reload();
                } else {
                    window.location.href = "./" + response;
                }
            },
            error : function(request, status, error) {
                alert(error);
                location.reload();
            }
        });
    });

    $("#add-admin").click(function() {
        $.ajax({
            url : "../json/addAdmin/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : $("#admin-username").val(),
            contentType : "application/json; charset=utf-8",
            success : function(data) {
                location.reload();
            },
            error : function(request, status, error) {
                alert(error);
                location.reload();
            }
        });
    });

    var deleteAdmin = function(event) {
        $('#delete-admin').dialog({
            resizable : false,
            height : 180,
            modal : true,
            buttons : {
                "Remove this admin" : function() {
                    $.ajax({
                        url : "../json/removeAdmin/" + areaName,
                        type : 'POST',
                        dataType : 'json',
                        data : event.target.id,
                        contentType : "application/json; charset=utf-8",
                        success : function(data) {
                            location.reload();
                        },
                        error : function(request, status, error) {
                            alert(error);
                            location.reload();
                        }
                    });
                    $(this).dialog("close");
                },
                Cancel : function() {
                    $(this).dialog("close");
                }
            }
        });
    };

    $("#add-editor").click(function() {
        $.ajax({
            url : "../json/addEditor/" + areaName,
            type : 'POST',
            dataType : 'json',
            data : $("#editor-username").val(),
            contentType : "application/json; charset=utf-8",
            success : function(data) {
                location.reload();
            },
            error : function(request, status, error) {
                alert(error);
                location.reload();
            }
        });
    });

    var deleteEditor = function(event) {
        $('#delete-editor').dialog({
            resizable : false,
            height : 180,
            modal : true,
            buttons : {
                "Remove this editor" : function() {
                    $.ajax({
                        url : "../json/removeEditor/" + areaName,
                        type : 'POST',
                        dataType : 'json',
                        data : event.target.id,
                        contentType : "application/json; charset=utf-8",
                        success : function(data) {
                            location.reload();
                        },
                        error : function(request, status, error) {
                            alert(error);
                            location.reload();
                        }
                    });
                    $(this).dialog("close");
                },
                Cancel : function() {
                    $(this).dialog("close");
                }
            }
        });
    };

    var clickedIcon = null;
    var selectIcon = function(event) {
        $('#image_container').dialog({
            resizable : true,
            height : 400,
            width : 800
        });
        clickedIcon = $(this);
    };

    var updateAttribute = function(id) {

        var attribute = new Object();
        attribute.id = id;
        attribute.name = $("input#" + id).val();
        var valid = true;

        var options = new Array();
        var compareValue = 1;

        $("#ul" + id).children("li").each(function() {
            var option = new Object();

            var isSeries = $(this).hasClass("series");
            var id = $(this).attr("id");

            if (id.indexOf("NewOption") == -1) {
                option.id = id;
            } else {
                //Set a negative value, which gets handled
                //as a new option on the server
                option.id = -compareValue;
            }

            option.name = $(this).children("input#name"+id).val();
            option.iconEnabled = $("#iconEnabled"+id).is(':checked');
            option.icon = $("#icon"+id).attr("icon");

            if (isSeries) {
                var seriesStart = parseInt($(this).children("input#seriesStart"+id).val());
                var seriesEnd = parseInt($(this).children("input#seriesEnd"+id).val());
                var seriesIncrement = parseInt($(this).children("input#seriesIncrement"+id).val());
                var name = option.name;
                var iconEnabled = option.iconEnabled;
                var icon = option.icon;
                
                if (isNaN(seriesStart) || isNaN(seriesEnd) || isNaN(seriesIncrement)) {
                    var invalidDialog = $(document.createElement('div'));
                    $(invalidDialog).attr('title', 'Invalid options');
                    $(invalidDialog).html('<p>Invalid series! Only numbers are allowed</p>');
                    invalidDialog.dialog({
                        modal: true,
                        width: 325,
                        buttons: {
                            Ok: function() {
                                location.reload();
                                $( this ).dialog( "close" );
                            }
                        }
                    });
                    valid = false;
                    return false; //Breaks li.each function
                }

                var count = 0;                
                for (var number = seriesStart; number <= seriesEnd; number+=seriesIncrement) {
                    if (count++ > 1500) {
                        var invalidDialog = $(document.createElement('div'));
                        $(invalidDialog).attr('title', 'Invalid options');
                        $(invalidDialog).html('<p>Too many or invalid attribute options! Must be integers and max limit is 1500 options!</p>');
                        invalidDialog.dialog({
                            modal: true,
                            width: 325,
                            buttons: {
                                Ok: function() {
                                    location.reload();
                                    $( this ).dialog( "close" );
                                }
                            }
                        });
                        valid = false;
                        return false; //Breaks li.each function
                    } else {
                        var option = new Object();
                        var series = seriesIds[id];
                        if (series != null) {
                            option.id = seriesIds[id][number];
                        }
                        if (option.id == null) {
                            option.id = -compareValue;
                        }
                        option.iconEnabled = iconEnabled;
                        option.icon = icon;
                        option.seriesIncrement = seriesIncrement;
                        option.name = name + " " + number.toFixed();
                        option.compareValue = compareValue++;
                        options.push(option);
                    }
                };
                if (count == 0) {
                    var invalidDialog = $(document.createElement('div'));
                    $(invalidDialog).attr('title', 'Invalid options');
                    $(invalidDialog).html('<p>The series contained no elements, try again</p>');
                    invalidDialog.dialog({
                        modal: true,
                        width: 325,
                        buttons: {
                            Ok: function() {
                                location.reload();
                                $( this ).dialog( "close" );
                            }
                        }
                    });
                    valid = false;
                    return false; //Breaks li.each function
                }
            } else {
                option.compareValue = compareValue++;
                options.push(option);
            };
        });

        if (valid) {
            attribute.options = options;
            $.ajax({
                url : "../json/updateAttribute/" + areaName,
                type : 'POST',
                async: false,
                dataType : 'json',
                async : false,
                data : JSON.stringify(attribute),
                contentType : "application/json; charset=utf-8",
                error : function(request, status, error) {
                    alert(error);
                }
            });
            return true;
        }
        return false;
    };

    $('.checkbox').change(function() {
        var id = $(this).closest("li").attr("id");
        $("#icon"+id).toggleClass("icon-hidden");
    });

    $("#save").button().click(function() {
        if (updateAttribute(storyAttr1Id)
                & updateAttribute(storyAttr2Id)
                & updateAttribute(storyAttr3Id)
                & updateAttribute(taskAttr1Id)) {
            location.reload();
        }
    });
    $(".deleteAdminButton").click(deleteAdmin);
    $(".deleteEditorButton").click(deleteEditor);
    $(".attrIcon").click(selectIcon);
    $("ul").sortable();

    $('div#image_container img').click(function() {
        var id = clickedIcon.attr("id");
        var name = $("input#" + id).val();
        var icon = $(this).attr("id");

        clickedIcon.attr("src", $(this).attr("src"));
        clickedIcon.attr("icon", icon);

        $('#image_container').dialog("close");
    });

    var newCount = 0;

    $(".addOption").click(function(event) {
        var parentId = $(this).attr("id");
        var id = 'NewOption' + ++newCount;
        $("#ul" + parentId).append('<li class="new-attribute" id="' + id + '">'
                + '<span class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>'
                + '<div class="inline-block icon-container">'
                + '<input id="iconEnabled' + id + '"'
                + 'class="checkbox inline-block" type="checkbox" title="Display icon" checked="checked" />'
                + '<img class="attrIcon" id="icon' + id + '"'
                + 'src="../resources/image/new.png" icon="new.png" /> '
                + '</div> <input id="name' + id + '" maxlength="15"'
                + 'class="inline-block attrOptionTitle ui-corner-all">'
                + '<img class="removeOption" src="../resources/image/delete.png" />'
                + '</li>');
        event.stopPropagation();
        $(".attrIcon").click(selectIcon);
        $(".removeOption").click(function(event) {
            $(this).closest("li").remove();
        });
        $('.remove-button').button({
            text: false,
            icons: {
                primary: 'silk-icon-delete'
            }
        });
        $('.checkbox').change(function() {
            var id = $(this).closest("li").attr("id");
            $("#icon"+id).toggleClass("icon-hidden");
        });
    });
    
    $(".addOptionSeries").click(function(event) {
        var parentId = $(this).attr("id");
        var id = 'NewOption' + ++newCount;
        $("#ul" + parentId).append('<li id="' + id + '" class="new-attribute series" >'
                + '<span class="ui-icon ui-icon-arrowthick-2-n-s inline-block"></span>'
                + '<div class="inline-block icon-container">'
                + '<input id="iconEnabled' + id + '"'
                + 'class="checkbox inline-block" type="checkbox" title="Display icon" checked="checked" />'
                + '<img class="attrIcon" id="icon' + id + '"'
                + 'src="../resources/image/new.png" icon="new.png" /> '
                + '</div> <input id="name' + id + '" maxlength="15" title="Optional name"'
                + 'class="inline-block attrOptionTitle ui-corner-all">'
                + '<input id="seriesStart' + id + '" maxlength="3" title="Series start" value="0"'
                + 'type="number" class="inline-block attrOptionSeriesBox ui-corner-all"> - '
                + '<input id="seriesEnd' + id + '" maxlength="3" title="Series end" value="10"'
                + 'type="number" class="inline-block attrOptionSeriesBox ui-corner-all">'
                + '<input id="seriesIncrement' + id + '" maxlength="3" title="Series increment" value="1"'
                + 'type="number" class="inline-block attrOptionSeriesBox ui-corner-all">'
                + '<img class="removeOption" src="../resources/image/delete.png" />'
                + '</li>');
        event.stopPropagation();
        $(".attrIcon").click(selectIcon);
        $(".removeOption").click(function(event) {
            $(this).closest("li").remove();
        });
        $('.checkbox').change(function() {
            var id = $(this).closest("li").attr("id");
            $("#icon"+id).toggleClass("icon-hidden");
        });
    });
    

    $(".removeOption").click(function(event) {
        $(this).closest("li").remove();
    });

});