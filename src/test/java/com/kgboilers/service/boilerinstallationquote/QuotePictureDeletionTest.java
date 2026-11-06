package com.kgboilers.service.boilerinstallationquote;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuotePictureDeletionTest {
    @TempDir Path root;

    @Test
    void rejectsPathsOutsideTheUploadDirectoryAndDeletesOnlyStoredFilenames() throws Exception {
        Path uploads = Files.createDirectory(root.resolve("uploads"));
        Path outside = Files.writeString(root.resolve("keep.png"), "keep");
        var storage = new QuotePictureStorageService(uploads.toString());
        assertThatThrownBy(() -> storage.deletePicture("../keep.png")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.deletePicture(outside.toString())).isInstanceOf(IllegalArgumentException.class);
        assertThat(outside).exists();
        String filename = "12345678-1234-1234-1234-123456789abc.png";
        Path picture = Files.writeString(uploads.resolve(filename), "picture");
        storage.deletePicture(filename);
        storage.deletePicture(filename);
        assertThat(picture).doesNotExist();
        assertThat(outside).hasContent("keep");
    }
}
