package com.example.studystayandroid.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Clase que representa una reseña de un alojamiento.
 */
public class AccommodationReview implements Serializable {
    private Long reviewId; // Identificador único de la reseña
    private Accommodation accommodation; // Alojamiento al que pertenece la reseña
    private User author; // Autor de la reseña
    private double rating; // Calificación de la reseña
    private String comment; // Comentario de la reseña
    private LocalDateTime dateTime; // Fecha y hora de la reseña

    // Constructores

    public AccommodationReview() {
    }

    public AccommodationReview(Accommodation accommodation, User author, double rating, String comment, LocalDateTime dateTime) {
        this.accommodation = accommodation;
        this.author = author;
        this.rating = rating;
        this.comment = comment;
        this.dateTime = dateTime;
    }

    // Getters y setters

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        AccommodationReview that = (AccommodationReview) o;
        return Objects.equals(reviewId, that.reviewId) &&
                Objects.equals(accommodation, that.accommodation) &&
                Objects.equals(author, that.author) &&
                Objects.equals(rating, that.rating) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, accommodation, author, rating, comment, dateTime);
    }

    @Override
    public String toString() {
        return "AccommodationReview{" +
                "reviewId=" + reviewId +
                ", accommodation=" + accommodation +
                ", author=" + author +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
