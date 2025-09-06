package br.com.devquote.configuration;

import br.com.devquote.service.storage.FileStorageStrategy;
import br.com.devquote.service.storage.S3FileStorageStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class StorageConfiguration {

    @Bean
    @Primary
    public FileStorageStrategy fileStorageStrategy(S3FileStorageStrategy s3FileStorageStrategy) {
        // Por padrão usa S3, mas pode ser facilmente trocado para outro storage
        return s3FileStorageStrategy;
    }
}