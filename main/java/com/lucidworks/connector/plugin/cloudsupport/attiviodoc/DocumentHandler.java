package com.lucidworks.connector.plugin.cloudsupport.attiviodoc;

import com.attivio.sdk.ingest.Commit;
import com.attivio.sdk.ingest.DocumentList;

/**
 * This interface should be implemented by the handler of Attivio documents read from S3.
 */
public interface DocumentHandler {
    public void handle(DocumentList list);

    public void handle(Commit commit);
}
