package com.javaquery.ftp;

import com.javaquery.ftp.exception.FTPException;
import com.javaquery.ftp.io.RemoteFile;

import java.util.List;

/**
 * @author javaquery
 * @since 2025-10-30
 * <p>
 * Client for handling FTP, SFTP, and FTPS file transfers using a unified interface.
 */
public class JFTPClient {

    private final FileTransferClient fileTransferClient;

    public JFTPClient(FTPType ftpType) {
        switch (ftpType) {
            case FTP:
                this.fileTransferClient = new FTPClientImpl();
                break;
            case SFTP:
                this.fileTransferClient = new SFTPClientImpl();
                break;
            case FTPS:
                this.fileTransferClient = new FTPSClientImpl();
                break;
            default:
                throw new IllegalArgumentException("Unsupported FTP type: " + ftpType);
        }
    }

    public void connect(Credentials credentials) throws FTPException {
        fileTransferClient.connect(credentials);
    }

    public void disconnect() throws FTPException {
        fileTransferClient.disconnect();
    }

    public List<RemoteFile> listFiles(String directoryPath, FileFilter<RemoteFile> fileFilter) throws FTPException {
        return fileTransferClient.listFiles(directoryPath, fileFilter);
    }

    public boolean uploadFile(String localFilePath, String remoteFilePath) throws FTPException {
        return fileTransferClient.uploadFile(localFilePath, remoteFilePath);
    }

    public boolean downloadFile(String remoteFilePath, String localFilePath) throws FTPException {
        return fileTransferClient.downloadFile(remoteFilePath, localFilePath);
    }

    public boolean deleteFile(String remoteFilePath) throws FTPException {
        return fileTransferClient.deleteFile(remoteFilePath);
    }
}
