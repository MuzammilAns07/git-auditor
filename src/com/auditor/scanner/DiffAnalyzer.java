package com.auditor.scanner;

import com.auditor.model.*;
import java.util.*;
import java.util.regex.*;

public class DiffAnalyzer {

    private static final Pattern LINE_START = Pattern.compile("\\+(\\d+)");
    private final SecretScanner scanner = new SecretScanner();

    public AuditReport analyze(String repoPath, String rawDiff) {
        Set<String> files = new LinkedHashSet<>();
        List<SecretFinding> alerts = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int added = 0, removed = 0, lineNum = 0;
        String file = "unknown";

        for (String line : rawDiff.split("\\R")) {
            if (line.startsWith("+++ b/")) {
                file = line.substring(6);
                files.add(file);
            } else if (line.startsWith("@@")) {
                Matcher m = LINE_START.matcher(line);
                if (m.find()) lineNum = Integer.parseInt(m.group(1)) - 1;
            } else if (line.startsWith("+")) {
                lineNum++;
                added++;
                String code = line.substring(1);
                alerts.addAll(scanner.scanLine(file, lineNum, code));
                if (code.contains("System.out.println")) warnings.add(file + ":" + lineNum + " -> Leftover print statement");
                if (code.contains("TODO")) warnings.add(file + ":" + lineNum + " -> Unresolved TODO comment");
            } else if (line.startsWith("-")) {
                removed++;
            } else if (line.startsWith(" ")) {
                lineNum++;
            }
        }

        CodeAnalysis analysis = new CodeAnalysis(suggestCommit(files), added, removed, warnings);
        return new AuditReport(repoPath, files.size(), alerts.isEmpty(), alerts, analysis);
    }

    private String suggestCommit(Set<String> files) {
        if (files.isEmpty()) return "chore: no file changes detected";
        if (files.stream().allMatch(f -> f.toLowerCase().contains("test"))) return "test: update " + files.size() + " file(s)";
        if (files.stream().allMatch(f -> f.endsWith(".md"))) return "docs: update " + files.size() + " file(s)";
        return "feat: update " + files.size() + " file(s)";
    }
}