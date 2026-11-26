package com.auditor.model;

import java.util.stream.Collectors;

public class JsonFormatter {

    public static String toJson(AuditReport report) {
        String alerts = report.securityAlerts().stream()
            .map(a -> """
                    {
                      "file": "%s",
                      "line": %d,
                      "rule": "%s",
                      "snippet": "%s"
                    }""".formatted(esc(a.filePath()), a.lineNumber(), esc(a.ruleName()), esc(a.maskedSnippet())))
            .collect(Collectors.joining(",\n"));

        String warnings = report.analysis().codeWarnings().stream()
            .map(w -> "\"" + esc(w) + "\"")
            .collect(Collectors.joining(", "));

        return """
            {
              "repositoryPath": "%s",
              "filesChanged": %d,
              "passedSecurityCheck": %b,
              "securityAlerts": [%s],
              "analysis": {
                "suggestedCommitHeader": "%s",
                "linesAdded": %d,
                "linesRemoved": %d,
                "codeWarnings": [%s]
              }
            }
            """.formatted(
                esc(report.repositoryPath()),
                report.filesChanged(),
                report.passedSecurityCheck(),
                alerts.isEmpty() ? "" : "\n" + alerts + "\n  ",
                esc(report.analysis().suggestedCommitHeader()),
                report.analysis().linesAdded(),
                report.analysis().linesRemoved(),
                warnings
            );
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}