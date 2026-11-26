package com.auditor.model;

public record SecretFinding(
    String filePath,
    int lineNumber,
    String ruleName,
    String maskedSnippet
){}
