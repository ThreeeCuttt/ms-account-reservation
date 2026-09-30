package com.example.ms_account_reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "citizenship", length = 100)
    private String citizenship;

    @Column(name = "client_type", length = 50)
    private String clientType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    @Column(name = "document_series", length = 50)
    private String documentSeries;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "mdm_code")
    private Long mdmCode;

    public Client() {
    }

    // пост гет и сет

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCitizenship() { return citizenship; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship; }

    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getDocumentSeries() { return documentSeries; }
    public void setDocumentSeries(String documentSeries) { this.documentSeries = documentSeries; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getMdmCode() { return mdmCode; }
    public void setMdmCode(Long mdmCode) { this.mdmCode = mdmCode; }
}