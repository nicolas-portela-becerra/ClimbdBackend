package com.climbingapp.domain.usecase;

public interface ImageCompressionUseCase {

    byte[] compress(byte[] imageData, int maxPx);

    byte[] createThumbnail(byte[] imageData, int width);
}
