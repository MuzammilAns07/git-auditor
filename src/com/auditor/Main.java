package com.auditor;

import com.auditor.exception.GitAuditException;
import com.auditor.git.GitInspector;
import com.auditor.model.AuditReport;
import com.auditor.model.JsonFormatter;
import com.auditor.scanner.DiffAnalyzer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        String targetPath = args.length > 0 ? args[0] : ".";

        try {
            GitInspector inspector = new GitInspector();
            Path repoPath = inspector.validateRepository(targetPath);
            String rawDiff = inspector.extractDiff(repoPath);

            DiffAnalyzer analyzer = new DiffAnalyzer();
            AuditReport report = analyzer.analyze(repoPath.toString(), rawDiff);

            Files.writeString(Path.of("audit-report.json"), JsonFormatter.toJson(report));
            printDashboard(report);

            if (!report.passedSecurityCheck()) {
                System.exit(1);
            }
        } catch (GitAuditException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(2);
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected failure: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void printDashboard(AuditReport report) {
        String status = report.passedSecurityCheck() ? "PASSED" : "FAILED (Commit Blocked)";
        var a = report.analysis();

        System.out.println("==================================================");
        System.out.println(" GIT SECURITY & CODE AUDIT");
        System.out.println("==================================================");
        System.out.println(" Status        : " + status);
        System.out.println(" Repository    : " + report.repositoryPath());
        System.out.println(" Files Changed : " + report.filesChanged() + " (+" + a.linesAdded() + " / -" + a.linesRemoved() + " lines)");
        System.out.println(" Commit Header : " + a.suggestedCommitHeader());
        System.out.println("--------------------------------------------------");

        System.out.println(" SECURITY ALERTS (" + report.securityAlerts().size() + ")");
        if (report.securityAlerts().isEmpty()) {
            System.out.println("   None detected.");
        } else {
            report.securityAlerts().forEach(alert ->
                System.out.printf("   [!] %-16s | %s:%d -> %s%n",
                    alert.ruleName(), alert.filePath(), alert.lineNumber(), alert.maskedSnippet())
            );
        }

        System.out.println("--------------------------------------------------");
        System.out.println(" CODE WARNINGS (" + a.codeWarnings().size() + ")");
        if (a.codeWarnings().isEmpty()) {
            System.out.println("   None detected.");
        } else {
            a.codeWarnings().forEach(w -> System.out.println("   [-] " + w));
        }

        System.out.println("==================================================");
        System.out.println(" Full JSON saved to: audit-report.json");
    }
}