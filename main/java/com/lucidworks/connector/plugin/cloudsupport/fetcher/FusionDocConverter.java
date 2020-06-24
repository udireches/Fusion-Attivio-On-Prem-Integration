package com.lucidworks.connector.plugin.cloudsupport.fetcher;

import com.attivio.sdk.ingest.*;
import com.lucidworks.connector.plugin.cloudsupport.attiviodoc.DocumentHandler;
import com.lucidworks.connector.plugin.cloudsupport.attiviodoc.S3ContentPointer;
import com.lucidworks.connector.plugin.cloudsupport.fetcher.aws.AwsClientFactory;
import com.lucidworks.connector.plugin.cloudsupport.fetcher.aws.AwsConfiguration;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher.FetchContext;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.TreeMap;

//TODO: Convert more types to Fusion rather than doing it in the pipeline
//TODO: Multi value support
//TODO: Document deletion and partila update
public class FusionDocConverter implements DocumentHandler {
    private static final String ON_PREM_CONNECTOR_NAME_FIELD = "on_prem_connector_name_s";
    private final Logger logger;
    private final FetchContext fetchContext;
    private final AwsConfiguration awsConfiguration;
    private final AwsClientFactory clientFactory;

    public FusionDocConverter(FetchContext fetchContext, Logger logger, AwsConfiguration awsConfiguration, AwsClientFactory clientFactory) {

        this.fetchContext = fetchContext;
        this.logger = logger;
        this.awsConfiguration = awsConfiguration;
        this.clientFactory = clientFactory;
    }

    @Override
    public void handle(DocumentList documentList) {
        documentList.forEach(this::createFusionDocument);
    }

    @Override
    public void handle(Commit commit) {

    }

    private void createFusionDocument(IngestDocument attivioDoc) {
        ContentAndFields contentAndFields = convertToFusion(attivioDoc);
        logger.trace("Handling Attivio document {}", attivioDoc);
        if (contentAndFields.getContentStream() == null) {
            fetchContext.newDocument(attivioDoc.getId()).fields(f -> f.merge(contentAndFields)).emit();
            logger.info("Created document {}", attivioDoc.getId());
        } else {
            fetchContext.newContent(attivioDoc.getId(), contentAndFields::getContentStream).fields(mb -> mb.merge(contentAndFields)).emit();
            logger.info("Created document {} with content", attivioDoc.getId());
        }
    }

    private ContentAndFields convertToFusion(IngestDocument attivioDoc) {
        ContentAndFields map = new ContentAndFields();
        attivioDoc.forEach(ifld -> map.put(ifld.getName(), convert(ifld)));
        map.put(ON_PREM_CONNECTOR_NAME_FIELD, awsConfiguration.getConnectorName());
        return map;
    }


    private Object convert(IngestField ifld) {
        try {
            IngestFieldValue ifv = ifld.getFirstValue();
            if (ifv == null)
                return null;
            Object val = ifv.getValue();
            if (val == null)
                return null;
            if (val instanceof ByteArrayContentPointer)
                return ((ByteArrayContentPointer) val).getStream();
            else if (val instanceof ContentPointer)
                return S3ContentPointer.S3CPFromSDKCP(clientFactory.getAmazonS3(), (ContentPointer) val, awsConfiguration.getRegion());
            else return val.toString();
        } catch (Throwable t) {
            return logConversionError(String.format("Conversion of %s failed : %s", ifld.getFirstValue().getValue().toString(), t.toString()));
        }
    }


    private String logConversionError(String errorMessage) {
        logger.error(errorMessage);
        return errorMessage;
    }

    private class ContentAndFields extends TreeMap<String, Object> {

        private InputStream contentStream;

        @Override
        public Object put(String key, Object value) {
            if (value instanceof InputStream) {
                contentStream = (InputStream) value;
                return null;
            } else if (value instanceof S3ContentPointer) {
                generateS3Content(key, (S3ContentPointer) value);
                return null;
            } else return super.put(key, value);
        }

        private void generateS3Content(String key, S3ContentPointer cp) {
            try {
                contentStream = cp.getStream();
            } catch (Throwable t) {
                String errorMessage = String.format("Failed to get S3 content from pointer %s: %s", cp.toString(), t.toString());
                super.put(key, errorMessage);
                logConversionError(errorMessage);
            }
            if (contentStream == null) {
                String errorMessage = String.format("Couldn't find the S3 object for %s", cp.toString());
                super.put(key, errorMessage);
                logConversionError(errorMessage);
            }
        }

        public InputStream getContentStream() {
            return contentStream;
        }


    }
}
