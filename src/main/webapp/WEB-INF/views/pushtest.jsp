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
    <title>Backlog Tool</title>
    <link rel="shortcut icon" href="<c:url value="/resources/css/ui-lightness/images/favicon.ico" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/styles.css?v=${versionNoDots}" />"></link>
    <link rel="stylesheet" type="text/css" href="<c:url value="/resources/css/ui-lightness/jquery-ui-1.10.3.custom.min.css" />"></link>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-2.0.1.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery-ui-1.10.3.custom.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/resources/js/jquery.atmosphere.js" />"></script>
    
    <script type="text/javascript">
        var lastArea = "${lastArea}";
    
        $(document).ready(function() {
            $(".home-link").css("color", "#1c94c4");
            $('#login-out').button();
            
            var socket = $.atmosphere;
            
            var request = new $.atmosphere.AtmosphereRequest();
            request.url = 'json/test';
            request.contentType = "application/json";
            request.transport = 'websocket';
            request.fallbackTransport = 'long-polling';
            

            request.onMessage = function (response) {
                var message = response.responseBody;
				alert("msg recieved: " + message);
            };

            request.onError = function(response) {
				alert("error");
            };
            
            var subSocket = socket.subscribe(request);
            
            $("button#push").click(function() {
                subSocket.push($("input#input").val());
            });
        });
    </script>
</head>

<body>
    <div id="wrap">
        <div id="header">
            <h1>
                <a href="${pageContext.request.contextPath}">
                    <p id="topic" class="textstyle inline">Backlog
                        tool</p>
                </a>
            </h1>
            <br style="clear: both" /> 
        </div>
        <div id="main">
            <div id="list-container-div">
                    <input id="input">
                    <button id="push">Push</button>
            </div>
        </div>
    </div>
    <div id="footer">
        <c:import url="footer.jsp" />
    </div>

</body>

</html>
