package com.example.studystayandroid.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Conversation implements Serializable {
    private Long conversationId; // Identificador único de la conversación
    private Long user1Id; // ID del primer usuario
    private Long user2Id; // ID del segundo usuario
    private List<Message> messages; // Lista de mensajes en la conversación

    // Constructor
    public Conversation() {
        this.messages = new ArrayList<>();
    }

    // Constructor parametrizado
    public Conversation(Long conversationId, Long user1Id, Long user2Id, List<Message> messages) {
        this.conversationId = conversationId;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.messages = messages;
    }

    // Getters y setters
    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // equals, hashCode y toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return Objects.equals(conversationId, that.conversationId) &&
                Objects.equals(user1Id, that.user1Id) &&
                Objects.equals(user2Id, that.user2Id) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, user1Id, user2Id, messages);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId=" + conversationId +
                ", user1Id=" + user1Id +
                ", user2Id=" + user2Id +
                ", messages=" + messages +
                '}';
    }
}
