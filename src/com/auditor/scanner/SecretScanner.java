package com.auditor.scanner;

import com.auditor.model.SecretFinding;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SecretScanner {

    private static final Map<String, Pattern> RULES = Map.of(
        "AWS Access Key", Pattern.compile("AKIA[0-9A-Z]{16}"),
        "GitHub Token", Pattern.compile("ghp_[A-Za-z0-9]{36}"),
        "Hardcoded Secret", Pattern.compile("(?i)(password|secret|api_key|token)\\s*=\\s*[\"'][^\"']{8,}[\"']")
    );

    public List<SecretFinding> scanLine(String file, int line, String code) {
        return RULES.entrySet().stream()
            .flatMap(e -> e.getValue().matcher(code).results()
                .map(m -> new SecretFinding(file, line, e.getKey(), mask(m.group()))))
            .toList();
    }

    private String mask(String s) {
        return s.length() <= 8 ? "****" : s.substring(0, 4) + "****" + s.substring(s.length() - 4);
    }
}