jQuery.atmosphere=function(){jQuery(window).bind("unload.atmosphere",function(){jQuery.atmosphere.unsubscribe()
});
jQuery(window).keypress(function(b){if(b.keyCode==27){b.preventDefault()
}});
var a=function(c){var b,e=/^(.*?):[ \t]*([^\r\n]*)\r?$/mg,d={};
while(b=e.exec(c)){d[b[1]]=b[2]
}return d
};
return{version:"1.0.13",requests:[],callbacks:[],onError:function(b){},onClose:function(b){},onOpen:function(b){},onMessage:function(b){},onReconnect:function(c,b){},onMessagePublished:function(b){},onTransportFailure:function(c,b){},onLocalMessage:function(b){},AtmosphereRequest:function(E){var G={timeout:300000,method:"GET",headers:{},contentType:"",callback:null,url:"",data:"",suspend:true,maxRequest:-1,reconnect:true,maxStreamingLength:10000000,lastIndex:0,logLevel:"info",requestCount:0,fallbackMethod:"GET",fallbackTransport:"streaming",transport:"long-polling",webSocketImpl:null,webSocketUrl:null,webSocketPathDelimiter:"@@",enableXDR:false,rewriteURL:false,attachHeadersAsQueryString:true,executeCallbackBeforeReconnect:false,readyState:0,lastTimestamp:0,withCredentials:false,trackMessageLength:false,messageDelimiter:"|",connectTimeout:-1,reconnectInterval:0,dropAtmosphereHeaders:true,uuid:0,shared:false,readResponsesHeaders:true,maxReconnectOnClose:5,enableProtocol:false,onError:function(aq){},onClose:function(aq){},onOpen:function(aq){},onMessage:function(aq){},onReconnect:function(ar,aq){},onMessagePublished:function(aq){},onTransportFailure:function(ar,aq){},onLocalMessage:function(aq){}};
var O={status:200,reasonPhrase:"OK",responseBody:"",messages:[],headers:[],state:"messageReceived",transport:"polling",error:null,request:null,partialMessage:"",errorHandled:false,id:0};
var R=null;
var i=null;
var o=null;
var w=null;
var y=null;
var ab=true;
var f=0;
var an=false;
var S=null;
var ah;
var k=null;
var B=jQuery.now();
var C;
ap(E);
function ai(){ab=true;
an=false;
f=0;
R=null;
i=null;
o=null;
w=null
}function s(){ad();
ai()
}function ap(aq){s();
G=jQuery.extend(G,aq);
G.mrequest=G.reconnect;
if(!G.reconnect){G.reconnect=true
}}function j(){return G.webSocketImpl!=null||window.WebSocket||window.MozWebSocket
}function K(){return window.EventSource
}function m(){if(G.shared){k=Z(G);
if(k!=null){if(G.logLevel=="debug"){jQuery.atmosphere.debug("Storage service available. All communication will be local")
}if(k.open(G)){return
}}if(G.logLevel=="debug"){jQuery.atmosphere.debug("No Storage service available.")
}k=null
}G.firstMessage=true;
G.ctime=jQuery.now();
if(G.transport!="websocket"&&G.transport!="sse"){setTimeout(function(){F("opening",G.transport,G)
},500);
l()
}else{if(G.transport=="websocket"){if(!j()){I("Websocket is not supported, using request.fallbackTransport ("+G.fallbackTransport+")")
}else{ac(false)
}}else{if(G.transport=="sse"){if(!K()){I("Server Side Events(SSE) is not supported, using request.fallbackTransport ("+G.fallbackTransport+")")
}else{A(false)
}}}}}function Z(av){var ay,ar,au,at="atmosphere-"+av.url,aq={storage:function(){if(!jQuery.atmosphere.supportStorage()){return
}var aB=window.localStorage,az=function(aC){return jQuery.parseJSON(aB.getItem(at+"-"+aC))
},aA=function(aC,aD){aB.setItem(at+"-"+aC,jQuery.stringifyJSON(aD))
};
return{init:function(){aA("children",az("children").concat([B]));
jQuery(window).on("storage.socket",function(aC){aC=aC.originalEvent;
if(aC.key===at&&aC.newValue){ax(aC.newValue)
}});
return az("opened")
},signal:function(aC,aD){aB.setItem(at,jQuery.stringifyJSON({target:"p",type:aC,data:aD}))
},close:function(){var aC,aD=az("children");
jQuery(window).off("storage.socket");
if(aD){aC=jQuery.inArray(av.id,aD);
if(aC>-1){aD.splice(aC,1);
aA("children",aD)
}}}}
},windowref:function(){var az=window.open("",at.replace(/\W/g,""));
if(!az||az.closed||!az.callbacks){return
}return{init:function(){az.callbacks.push(ax);
az.children.push(B);
return az.opened
},signal:function(aA,aB){if(!az.closed&&az.fire){az.fire(jQuery.stringifyJSON({target:"p",type:aA,data:aB}))
}},close:function(){function aA(aD,aC){var aB=jQuery.inArray(aC,aD);
if(aB>-1){aD.splice(aB,1)
}}if(!au){aA(az.callbacks,ax);
aA(az.children,B)
}}}
}};
function ax(az){var aB=jQuery.parseJSON(az),aA=aB.data;
if(aB.target==="c"){switch(aB.type){case"open":F("opening","local",G);
break;
case"close":if(!au){au=true;
if(aA.reason==="aborted"){af()
}else{if(aA.heir===B){m()
}else{setTimeout(function(){m()
},100)
}}}break;
case"message":x(aA,"messageReceived",200,av.transport);
break;
case"localMessage":V(aA);
break
}}}function aw(){var az=new RegExp("(?:^|; )("+encodeURIComponent(at)+")=([^;]*)").exec(document.cookie);
if(az){return jQuery.parseJSON(decodeURIComponent(az[2]))
}}ay=aw();
if(!ay||jQuery.now()-ay.ts>1000){return
}ar=aq.storage()||aq.windowref();
if(!ar){return
}return{open:function(){var az;
C=setInterval(function(){var aA=ay;
ay=aw();
if(!ay||aA.ts===ay.ts){ax(jQuery.stringifyJSON({target:"c",type:"close",data:{reason:"error",heir:aA.heir}}))
}},1000);
az=ar.init();
if(az){setTimeout(function(){F("opening","local",av)
},50)
}return az
},send:function(az){ar.signal("send",az)
},localSend:function(az){ar.signal("localSend",jQuery.stringifyJSON({id:B,event:az}))
},close:function(){if(!an){clearInterval(C);
ar.signal("close");
ar.close()
}}}
}function W(){var ar,aq="atmosphere-"+G.url,aw={storage:function(){if(!jQuery.atmosphere.supportStorage()){return
}var ax=window.localStorage;
return{init:function(){jQuery(window).on("storage.socket",function(ay){ay=ay.originalEvent;
if(ay.key===aq&&ay.newValue){at(ay.newValue)
}})
},signal:function(ay,az){ax.setItem(aq,jQuery.stringifyJSON({target:"c",type:ay,data:az}))
},get:function(ay){return jQuery.parseJSON(ax.getItem(aq+"-"+ay))
},set:function(ay,az){ax.setItem(aq+"-"+ay,jQuery.stringifyJSON(az))
},close:function(){jQuery(window).off("storage.socket");
ax.removeItem(aq);
ax.removeItem(aq+"-opened");
ax.removeItem(aq+"-children")
}}
},windowref:function(){var ax=aq.replace(/\W/g,""),ay=(jQuery('iframe[name="'+ax+'"]')[0]||jQuery('<iframe name="'+ax+'" />').hide().appendTo("body")[0]).contentWindow;
return{init:function(){ay.callbacks=[at];
ay.fire=function(az){var aA;
for(aA=0;
aA<ay.callbacks.length;
aA++){ay.callbacks[aA](az)
}}
},signal:function(az,aA){if(!ay.closed&&ay.fire){ay.fire(jQuery.stringifyJSON({target:"c",type:az,data:aA}))
}},get:function(az){return !ay.closed?ay[az]:null
},set:function(az,aA){if(!ay.closed){ay[az]=aA
}},close:function(){}}
}};
function at(ax){var az=jQuery.parseJSON(ax),ay=az.data;
if(az.target==="p"){switch(az.type){case"send":ae(ay);
break;
case"localSend":V(ay);
break;
case"close":af();
break
}}}S=function av(ax){ar.signal("message",ax)
};
function au(){document.cookie=encodeURIComponent(aq)+"="+encodeURIComponent(jQuery.stringifyJSON({ts:jQuery.now()+1,heir:(ar.get("children")||[])[0]}))
}ar=aw.storage()||aw.windowref();
ar.init();
if(G.logLevel=="debug"){jQuery.atmosphere.debug("Installed StorageService "+ar)
}ar.set("children",[]);
if(ar.get("opened")!=null&&!ar.get("opened")){ar.set("opened",false)
}au();
C=setInterval(au,1000);
ah=ar
}function F(at,aw,ar){if(G.shared&&aw!="local"){W()
}if(ah!=null){ah.set("opened",true)
}ar.close=function(){af()
};
if(O.error==null){O.request=ar;
var au=O.state;
O.state=at;
O.status=200;
var aq=O.transport;
O.transport=aw;
var av=O.responseBody;
u();
O.responseBody=av;
O.state=au;
O.transport=aq
}}function r(at){at.transport="jsonp";
var ar=G;
if((at!=null)&&(typeof(at)!="undefined")){ar=at
}var aq=ar.url;
var au=ar.data;
if(ar.attachHeadersAsQueryString){aq=P(ar);
if(au!=""){aq+="&X-Atmosphere-Post-Body="+encodeURIComponent(au)
}au=""
}y=jQuery.ajax({url:aq,type:ar.method,dataType:"jsonp",error:function(av,ax,aw){O.error=true;
if(av.status<300&&ar.reconnect&&f++<ar.maxReconnectOnClose){J(y,ar)
}else{X(av.status,aw)
}},jsonp:"jsonpTransport",success:function(av){if(ar.reconnect){if(ar.maxRequest==-1||ar.requestCount++<ar.maxRequest){Y(y,ar);
if(!ar.executeCallbackBeforeReconnect){J(y,ar)
}var ax=av.message;
if(ax!=null&&typeof ax!="string"){try{ax=jQuery.stringifyJSON(ax)
}catch(aw){}}if(n(ar,ax)){x(ax,"messageReceived",200,ar.transport)
}if(ar.executeCallbackBeforeReconnect){J(y,ar)
}}else{jQuery.atmosphere.log(G.logLevel,["JSONP reconnect maximum try reached "+G.requestCount]);
X(0,"maxRequest reached")
}}},data:ar.data,beforeSend:function(av){b(av,ar,false)
}})
}function T(au){var ar=G;
if((au!=null)&&(typeof(au)!="undefined")){ar=au
}var aq=ar.url;
var av=ar.data;
if(ar.attachHeadersAsQueryString){aq=P(ar);
if(av!=""){aq+="&X-Atmosphere-Post-Body="+encodeURIComponent(av)
}av=""
}var at=typeof(ar.async)!="undefined"?ar.async:true;
y=jQuery.ajax({url:aq,type:ar.method,error:function(aw,ay,ax){O.error=true;
if(aw.status<300){J(y,ar)
}else{X(aw.status,ax)
}},success:function(ax,ay,aw){if(ar.reconnect){if(ar.maxRequest==-1||ar.requestCount++<ar.maxRequest){if(!ar.executeCallbackBeforeReconnect){J(y,ar)
}if(n(ar,ax)){x(ax,"messageReceived",200,ar.transport)
}if(ar.executeCallbackBeforeReconnect){J(y,ar)
}}else{jQuery.atmosphere.log(G.logLevel,["AJAX reconnect maximum try reached "+G.requestCount]);
X(0,"maxRequest reached")
}}},beforeSend:function(aw){b(aw,ar,false)
},crossDomain:ar.enableXDR,async:at})
}function d(aq){if(G.webSocketImpl!=null){return G.webSocketImpl
}else{if(window.WebSocket){return new WebSocket(aq)
}else{return new MozWebSocket(aq)
}}}function e(){var aq=P(G);
return decodeURI(jQuery('<a href="'+aq+'"/>')[0].href.replace(/^http/,"ws"))
}function ao(){var aq=P(G);
return aq
}function A(ar){O.transport="sse";
var aq=ao(G.url);
if(G.logLevel=="debug"){jQuery.atmosphere.debug("Invoking executeSSE");
jQuery.atmosphere.debug("Using URL: "+aq)
}if(ar){F("re-opening","sse",G)
}if(G.enableProtocol&&ar){var au=jQuery.now()-G.ctime;
G.lastTimestamp=Number(G.stime)+Number(au)
}if(ar&&!G.reconnect){if(i!=null){ad()
}return
}try{i=new EventSource(aq,{withCredentials:G.withCredentials})
}catch(at){X(0,at);
I("SSE failed. Downgrading to fallback transport and resending");
return
}if(G.connectTimeout>0){G.id=setTimeout(function(){if(!ar){ad()
}},G.connectTimeout)
}i.onopen=function(av){if(G.logLevel=="debug"){jQuery.atmosphere.debug("SSE successfully opened")
}if(!ar){F("opening","sse",G)
}ar=true;
if(G.method=="POST"){O.state="messageReceived";
i.send(G.data)
}};
i.onmessage=function(aw){if(aw.origin!=window.location.protocol+"//"+window.location.host){jQuery.atmosphere.log(G.logLevel,["Origin was not "+window.location.protocol+"//"+window.location.host]);
return
}var ax=aw.data;
if(!n(G,ax)){return
}O.state="messageReceived";
O.status=200;
var av=q(ax,G,O);
if(!av){u();
O.responseBody="";
O.messages=[]
}};
i.onerror=function(av){clearTimeout(G.id);
aa(ar);
ad();
if(an){jQuery.atmosphere.log(G.logLevel,["SSE closed normally"])
}else{if(!ar){I("SSE failed. Downgrading to fallback transport and resending")
}else{if(G.reconnect&&(O.transport=="sse")){if(f++<G.maxReconnectOnClose){G.id=setTimeout(function(){A(true)
},G.reconnectInterval);
O.responseBody="";
O.messages=[]
}else{jQuery.atmosphere.log(G.logLevel,["SSE reconnect maximum try reached "+f]);
X(0,"maxReconnectOnClose reached")
}}}}}
}function ac(ar){O.transport="websocket";
if(G.enableProtocol&&ar){var au=jQuery.now()-G.ctime;
G.lastTimestamp=Number(G.stime)+Number(au)
}var aq=e(G.url);
var at=false;
if(G.logLevel=="debug"){jQuery.atmosphere.debug("Invoking executeWebSocket");
jQuery.atmosphere.debug("Using URL: "+aq)
}if(ar){F("re-opening","websocket",G)
}if(ar&&!G.reconnect){if(R!=null){ad()
}return
}R=d(aq);
if(G.connectTimeout>0){G.id=setTimeout(function(){if(!ar){var av={code:1002,reason:"",wasClean:false};
R.onclose(av);
try{ad()
}catch(aw){}return
}},G.connectTimeout)
}G.id=setTimeout(function(){setTimeout(function(){ad()
},G.reconnectInterval)
},G.timeout);
R.onopen=function(av){if(G.logLevel=="debug"){jQuery.atmosphere.debug("Websocket successfully opened")
}if(!ar){F("opening","websocket",G)
}ar=true;
R.webSocketOpened=ar;
if(G.method=="POST"){O.state="messageReceived";
R.send(G.data)
}};
R.onmessage=function(aw){clearTimeout(G.id);
G.id=setTimeout(function(){setTimeout(function(){ad()
},G.reconnectInterval)
},G.timeout);
var ax=aw.data;
if(!n(G,ax)){return
}O.state="messageReceived";
O.status=200;
var av=q(ax,G,O);
if(!av){u();
O.responseBody="";
O.messages=[]
}};
R.onerror=function(av){clearTimeout(G.id)
};
R.onclose=function(av){if(at){return
}clearTimeout(G.id);
var aw=av.reason;
if(aw===""){switch(av.code){case 1000:aw="Normal closure; the connection successfully completed whatever purpose for which it was created.";
break;
case 1001:aw="The endpoint is going away, either because of a server failure or because the browser is navigating away from the page that opened the connection.";
break;
case 1002:aw="The endpoint is terminating the connection due to a protocol error.";
break;
case 1003:aw="The connection is being terminated because the endpoint received data of a type it cannot accept (for example, a text-only endpoint received binary data).";
break;
case 1004:aw="The endpoint is terminating the connection because a data frame was received that is too large.";
break;
case 1005:aw="Unknown: no status code was provided even though one was expected.";
break;
case 1006:aw="Connection was closed abnormally (that is, with no close frame being sent).";
break
}}jQuery.atmosphere.warn("Websocket closed, reason: "+aw);
jQuery.atmosphere.warn("Websocket closed, wasClean: "+av.wasClean);
aa(ar);
at=true;
if(an){jQuery.atmosphere.log(G.logLevel,["Websocket closed normally"])
}else{if(!ar){I("Websocket failed. Downgrading to Comet and resending")
}else{if(G.reconnect&&O.transport=="websocket"){ad();
if(G.reconnect&&f++<G.maxReconnectOnClose){G.id=setTimeout(function(){O.responseBody="";
O.messages=[];
ac(true)
},G.reconnectInterval)
}else{jQuery.atmosphere.log(G.logLevel,["Websocket reconnect maximum try reached "+f]);
jQuery.atmosphere.warn("Websocket error, reason: "+av.reason);
X(0,"maxReconnectOnClose reached")
}}}}}
}function n(at,ar){if(jQuery.trim(ar)!=0&&at.enableProtocol&&at.firstMessage){at.firstMessage=false;
var aq=ar.split(at.messageDelimiter);
var au=aq.length==2?0:1;
at.uuid=jQuery.trim(aq[au]);
at.stime=jQuery.trim(aq[au+1]);
return false
}return true
}function X(aq,ar){ad();
O.state="error";
O.reasonPhrase=ar;
O.responseBody="";
O.messages=[];
O.status=aq;
u()
}function q(av,au,aq){if(au.trackMessageLength){if(aq.partialMessage.length!=0){av=aq.partialMessage+av
}var at=[];
var aw=0;
var ar=av.indexOf(au.messageDelimiter);
while(ar!=-1){aw=jQuery.trim(av.substring(aw,ar));
av=av.substring(ar+au.messageDelimiter.length,av.length);
if(av.length==0||av.length<aw){break
}ar=av.indexOf(au.messageDelimiter);
at.push(av.substring(0,aw))
}if(at.length==0||(ar!=-1&&av.length!=0&&aw!=av.length)){aq.partialMessage=aw+au.messageDelimiter+av
}else{aq.partialMessage=""
}if(at.length!=0){aq.responseBody=at.join(au.messageDelimiter);
aq.messages=at;
return false
}else{aq.responseBody="";
aq.messages=[];
return true
}}else{aq.responseBody=av
}return false
}function I(aq){jQuery.atmosphere.log(G.logLevel,[aq]);
if(typeof(G.onTransportFailure)!="undefined"){G.onTransportFailure(aq,G)
}else{if(typeof(jQuery.atmosphere.onTransportFailure)!="undefined"){jQuery.atmosphere.onTransportFailure(aq,G)
}}G.transport=G.fallbackTransport;
var ar=G.connectTimeout==-1?0:G.connectTimeout;
if(G.reconnect&&G.transport!="none"||G.transport==null){G.method=G.fallbackMethod;
O.transport=G.fallbackTransport;
G.fallbackTransport="none";
G.id=setTimeout(function(){m()
},ar)
}else{X(500,"Unable to reconnect with fallback transport")
}}function P(at){var ar=G;
if((at!=null)&&(typeof(at)!="undefined")){ar=at
}var aq=ar.url;
if(!ar.attachHeadersAsQueryString){return aq
}if(aq.indexOf("X-Atmosphere-Framework")!=-1){return aq
}aq+=(aq.indexOf("?")!=-1)?"&":"?";
aq+="X-Atmosphere-tracking-id="+ar.uuid;
aq+="&X-Atmosphere-Framework="+jQuery.atmosphere.version;
aq+="&X-Atmosphere-Transport="+ar.transport;
if(ar.trackMessageLength){aq+="&X-Atmosphere-TrackMessageSize=true"
}if(ar.lastTimestamp!=undefined){aq+="&X-Cache-Date="+ar.lastTimestamp
}else{aq+="&X-Cache-Date="+0
}if(ar.contentType!=""){aq+="&Content-Type="+ar.contentType
}if(ar.enableProtocol){aq+="&X-atmo-protocol=true"
}jQuery.each(ar.headers,function(au,aw){var av=jQuery.isFunction(aw)?aw.call(this,ar,at,O):aw;
if(av!=null){aq+="&"+encodeURIComponent(au)+"="+encodeURIComponent(av)
}});
return aq
}function aj(){if(jQuery.browser.msie){if(typeof XMLHttpRequest=="undefined"){XMLHttpRequest=function(){try{return new ActiveXObject("Msxml2.XMLHTTP.6.0")
}catch(aq){}try{return new ActiveXObject("Msxml2.XMLHTTP.3.0")
}catch(aq){}try{return new ActiveXObject("Microsoft.XMLHTTP")
}catch(aq){}throw new Error("This browser does not support XMLHttpRequest.")
}
}}return new XMLHttpRequest()
}function l(at){var aq=G;
if((at!=null)||(typeof(at)!="undefined")){aq=at
}aq.lastIndex=0;
aq.readyState=0;
if((aq.transport=="jsonp")||((aq.enableXDR)&&(jQuery.atmosphere.checkCORSSupport()))){r(aq);
return
}if(aq.transport=="ajax"){T(at);
return
}if(jQuery.browser.msie&&jQuery.browser.version<10){if((aq.transport=="streaming")){aq.enableXDR&&window.XDomainRequest?H(aq):am(aq);
return
}if((aq.enableXDR)&&(window.XDomainRequest)){H(aq);
return
}}var au=function(){if(aq.reconnect&&f++<aq.maxReconnectOnClose){J(ar,aq,true)
}else{X(0,"maxReconnectOnClose reached")
}};
if(aq.reconnect&&(aq.maxRequest==-1||aq.requestCount++<aq.maxRequest)){var ar=aj();
b(ar,aq,true);
if(aq.suspend){o=ar
}if(aq.transport!="polling"){O.transport=aq.transport
}ar.onabort=function(){aa(true)
};
ar.onerror=function(){O.error=true;
try{O.status=XMLHttpRequest.status
}catch(av){O.status=500
}if(!O.status){O.status=500
}ad();
if(!O.errorHandled){au()
}};
ar.onreadystatechange=function(){if(an){return
}O.error=null;
var aw=false;
var aA=false;
if(jQuery.browser.opera&&aq.transport=="streaming"&&aq.readyState>2&&ar.readyState==4){aq.readyState=0;
aq.lastIndex=0;
au();
return
}aq.readyState=ar.readyState;
if(aq.transport=="streaming"&&ar.readyState>=3){aA=true
}else{if(aq.transport=="long-polling"&&ar.readyState===4){aA=true
}}clearTimeout(aq.id);
if(aA){var av=0;
if(ar.readyState!=0){av=ar.status>1000?0:ar.status
}if(av>=300||av==0){O.errorHandled=true;
ad();
au();
return
}var ay=ar.responseText;
if(jQuery.trim(ay.length)==0&&aq.transport=="long-polling"){if(!ar.hasData){au()
}else{ar.hasData=false
}return
}ar.hasData=true;
Y(ar,G);
if(aq.transport=="streaming"){if(!jQuery.browser.opera){var ax=ay.substring(aq.lastIndex,ay.length);
aq.lastIndex=ay.length;
if(!n(G,ax)){return
}aw=q(ax,aq,O)
}else{jQuery.atmosphere.iterate(function(){if(O.status!=500&&ar.responseText.length>aq.lastIndex){try{O.status=ar.status
}catch(aC){O.status=404
}O.state="messageReceived";
var aB=ar.responseText.substring(aq.lastIndex);
aq.lastIndex=ar.responseText.length;
if(n(G,aB)){aw=q(aB,aq,O);
if(!aw){u()
}D(ar,aq)
}}else{if(O.status>400){aq.lastIndex=ar.responseText.length;
return false
}}},0)
}}else{if(!n(G,ay)){J(ar,aq,false);
return
}aw=q(ay,aq,O);
aq.lastIndex=ay.length
}try{O.status=ar.status;
O.headers=a(ar.getAllResponseHeaders());
Y(ar,aq)
}catch(az){O.status=404
}if(aq.suspend){O.state=O.status==0?"closed":"messageReceived"
}else{O.state="messagePublished"
}if(!aq.executeCallbackBeforeReconnect){J(ar,aq,false)
}if(O.responseBody.indexOf("parent.callback")!=-1){jQuery.atmosphere.log(aq.logLevel,["parent.callback no longer supported with 0.8 version and up. Please upgrade"])
}if(!aw){u()
}if(aq.executeCallbackBeforeReconnect){J(ar,aq,false)
}D(ar,aq)
}};
ar.send(aq.data);
if(aq.suspend){aq.id=setTimeout(function(){if(ab){setTimeout(function(){ad();
l(aq)
},aq.reconnectInterval)
}},aq.timeout)
}ab=true
}else{if(aq.logLevel=="debug"){jQuery.atmosphere.log(aq.logLevel,["Max re-connection reached."])
}X(0,"maxRequest reached")
}}function b(at,au,ar){var aq=P(au);
aq=jQuery.atmosphere.prepareURL(aq);
if(ar){at.open(au.method,aq,true);
if(au.connectTimeout>-1){au.id=setTimeout(function(){if(au.requestCount==0){ad();
x("Connect timeout","closed",200,au.transport)
}},au.connectTimeout)
}}if(G.withCredentials){if("withCredentials" in at){at.withCredentials=true
}}if(!G.dropAtmosphereHeaders){at.setRequestHeader("X-Atmosphere-Framework",jQuery.atmosphere.version);
at.setRequestHeader("X-Atmosphere-Transport",au.transport);
if(au.lastTimestamp!=undefined){at.setRequestHeader("X-Cache-Date",au.lastTimestamp)
}else{at.setRequestHeader("X-Cache-Date",0)
}if(au.trackMessageLength){at.setRequestHeader("X-Atmosphere-TrackMessageSize","true")
}at.setRequestHeader("X-Atmosphere-tracking-id",au.uuid)
}if(au.contentType!=""){at.setRequestHeader("Content-Type",au.contentType)
}jQuery.each(au.headers,function(av,ax){var aw=jQuery.isFunction(ax)?ax.call(this,at,au,ar,O):ax;
if(aw!=null){at.setRequestHeader(av,aw)
}})
}function J(ar,at,av){if(av||at.transport!="streaming"){if(at.reconnect||(at.suspend&&ab)){var aq=0;
if(ar.readyState!=0){aq=ar.status>1000?0:ar.status
}O.status=aq==0?204:aq;
O.reason=aq==0?"Server resumed the connection or down.":"OK";
var au=(at.connectTimeout==-1)?0:at.connectTimeout;
if(!av){at.id=setTimeout(function(){l(at)
},au)
}else{l(at)
}}}}function H(aq){if(aq.transport!="polling"){w=N(aq);
w.open()
}else{N(aq).open()
}}function N(at){var ar=G;
if((at!=null)&&(typeof(at)!="undefined")){ar=at
}var ay=ar.transport;
var ax=0;
var aw=function(az){var aA=az.responseText;
if(!n(at,aA)){return
}x(aA,"messageReceived",200,ay)
};
var aq=new window.XDomainRequest();
var av=ar.rewriteURL||function(aA){var az=/(?:^|;\s*)(JSESSIONID|PHPSESSID)=([^;]*)/.exec(document.cookie);
switch(az&&az[1]){case"JSESSIONID":return aA.replace(/;jsessionid=[^\?]*|(\?)|$/,";jsessionid="+az[2]+"$1");
case"PHPSESSID":return aA.replace(/\?PHPSESSID=[^&]*&?|\?|$/,"?PHPSESSID="+az[2]+"&").replace(/&$/,"")
}return aA
};
aq.onprogress=function(){au(aq)
};
aq.onerror=function(){if(ar.transport!="polling"){J(aq,ar,false)
}};
aq.onload=function(){au(aq)
};
var au=function(az){if(ar.lastMessage==az.responseText){return
}if(ar.executeCallbackBeforeReconnect){aw(az)
}if(ar.transport=="long-polling"&&(ar.reconnect&&(ar.maxRequest==-1||ar.requestCount++<ar.maxRequest))){az.status=200;
J(az,ar,false)
}if(!ar.executeCallbackBeforeReconnect){aw(az)
}ar.lastMessage=az.responseText
};
return{open:function(){if(ar.method=="POST"){ar.attachHeadersAsQueryString=true
}var az=P(ar);
if(ar.method=="POST"){az+="&X-Atmosphere-Post-Body="+encodeURIComponent(ar.data)
}aq.open(ar.method,av(az));
aq.send();
if(ar.connectTimeout>-1){ar.id=setTimeout(function(){if(ar.requestCount==0){ad();
x("Connect timeout","closed",200,ar.transport)
}},ar.connectTimeout)
}},close:function(){aq.abort();
x(aq.responseText,"closed",200,ay)
}}
}function am(aq){w=p(aq);
w.open()
}function p(au){var at=G;
if((au!=null)&&(typeof(au)!="undefined")){at=au
}var ar;
var av=new window.ActiveXObject("htmlfile");
av.open();
av.close();
var aq=at.url;
if(at.transport!="polling"){O.transport=at.transport
}return{open:function(){var aw=av.createElement("iframe");
aq=P(at);
if(at.data!=""){aq+="&X-Atmosphere-Post-Body="+encodeURIComponent(at.data)
}aq=jQuery.atmosphere.prepareURL(aq);
aw.src=aq;
av.body.appendChild(aw);
var ax=aw.contentDocument||aw.contentWindow.document;
ar=jQuery.atmosphere.iterate(function(){try{if(!ax.firstChild){return
}if(ax.readyState==="complete"){try{jQuery.noop(ax.fileSize)
}catch(aD){x("Connection Failure","error",500,at.transport);
return false
}}var aA=ax.body?ax.body.lastChild:ax;
var aC=function(){var aF=aA.cloneNode(true);
aF.appendChild(ax.createTextNode("."));
var aE=aF.innerText;
aE=aE.substring(0,aE.length-1);
return aE
};
if(!jQuery.nodeName(aA,"pre")){var az=ax.head||ax.getElementsByTagName("head")[0]||ax.documentElement||ax;
var ay=ax.createElement("script");
ay.text="document.write('<plaintext>')";
az.insertBefore(ay,az.firstChild);
az.removeChild(ay);
aA=ax.body.lastChild
}x(aC(),"opening",200,at.transport);
ar=jQuery.atmosphere.iterate(function(){var aF=aC();
if(aF.length>at.lastIndex){O.status=200;
O.error=null;
var aE=aF;
if(aE.length!=0&&n(at,aE)){aA.innerText="";
x(aE,"messageReceived",200,at.transport)
}at.lastIndex=0
}if(ax.readyState==="complete"){x("","closed",200,at.transport);
x("","re-opening",200,at.transport);
at.id=setTimeout(function(){am(at)
},at.reconnectInterval);
return false
}},null);
return false
}catch(aB){O.error=true;
if(f++<at.maxReconnectOnClose){at.id=setTimeout(function(){am(at)
},at.reconnectInterval)
}else{X(0,"maxReconnectOnClose reached")
}av.execCommand("Stop");
av.close();
return false
}})
},close:function(){if(ar){ar()
}av.execCommand("Stop");
x("","closed",200,at.transport)
}}
}function ae(aq){if(O.status==408){h(aq)
}else{if(k!=null){g(aq)
}else{if(o!=null||i!=null){c(aq)
}else{if(w!=null){Q(aq)
}else{if(y!=null){M(aq)
}else{if(R!=null){z(aq)
}}}}}}}function h(ar){var aq=ag(ar);
aq.transport="ajax";
aq.method="GET";
aq.async=false;
aq.reconnect=false;
l(aq)
}function g(aq){k.send(aq)
}function t(ar){if(ar.length==0){return
}try{if(k){k.localSend(ar)
}else{if(ah){ah.signal("localMessage",jQuery.stringifyJSON({id:B,event:ar}))
}}}catch(aq){jQuery.atmosphere.error(aq)
}}function c(ar){var aq=ag(ar);
l(aq)
}function Q(ar){if(G.enableXDR&&jQuery.atmosphere.checkCORSSupport()){var aq=ag(ar);
aq.reconnect=false;
r(aq)
}else{c(ar)
}}function M(aq){c(aq)
}function L(aq){var ar=aq;
if(typeof(ar)=="object"){ar=aq.data
}return ar
}function ag(ar){var at=L(ar);
var aq={connected:false,timeout:60000,method:"POST",url:G.url,contentType:G.contentType,headers:{},reconnect:true,callback:null,data:at,suspend:false,maxRequest:-1,logLevel:"info",requestCount:0,withCredentials:G.withCredentials,transport:"polling",attachHeadersAsQueryString:true,enableXDR:G.enableXDR,uuid:G.uuid,messageDelimiter:"|",enableProtocol:false,maxReconnectOnClose:G.maxReconnectOnClose};
if(typeof(ar)=="object"){aq=jQuery.extend(aq,ar)
}return aq
}function z(aq){var au=L(aq);
var ar;
try{if(G.webSocketUrl!=null){ar=G.webSocketPathDelimiter+G.webSocketUrl+G.webSocketPathDelimiter+au
}else{ar=au
}R.send(ar)
}catch(at){R.onclose=function(av){};
ad();
I("Websocket failed. Downgrading to Comet and resending "+ar);
c(aq)
}}function V(ar){var aq=jQuery.parseJSON(ar);
if(aq.id!=B){if(typeof(G.onLocalMessage)!="undefined"){G.onLocalMessage(aq.event)
}else{if(typeof(jQuery.atmosphere.onLocalMessage)!="undefined"){jQuery.atmosphere.onLocalMessage(aq.event)
}}}}function x(au,aq,ar,at){if(aq=="messageReceived"){if(q(au,G,O)){return
}}else{O.responseBody=au
}O.transport=at;
O.status=ar;
O.state=aq;
u()
}function Y(aq,au){if(!au.readResponsesHeaders&&!au.enableProtocol){au.lastTimestamp=jQuery.now();
au.uuid=jQuery.atmosphere.guid();
return
}try{var at=aq.getResponseHeader("X-Cache-Date");
if(at&&at!=null&&at.length>0){au.lastTimestamp=at.split(" ").pop()
}var ar=aq.getResponseHeader("X-Atmosphere-tracking-id");
if(ar&&ar!=null){au.uuid=ar.split(" ").pop()
}if(au.headers){jQuery.each(G.headers,function(ax){var aw=aq.getResponseHeader(ax);
if(aw){O.headers[ax]=aw
}})
}}catch(av){}}function U(aq){al(aq,G);
al(aq,jQuery.atmosphere)
}function al(ar,at){switch(ar.state){case"messageReceived":f=0;
if(typeof(at.onMessage)!="undefined"){at.onMessage(ar)
}break;
case"error":if(typeof(at.onError)!="undefined"){at.onError(ar)
}break;
case"opening":if(typeof(at.onOpen)!="undefined"){at.onOpen(ar)
}break;
case"messagePublished":if(typeof(at.onMessagePublished)!="undefined"){at.onMessagePublished(ar)
}break;
case"re-opening":if(typeof(at.onReconnect)!="undefined"){at.onReconnect(G,ar)
}break;
case"unsubscribe":case"closed":var aq=typeof(G.closed)!="undefined"?G.closed:false;
if(typeof(at.onClose)!="undefined"&&!aq){at.onClose(ar)
}G.closed=true;
break
}}function aa(aq){O.state="closed";
O.responseBody="";
O.messages=[];
O.status=!aq?501:200;
u()
}function u(){var ar=function(av,aw){aw(O)
};
if(k==null&&S!=null){S(O.responseBody)
}G.reconnect=G.mrequest;
var at=(typeof(O.responseBody)=="string"&&G.trackMessageLength)?(O.messages.length>0?O.messages:[""]):new Array(O.responseBody);
for(var aq=0;
aq<at.length;
aq++){if(at.length>1&&at[aq].length==0){continue
}O.responseBody=jQuery.trim(at[aq]);
if(O.responseBody.length==0&&O.state=="messageReceived"){continue
}U(O);
if(jQuery.atmosphere.callbacks.length>0){if(G.logLevel=="debug"){jQuery.atmosphere.debug("Invoking "+jQuery.atmosphere.callbacks.length+" global callbacks: "+O.state)
}try{jQuery.each(jQuery.atmosphere.callbacks,ar)
}catch(au){jQuery.atmosphere.log(G.logLevel,["Callback exception"+au])
}}if(typeof(G.callback)=="function"){if(G.logLevel=="debug"){jQuery.atmosphere.debug("Invoking request callbacks")
}try{G.callback(O)
}catch(au){jQuery.atmosphere.log(G.logLevel,["Callback exception"+au])
}}}}function D(ar,aq){if(O.partialMessage==""&&(aq.transport=="streaming")&&(ar.responseText.length>aq.maxStreamingLength)){O.messages=[];
aa(true);
v();
ad();
J(ar,aq,true)
}}function v(){if(G.enableProtocol){var ar="X-Atmosphere-Transport=close&X-Atmosphere-tracking-id="+G.uuid;
var aq=G.url.replace(/([?&])_=[^&]*/,ar);
aq=aq+(aq===G.url?(/\?/.test(G.url)?"&":"?")+ar:"");
if(G.connectTimeout>-1){jQuery.ajax({url:aq,async:false,timeout:G.connectTimeout})
}else{jQuery.ajax({url:aq,async:false})
}}}function af(){G.reconnect=false;
an=true;
O.request=G;
O.state="unsubscribe";
O.responseBody="";
O.messages=[];
O.status=408;
u();
ad()
}function ad(){if(w!=null){w.close();
w=null
}if(y!=null){y.abort();
y=null
}if(o!=null){o.abort();
o=null
}if(R!=null){if(R.webSocketOpened){R.close()
}R=null
}if(i!=null){i.close();
i=null
}ak()
}function ak(){if(ah!=null){clearInterval(C);
document.cookie=encodeURIComponent("atmosphere-"+G.url)+"=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
ah.signal("close",{reason:"",heir:!an?B:(ah.get("children")||[])[0]});
ah.close()
}if(k!=null){k.close()
}}this.subscribe=function(aq){ap(aq);
m()
};
this.execute=function(){m()
};
this.invokeCallback=function(){u()
};
this.close=function(){af()
};
this.disconnect=function(){v()
};
this.getUrl=function(){return G.url
};
this.getUUID=function(){return G.uuid
};
this.push=function(aq){ae(aq)
};
this.pushLocal=function(aq){t(aq)
};
this.enableProtocol=function(aq){return G.enableProtocol
};
this.response=O
},subscribe:function(b,e,d){if(typeof(e)=="function"){jQuery.atmosphere.addCallback(e)
}if(typeof(b)!="string"){d=b
}else{d.url=b
}var c=new jQuery.atmosphere.AtmosphereRequest(d);
c.execute();
jQuery.atmosphere.requests[jQuery.atmosphere.requests.length]=c;
return c
},addCallback:function(b){if(jQuery.inArray(b,jQuery.atmosphere.callbacks)==-1){jQuery.atmosphere.callbacks.push(b)
}},removeCallback:function(c){var b=jQuery.inArray(c,jQuery.atmosphere.callbacks);
if(b!=-1){jQuery.atmosphere.callbacks.splice(b,1)
}},unsubscribe:function(){if(jQuery.atmosphere.requests.length>0){var b=[].concat(jQuery.atmosphere.requests);
for(var d=0;
d<b.length;
d++){var c=b[d];
c.disconnect();
c.close();
clearTimeout(c.response.request.id)
}}jQuery.atmosphere.requests=[];
jQuery.atmosphere.callbacks=[]
},unsubscribeUrl:function(c){var b=-1;
if(jQuery.atmosphere.requests.length>0){for(var e=0;
e<jQuery.atmosphere.requests.length;
e++){var d=jQuery.atmosphere.requests[e];
if(d.getUrl()==c){d.disconnect();
d.close();
clearTimeout(d.response.request.id);
b=e;
break
}}}if(b>=0){jQuery.atmosphere.requests.splice(b,1)
}},publish:function(c){if(typeof(c.callback)=="function"){jQuery.atmosphere.addCallback(callback)
}c.transport="polling";
var b=new jQuery.atmosphere.AtmosphereRequest(c);
jQuery.atmosphere.requests[jQuery.atmosphere.requests.length]=b;
return b
},checkCORSSupport:function(){if(jQuery.browser.msie&&!window.XDomainRequest){return true
}else{if(jQuery.browser.opera&&jQuery.browser.version<12){return true
}}var b=navigator.userAgent.toLowerCase();
var c=b.indexOf("android")>-1;
if(c){return true
}return false
},S4:function(){return(((1+Math.random())*65536)|0).toString(16).substring(1)
},guid:function(){return(jQuery.atmosphere.S4()+jQuery.atmosphere.S4()+"-"+jQuery.atmosphere.S4()+"-"+jQuery.atmosphere.S4()+"-"+jQuery.atmosphere.S4()+"-"+jQuery.atmosphere.S4()+jQuery.atmosphere.S4()+jQuery.atmosphere.S4())
},prepareURL:function(c){var d=jQuery.now();
var b=c.replace(/([?&])_=[^&]*/,"$1_="+d);
return b+(b===c?(/\?/.test(c)?"&":"?")+"_="+d:"")
},param:function(b){return jQuery.param(b,jQuery.ajaxSettings.traditional)
},supportStorage:function(){var c=window.localStorage;
if(c){try{c.setItem("t","t");
c.removeItem("t");
return window.StorageEvent&&!jQuery.browser.msie&&!(jQuery.browser.mozilla&&jQuery.browser.version.split(".")[0]==="1")
}catch(b){}}return false
},iterate:function(d,c){var e;
c=c||0;
(function b(){e=setTimeout(function(){if(d()===false){return
}b()
},c)
})();
return function(){clearTimeout(e)
}
},log:function(d,c){if(window.console){var b=window.console[d];
if(typeof b=="function"){b.apply(window.console,c)
}}},warn:function(){jQuery.atmosphere.log("warn",arguments)
},info:function(){jQuery.atmosphere.log("info",arguments)
},debug:function(){jQuery.atmosphere.log("debug",arguments)
},error:function(){jQuery.atmosphere.log("error",arguments)
}}
}();
(function(){var a,b;
jQuery.uaMatch=function(d){d=d.toLowerCase();
var c=/(chrome)[ \/]([\w.]+)/.exec(d)||/(webkit)[ \/]([\w.]+)/.exec(d)||/(opera)(?:.*version|)[ \/]([\w.]+)/.exec(d)||/(msie) ([\w.]+)/.exec(d)||d.indexOf("compatible")<0&&/(mozilla)(?:.*? rv:([\w.]+)|)/.exec(d)||[];
return{browser:c[1]||"",version:c[2]||"0"}
};
a=jQuery.uaMatch(navigator.userAgent);
b={};
if(a.browser){b[a.browser]=true;
b.version=a.version
}if(b.chrome){b.webkit=true
}else{if(b.webkit){b.safari=true
}}jQuery.browser=b;
jQuery.sub=function(){function c(f,g){return new c.fn.init(f,g)
}jQuery.extend(true,c,this);
c.superclass=this;
c.fn=c.prototype=this();
c.fn.constructor=c;
c.sub=this.sub;
c.fn.init=function e(f,g){if(g&&g instanceof jQuery&&!(g instanceof c)){g=c(g)
}return jQuery.fn.init.call(this,f,g,d)
};
c.fn.init.prototype=c.fn;
var d=c(document);
return c
}
})();
(function(d){var g=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,c={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"};
function a(f){return'"'+f.replace(g,function(h){var i=c[h];
return typeof i==="string"?i:"\\u"+("0000"+h.charCodeAt(0).toString(16)).slice(-4)
})+'"'
}function b(f){return f<10?"0"+f:f
}function e(m,l){var k,j,f,h,o=l[m],n=typeof o;
if(o&&typeof o==="object"&&typeof o.toJSON==="function"){o=o.toJSON(m);
n=typeof o
}switch(n){case"string":return a(o);
case"number":return isFinite(o)?String(o):"null";
case"boolean":return String(o);
case"object":if(!o){return"null"
}switch(Object.prototype.toString.call(o)){case"[object Date]":return isFinite(o.valueOf())?'"'+o.getUTCFullYear()+"-"+b(o.getUTCMonth()+1)+"-"+b(o.getUTCDate())+"T"+b(o.getUTCHours())+":"+b(o.getUTCMinutes())+":"+b(o.getUTCSeconds())+'Z"':"null";
case"[object Array]":f=o.length;
h=[];
for(k=0;
k<f;
k++){h.push(e(k,o)||"null")
}return"["+h.join(",")+"]";
default:h=[];
for(k in o){if(Object.prototype.hasOwnProperty.call(o,k)){j=e(k,o);
if(j){h.push(a(k)+":"+j)
}}}return"{"+h.join(",")+"}"
}}}d.stringifyJSON=function(f){if(window.JSON&&window.JSON.stringify){return window.JSON.stringify(f)
}return e("",{"":f})
}
}(jQuery));