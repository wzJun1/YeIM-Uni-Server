package cn.wzjun1.yeimServer.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException() {

    }

    public FileUploadException(String message) {
        super(message);
    }
}
