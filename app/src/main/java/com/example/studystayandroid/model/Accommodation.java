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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un alojamiento.
 */
public class Accommodation implements Serializable {
    private Long accommodationId;
    private User owner;
    private String address;
    private String city;
    private BigDecimal price;
    private String description;
    private int capacity;
    private String services;
    private boolean availability;
    private double rating;
    private List<Booking> bookings;
    private List<AccommodationReview> reviews;
    private List<User> tenants;
    private List<AccommodationPhoto> photos;

    /**
     * Constructor por defecto.
     */
    public Accommodation() {
        tenants = new ArrayList<>();
        photos = new ArrayList<>();
    }

    /**
     * Constructor con parámetros.
     *
     * @param owner       el propietario del alojamiento
     * @param address     la dirección del alojamiento
     * @param city        la ciudad del alojamiento
     * @param price       el precio del alojamiento
     * @param description la descripción del alojamiento
     * @param capacity    la capacidad del alojamiento
     * @param services    los servicios ofrecidos por el alojamiento
     */
    public Accommodation(User owner, String address, String city, BigDecimal price, String description, int capacity, String services) {
        this.owner = owner;
        this.address = address;
        this.city = city;
        this.price = price;
        this.description = description;
        this.capacity = capacity;
        this.services = services;
        this.availability = true;
        this.rating = 0;
        tenants = new ArrayList<>();
        photos = new ArrayList<>();
    }

    // Getters and setters

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<AccommodationReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<AccommodationReview> reviews) {
        this.reviews = reviews;
    }

    public List<User> getTenants() {
        return tenants;
    }

    public void setTenants(List<User> tenants) {
        this.tenants = tenants;
    }

    public List<AccommodationPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<AccommodationPhoto> photos) {
        this.photos = photos;
    }

    /**
     * Agrega un inquilino al alojamiento.
     *
     * @param tenant el inquilino a agregar
     * @return true si el inquilino fue agregado exitosamente, false si no hay espacio disponible
     */
    public boolean addTenant(User tenant) {
        if (tenants.size() < capacity) {
            tenants.add(tenant);
            return true; // Inquilino agregado exitosamente
        } else {
            return false; // No hay espacio disponible para agregar más inquilinos
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Accommodation that = (Accommodation) o;
        return Objects.equals(accommodationId, that.accommodationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accommodationId);
    }

    @Override
    public String toString() {
        return "Accommodation{" +
                "accommodationId=" + accommodationId +
                ", owner=" + owner +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", capacity=" + capacity +
                ", services='" + services + '\'' +
                ", availability=" + availability +
                ", rating=" + rating +
                '}';
    }
}
