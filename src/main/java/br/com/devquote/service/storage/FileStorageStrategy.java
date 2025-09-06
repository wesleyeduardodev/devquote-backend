package br.com.devquote.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageStrategy {

    /**
     * Faz upload de um arquivo
     * @param file Arquivo a ser enviado
     * @param path Caminho onde o arquivo será armazenado
     * @return URL ou caminho do arquivo armazenado
     */
    String uploadFile(MultipartFile file, String path) throws IOException;

    /**
     * Obtém URL temporária para download do arquivo
     * @param filePath Caminho do arquivo
     * @return URL temporária para download
     */
    String getFileUrl(String filePath);

    /**
     * Obtém stream do arquivo
     * @param filePath Caminho do arquivo
     * @return InputStream do arquivo
     */
    InputStream getFileStream(String filePath) throws IOException;

    /**
     * Exclui um arquivo
     * @param filePath Caminho do arquivo a ser excluído
     * @return true se excluído com sucesso
     */
    boolean deleteFile(String filePath);

    /**
     * Verifica se um arquivo existe
     * @param filePath Caminho do arquivo
     * @return true se existe
     */
    boolean fileExists(String filePath);
}