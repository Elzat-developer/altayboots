package altay.boots.altayboots.service;

import altay.boots.altayboots.model.entity.ProductPhoto;

import java.util.List;

public interface PhotosOwner {
    List<ProductPhoto> getPhotos();
    int getId(); // Для логгирования
}
