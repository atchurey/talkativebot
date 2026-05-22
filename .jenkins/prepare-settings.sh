#!/usr/bin/env sh
# Renders .jenkins/settings.effective.xml from the template using environment variables.
# Used by Jenkins pipeline; can also be run locally before mvn deploy/release.
set -e

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
TEMPLATE="${SCRIPT_DIR}/settings.xml.template"
OUTPUT="${1:-${SCRIPT_DIR}/settings.effective.xml}"

: "${NEXUS_MIRROR_URL:=http://nexus:8081/repository/maven-public/}"

export NEXUS_MIRROR_URL
export NEXUS_USERNAME="${NEXUS_USERNAME:-}"
export NEXUS_PASSWORD="${NEXUS_PASSWORD:-}"
export CENTRAL_USERNAME="${CENTRAL_USERNAME:-}"
export CENTRAL_PASSWORD="${CENTRAL_PASSWORD:-}"
export GPG_PASSPHRASE="${GPG_PASSPHRASE:-}"
export GPG_KEY_ID="${GPG_KEY_ID:-}"

envsubst '${NEXUS_MIRROR_URL} ${NEXUS_USERNAME} ${NEXUS_PASSWORD} ${CENTRAL_USERNAME} ${CENTRAL_PASSWORD} ${GPG_PASSPHRASE} ${GPG_KEY_ID}' \
  < "${TEMPLATE}" > "${OUTPUT}"

echo "Wrote ${OUTPUT}"
