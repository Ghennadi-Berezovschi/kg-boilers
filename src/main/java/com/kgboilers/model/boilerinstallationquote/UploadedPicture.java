package com.kgboilers.model.boilerinstallationquote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedPicture implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String originalFilename;
    private String storedFilename;
    private String url;
    private String contentType;
    private long sizeBytes;
}
