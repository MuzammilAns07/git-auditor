package com.auditor.git;

import com.auditor.exception.GitAuditException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class GitInspector {

    public Path validateRepository(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new GitAuditException("Repository path cannot be empty.");
        }
        Path repoPath = Path.of(rawPath).toAbsolutePath().normalize();
        if (!Files.isDirectory(repoPath) || !Files.exists(repoPath.resolve(".git"))) {
            throw new GitAuditException("Invalid Git repository: " + repoPath);
        }
        return repoPath;
    }

    public String extractDiff(Path repoPath) {
        String staged = runGitCommand(repoPath, "git", "diff", "--cached");
        return staged.isBlank() ? runGitCommand(repoPath, "git", "diff") : staged;
    }

    private String runGitCommand(Path repoPath, String... command) {
        try {
            Process process = new ProcessBuilder(command)
                .directory(repoPath.toFile())
                .redirectErrorStream(true)
                .start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String output = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new GitAuditException("Git command failed: " + output);
                }
                return output;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitAuditException("Failed to execute git command", e);
        }
    }
}