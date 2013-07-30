package com.sonymobile.backlogtool;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;

@Cache(usage=READ_WRITE)
@Entity
@Table(name="Notes")
public class Note {
    public static final int MESSAGE_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private int id;

    @Column(length=255)
    private String username;

    @Column(length=MESSAGE_LENGTH)
    private String message = "";

    private boolean sysGenerated;

    @JoinColumn(name="storyId")
    @ManyToOne
    private Story story;

    private Date created;
    private Date modified;
    
    /**
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id for this note. The server uses this one.
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * @return The message
     */
    public String getMessage() {
        return StringEscapeUtils.escapeHtml(message);
    }

    /**
     * @return Message where <a>-tags have been added around URLs
     * and newline-chars have been replaced with <br />.
     */
    @JsonIgnore
    public String getMessageWithLinksAndLineBreaks() {
        return Util.textAsHtmlLinksAndLineBreaks(getMessage());
    }

    /**
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = StringEscapeUtils.unescapeHtml(message);
    }
    
    public void setCreatedDate(Date date) {
        this.created = date;
    }
    
    public Date getCreatedDate() {
        return created;
    }
    
    public void setModifiedDate(Date date) {
        this.modified = date;
    }
    
    public Date getModifiedDate() {
        return modified;
    }
    
    public void setSystemGenerated(boolean systemgenerated) {
        this.sysGenerated = systemgenerated;
    }
    
    public boolean isSystemGenerated() {
        return sysGenerated;
    }
    
    public void setStory(Story story) {
        this.story = story;
    }
    
    @JsonIgnore
    public Story getStory() {
        return story;
    }
    
    public void setUser(String user) {
        this.username = user;
    }
    
    public String getUser() {
        return username;
    }
    
}
