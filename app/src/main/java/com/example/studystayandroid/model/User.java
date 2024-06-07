package com.example.studystayandroid.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un usuario en el sistema.
 */
public class User implements Serializable {
    private Long userId; // Identificador único del usuario
    private String name; // Nombre del usuario
    private String lastName; // Apellidos del usuario
    private String email; // Correo electrónico del usuario
    private String password; // Contraseña del usuario
    private String phone; // Número de teléfono del usuario
    private LocalDate birthDate; // Fecha de nacimiento del usuario
    private LocalDateTime registrationDate; // Fecha de registro del usuario
    private Gender gender; // Género del usuario
    private String dni; // Documento nacional de identidad del usuario
    private byte[] profilePicture; // Imagen de perfil del usuario
    private String bio; // Descripción o biografía del usuario
    private boolean isAdmin; // Indica si el usuario es administrador del sistema
    private List<Booking> bookings; // Lista de reservas realizadas por el usuario
    private List<ForumComment> posts; // Lista de publicaciones realizadas por el usuario
    private List<AccommodationReview> reviews; // Lista de reseñas realizadas por el usuario
    private List<Accommodation> accommodations; // Lista de alojamientos gestionados por el usuario

    /**
     * Enumeración que representa el género del usuario.
     */
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    // Constructores

    /**
     * Constructor por defecto.
     */
    public User() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param name      el nombre del usuario
     * @param lastName  los apellidos del usuario
     * @param email     el correo electrónico del usuario
     * @param password  la contraseña del usuario
     * @param phone     el número de teléfono del usuario
     * @param birthDate la fecha de nacimiento del usuario
     * @param gender    el género del usuario
     * @param dni       el documento nacional de identidad del usuario
     * @param admin     indica si el usuario es administrador
     */
    public User(String name, String lastName, String email, String password, String phone, LocalDate birthDate, Gender gender, String dni, boolean admin) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.dni = dni;
        this.isAdmin = admin;
    }

    /**
     * Constructor con todos los parámetros.
     *
     * @param name             el nombre del usuario
     * @param lastName         los apellidos del usuario
     * @param email            el correo electrónico del usuario
     * @param password         la contraseña del usuario
     * @param phone            el número de teléfono del usuario
     * @param birthDate        la fecha de nacimiento del usuario
     * @param registrationDate la fecha de registro del usuario
     * @param gender           el género del usuario
     * @param dni              el documento nacional de identidad del usuario
     * @param profilePicture   la imagen de perfil del usuario
     * @param bio              la biografía del usuario
     * @param isAdmin          indica si el usuario es administrador
     */
    public User(String name, String lastName, String email, String password, String phone, LocalDate birthDate, LocalDateTime registrationDate, Gender gender, String dni, byte[] profilePicture, String bio, boolean isAdmin) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.birthDate = birthDate;
        this.registrationDate = registrationDate;
        this.gender = gender;
        this.dni = dni;
        this.profilePicture = profilePicture;
        this.bio = bio;
        this.isAdmin = isAdmin;
    }

    // Getters y setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<ForumComment> getPosts() {
        return posts;
    }

    public void setPosts(List<ForumComment> posts) {
        this.posts = posts;
    }

    public List<AccommodationReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<AccommodationReview> reviews) {
        this.reviews = reviews;
    }

    public List<Accommodation> getAccommodations() {
        return accommodations;
    }

    public void setAccommodations(List<Accommodation> accommodations) {
        this.accommodations = accommodations;
    }

    // equals, hashCode y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isAdmin == user.isAdmin &&
                Objects.equals(userId, user.userId) &&
                Objects.equals(name, user.name) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password) &&
                Objects.equals(phone, user.phone) &&
                Objects.equals(birthDate, user.birthDate) &&
                Objects.equals(registrationDate, user.registrationDate) &&
                gender == user.gender &&
                Objects.equals(dni, user.dni) &&
                Arrays.equals(profilePicture, user.profilePicture) &&
                Objects.equals(bio, user.bio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, lastName, email, password, phone, birthDate, registrationDate, gender, dni, bio, isAdmin) + Arrays.hashCode(profilePicture);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", birthDate=" + birthDate +
                ", registrationDate=" + registrationDate +
                ", gender=" + gender +
                ", dni='" + dni + '\'' +
                ", profilePicture=" + Arrays.toString(profilePicture) +
                ", bio='" + bio + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}
