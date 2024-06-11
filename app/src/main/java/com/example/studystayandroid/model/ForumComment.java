/*
 * StudyStay © 2024
 *
 * All rights reserved.
 *
 * This software and associated documentation files (the "Software") are owned by StudyStay. Unauthorized copying, distribution, or modification of this Software is strictly prohibited.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this Software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * StudyStay
 * José María Pozo Hidalgo
 * Email: josemariph7@gmail.com
 *
 *
 */

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
