package org.jsweet.cli.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class JavaSourceFile {
    private final String filePath;
    private final String fileName;
    private String packageName;
    private String className;
    private String content;
    private Instant lastModified;
    private long size;

    public JavaSourceFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }
        
        this.filePath = path.toAbsolutePath().toString();
        this.fileName = path.getFileName().toString();
        
        if (!this.fileName.endsWith(".java")) {
            throw new IllegalArgumentException("File must have .java extension: " + fileName);
        }
        
        loadFileContent();
        extractMetadata();
    }
    
    private void loadFileContent() throws IOException {
        Path path = Paths.get(filePath);
        this.content = Files.readString(path);
        this.size = Files.size(path);
        this.lastModified = Files.getLastModifiedTime(path).toInstant();
    }
    
    private void extractMetadata() {
        // Extract package name
        String packagePattern = "package\\s+([a-zA-Z][a-zA-Z0-9_.]*);";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(packagePattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            this.packageName = matcher.group(1);
        } else {
            this.packageName = ""; // Default package
        }
        
        // Extract class name from filename (Java convention)
        this.className = fileName.substring(0, fileName.length() - 5); // Remove .java extension
    }
    
    // Getters
    public String getFilePath() {
        return filePath;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getContent() {
        return content;
    }
    
    public Instant getLastModified() {
        return lastModified;
    }
    
    public long getSize() {
        return size;
    }
    
    @Override
    public String toString() {
        return String.format("JavaSourceFile{fileName='%s', className='%s', packageName='%s'}", 
                           fileName, className, packageName);
    }
}