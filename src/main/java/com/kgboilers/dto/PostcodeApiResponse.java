package com.kgboilers.dto;

import lombok.Data;

@Data
public class PostcodeApiResponse {

    private Result result;

    @Data
    public static class Result {
        private double latitude;
        private double longitude;
    }
}