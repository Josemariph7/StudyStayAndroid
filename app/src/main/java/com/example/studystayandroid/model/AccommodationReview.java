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

    /**
     * Constructor por defecto.
     */
    public AccommodationReview() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param accommodation el alojamiento al que pertenece la reseña
     * @param author        el autor de la reseña
     * @param rating        la calificación de la reseña
     * @param comment       el comentario de la reseña
     * @param dateTime      la fecha y hora de la reseña
     */
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
        return Double.compare(that.rating, rating) == 0 &&
                Objects.equals(reviewId, that.reviewId) &&
                Objects.equals(accommodation, that.accommodation) &&
                Objects.equals(author, that.author) &&
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
