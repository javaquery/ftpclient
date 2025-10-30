# FTP Client Library

A lightweight and easy-to-use Java FTP client library that provides a unified interface for FTP, FTPS, and SFTP file transfers.

## Features

- **Unified Interface**: Single API for FTP, FTPS, and SFTP protocols
- **Simple Integration**: Easy to integrate with minimal configuration
- **File Operations**: Upload, download, delete, and list files
- **File Filtering**: Filter files based on custom criteria
- **Timeout Configuration**: Configurable connection and socket timeouts
- **Secure Connections**: Support for FTPS (FTP over SSL/TLS) and SFTP (SSH File Transfer Protocol)

## Dependencies

This library uses the following dependencies:
- Apache Commons Net (for FTP/FTPS)
- JSch (for SFTP)
- Lombok (for cleaner code)
- SLF4J (for logging)

## Installation

### Gradle

```gradle
dependencies {
    implementation 'com.javaquery:ftpclient:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.javaquery</groupId>
    <artifactId>ftpclient</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

```java
import com.javaquery.ftp.Credentials;
import com.javaquery.ftp.FTPType;
import com.javaquery.ftp.JFTPClient;

public class FTPExample {
    public static void main(String[] args) {
        // Create credentials
        Credentials credentials = Credentials.builder()
                .host("ftp.example.com")
                .port(21)
                .username("your-username")
                .password("your-password")
                .build();

        // Create FTP client (use FTPType.FTPS or FTPType.SFTP for secure connections)
        JFTPClient ftpClient = new JFTPClient(FTPType.FTP);
        
        try {
            // Connect to server
            ftpClient.connect(credentials);
            System.out.println("Connected successfully!");
            
            // Perform file operations here
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Disconnect
            ftpClient.disconnect();
        }
    }
}
```

## API Reference

### JFTPClient

Main client class for FTP operations.

#### Constructor
- `JFTPClient(FTPType ftpType)` - Creates a new client instance for the specified protocol type

#### Methods
- `void connect(Credentials credentials)` - Establishes connection to the FTP server
- `void disconnect()` - Closes the connection to the FTP server
- `List<RemoteFile> listFiles(String directoryPath, FileFilter<RemoteFile> fileFilter)` - Lists files in the specified directory with optional filtering
- `boolean uploadFile(String localFilePath, String remoteFilePath)` - Uploads a file to the server
- `boolean downloadFile(String remoteFilePath, String localFilePath)` - Downloads a file from the server
- `boolean deleteFile(String remoteFilePath)` - Deletes a file from the server

### FTPType

Enum for specifying the protocol type.

**Values:**
- `FTP` - Standard FTP protocol
- `FTPS` - FTP over SSL/TLS
- `SFTP` - SSH File Transfer Protocol

### Credentials

Builder class for connection credentials.

**Properties:**
- `host` - FTP server hostname or IP address
- `port` - FTP server port (default: 21 for FTP/FTPS, 22 for SFTP)
- `username` - Username for authentication
- `password` - Password for authentication
- `connectTimeout` - Connection timeout in milliseconds (default: 15000)
- `socketTimeout` - Socket timeout in milliseconds (default: 60000)
- `isImplicit` - Use implicit FTPS mode (default: false)

### RemoteFile

Represents a file or directory on the remote server.

**Properties:**
- `name` - File or directory name
- `isFile` - Whether this is a file
- `isDirectory` - Whether this is a directory
- `size` - File size in bytes
- `timestamp` - Last modified timestamp
- `path` - Full path on the server

### FileFilter

Functional interface for filtering files.

**Method:**
- `boolean accept(RemoteFile file)` - Returns true if the file should be included in the results

## Error Handling

The library throws `FTPException` for all FTP-related errors. Always wrap operations in try-catch blocks:

```java
try {
    ftpClient.connect(credentials);
    // Perform operations
} catch (FTPException e) {
    System.err.println("FTP Error: " + e.getMessage());
    e.printStackTrace();
} finally {
    ftpClient.disconnect();
}
```

## Best Practices

1. **Always disconnect**: Use try-finally blocks to ensure disconnection
2. **Configure timeouts**: Set appropriate timeout values based on your network conditions
3. **Handle exceptions**: Properly handle `FTPException` in your code
4. **Use filters**: Leverage `FileFilter` for efficient file listing
5. **Validate paths**: Ensure file paths are valid before operations
6. **Logging**: Configure SLF4J for proper logging support

## Requirements

- Java 8 or higher
- Internet connectivity to FTP server
