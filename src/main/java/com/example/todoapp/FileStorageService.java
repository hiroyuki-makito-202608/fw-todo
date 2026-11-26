package com.example.todoapp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private final Path storageDirectory;

    public FileStorageService(@Value("${app.file-storage.directory:uploads}") String directory) {
        this.storageDirectory = Paths.get(directory).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("空のファイルは保存できません。");
        }
        String original = sanitizeFilename(file.getOriginalFilename());
        if (original.isBlank()) {
            throw new IllegalArgumentException("ファイル名が必要です。");
        }
        String extension = StringUtils.getFilenameExtension(original);
        String stored = UUID.randomUUID() + (extension == null || extension.isBlank() ? "" : "." + extension);
        Files.createDirectories(storageDirectory);
        Path target = storageDirectory.resolve(stored).normalize();
        if (!target.startsWith(storageDirectory)) {
            throw new IOException("不正なファイルパスです。");
        }
        file.transferTo(target);
        return new StoredFile(original, stored, file.getContentType(), file.getSize());
    }

    public StoredFile storeBytes(byte[] bytes, String originalFileName, String contentType) throws IOException {
        String original = sanitizeFilename(originalFileName);
        if (bytes == null || bytes.length == 0 || original.isBlank()) {
            throw new IllegalArgumentException("ファイル名と内容が必要です。");
        }
        String extension = StringUtils.getFilenameExtension(original);
        String stored = UUID.randomUUID() + (extension == null || extension.isBlank() ? "" : "." + extension);
        Files.createDirectories(storageDirectory);
        Files.write(resolve(stored), bytes);
        return new StoredFile(original, stored, contentType, bytes.length);
    }

    public Resource load(String storedFileName) throws IOException {
        Path path = resolve(storedFileName);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("添付ファイルが見つかりません。");
        }
        return resource;
    }

    public void delete(String storedFileName) throws IOException {
        Files.deleteIfExists(resolve(storedFileName));
    }

    private Path resolve(String storedFileName) throws IOException {
        if (storedFileName == null || storedFileName.isBlank() || storedFileName.contains("/")
                || storedFileName.contains("\\") || storedFileName.contains("..")) {
            throw new IOException("不正な保存ファイル名です。");
        }
        Path path = storageDirectory.resolve(storedFileName).normalize();
        if (!path.startsWith(storageDirectory)) {
            throw new IOException("不正なファイルパスです。");
        }
        return path;
    }

    static String sanitizeFilename(String filename) {
        if (filename == null) return "";
        String normalized = filename.replace('\\', '/');
        String basename = normalized.substring(normalized.lastIndexOf('/') + 1);
        basename = basename.replaceAll("[\\p{Cntrl}]", "").trim();
        return basename.length() > 255 ? basename.substring(basename.length() - 255) : basename;
    }

    public record StoredFile(String originalFileName, String storedFileName, String contentType, long size) { }
}
