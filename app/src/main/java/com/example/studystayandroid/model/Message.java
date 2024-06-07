package com.example.studystayandroid.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase que representa un mensaje en una conversación.
 */
public class Message implements Serializable {
    private Long messageId; // Identificador único del mensaje
    private Long conversationId; // Identificador de la conversación a la que pertenece el mensaje
    private Long senderId; // Identificador del remitente del mensaje
    private Long receiverId; // Identificador del destinatario del mensaje
    private String content; // Contenido del mensaje
    private LocalDateTime dateTime; // Fecha y hora de envío del mensaje

    // Constructor

    /**
     * Constructor por defecto.
     */
    public Message() {
    }

    /**
     * Constructor parametrizado.
     *
     * @param messageId      el identificador único del mensaje
     * @param conversationId el identificador de la conversación a la que pertenece el mensaje
     * @param senderId       el identificador del remitente del mensaje
     * @param receiverId     el identificador del destinatario del mensaje
     * @param content        el contenido del mensaje
     * @param dateTime       la fecha y hora de envío del mensaje
     */
    public Message(Long messageId, Long conversationId, Long senderId, Long receiverId, String content, LocalDateTime dateTime) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.dateTime = dateTime;
    }

    // Getters y setters

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
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
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId) &&
                Objects.equals(conversationId, message.conversationId) &&
                Objects.equals(senderId, message.senderId) &&
                Objects.equals(receiverId, message.receiverId) &&
                Objects.equals(content, message.content) &&
                Objects.equals(dateTime, message.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, conversationId, senderId, receiverId, content, dateTime);
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", content='" + content + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
