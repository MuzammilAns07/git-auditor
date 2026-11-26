package com.auditor.model;

import java.util.*;

public record AuditReport(
    String repositoryPath,
    int filesChanged,
    boolean passedSecurityCheck,
    List<SecretFinding> securityAlerts,
    CodeAnalysis analysis
) {}
