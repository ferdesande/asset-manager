package org.fsg.assetmanager.infrastructure.adapter.out.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.fsg.assetmanager.domain.model.AssetStatus;

import java.time.Instant;

@Entity
@Table(name = "assets", indexes = {
        @Index(name = "idx_external_id", columnList = "external_id", unique = true),
        @Index(name = "idx_upload_date", columnList = "upload_date"),
        @Index(name = "idx_content_type", columnList = "content_type")
})
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String externalId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private Instant uploadDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AssetStatus status;

    @Column
    private String url;

    public AssetEntity(String externalId, String filename, String contentType, String url, Long size,
                       Instant uploadDate) {
        this.externalId = externalId;
        this.filename = filename;
        this.contentType = contentType;
        this.url = url;
        this.size = size;
        this.uploadDate = uploadDate;
    }
}
