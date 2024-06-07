package com.example.studystayandroid.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase que representa un comentario en un tema de foro.
 */
public class ForumComment implements Serializable {
    private Long commentId; // Identificador único del comentario
    private ForumTopic topic; // Tema al que pertenece el comentario
    private User author; // Autor del comentario
    private String content; // Contenido del comentario
    private LocalDateTime dateTime; // Fecha y hora de creación del comentario

    // Constructores

    /**
     * Constructor por defecto.
     */
    public ForumComment() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param topic    el tema al que pertenece el comentario
     * @param author   el autor del comentario
     * @param content  el contenido del comentario
     * @param dateTime la fecha y hora de creación del comentario
     */
    public ForumComment(ForumTopic topic, User author, String content, LocalDateTime dateTime) {
        this.topic = topic;
        this.author = author;
        this.content = content;
        this.dateTime = dateTime;
    }

    /**
     * Constructor con todos los parámetros.
     *
     * @param commentId el identificador único del comentario
     * @param topic     el tema al que pertenece el comentario
     * @param author    el autor del comentario
     * @param content   el contenido del comentario
     * @param dateTime  la fecha y hora de creación del comentario
     */
    public ForumComment(long commentId, ForumTopic topic, User author, String content, LocalDateTime dateTime) {
        this.commentId = commentId;
        this.topic = topic;
        this.author = author;
        this.content = content;
        this.dateTime = dateTime;
    }

    // Getters y setters

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public ForumTopic getTopic() {
        return topic;
    }

    public void setTopic(ForumTopic topic) {
        this.topic = topic;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    // equals, hashCode y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForumComment that = (ForumComment) o;
        return Objects.equals(commentId, that.commentId) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(author, that.author) &&
                Objects.equals(content, that.content) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, topic, author, content, dateTime);
    }

    @Override
    public String toString() {
        return "ForumComment{" +
                "commentId=" + commentId +
                ", topic=" + topic +
                ", author=" + author +
                ", content='" + content + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
