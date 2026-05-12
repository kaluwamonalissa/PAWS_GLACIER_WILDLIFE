# AGENTS.md

This guide helps AI coding agents understand this codebase's essential patterns, workflows, and architecture.

## Development Standards for AI Agents

**⚠️ MANDATORY FOR ALL AI AGENTS ⚠️**

AI agents working in this codebase must follow **ALL human developer standards** documented in `non-interactive/api/CONTRIBUTING.md`, **PLUS** additional BOT-specific rules. Every guideline that applies to human developers also applies to AI-generated code.

### Core Principle

**All AI-generated code must be indistinguishable from high-quality human-written code.** This means following every standard, convention, and best practice in the CONTRIBUTING.md file without exception.

### Required Reading from CONTRIBUTING.md

Before making any code changes, AI agents must understand these sections:

1. **Coding Standards** - Java code style, naming conventions, documentation requirements
2. **Testing Standards** - Test coverage requirements (≥90%), test naming patterns, mock usage
3. **Build and Quality Gates** - Gradle commands, Spotless formatting, JaCoCo coverage
4. **Commit Conventions** - Conventional Commits format for all commits
5. **Pull Requests** - PR description requirements, review process
6. **For BOTS** - Additional AI-specific rules (summarized below)

**Read the full document**: `non-interactive/api/CONTRIBUTING.md`

### Additional BOT-Specific Rules

These rules are **in addition to** all human developer standards:

#### ✅ Pre-commit Hooks Run Automatically
After pre-commit hooks are installed in `non-interactive/api` (via `pre-commit install`), each commit from that module triggers automated checks (configured in `non-interactive/api/.pre-commit-config.yaml`):
- **Spotless Java Formatting** - Auto-formats `.java` files
- **TruffleHog Secret Scanning** - Detects leaked secrets/credentials (blocks commits with secrets)
- **Trailing Whitespace** - Removes trailing spaces
- **YAML/JSON Validation** - Syntax checking
- **No Commit to Main** - Prevents direct commits to `main` branch

**AI agents must ensure code passes these checks.** Run manually from `non-interactive/api`: `pre-commit run --all-files`

#### ❌ NEVER Use Fully Qualified Class Names
Always use proper imports instead of inline fully qualified names.

```java
// ❌ BAD: uk.co.sainsburys.digital_screens.api.service.BroadSignApiService
// ✅ GOOD: import uk.co.sainsburys.digital_screens.api.service.BroadSignApiService;
```

#### ❌ DO NOT Create Unsolicited Documentation Files
Never create summary files unless explicitly requested:
- No `CHANGES_SUMMARY.md`, `REFACTORING_NOTES.md`, `TEST_RESULTS.md`, etc.
- Output results in chat instead

#### ✅ All Method Parameters Must Be `final`
```java
// ✅ GOOD
public void processData(final String input, final int count) { }

// ❌ BAD
public void processData(String input, int count) { }
```

#### ✅ Use Descriptive Test Names
Follow the pattern: `givenCondition_andOtherCondition_shouldExpectedBehavior()`

```java
@Test
void givenValidMacAddress_andActiveStore_shouldRegisterSuccessfully() { }
```

#### ✅ Extract String Literals to Constants
No magic strings - extract to well-named constants.

```java
private static final String REPORTS_BASE_PATH = "/api/v1/reports";
```

#### ✅ Validate All Changes
Before presenting code:
1. ✓ Verify compilation succeeds
2. ✓ Run tests: `./app test`
3. ✓ Check Spotless formatting
4. ✓ Confirm ≥90% coverage maintained
5. ✓ Review against existing patterns


### Workflow for AI Code Changes

1. **Understand context** - Read relevant existing code and documentation
2. **Follow patterns** - Match the style and structure of surrounding code
3. **Make targeted changes** - Avoid unnecessary refactoring
4. **Write tests** - Generate comprehensive test cases with ≥90% coverage
5. **Validate thoroughly** - Run build, tests, and formatting checks
6. **Check pre-commit hooks** - Ensure code passes TruffleHog secret scanning and all validators
7. **Communicate clearly** - Explain changes concisely

**Commit message requirements**: Follow Conventional Commits format (validated by Dangerfile):
```bash
feat(pop): add campaign-level proof of play reports
fix(soti): handle special characters in passwords
refactor(api): extract time filtering to DateTimeUtils
```

**Complete guidelines**: `CONTRIBUTING.md` (sections: Coding Standards, Testing Standards, For BOTS)