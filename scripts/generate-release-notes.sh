#!/usr/bin/env bash
set -euo pipefail

OUTPUT="release-notes-$(date +%Y%m%d-%H%M%S).md"
{
  echo "# Release Notes"
  echo
  echo "Generated on $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
  echo
  echo "## Changes"
  echo
  git log --pretty=format:'- %h %ad %an: %s' --date=short $(git describe --tags --abbrev=0 --always)..HEAD || git log --pretty=format:'- %h %ad %an: %s' --date=short -n 50
} > "$OUTPUT"

echo "Release notes written to $OUTPUT"
