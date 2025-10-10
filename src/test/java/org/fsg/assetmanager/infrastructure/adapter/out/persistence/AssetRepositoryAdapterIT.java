package org.fsg.assetmanager.infrastructure.adapter.out.persistence;

import org.fsg.assetmanager.domain.exception.AssetAlreadyExistsException;
import org.fsg.assetmanager.domain.model.*;
import org.fsg.assetmanager.domain.mother.AssetMother;
import org.fsg.assetmanager.domain.port.out.AssetSearchCriteria;
import org.fsg.assetmanager.infrastructure.IntegrationTestBase;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.AssetJpaRepository;
import org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.entity.AssetEntity;
import org.fsg.assetmanager.testutils.ColonDelimiterParam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AssetRepositoryAdapter Integration Tests")
class AssetRepositoryAdapterIT extends IntegrationTestBase {
    private static final String UUID1 = "00000000-0000-0000-0000-000000000001";
    private static final String UUID2 = "00000000-0000-0000-0000-000000000002";
    private static final String UUID3 = "00000000-0000-0000-0000-000000000003";
    private static final String SAMPLE_CONTENT_TYPE = "image/png";
    private static final String ANOTHER_CONTENT_TYPE = "image/jpeg";
    private static final String INSTANT_1 = "2018-05-05T11:45:00Z";
    private static final String INSTANT_2 = "2018-05-05T12:00:00Z";
    private static final String INSTANT_3 = "2018-05-05T12:15:00Z";
    private static final String BEFORE_INSTANT_1 = "2018-05-05T11:30:00Z";
    private static final String AFTER_INSTANT_3 = "2018-05-05T12:30:00Z";
    private static final AssetSearchCriteria EMPTY_SEARCH_CRITERIA = new AssetSearchCriteria(null, null, null, null, null);

    // Hint: Entities are created as fresh to avoid problems with hibernate
    private static final AssetEntity ENTITY_1 = createEntity1();

    @Autowired
    private AssetRepositoryAdapter adapter;

    @Autowired
    private AssetJpaRepository jpaRepository;

    @AfterEach
    void tearDown() {
        jpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Find tests")
    class FindTest {

        @Test
        @DisplayName("Should return empty list when criteria database is empty")
        void shouldReturnEmptyListWhenDataIsEmpty() {
            // When
            List<String> assetIds = adapter.find(EMPTY_SEARCH_CRITERIA)
                    .stream()
                    .map(asset -> asset.id().value()).toList();

            // Then
            assertThat(assetIds, emptyIterable());
        }

        @Test
        @DisplayName("Should return all assets sorted by upload date asc when criteria is not applied")
        void shouldReturnAllAssetsSortedByUploadDeteAscWhenDataIsNotFound() {
            assertFindSortsCorrectly(EMPTY_SEARCH_CRITERIA, UUID1, UUID2, UUID3);
        }

        @Test
        @DisplayName("Should return assets sorted by upload date desc when criteria has sort direction desc")
        void shouldReturnAssetsSortedByUploadDateDescWhenCriteriaHasSortDirectionDesc() {
            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, null, null, SortDirection.DESC);
            assertFindSortsCorrectly(criteria, UUID3, UUID2, UUID1);
        }

        @Test
        @DisplayName("Should return assets sorted by upload date asc when criteria has sort direction asc")
        void shouldReturnAssetsSortedByUploadDateAscWhenCriteriaHasSortDirectionAsc() {
            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, null, null, SortDirection.ASC);
            assertFindSortsCorrectly(criteria, UUID1, UUID2, UUID3);
        }

        @ParameterizedTest
        @CsvSource(value = {
                INSTANT_1 + ", " + INSTANT_3 + ", " + UUID1 + ":" + UUID2 + ":" + UUID3,
                INSTANT_1 + ", " + INSTANT_2 + ", " + UUID1 + ":" + UUID2,
                INSTANT_2 + ", " + INSTANT_3 + ", " + UUID2 + ":" + UUID3,
                INSTANT_3 + ", " + INSTANT_1 + ", ''",
                BEFORE_INSTANT_1 + ", " + AFTER_INSTANT_3 + ", " + UUID1 + ":" + UUID2 + ":" + UUID3,
        })
        @DisplayName("Should return assets when filter by upload date")
        void shouldReturnAssetsWhenFilterByUploadDate(
                String startValue, String endValue, @ColonDelimiterParam List<String> expectedIds) {
            Instant start = startValue == null ? null : Instant.parse(startValue);
            Instant end = startValue == null ? null : Instant.parse(endValue);

            AssetSearchCriteria criteria = new AssetSearchCriteria(start, end, null, null, null);
            assertFindSortsCorrectly(criteria, expectedIds.toArray(new String[0]));
        }

        @ParameterizedTest
        @CsvSource(value = {
                "''",
                "a",
                "..",
                "'       '"
        })
        @DisplayName("Should return all assets when filter by filename with less than three chars or is blank")
        void shouldReturnAssetsWhenFilterByFilenameWithLessThanThreeCharsOrIsBlank(String filenamePattern) {
            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, filenamePattern, null, null);
            assertFindSortsCorrectly(criteria, UUID1, UUID2, UUID3);
        }

        @ParameterizedTest
        @CsvSource(value = {
                "e p, " + UUID2 + ":" + UUID3,
                "'rauw,', " + UUID1,
                "TOKIO, " + UUID2,
                "PaNdA, " + UUID3,
                "PANDA, " + UUID3,
        })
        @DisplayName("Should return assets when filter by filename")
        void shouldReturnAssetsWhenFilterByFilename(
                String filenamePattern, @ColonDelimiterParam List<String> expectedIds) {

            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, filenamePattern, null, null);
            assertFindSortsCorrectly(criteria, expectedIds.toArray(new String[0]));
        }

        @Test
        @DisplayName("Should return empty list when filter by content type does not match completely")
        void shouldReturnEmptyListWhenFilterByContentTypeDoesNotMatchCompletely() {

            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, null,
                    SAMPLE_CONTENT_TYPE.substring(2), null);
            assertFindSortsCorrectly(criteria);
        }

        @Test
        @DisplayName("Should return assets when filter by content type uppercase")
        void shouldReturnAssetsWhenFilterByContentTypeUppercase() {
            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, null,
                    SAMPLE_CONTENT_TYPE.toUpperCase(), null);
            assertFindSortsCorrectly(criteria, UUID1, UUID2);
        }

        @Test
        @DisplayName("Should return assets when filter by content type lowercase")
        void shouldReturnAssetsWhenFilterByContentTypeLowercase() {

            AssetSearchCriteria criteria = new AssetSearchCriteria(null, null, null,
                    SAMPLE_CONTENT_TYPE.toLowerCase(), null);
            assertFindSortsCorrectly(criteria, UUID1, UUID2);
        }

        private void assertFindSortsCorrectly(AssetSearchCriteria criteria, String... orderedUUIDs) {
            // Given
            populateDatabase();

            // When
            List<String> assetIds = adapter.find(criteria).stream().map(asset -> asset.id().value()).toList();

            // Then
            if (orderedUUIDs.length == 0) {
                assertThat(assetIds, emptyIterable());
            } else {
                assertThat(assetIds, contains(orderedUUIDs));
            }
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save an asset when it does not exist in DB")
        void shouldSaveAndAssetWhenSave() {
            // Given
            Asset asset = AssetMother.SAMPLE_ASSET;
            assertThat(jpaRepository.count(), equalTo(0L));

            // When
            Asset savedAsset = adapter.save(asset);

            // Then
            assertThat(jpaRepository.count(), equalTo(1L));
            assertThat(savedAsset, equalTo(asset));
        }

        @Test
        @DisplayName("Should save an asset with lowercase content type")
        void shouldSaveAndAssetWithLowercaseContentType() {
            // Given
            Asset asset = AssetMother.SAMPLE_ASSET.withContentType(new ContentType("UPPERCASE"));
            assertThat(jpaRepository.count(), equalTo(0L));

            // When
            Asset savedAsset = adapter.save(asset);

            // Then
            assertThat(jpaRepository.count(), equalTo(1L));
            assertThat(savedAsset.contentType().value(), equalTo("uppercase"));
            assertThat(jpaRepository.findAll().stream().findFirst().map(AssetEntity::getContentType).orElse(null),
                    equalTo("uppercase"));
        }

        @Test
        @DisplayName("Should throw an exception when save an entity with same external ID")
        void shouldThrowExceptionWhenSaveAnEntityWithSameExternalId() {
            // Given
            populateDatabase();
            Asset assetWithDuplicatedExtId = AssetMother.SAMPLE_ASSET.withId(new AssetId(ENTITY_1.getExternalId()));

            // When
            AssetAlreadyExistsException ex = assertThrows(AssetAlreadyExistsException.class,
                    () -> adapter.save(assetWithDuplicatedExtId));

            // Then
            assertThat(ex.getMessage(), equalTo("Asset with ID '" + ENTITY_1.getExternalId() + "' already exists"));
        }
    }

    private static AssetEntity createEntity1() {
        return AssetEntity.builder()
                .externalId(UUID1)
                .contentType(SAMPLE_CONTENT_TYPE)
                .uploadDate(Instant.parse(INSTANT_1))
                .status(AssetStatus.PUBLISHED)
                .filename("Rauw, my favourite dog")
                .size(3L)
                .build();
    }

    private static AssetEntity createEntity2() {
        return AssetEntity.builder()
                .externalId(UUID2)
                .contentType(SAMPLE_CONTENT_TYPE)
                .uploadDate(Instant.parse(INSTANT_2))
                .status(AssetStatus.PUBLISHED)
                .filename("Nice Pagoda in Tokio")
                .size(5L)
                .build();
    }

    private static AssetEntity createEntity3() {
        return AssetEntity.builder()
                .externalId(UUID3)
                .contentType(ANOTHER_CONTENT_TYPE)
                .uploadDate(Instant.parse(INSTANT_3))
                .status(AssetStatus.PUBLISHED)
                .filename("Awesome Panda photo")
                .size(6L)
                .build();
    }

    private void populateDatabase() {
        // Hint: Entity instances must be created fresh per test to avoid JPA state conflicts
        jpaRepository.save(createEntity1());
        jpaRepository.save(createEntity2());
        jpaRepository.save(createEntity3());
        assertThat(jpaRepository.count(), equalTo(3L));
    }
}