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
 */
public class JFTPClientTest {

    private FakeFtpServer fakeFtpServer;

    @BeforeEach
    public void setup() {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "pass1word", "/data-jftp"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data-jftp"));
        fileSystem.add(new FileEntry("/data-jftp/foobar.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.start();
    }

    @Test
    void connectFTPClient_success() {
        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        jftpClient.connect(credentials);
        jftpClient.disconnect();
    }

    @Test
    void connect_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("invalidUser")
                .password("invalidPassword")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        assertThrows(FTPException.class, () -> jftpClient.connect(credentials));
    }

    @Test
    void disconnect_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        jftpClient.disconnect();
    }

    @Test
    void listFiles_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        List<RemoteFile> files = jftpClient.listFiles("/data-jftp", null);
        assertFalse(files.isEmpty());
        jftpClient.disconnect();
    }

    @Test
    void listFilesWithFilter_success() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        List<RemoteFile> files = jftpClient.listFiles("/data-jftp", remoteFile -> remoteFile.getName().endsWith(".pdf"));
        assertTrue(files.isEmpty());
        jftpClient.disconnect();
    }

    @Test
    void listFilesWithoutConnect_failure() {
        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        assertThrows(FTPException.class, () -> jftpClient.listFiles("/data-jftp", null));
    }

    @Test
    void listFilesWithNullDirectoryPath_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        List<RemoteFile> files = jftpClient.listFiles(null, null);
        assertNull(files);
        jftpClient.disconnect();
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
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);

        jftpClient.uploadFile(file.getAbsolutePath(), "/data-jftp/newfile.json");
        List<RemoteFile> files = jftpClient.listFiles("/data-jftp", remoteFile -> remoteFile.getName().equals("newfile.json"));
        assertFalse(files.isEmpty());
        jftpClient.disconnect();
    }

    @Test
    void uploadFileNotFound_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        assertFalse(jftpClient.uploadFile("/invalid/path/to/file.json", "/data-jftp/newfile.json"));
        jftpClient.disconnect();
    }

    @Test
    void uploadFileWithoutConnect_failure() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".json";
        File file = File.createTempFile(fileNamePrefix, fileNameSuffix);
        Files.writeToFile(file, "{\"key\":\"value\"}");

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        assertThrows(FTPException.class, () -> jftpClient.uploadFile(file.getAbsolutePath(), "/data-jftp/newfile.json"));
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
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);

        jftpClient.downloadFile("/data-jftp/foobar.txt", downloadPath);
        String content = Files.readFromFile(new File(downloadPath));
        assertEquals("abcdef 1234567890", content);
        jftpClient.disconnect();
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
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);

        assertThrows(FTPException.class, () -> jftpClient.downloadFile("/data-jftp/foobar.txt", downloadFile.getAbsolutePath()));
        jftpClient.disconnect();
    }

    @Test
    void downloadFileWithoutConnect_failure() throws IOException {
        String fileNamePrefix = UUID.randomUUID().toString();
        String fileNameSuffix = ".txt";
        File downloadFile = File.createTempFile(fileNamePrefix, fileNameSuffix);

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        assertThrows(FTPException.class, () -> jftpClient.downloadFile("/data-jftp/foobar.txt", downloadFile.getAbsolutePath()));
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
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);

        jftpClient.uploadFile(file.getAbsolutePath(), "/data-jftp/" + fileName);
        boolean deleted = jftpClient.deleteFile("/data-jftp/" + fileName);
        assertTrue(deleted);
        jftpClient.disconnect();
    }

    @Test
    void deleteFileWithoutConnect_failure() {
        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        assertThrows(FTPException.class, () -> jftpClient.deleteFile("/data-jftp/foobar.txt"));
    }

    @Test
    void deleteNonExistingFile_failure() {
        Credentials credentials = Credentials.builder()
                .host("localhost")
                .port(fakeFtpServer.getServerControlPort())
                .username("user")
                .password("pass1word")
                .build();

        JFTPClient jftpClient = new JFTPClient(FTPType.FTP);
        jftpClient.connect(credentials);
        boolean deleted = jftpClient.deleteFile("/data-jftp/nonexistingfile.txt");
        assertFalse(deleted);
        jftpClient.disconnect();
    }

    @AfterEach
    public void teardown() {
        fakeFtpServer.stop();
    }
}
