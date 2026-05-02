#!/bin/sh
set -e

COBOL_SRC="src"
COPYBOOK_DIR="src/copybooks"
ERRORS=0

red()  { printf '\033[1;31m%s\033[0m\n' "$1"; }
yel()  { printf '\033[1;33m%s\033[0m\n' "$1"; }
grn()  { printf '\033[1;32m%s\033[0m\n' "$1"; }
fail() { red "FAIL: $1"; ERRORS=$((ERRORS + 1)); }
warn() { yel "WARN: $1"; }

for file in $(find "$COBOL_SRC" -name '*.cob' -o -name '*.cpy' | sort); do
    basename=$(basename "$file")
    linenum=0

    while IFS= read -r line || [ -n "$line" ]; do
        linenum=$((linenum + 1))
        len=${#line}

        if [ "$len" -eq 0 ]; then
            continue
        fi

        if [ "$len" -ge 7 ]; then
            col7=$(printf '%s' "$line" | cut -c7)
            case "$col7" in
                '*'|'/'|'-'|' ') ;;
                *)  fail "$file:$linenum: column 7 indicator must be '*', '/', '-', or space, got '$col7'" ;;
            esac
        else
            if [ "$len" -ge 1 ]; then
                first=$(printf '%s' "$line" | cut -c1)
                if [ "$first" != " " ]; then
                    fail "$file:$linenum: line shorter than 7 chars but starts with '$first', not a space"
                fi
            fi
        fi

        if [ "$len" -gt 80 ]; then
            fail "$file:$linenum: line exceeds 80 columns ($len chars)"
        fi
    done < "$file"

    if [ "$(tail -c 1 "$file" | wc -l)" -eq 0 ]; then
        fail "$file: missing trailing newline"
    fi

    case "$file" in
        *.cpy) maxlen=8 ;;
        *)     maxlen=30 ;;
    esac
    namelen=${#basename}
    extlen=4
    basenamelen=$((namelen - extlen))
    if [ "$basenamelen" -gt "$maxlen" ]; then
        fail "$file: filename base exceeds $maxlen characters (IBM COBOL COPY name limit)"
    fi
done

for file in $(find "$COBOL_SRC" -name '*.cob' | sort); do
    linenum=0
    while IFS= read -r line || [ -n "$line" ]; do
        linenum=$((linenum + 1))
        case "$line" in
            *'*> '*|*'*>')
                fail "$file:$linenum: free-format comment '*>' found; use fixed-format '*' in column 7 instead"
                ;;
        esac

        case "$line" in
            COPY\ *)
                cpyname=$(printf '%s' "$line" | sed 's/.*COPY[ ]*\([^ .]*\).*/\1/' | tr 'A-Z' 'a-z')
                if [ -n "$cpyname" ]; then
                    found=0
                    for ext in .cpy .copy .CPY .COPY; do
                        if [ -f "$COPYBOOK_DIR/${cpyname}${ext}" ]; then
                            found=1
                            break
                        fi
                    done
                    if [ "$found" -eq 0 ]; then
                        fail "$file:$linenum: COPY '$cpyname' references file not found in $COPYBOOK_DIR/"
                    fi
                fi
                ;;
        esac
    done < "$file"
done

if command -v cobc >/dev/null 2>&1; then
    for file in $(find "$COBOL_SRC" -name '*.cob' | sort); do
        cobc -Wall -fsyntax-only -I "$COPYBOOK_DIR" "$file" 2>&1 || \
            fail "$file: GnuCOBOL syntax check failed"
    done
else
    warn "cobc not found; skipping compile check"
fi

if [ "$ERRORS" -gt 0 ]; then
    red "========================================="
    red "  $ERRORS error(s) found"
    red "========================================="
    exit 1
else
    grn "All COBOL lint checks passed."
    exit 0
fi