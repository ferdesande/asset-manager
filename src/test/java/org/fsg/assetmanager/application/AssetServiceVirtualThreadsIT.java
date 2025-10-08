package org.fsg.assetmanager.application;

import org.fsg.assetmanager.domain.model.Asset;
import org.fsg.assetmanager.domain.mother.AssetMother;
import org.fsg.assetmanager.domain.port.in.AssetUploadCommand;
import org.fsg.assetmanager.domain.port.out.AssetPublisher;
import org.fsg.assetmanager.domain.port.out.AssetRepository;
import org.fsg.assetmanager.domain.port.out.PublishedUrl;
import org.fsg.assetmanager.domain.service.AssetValidator;
import org.fsg.assetmanager.infrastructure.config.ApplicationConfig;
import org.fsg.assetmanager.infrastructure.config.AsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {ApplicationConfig.class, AsyncConfig.class})
class AssetServiceVirtualThreadsIT {
    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");


    @MockitoBean
    private AssetValidator validator;

    @MockitoBean
    private AssetRepository repository;

    @MockitoBean
    private AssetPublisher publisher;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private IdGenerator idGenerator;

    @Autowired
    private AssetService service;

    @Test
    void shouldUseVirtualThreadsForAsyncUpload() throws InterruptedException {
        // Given
        configureClock();
        configureIdGenerator();
        configureRepository();

        // Here, we check the current threat leveraging mockito
        AtomicReference<Thread> asyncThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            asyncThread.set(Thread.currentThread());
            latch.countDown();
            return new PublishedUrl("foo");
        }).when(publisher).publish(any(Asset.class), any(byte[].class));

        // When
        AssetUploadCommand command = new AssetUploadCommand(
                "test.png",
                "image/png",
                3,
                new byte[]{1, 2, 3});
        service.upload(command);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS), equalTo(true));
        assertThat(asyncThread.get(), notNullValue());
        assertThat(String.format("Thread should be virtual but was: %s", asyncThread.get()),
                asyncThread.get().isVirtual(), equalTo(true));
    }

    private void configureRepository() {
        when(repository.save(any(Asset.class))).thenAnswer(returnsFirstArg());
    }

    private void configureIdGenerator() {
        when(idGenerator.generateId()).thenReturn(SAMPLE_UUID);
    }

    private void configureClock() {
        when(clock.instant()).thenReturn(AssetMother.SAMPLE_UPLOAD_DATE);
    }
}