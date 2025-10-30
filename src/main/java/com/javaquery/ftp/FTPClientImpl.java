package com.javaquery.ftp;

import com.javaquery.ftp.exception.FTPException;
import com.javaquery.ftp.io.RemoteFile;
import com.javaquery.util.Is;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author javaquery
 * @since 2025-10-30
 */
public class FTPClientImpl implements FileTransferClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPClientImpl.class);
    private FTPClient ftpClient;

    @Override
    public void connect(Credentials credentials) throws FTPException {
        try {
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(credentials.getConnectTimeout());
            ftpClient.connect(credentials.getHost(), credentials.getPort());
            boolean login = ftpClient.login(credentials.getUsername(), credentials.getPassword());
            if (!login) {
                throw new FTPException("Failed to login to FTP server with provided credentials", null);
            }
            // socket timeout can only be set only after login
            ftpClient.setSoTimeout(credentials.getSocketTimeout());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
                @Override
                public void protocolCommandSent(ProtocolCommandEvent event) {
                    LOGGER.info(event.getMessage());
                }

                @Override
                public void protocolReplyReceived(ProtocolCommandEvent event) {
                    LOGGER.info(event.getMessage());
                }
            });
        } catch (Exception e) {
            throw new FTPException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws FTPException {
        Is.nonNull(ftpClient, () -> {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        });
    }

    @Override
    public List<RemoteFile> listFiles(String directoryPath, FileFilter<RemoteFile> fileFilter) throws FTPException {
        if (Is.nonNull(directoryPath)) {
            List<RemoteFile> result = new ArrayList<>();
            try {
                FTPFile[] files = ftpClient.listFiles(directoryPath);
                for (FTPFile ftpFile : files) {
                    String filepath = directoryPath.endsWith(File.separator) ? directoryPath + ftpFile.getName() : directoryPath + File.separatorChar + ftpFile.getName();

                    RemoteFile remoteFile = RemoteFile.builder()
                            .name(ftpFile.getName())
                            .isFile(ftpFile.isFile())
                            .isDirectory(ftpFile.isDirectory())
                            .size(ftpFile.getSize())
                            .timestamp(ftpFile.getTimestamp())
                            .path(filepath)
                            .build();
                    if (Is.nonNull(fileFilter)) {
                        if (fileFilter.accept(remoteFile)) {
                            result.add(remoteFile);
                        }
                    } else {
                        result.add(remoteFile);
                    }
                }
                return result;
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public boolean uploadFile(String localFilePath, String remoteFilePath) throws FTPException {
        boolean result = false;
        if (Is.nonNullNonEmpty(localFilePath) && Is.nonNullNonEmpty(remoteFilePath)) {
            try {
                File localFile = new File(localFilePath);
                if (localFile.exists() && localFile.isFile()) {
                    try (InputStream inputStream = Files.newInputStream(localFile.toPath())) {
                        result = ftpClient.storeFile(remoteFilePath, inputStream);
                    }
                }
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public boolean downloadFile(String remoteFilePath, String localFilePath) throws FTPException {
        boolean result = false;
        if (Is.nonNullNonEmpty(localFilePath) && Is.nonNullNonEmpty(remoteFilePath)) {
            try {
                File localFile = new File(localFilePath);
                try (InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath)) {
                    if (Is.nonNull(inputStream)) {
                        Files.copy(inputStream, localFile.toPath());
                        result = ftpClient.completePendingCommand();
                    }
                }
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public boolean deleteFile(String remoteFilePath) throws FTPException {
        boolean result = false;
        if (Is.nonNullNonEmpty(remoteFilePath)) {
            try {
                result = ftpClient.deleteFile(remoteFilePath);
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return result;
    }
}
