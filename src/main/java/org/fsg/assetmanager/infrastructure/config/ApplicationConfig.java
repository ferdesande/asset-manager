package org.fsg.assetmanager.infrastructure.config;

import org.fsg.assetmanager.application.AssetService;
import org.fsg.assetmanager.domain.model.Asset;
import org.fsg.assetmanager.domain.port.out.AssetPublisher;
import org.fsg.assetmanager.domain.port.out.AssetRepository;
import org.fsg.assetmanager.domain.port.out.AssetSearchCriteria;
import org.fsg.assetmanager.domain.port.out.PublishedUrl;
import org.fsg.assetmanager.domain.service.AssetValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

import java.time.Clock;
import java.util.List;

@Configuration
public class ApplicationConfig {
    @Bean
    public AssetService assetService(
            @Lazy ObjectProvider<AssetService> selfProvider, // See hint in AssetService
            AssetValidator validator,
            AssetRepository repository,
            AssetPublisher assetPublisher,
            Clock clock,
            IdGenerator idGenerator) {
        return new AssetService(selfProvider, validator, repository, assetPublisher, clock, idGenerator);
    }

    @Bean
    public AssetValidator assetValidator() {
        return new AssetValidator();
    }

    @Bean
    public AssetRepository assetRepository() {
        // TODO: Must be replaced
        return new AssetRepository() {
            @Override
            public Asset save(Asset asset) {
                return asset;
            }

            @Override
            public List<Asset> find(AssetSearchCriteria criteria) {
                return List.of();
            }
        };
    }

    @Bean
    public AssetPublisher assetPublisher() {
        // TODO: Must be replaced
        return (asset, content) -> new PublishedUrl("123");
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public IdGenerator idGenerator() {
        return new JdkIdGenerator();
    }
}
