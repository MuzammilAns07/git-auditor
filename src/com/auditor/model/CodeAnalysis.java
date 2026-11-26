package com.auditor.model;

import java.util.List;

public record CodeAnalysis(
    String suggestedCommitHeader,
    int linesAdded,
    int linesRemoved,
    List<String> codeWarnings
) {} 
