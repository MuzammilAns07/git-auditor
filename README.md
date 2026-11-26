# Git Security & Code Auditor (Pure Java CLI)

A zero-dependency Java 21 command-line tool that inspects staged Git diffs (`git diff --cached`) to block leaked credentials, detect code smells, and generate structured JSON audit reports before commits enter repository history.

## Features
- **Secret Detection & Masking:** Scans newly added diff lines using pre-compiled Regex patterns for AWS Access Keys (`AKIA...`), GitHub Personal Access Tokens (`ghp_...`), and hardcoded secret assignments, censoring detected values in reports.
- **Static Diff Analysis:** Tracks exact file paths and line numbers from unified diff hunk headers, flags leftover `System.out.println` statements and `TODO` comments, and suggests conventional commit headers (`feat:`, `test:`, `docs:`).
- **Dual Reporting & CI/CD Exit Codes:** Prints a formatted terminal dashboard, exports `audit-report.json` via custom Java Text Block serialization, and returns exit code `1` when security checks fail so Git `pre-commit` hooks or CI pipelines automatically block dirty commits.

## Project Structure
```text
src/com/auditor/
├── Main.java                      # CLI entry point, dashboard printer & exit code handler
├── exception/
│   └── GitAuditException.java     # Custom domain exception for Git/IO errors
├── git/
│   └── GitInspector.java          # Repository validation & ProcessBuilder diff extraction
├── model/
│   ├── AuditReport.java           # Top-level immutable audit record
│   ├── CodeAnalysis.java          # Diff metrics & code warning record
│   ├── SecretFinding.java         # Masked credential alert record
│   └── JsonFormatter.java         # Zero-dependency JSON serializer
└── scanner/
    ├── SecretScanner.java         # Regex secret matching & masking engine
    └── DiffAnalyzer.java          # Unified diff parser & heuristic analyzer