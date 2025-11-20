#!/usr/bin/env bash
##############################################
# Migration script: Move provider implementations from core to plugins/core-impl
# Purpose: Support strict pluginization by moving concrete implementations out of core module
# Author: Team20 Text Editor Team
# Version: 1.0.0
##############################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Working directory should be repository root
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${REPO_ROOT}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Provider Migration Script v1.0.0${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if we're in a git repository
IS_GIT_REPO=false
if git rev-parse --git-dir > /dev/null 2>&1; then
    IS_GIT_REPO=true
    echo -e "${GREEN}✓${NC} Git repository detected"
else
    echo -e "${YELLOW}⚠${NC} Not a git repository - files will be moved without version control"
fi

# Define source -> destination mappings
# Format: "source_path:destination_path"
declare -a FILE_MAPPINGS=(
    # Workspace Commands
    "core/src/main/java/com/team20/editor/domain/command/impl/workspace/CloseCommand.java:plugins/core-impl/src/main/java/com/team20/editor/domain/command/impl/workspace/CloseCommand.java"
    "core/src/main/java/com/team20/editor/domain/command/impl/workspace/EditCommand.java:plugins/core-impl/src/main/java/com/team20/editor/domain/command/impl/workspace/EditCommand.java"
    
    # Logging Provider (already in plugins, but included for completeness)
    # "core/src/main/java/com/team20/editor/domain/command/impl/logging/LoggingCommandProvider.java:plugins/core-impl/src/main/java/com/team20/editor/domain/command/impl/logging/LoggingCommandProvider.java"
    
    # Node Adapter Provider (already in plugins)
    # "core/src/main/java/com/team20/editor/representation/tree/providers/CoreNodeAdapterProvider.java:plugins/core-impl/src/main/java/com/team20/editor/representation/tree/providers/CoreNodeAdapterProvider.java"
    
    # Serializer Provider (already in plugins)
    # "core/src/main/java/com/team20/editor/infrastructure/persistence/DefaultSerializerProvider.java:plugins/core-impl/src/main/java/com/team20/editor/infrastructure/persistence/DefaultSerializerProvider.java"
    
    # Log Sinks (already in plugins)
    # "core/src/main/java/com/team20/editor/monitoring/logging/ConsoleLogSink.java:plugins/core-impl/src/main/java/com/team20/editor/monitoring/logging/ConsoleLogSink.java"
    # "core/src/main/java/com/team20/editor/monitoring/logging/FileLogSink.java:plugins/core-impl/src/main/java/com/team20/editor/monitoring/logging/FileLogSink.java"
    
    # JSON Serializer (already in plugins)
    # "core/src/main/java/com/team20/editor/infrastructure/persistence/JsonSerializer.java:plugins/core-impl/src/main/java/com/team20/editor/infrastructure/persistence/JsonSerializer.java"
    
    # Text Editor Provider (already in plugins)
    # "core/src/main/java/com/team20/editor/domain/editor/text/TextEditorProvider.java:plugins/core-impl/src/main/java/com/team20/editor/domain/editor/text/TextEditorProvider.java"
)

# Service file mappings (core -> plugins)
CORE_SERVICES_DIR="core/src/main/resources/META-INF/services"
PLUGIN_SERVICES_DIR="plugins/core-impl/src/main/resources/META-INF/services"

# Track migration statistics
MOVED_COUNT=0
SKIPPED_COUNT=0
FAILED_COUNT=0

echo -e "\n${BLUE}Phase 1: Migrating Implementation Files${NC}"
echo "=========================================="

# Function to move a file
move_file() {
    local src="$1"
    local dst="$2"
    
    # Check if source exists
    if [ ! -f "${src}" ]; then
        echo -e "${YELLOW}⊗${NC} Skipping ${src} (not found)"
        ((SKIPPED_COUNT++))
        return
    fi
    
    # Check if destination already exists
    if [ -f "${dst}" ]; then
        echo -e "${YELLOW}⊗${NC} Skipping ${src} (destination already exists)"
        ((SKIPPED_COUNT++))
        return
    fi
    
    # Create destination directory if needed
    local dst_dir="$(dirname "${dst}")"
    mkdir -p "${dst_dir}"
    
    # Move the file
    if [ "${IS_GIT_REPO}" = true ]; then
        # Use git mv
        if git mv "${src}" "${dst}" 2>/dev/null; then
            echo -e "${GREEN}✓${NC} Moved ${src} -> ${dst}"
            ((MOVED_COUNT++))
        else
            # Fallback to regular mv if git mv fails
            if mv "${src}" "${dst}" 2>/dev/null; then
                echo -e "${GREEN}✓${NC} Moved ${src} -> ${dst} (fallback)"
                ((MOVED_COUNT++))
            else
                echo -e "${RED}✗${NC} Failed to move ${src}"
                ((FAILED_COUNT++))
            fi
        fi
    else
        # Use regular mv
        if mv "${src}" "${dst}" 2>/dev/null; then
            echo -e "${GREEN}✓${NC} Moved ${src} -> ${dst}"
            ((MOVED_COUNT++))
        else
            echo -e "${RED}✗${NC} Failed to move ${src}"
            ((FAILED_COUNT++))
        fi
    fi
}

# Process all file mappings
for mapping in "${FILE_MAPPINGS[@]}"; do
    IFS=':' read -r src dst <<< "${mapping}"
    move_file "${src}" "${dst}"
done

echo ""
echo -e "${BLUE}Phase 2: Merging Service Provider Files${NC}"
echo "=========================================="

# Function to merge service files
merge_service_file() {
    local service_file="$1"
    local core_path="${CORE_SERVICES_DIR}/${service_file}"
    local plugin_path="${PLUGIN_SERVICES_DIR}/${service_file}"
    
    if [ ! -f "${core_path}" ]; then
        echo -e "${YELLOW}⊗${NC} No core service file: ${service_file}"
        return
    fi
    
    echo -e "${BLUE}•${NC} Processing service: ${service_file}"
    
    # Create plugin services directory if needed
    mkdir -p "${PLUGIN_SERVICES_DIR}"
    
    # If plugin service file exists, merge unique lines
    if [ -f "${plugin_path}" ]; then
        # Create temporary file with merged content (unique lines only)
        cat "${plugin_path}" "${core_path}" | sort -u > "${plugin_path}.tmp"
        mv "${plugin_path}.tmp" "${plugin_path}"
        echo -e "  ${GREEN}✓${NC} Merged with existing plugin service file"
    else
        # Copy core service file to plugin
        cp "${core_path}" "${plugin_path}"
        echo -e "  ${GREEN}✓${NC} Copied service file to plugin"
    fi
    
    # Remove core service file
    if [ "${IS_GIT_REPO}" = true ]; then
        git rm -f "${core_path}" 2>/dev/null || rm -f "${core_path}"
    else
        rm -f "${core_path}"
    fi
    echo -e "  ${GREEN}✓${NC} Removed core service file"
}

# Check if core services directory exists
if [ -d "${CORE_SERVICES_DIR}" ]; then
    for service_file in $(ls "${CORE_SERVICES_DIR}" 2>/dev/null || true); do
        merge_service_file "${service_file}"
    done
    
    # Remove core services directory if empty
    if [ -z "$(ls -A "${CORE_SERVICES_DIR}" 2>/dev/null)" ]; then
        rmdir "${CORE_SERVICES_DIR}" 2>/dev/null || true
        rmdir "$(dirname "${CORE_SERVICES_DIR}")" 2>/dev/null || true
        echo -e "${GREEN}✓${NC} Removed empty core services directory"
    fi
else
    echo -e "${YELLOW}⊗${NC} No core services directory found"
fi

echo ""
echo -e "${BLUE}Phase 3: Validating Plugin Configuration${NC}"
echo "=========================================="

# Check if plugins/core-impl/pom.xml has dependency on core
PLUGIN_POM="plugins/core-impl/pom.xml"
if [ -f "${PLUGIN_POM}" ]; then
    if grep -q "text-editor</artifactId>" "${PLUGIN_POM}"; then
        echo -e "${GREEN}✓${NC} Plugin declares dependency on core module"
    else
        echo -e "${YELLOW}⚠${NC} Plugin pom.xml might be missing core dependency"
        echo -e "${YELLOW}  Add this to plugins/core-impl/pom.xml:${NC}"
        echo ""
        echo "    <dependency>"
        echo "      <groupId>com.team20</groupId>"
        echo "      <artifactId>text-editor</artifactId>"
        echo "      <version>\${project.version}</version>"
        echo "      <scope>compile</scope>"
        echo "    </dependency>"
        echo ""
    fi
else
    echo -e "${RED}✗${NC} Plugin pom.xml not found at ${PLUGIN_POM}"
fi

echo ""
echo -e "${BLUE}Migration Summary${NC}"
echo "=========================================="
echo -e "Files moved:    ${GREEN}${MOVED_COUNT}${NC}"
echo -e "Files skipped:  ${YELLOW}${SKIPPED_COUNT}${NC}"
echo -e "Files failed:   ${RED}${FAILED_COUNT}${NC}"

# Commit changes if in a git repository
if [ "${IS_GIT_REPO}" = true ] && [ ${MOVED_COUNT} -gt 0 ]; then
    echo ""
    echo -e "${BLUE}Phase 4: Committing Changes${NC}"
    echo "=========================================="
    
    # Stage all changes
    git add -A
    
    # Commit with descriptive message
    git commit -m "Migrate provider implementations to plugins/core-impl (strict pluginization)

- Moved ${MOVED_COUNT} implementation file(s) from core to plugins/core-impl
- Merged service provider configurations
- Maintains strict separation: core = SPI/domain, plugins = implementations

This migration supports better modularity, extensibility, and testing.
See docs/PLUGIN_GUIDE.md for details on the plugin architecture." || {
        echo -e "${YELLOW}⚠${NC} Git commit failed or no changes to commit"
    }
    
    echo -e "${GREEN}✓${NC} Changes committed successfully"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
if [ ${FAILED_COUNT} -eq 0 ]; then
    echo -e "${GREEN}✓ Migration completed successfully!${NC}"
else
    echo -e "${YELLOW}⚠ Migration completed with ${FAILED_COUNT} failure(s)${NC}"
fi
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Review the changes: git status && git diff HEAD~1"
echo "2. Build the project: mvn -am -pl core,plugins/core-impl clean package"
echo "3. Test the application: ./build.sh run"
echo "4. Verify plugin loading: check application output for provider registrations"
echo ""
echo "For more information, see docs/PLUGIN_GUIDE.md"
