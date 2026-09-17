package br.com.erudio.services;

import br.com.erudio.config.FileStorageConfig;
import br.com.erudio.exception.FileNotFoundException;
import br.com.erudio.exception.FileStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageConfig fileStorageConfig) {

        this.fileStorageLocation = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath()
                .toAbsolutePath().normalize();
        try {
            LOGGER.info("Creating Directories");
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e) {
            LOGGER.error("Could not create the directory: {}", String.valueOf(e));
            throw new FileStorageException("Could not create the directory: " + e);
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                LOGGER.error("Sorry! Filename contains a invalid path sequence {}", fileName);
                throw new FileStorageException("Sorry! Filename contains a invalid path sequence " + fileName);
            }

            LOGGER.info("Saving file in disk");

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            LOGGER.error("Could not store file {}. Please try again.", fileName, e);
            throw new FileStorageException("Could not store file " + fileName + ". Please try again.", e);
        }

    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                LOGGER.error("Could not read file {}. Please try again.", fileName);
                throw new FileNotFoundException("Could not load file " + fileName + ". Please try again.");
            }
        } catch (Exception e) {
            LOGGER.error("Could not load file {}. Please try again.", fileName, e);
            throw new FileNotFoundException("Could not load file " + fileName + ". Please try again.", e);
        }
    }

}
