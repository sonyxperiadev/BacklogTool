<li id="note-${note.id}" class="note <c:if test='${note.isSystemGenerated()}'>sysgenerated</c:if>">
    <div class="note single-note">
        <a id="note-delete-${note.id}" title="Remove note"
            class="icon deleteItem delete-icon" style="float: right;"></a>
        <p class="user-msg"><span class="user">${note.user}</span>: <span class="message">${note.getMessageWithLinksAndLineBreaks()}</span>
        </p>
        <span class="date"><fmt:formatDate value="${note.createdDate}" pattern="yyyy-MM-dd HH:mm" /></span>
    </div>
</li>