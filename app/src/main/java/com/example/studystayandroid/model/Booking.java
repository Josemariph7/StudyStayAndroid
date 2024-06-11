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
 * Clase que representa una reserva en el sistema.
 */
public class Booking implements Serializable {
    private Long bookingId; // Identificador único de la reserva
    private Accommodation accommodation; // Alojamiento reservado
    private User user; // Usuario que realizó la reserva
    private LocalDateTime startDate; // Fecha y hora de inicio de la reserva
    private LocalDateTime endDate; // Fecha y hora de fin de la reserva
    private BookingStatus status; // Estado de la reserva

    /**
     * Enumeración que representa los posibles estados de una reserva.
     */
    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    // Constructores

    /**
     * Constructor por defecto.
     */
    public Booking() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param accommodation el alojamiento reservado
     * @param user          el usuario que realizó la reserva
     * @param startDate     la fecha y hora de inicio de la reserva
     * @param endDate       la fecha y hora de fin de la reserva
     * @param status        el estado de la reserva
     */
    public Booking(Accommodation accommodation, User user, LocalDateTime startDate, LocalDateTime endDate, BookingStatus status) {
        this.accommodation = accommodation;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters y setters

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    // equals, hashCode y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(bookingId, booking.bookingId) &&
                Objects.equals(accommodation, booking.accommodation) &&
                Objects.equals(user, booking.user) &&
                Objects.equals(startDate, booking.startDate) &&
                Objects.equals(endDate, booking.endDate) &&
                status == booking.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, accommodation, user, startDate, endDate, status);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", accommodation=" + accommodation +
                ", user=" + user +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
