package com.auditor.exception;

// public class GitAuditException {
//     public static void main(String[] args) {
        
//     }
// }b 

public class GitAuditException extends RuntimeException{
    public GitAuditException(String message){
        super(message);
    }
    public GitAuditException(String message , Throwable cause){
        super(message ,cause);
    }
}
