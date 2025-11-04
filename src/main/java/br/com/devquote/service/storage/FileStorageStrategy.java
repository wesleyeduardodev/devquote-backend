package br.com.devquote.service.storage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface FileStorageStrategy {

    String uploadFile(MultipartFile file, String path) throws IOException;

    String getFileUrl(String filePath);

    InputStream getFileStream(String filePath) throws IOException;

    boolean deleteFile(String filePath);

    boolean deleteFolder(String folderPath);

    boolean fileExists(String filePath);
}