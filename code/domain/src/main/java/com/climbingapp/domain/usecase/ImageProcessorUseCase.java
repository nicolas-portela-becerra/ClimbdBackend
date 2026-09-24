package com.climbingapp.domain.usecase;

public interface ImageProcessorUseCase {

    byte[] compress(byte[] imageData, int maxPx);

    byte[] createThumbnail(byte[] imageData, int width);
}
