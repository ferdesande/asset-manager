package org.fsg.assetmanager.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.fsg.assetmanager.domain.model.Asset;
import org.fsg.assetmanager.domain.model.AssetId;
import org.fsg.assetmanager.domain.model.AssetStatus;
import org.fsg.assetmanager.domain.model.SortDirection;
import org.fsg.assetmanager.domain.mother.AssetMother;
import org.fsg.assetmanager.domain.port.in.AssetSearchQuery;
import org.fsg.assetmanager.domain.port.in.AssetUploadCommand;
import org.fsg.assetmanager.domain.port.in.AssetUploadResult;
import org.fsg.assetmanager.domain.port.out.AssetPublisher;
import org.fsg.assetmanager.domain.port.out.AssetRepository;
import org.fsg.assetmanager.domain.port.out.AssetSearchCriteria;
import org.fsg.assetmanager.domain.port.out.PublishedUrl;
import org.fsg.assetmanager.domain.service.AssetValidator;
import org.fsg.assetmanager.testutils.LogAppender;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.LoggerFactory;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.fsg.assetmanager.testutils.CustomMatchers.hasFormattedLog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {
    private static final String SAMPLE_URL = "url-to-an-asset";
    private static final UUID SAMPLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final AssetId SAMPLE_ASSET_ID = new AssetId(SAMPLE_UUID.toString());
    private static final AssetSearchQuery ASSET_SEARCH_QUERY = new AssetSearchQuery(SortDirection.ASC);
    private static final AssetUploadCommand ASSET_UPLOAD_COMMAND = new AssetUploadCommand(
            AssetMother.SAMPLE_FILENAME.value(),
            AssetMother.SAMPLE_CONTENT_TYPE.value(),
            AssetMother.SAMPLE_CONTENT.length,
            AssetMother.SAMPLE_CONTENT);

    private final LogAppender logAppender = new LogAppender(Level.INFO);
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    private final Logger logger = (Logger) LoggerFactory.getLogger(AssetService.class);

    @Mock
    private AssetValidator validator;

    @Mock
    private AssetRepository repository;

    @Mock
    private AssetPublisher publisher;

    @Mock
    private Clock clock;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private AssetService service;

    @BeforeEach
    void setUp() {
        logger.addAppender(logAppender);
        logAppender.start();
    }

    @AfterEach
    void tearDown() {
        logAppender.stop();
        logAppender.clear();
    }

    @Nested
    @DisplayName("Search tests")
    class SearchTests {
        @Test
        @DisplayName("Search should return assets when found")
        void searchShouldReturnAssetsWhenFound() {
            // Given
            configureSearchAssets(AssetMother.SAMPLE_ASSET, AssetMother.ANOTHER_ASSET);

            // When
            List<Asset> assets = service.search(ASSET_SEARCH_QUERY);

            // Then
            assertThat(assets, contains(AssetMother.SAMPLE_ASSET, AssetMother.ANOTHER_ASSET));
        }

        @Test
        @DisplayName("Search should return empty list when no asset is found")
        void searchShouldReturnEmptyListWhenNoAssetsIsFound() {
            // Given
            configureSearchAssets();

            // When
            List<Asset> assets = service.search(ASSET_SEARCH_QUERY);

            // Then
            assertThat(assets, emptyIterable());
        }
    }

    @Nested
    @DisplayName("Upload tests")
    class UploadTests {
        @Test
        @DisplayName("Upload should return the ID when created")
        void uploadShouldReturnIdWhenCreated() {
            // Given
            configureClock();
            configureIdGenerator();
            configureAssetValidator();
            configureSaveAssetInRepository();
            configureAssetPublisher();

            // When
            AssetUploadResult result = service.upload(ASSET_UPLOAD_COMMAND);

            // Then
            assertThat(result.assetId(), equalTo(SAMPLE_ASSET_ID));

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(repository, times(2)).save(captor.capture());
            verify(publisher, times(1))
                    .publish(eq(captor.getAllValues().getFirst()), eq(AssetMother.SAMPLE_CONTENT));

            assertSavedAssets(captor, AssetStatus.PUBLISHED, SAMPLE_URL);

            assertThat(
                    logAppender.getEvents(),
                    contains(
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset info stored locally with ID '%s', filename: '%s'",
                                            SAMPLE_ASSET_ID, AssetMother.SAMPLE_FILENAME.value())),
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset with ID '%s' was published successfully with url '%s'",
                                            SAMPLE_ASSET_ID, SAMPLE_URL)),
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset with ID '%s' was marked as published", SAMPLE_ASSET_ID))
                    ));
        }

        @Test
        @DisplayName("Upload should not wrap an exception when is thrown by validator")
        void uploadShouldNotWrapAnExceptionWhenIsThrownByAssetValidator() {
            // Given
            configureClock();
            configureIdGenerator();

            IllegalStateException exception = new IllegalStateException("A fancy message");
            doThrow(exception).when(validator).validate(any());

            // When
            IllegalStateException actualException = assertThrows(IllegalStateException.class,
                    () -> service.upload(ASSET_UPLOAD_COMMAND));

            // Then
            assertThat(actualException, Matchers.sameInstance(exception));
            verifyNoInteractions(repository, publisher);
            assertThat(logAppender.getEvents(), emptyIterable());
        }

        @Test
        @DisplayName("Upload should not wrap an exception when is thrown by first call to repository")
        void uploadShouldNotWrapAnExceptionWhenIsThrownByFirstCallToRepository() {
            // Given
            configureClock();
            configureIdGenerator();
            configureAssetValidator();

            IllegalStateException exception = new IllegalStateException("A fancy message");
            when(repository.save(any(Asset.class))).thenThrow(exception);

            // When
            IllegalStateException actualException = assertThrows(IllegalStateException.class,
                    () -> service.upload(ASSET_UPLOAD_COMMAND));

            // Then
            assertThat(actualException, Matchers.sameInstance(exception));
            verify(repository, times(1)).save(any(Asset.class));
            verifyNoInteractions(publisher);
            assertThat(logAppender.getEvents(), emptyIterable());
        }

        @Test
        @DisplayName("Upload should log error and mark asset status as failed when publish fails")
        void uploadShouldLogErrorAndMarkAssetAsFailedWhenPublishFails() {
            // Given
            configureClock();
            configureIdGenerator();
            configureAssetValidator();
            configureSaveAssetInRepository();

            String exceptionMessage = "A fancy message";
            IllegalStateException exception = new IllegalStateException(exceptionMessage);
            when(publisher.publish(any(), eq(AssetMother.SAMPLE_CONTENT))).thenThrow(exception);

            // When
            AssetUploadResult result = service.upload(ASSET_UPLOAD_COMMAND);

            // Then
            assertThat(result.assetId(), equalTo(SAMPLE_ASSET_ID));

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(repository, times(2)).save(captor.capture());
            verify(publisher, times(1))
                    .publish(eq(captor.getAllValues().getFirst()), eq(AssetMother.SAMPLE_CONTENT));

            assertSavedAssets(captor, AssetStatus.FAILED, null);

            assertThat(
                    logAppender.getEvents(),
                    contains(
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset info stored locally with ID '%s', filename: '%s'",
                                            SAMPLE_ASSET_ID, AssetMother.SAMPLE_FILENAME.value())),
                            hasFormattedLog(
                                    Level.ERROR,
                                    String.format("Asset with ID '%s' failed to publish: %s",
                                            SAMPLE_ASSET_ID, exceptionMessage))
                    ));
        }

        @Test
        @DisplayName("Upload should log error as critical when update asset status fails after publishing")
        void uploadShouldLogErrorAsCriticalWhenUpdateAssetStatusFailsAfterPublishing() {
            // Given
            configureClock();
            configureIdGenerator();
            configureAssetValidator();
            configureAssetPublisher();

            String exceptionMessage = "A fancy message";
            IllegalStateException exception = new IllegalStateException(exceptionMessage);
            // Throws an exception when repo is called for the second time
            configureSaveAssetInRepository()
                    .thenThrow(exception);

            // When
            AssetUploadResult result = service.upload(ASSET_UPLOAD_COMMAND);

            // Then
            assertThat(result.assetId(), equalTo(SAMPLE_ASSET_ID));

            ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
            verify(repository, times(2)).save(captor.capture());
            verify(publisher, times(1))
                    .publish(eq(captor.getAllValues().getFirst()), eq(AssetMother.SAMPLE_CONTENT));

            assertThat(
                    logAppender.getEvents(),
                    contains(
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset info stored locally with ID '%s', filename: '%s'",
                                            SAMPLE_ASSET_ID, AssetMother.SAMPLE_FILENAME.value())),
                            hasFormattedLog(
                                    Level.INFO,
                                    String.format("Asset with ID '%s' was published successfully with url '%s'",
                                            SAMPLE_ASSET_ID, SAMPLE_URL)),
                            hasFormattedLog(
                                    Level.ERROR,
                                    String.format(
                                            "CRITICAL: Asset with ID '%s' was published but failed to update metadata",
                                            SAMPLE_ASSET_ID))
                    ));
        }

        private void assertSavedAssets(
                ArgumentCaptor<Asset> captor,
                AssetStatus expectedFinalStatus,
                String expectedSavedUrl) {
            List<Asset> allValues = captor.getAllValues();
            Asset firstSaving = allValues.get(0);
            Asset secondSaving = allValues.get(1);

            assertThat(firstSaving.id(), equalTo(SAMPLE_ASSET_ID));
            assertThat(firstSaving.filename().value(), equalTo(ASSET_UPLOAD_COMMAND.filename()));
            assertThat(firstSaving.contentType().value(), equalTo(ASSET_UPLOAD_COMMAND.contentType()));
            assertThat(firstSaving.fileSize().value(), equalTo(ASSET_UPLOAD_COMMAND.size()));
            assertThat(firstSaving.uploadDate(), equalTo(AssetMother.SAMPLE_UPLOAD_DATE));
            assertThat(firstSaving.publishedUrl(), nullValue());
            assertThat(firstSaving.status(), equalTo(AssetStatus.PENDING));

            assertThat(secondSaving.id(), equalTo(SAMPLE_ASSET_ID));
            assertThat(secondSaving.filename().value(), equalTo(ASSET_UPLOAD_COMMAND.filename()));
            assertThat(secondSaving.contentType().value(), equalTo(ASSET_UPLOAD_COMMAND.contentType()));
            assertThat(secondSaving.fileSize().value(), equalTo(ASSET_UPLOAD_COMMAND.size()));
            assertThat(secondSaving.uploadDate(), equalTo(AssetMother.SAMPLE_UPLOAD_DATE));
            assertThat(secondSaving.publishedUrl(), equalTo(expectedSavedUrl));
            assertThat(secondSaving.status(), equalTo(expectedFinalStatus));
        }
    }

    private void configureSearchAssets(Asset... assets) {
        AssetSearchCriteria criteria = new AssetSearchCriteria(ASSET_SEARCH_QUERY.sortDirection());
        when(repository.find(criteria)).thenReturn(Arrays.asList(assets));
    }

    private OngoingStubbing<Asset> configureSaveAssetInRepository() {
        return when(repository.save(any(Asset.class))).thenAnswer(returnsFirstArg());
    }

    private void configureAssetPublisher() {
        when(publisher.publish(any(), eq(AssetMother.SAMPLE_CONTENT))).thenReturn(new PublishedUrl(SAMPLE_URL));
    }

    private void configureIdGenerator() {
        when(idGenerator.generateId()).thenReturn(SAMPLE_UUID);
    }

    private void configureClock() {
        when(clock.instant()).thenReturn(AssetMother.SAMPLE_UPLOAD_DATE);
    }

    private void configureAssetValidator() {
        doNothing().when(validator).validate(any());
    }
}