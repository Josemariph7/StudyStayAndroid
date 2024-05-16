package com.example.studystayandroid.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un tema en un foro.
 */
public class ForumTopic {
    private Long topicId; // Identificador único del tema
    private String title; // Título del tema
    private String description; // Descripción del tema
    private User author; // Autor del tema
    private LocalDateTime dateTime; // Fecha y hora de creación del tema
    private List<ForumComment> comments; // Lista de comentarios en el tema

    // Constructores

    public ForumTopic() {
    }

    public ForumTopic(String title, String description, User author, LocalDateTime dateTime) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.dateTime = dateTime;
    }

    // Getters y setters

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<ForumComment> getComments(Long topicId) {
        return comments;
    }

    public void setComments(List<ForumComment> comments) {
        this.comments = comments;
    }

    // equals, hashCode y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForumTopic that = (ForumTopic) o;
        return Objects.equals(topicId, that.topicId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(author, that.author) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, title, description, author, dateTime);
    }

    @Override
    public String toString() {
        return "ForumTopic{" +
                "topicId=" + topicId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author=" + author +
                ", dateTime=" + dateTime +
                '}';
    }
}
