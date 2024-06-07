package com.example.studystayandroid.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clase que representa una foto de un alojamiento.
 */
public class AccommodationPhoto implements Serializable {
    private Long photoId;
    private Accommodation accommodation;
    private byte[] photoData;

    /**
     * Constructor por defecto.
     */
    public AccommodationPhoto() {
    }

    /**
     * Constructor con parámetros.
     *
     * @param accommodation el alojamiento al que pertenece la foto
     * @param photoData     los datos de la foto en formato byte[]
     */
    public AccommodationPhoto(Accommodation accommodation, byte[] photoData) {
        this.accommodation = accommodation;
        this.photoData = photoData;
    }

    // Getters and setters

    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(Accommodation accommodation) {
        this.accommodation = accommodation;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccommodationPhoto that = (AccommodationPhoto) o;
        return Objects.equals(photoId, that.photoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoId);
    }

    @Override
    public String toString() {
        return "AccommodationPhoto{" +
                "photoId=" + photoId +
                ", accommodation=" + accommodation +
                '}';
    }
}
