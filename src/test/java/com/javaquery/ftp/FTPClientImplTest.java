package com.javaquery.ftp;

import com.javaquery.ftp.exception.FTPException;
import com.javaquery.ftp.io.RemoteFile;
import com.javaquery.util.io.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author javaquery
 * @since 2025-10-30
 * <a href="https://mockftpserver.org/">mockftpserver</a>
 * NOTE: The MockFtpServer project does not support FTPS or SFTP.
 */
public class FTPClientImplTest {

    private FakeFtpServer fakeFtpServer;

    @BeforeEach
    public void setup() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/data"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data"));
        fileSystem.add(new FileEntry("/data/foobar.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.start();
    }

    @Test
    void connect_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        ftpClient.disconnect();
    }

    @Test
    void connect_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("invalidUser")
                .password("invalidPassword")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        assertThrows(FTPException.class, () -> ftpClient.connect(credentials));
    }

    @Test
    void disconnect_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        ftpClient.disconnect();
    }

    @Test
    void listFiles_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        List<RemoteFile> files = ftpClient.listFiles("/data", null);
        assertFalse(files.isEmpty());
        ftpClient.disconnect();
    }

    @Test
    void listFilesWithFilter_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        List<RemoteFile> files = ftpClient.listFiles("/data", remoteFile -> remoteFile.getName().endsWith(".pdf"));
        assertTrue(files.isEmpty());
        ftpClient.disconnect();
    }

    @Test
    void listFilesWithoutConnect_failure() {
        FTPClientImpl ftpClient = new FTPClientImpl();
        assertThrows(FTPException.class, () -> ftpClient.listFiles("/data", null));
    }

    @Test
    void listFilesWithNullDirectoryPath_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        List<RemoteFile> files = ftpClient.listFiles(null, null);
        assertNull(files);
        ftpClient.disconnect();
    }


    @Test
    void uploadFile_success() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".json";
        File file = File.createTempFile(fileNamePrefix, fileNameSuffix);
        Files.writeToFile(file, "{\"key\":\"value\"}");

        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);

        ftpClient.uploadFile(file.getAbsolutePath(), "/data/newfile.json");
        List<RemoteFile> files = ftpClient.listFiles("/data", remoteFile -> remoteFile.getName().equals("newfile.json"));
        assertFalse(files.isEmpty());
        ftpClient.disconnect();
    }

    @Test
    void uploadFileNotFound_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        assertFalse(ftpClient.uploadFile("/invalid/path/to/file.json", "/data/newfile.json"));
        ftpClient.disconnect();
    }

    @Test
    void uploadFileWithoutConnect_failure() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".json";
        File file = File.createTempFile(fileNamePrefix, fileNameSuffix);
        Files.writeToFile(file, "{\"key\":\"value\"}");

        FTPClientImpl ftpClient = new FTPClientImpl();
        assertThrows(FTPException.class, () -> ftpClient.uploadFile(file.getAbsolutePath(), "/data/newfile.json"));
    }

    @Test
    void downloadCreateFile_success() {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".txt";
        String downloadPath = Files.SYSTEM_TMP_DIR + File.separator + fileNamePrefix + fileNameSuffix;

        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);

        ftpClient.downloadFile("/data/foobar.txt", downloadPath);
        String content = Files.readFromFile(new File(downloadPath));
        assertEquals("abcdef 1234567890", content);
        ftpClient.disconnect();
    }

    @Test
    void downloadFileAlreadyExist_failure() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".txt";
        File downloadFile = File.createTempFile(fileNamePrefix, fileNameSuffix);

        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);

        assertThrows(FTPException.class, () -> ftpClient.downloadFile("/data/foobar.txt", downloadFile.getAbsolutePath()));
        ftpClient.disconnect();
    }

    @Test
    void downloadFileWithoutConnect_failure() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".txt";
        File downloadFile = File.createTempFile(fileNamePrefix, fileNameSuffix);

        FTPClientImpl ftpClient = new FTPClientImpl();
        assertThrows(FTPException.class, () -> ftpClient.downloadFile("/data/foobar.txt", downloadFile.getAbsolutePath()));
    }

    @Test
    void deleteFile_success() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".json";
        String fileName = fileNamePrefix + fileNameSuffix;
        File file = File.createTempFile(fileNamePrefix, fileNameSuffix);
        Files.writeToFile(file, "{\"key\":\"value\"}");

        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);

        ftpClient.uploadFile(file.getAbsolutePath(), "/data/" + fileName);
        boolean deleted = ftpClient.deleteFile("/data/" + fileName);
        assertTrue(deleted);
        ftpClient.disconnect();
    }

    @Test
    void deleteFileWithoutConnect_failure() {
        FTPClientImpl ftpClient = new FTPClientImpl();
        assertThrows(FTPException.class, () -> ftpClient.deleteFile("/data/foobar.txt"));
    }

    @Test
    void deleteNonExistingFile_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("password")
                .build();

        FTPClientImpl ftpClient = new FTPClientImpl();
        ftpClient.connect(credentials);
        boolean deleted = ftpClient.deleteFile("/data/nonexistingfile.txt");
        assertFalse(deleted);
        ftpClient.disconnect();
    }

    @AfterEach
    public void teardown() {
        fakeFtpServer.stop();
    }
}
