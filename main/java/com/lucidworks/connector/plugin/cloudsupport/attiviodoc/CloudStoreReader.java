package com.lucidworks.connector.plugin.cloudsupport.attiviodoc;

import com.attivio.util.StringUtils;
import com.attivio.util.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads a batch of messages the op-prem connector stored in S3
 */
public class CloudStoreReader implements Closeable {
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private Logger logger = LoggerFactory.getLogger(CloudStoreReader.class);

    InputStream is;

    public CloudStoreReader(InputStream is) {
        this.is = is;
    }

    /**
     * Reads the next object
     *
     * @return The next object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        String header = readLine();
        if (StringUtils.isBlank(header)) {
            return null;
        } else if (header.indexOf(CONTENT_LENGTH_HEADER + ":") != 0) {
            logger.warn("Malformed header content {}, attempting to locate next object - some data has "
                    + "likely been corrupted/lost", header);

            while (!StringUtils.isBlank(readLine())) {
            }
            readObject();
        }
        int len = Integer.parseInt(header.split(":")[1]);

        // skip next line to get to the content
        readLine();

        // read the content
        byte[] rawPayload = new byte[len];
        int read = is.read(rawPayload);
        int lenRead = read;

        while (len != lenRead && read != -1) {
            byte[] nextPayload = new byte[len - lenRead];
            read = is.read(nextPayload);
            System.arraycopy(nextPayload, 0, rawPayload, lenRead, read);
            lenRead += read;
        }
        if (read == -1) {
            throw new IOException(
                    "There are not enough bytes left in the stream to satisfy content length.  Data is likely corrupted");
        }
        String xmlPayload = new String(rawPayload, StandardCharsets.UTF_8);
        Object o = XMLSerializer.getInstance().deserialize(xmlPayload);
        return o;
    }

    private String readLine() throws IOException {

        ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
        byte b = (byte) is.read();
        while (b != (byte) '\n' && b != -1) {
            headerBytes.write(b);
            b = (byte) is.read();
        }
        return headerBytes.toString("UTF-8");
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
