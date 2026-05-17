---
description: Create a new OpenCode command from a description
---

Create a new OpenCode command in `.opencode/commands/` based on the following description:

$ARGUMENTS

## Steps

1. Determine an appropriate filename for the command (kebab-case, e.g., `my-command.md`)
2. Check if a command with that name already exists in `.opencode/commands/` — if so, suggest an alternative name
3. Create the markdown command file with:
   - Frontmatter containing at minimum a `description` field
   - A well-crafted prompt template that fulfills the requested behavior
   - Use `$ARGUMENTS`, `$1`, `$2`, etc. placeholders if the command should accept arguments
   - Use backtick-wrapped shell commands (e.g., `` !`git diff` ``) if the command needs to inject command output
   - Use `@file` references if specific files should be included in the prompt
4. Show the user the created file path and how to invoke the command (e.g., `/my-command`)

## Examples

### Simple command
```
/create-command "Review all TypeScript files for unused imports"
```
Creates `.opencode/commands/review-ts-imports.md`

### Command with arguments
```
/create-command "Create a command that generates unit tests for a given file path passed as argument"
```
Creates `.opencode/commands/generate-tests.md` using `$1` for the file path

### Command with shell output
```
/create-command "Create a command that reviews the last 5 git commits"
```
Creates a command using `` !`git log --oneline -5` ``
