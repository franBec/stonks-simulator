# Agent Context

## Project Overview

- This is a COBOL-Powered Meme Stock Trading Simulator with AI-Driven Chaos
- In README.md there's a first draft of what this project is looking for. Is important to understand that is a ROUGH DRAFT and not ultimate truth decisions. During development, tech choices might change

## Execution Environment

- You are running on a "OS: NixOS 25.11 (Xantusia) x86_64" "Kernel: Linux 6.12.81" "Shell: zsh 5.9". For more information, ask for permision for checking `/home/pollito/nixos-dotfiles`
- The environment is running **BusyBox**, which provides a lightweight version of common Unix tools.
- The `pgrep` command is a "stripped down" version and **does not support the `-g` flag**.

## Diagnostics

- LSP tends to show stale diagnostics from previous versions of a file. When you see an error that doesn't make sense, re-read the current state of the file from disk to verify correctness.
