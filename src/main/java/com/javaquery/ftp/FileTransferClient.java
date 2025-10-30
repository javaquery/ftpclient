package com.javaquery.ftp;

import com.javaquery.ftp.exception.FTPException;
import com.javaquery.ftp.io.RemoteFile;

import java.util.List;

/**
 * @author javaquery
 * @since 2025-10-30
 */
public interface FileTransferClient {
    void connect(Credentials credentials) throws FTPException;

    void disconnect() throws FTPException;

    List<RemoteFile> listFiles(String directoryPath, FileFilter<RemoteFile> fileFilter) throws FTPException;

    boolean uploadFile(String localFilePath, String remoteFilePath) throws FTPException;

    boolean downloadFile(String remoteFilePath, String localFilePath) throws FTPException;

    boolean deleteFile(String remoteFilePath) throws FTPException;
}
