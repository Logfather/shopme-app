#!/bin/bash

set -e  # Script bricht bei Fehlern ab

echo "==============================="
echo "📦 ShopMe Versioning Script"
echo "==============================="

# 1. Prüfen ob wir in einem Git Repo sind
if ! git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
  echo "❌ Kein Git Repository gefunden!"
  exit 1
fi

# 2. Aktuellen Branch anzeigen
CURRENT_BRANCH=$(git branch --show-current)
echo "🔧 Aktueller Branch: $CURRENT_BRANCH"

# 3. Status anzeigen
echo ""
echo "📊 Aktueller Git Status:"
git status

echo ""
read -p "👉 Möchtest du ALLE Änderungen stagen? (y/n): " STAGE_ALL

if [ "$STAGE_ALL" = "y" ]; then
  git add .
else
  echo ""
  echo "👉 Bitte gib Dateien an (z.B. app/src/...):"
  read -p "Dateien: " FILES
  git add $FILES
fi

# 4. Änderungen kompakt anzeigen
echo ""
echo "📊 Zusammenfassung der Änderungen:"
git diff --cached --stat

echo ""
read -p "👉 Vollständigen Diff anzeigen? (y/n): " SHOW_DIFF

if [ "$SHOW_DIFF" = "y" ]; then
  git diff --cached | less
fi

echo ""
read -p "👉 Sind diese Änderungen korrekt? (y/n): " CONFIRM_DIFF
if [ "$CONFIRM_DIFF" != "y" ]; then
  echo "❌ Abgebrochen."
  exit 1
fi

# 5. Commit Message erfassen (strukturiert!)
echo ""
echo "📝 Commit Beschreibung eingeben:"

read -p "Titel (kurz, prägnant): " TITLE
read -p "Beschreibung (optional): " DESCRIPTION
read -p "Ticket / Kontext (optional): " CONTEXT

COMMIT_MESSAGE="$TITLE"

if [ ! -z "$DESCRIPTION" ]; then
  COMMIT_MESSAGE="$COMMIT_MESSAGE

$DESCRIPTION"
fi

if [ ! -z "$CONTEXT" ]; then
  COMMIT_MESSAGE="$COMMIT_MESSAGE

Context: $CONTEXT"
fi

# 6. Commit durchführen
echo ""
echo "🚀 Commit wird erstellt..."
git commit -m "$COMMIT_MESSAGE"

# 7. Tagging (Version)
echo ""
read -p "👉 Möchtest du einen Version-Tag erstellen? (y/n): " CREATE_TAG

if [ "$CREATE_TAG" = "y" ]; then
  read -p "Version (z.B. v1.2.0): " VERSION
  read -p "Tag Beschreibung: " TAG_MESSAGE

  git tag -a "$VERSION" -m "$TAG_MESSAGE"
  echo "🏷️ Tag $VERSION erstellt"
fi

# 8. Push
echo ""
read -p "👉 Möchtest du pushen? (y/n): " PUSH

if [ "$PUSH" = "y" ]; then
  git push origin "$CURRENT_BRANCH"

  if [ "$CREATE_TAG" = "y" ]; then
    git push origin "$VERSION"
  fi

  echo "✅ Push abgeschlossen"
else
  echo "ℹ️ Änderungen nur lokal gespeichert"
fi

echo ""
echo "==============================="
echo "✅ Versionierung abgeschlossen"
echo "==============================="