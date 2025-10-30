package com.javaquery.ftp;

import com.javaquery.ftp.exception.FTPException;
import com.javaquery.ftp.io.RemoteFile;
import com.javaquery.util.Is;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * @author javaquery
 * @since 2025-10-30
 */
public class SFTPClientImpl implements FileTransferClient {

    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    @Override
    public void connect(Credentials credentials) throws FTPException {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(credentials.getUsername(), credentials.getHost(), credentials.getPort());
            session.setPassword(credentials.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(credentials.getConnectTimeout());
            session.setTimeout(credentials.getSocketTimeout());

            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
        } catch (Exception e) {
            throw new FTPException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws FTPException {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.exit();
            }
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception e) {
            throw new FTPException(e.getMessage(), e);
        }
    }

    @Override
    public List<RemoteFile> listFiles(String directoryPath, FileFilter<RemoteFile> fileFilter) throws FTPException {
        if (Is.nonNull(directoryPath)) {
            List<RemoteFile> result = new ArrayList<>();
            try {
                List<ChannelSftp.LsEntry> files = channelSftp.ls(directoryPath);
                for (ChannelSftp.LsEntry entry : files) {
                    String filepath = directoryPath.endsWith(File.separator) ? directoryPath + entry.getFilename() : directoryPath + File.separatorChar + entry.getFilename();

                    Calendar timestamp = Calendar.getInstance();
                    timestamp.setTimeInMillis(((long) entry.getAttrs().getMTime()) * 1000);

                    RemoteFile remoteFile = RemoteFile.builder()
                            .name(entry.getFilename())
                            .isFile(!entry.getAttrs().isDir())
                            .isDirectory(entry.getAttrs().isDir())
                            .size(entry.getAttrs().getSize())
                            .timestamp(timestamp)
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
                channelSftp.put(localFilePath, remoteFilePath);
                result = true;
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public boolean downloadFile(String remoteFilePath, String localFilePath) throws FTPException {
        boolean result = false;
        if (Is.nonNullNonEmpty(remoteFilePath) && Is.nonNullNonEmpty(localFilePath)) {
            try {
                channelSftp.get(remoteFilePath, localFilePath);
                result = true;
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
                channelSftp.rm(remoteFilePath);
                result = true;
            } catch (Exception e) {
                throw new FTPException(e.getMessage(), e);
            }
        }
        return result;
    }
}
