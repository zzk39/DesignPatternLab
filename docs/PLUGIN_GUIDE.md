# Plugin Development Guide

This guide explains the plugin architecture for the Team20 Text Editor, including the rationale for strict pluginization, guidelines for migrating implementations, and best practices for plugin development.

## Table of Contents

1. [Why Plugin Architecture?](#why-plugin-architecture)
2. [Core vs. Plugin Separation](#core-vs-plugin-separation)
3. [Service Provider Interface (SPI)](#service-provider-interface-spi)
4. [Creating a Command Provider](#creating-a-command-provider)
5. [Registering Services](#registering-services)
6. [Migration Process](#migration-process)
7. [Testing and Validation](#testing-and-validation)
8. [Best Practices](#best-practices)

---

## Why Plugin Architecture?

Moving concrete implementations from the `core` module into separate plugins (like `plugins/core-impl`) provides several key benefits:

### 1. **Improved Modularity**
- **Separation of Concerns**: Core module contains only interfaces (SPI), domain models, and framework code
- **Clean Architecture**: Business logic is separated from concrete implementations
- **Easier Maintenance**: Changes to implementations don't affect the core framework

### 2. **Enhanced Extensibility**
- **Third-Party Plugins**: External developers can provide alternative implementations
- **Custom Behaviors**: Teams can create organization-specific plugins without modifying core
- **Plugin Discovery**: Java ServiceLoader automatically discovers and loads plugins at runtime

### 3. **Better Testing**
- **Mock Implementations**: Test plugins can provide mock providers for unit testing
- **Isolated Testing**: Core and plugins can be tested independently
- **Integration Testing**: Plugin loading can be verified in integration tests

### 4. **Flexible Deployment**
- **Optional Features**: Plugins can be included/excluded based on deployment needs
- **Version Management**: Plugins can be versioned and updated independently
- **Dependency Management**: Clear dependency graph (plugins depend on core, not vice versa)

---

## Core vs. Plugin Separation

Understanding what belongs in each module is critical for maintaining a clean architecture:

### Core Module (`core/`)

**Should contain:**
- ✅ Service Provider Interfaces (SPI) - e.g., `CommandProvider`, `EditorProvider`, `SerializerProvider`
- ✅ Domain Models - e.g., `Workspace`, `Editor`, `Command`
- ✅ Framework Code - e.g., `ApplicationContext`, `CommandRegistry`, `EventBus`
- ✅ Abstract Classes - e.g., `AbstractNodeAdapter`, `UndoableCommand`
- ✅ Core Utilities - e.g., `Logger`, `EventPublisher`

**Should NOT contain:**
- ❌ Concrete Command Implementations - e.g., `CloseCommand`, `EditCommand`, `SaveCommand`
- ❌ Provider Implementations - e.g., `TextEditorProvider`, `LoggingCommandProvider`
- ❌ Adapter Implementations - e.g., `CoreNodeAdapterProvider`, `CommandTypeNodeAdapter`
- ❌ Serializer Implementations - e.g., `JsonSerializer`
- ❌ Log Sink Implementations - e.g., `ConsoleLogSink`, `FileLogSink`

### Plugin Module (`plugins/core-impl/`)

**Should contain:**
- ✅ Concrete Command Implementations
- ✅ Provider Implementations (implementing core SPIs)
- ✅ Adapter Implementations
- ✅ Utility Implementations (serializers, log sinks, etc.)
- ✅ META-INF/services registration files

---

## Service Provider Interface (SPI)

The core module defines several SPIs that plugins can implement:

### Available SPIs

1. **`com.team20.editor.extension.spi.command.CommandProvider`**
   - Purpose: Provide commands to the application
   - Method: `List<CommandDescriptor> getCommandDescriptors()`

2. **`com.team20.editor.extension.spi.editor.EditorProvider`**
   - Purpose: Provide editor implementations
   - Method: `Editor createEditor(String filepath)`

3. **`com.team20.editor.extension.spi.serialization.SerializerProvider`**
   - Purpose: Provide serialization implementations
   - Method: `Serializer getSerializer()`

4. **`com.team20.editor.extension.spi.node.NodeAdapterProvider`**
   - Purpose: Provide node adapters for tree representation
   - Method: `List<NodeAdapterFactory> getAdapterFactories()`

5. **`com.team20.editor.monitoring.logging.LogSink`**
   - Purpose: Provide logging implementations
   - Methods: `log(LogLevel level, String message)`, etc.

---

## Creating a Command Provider

Here's a complete example of implementing a `CommandProvider` for workspace commands:

```java
package com.team20.editor.domain.command.impl.workspace;

import com.team20.editor.domain.command.CommandDescriptor;
import com.team20.editor.extension.registry.EditorFactory;
import com.team20.editor.extension.spi.command.CommandProvider;
import com.team20.editor.infrastructure.persistence.PersistenceManager;

import java.util.List;

/**
 * Workspace command provider - provides close, edit, and other workspace-related commands.
 * This provider is loaded via Java ServiceLoader at runtime.
 */
public class WorkspaceCommandProvider implements CommandProvider {
    
    @Override
    public String getProviderName() {
        return "workspace-commands";
    }
    
    @Override
    public List<CommandDescriptor> getCommandDescriptors() {
        // Obtain dependencies from ApplicationContext or inject them
        EditorFactory editorFactory = /* obtain from context */;
        PersistenceManager persistenceManager = /* obtain from context */;
        
        return List.of(
            // Close command
            CommandDescriptor.of(
                "close",
                () -> new CloseCommand(),
                "Close the active editor or specified file"
            ),
            
            // Edit command
            CommandDescriptor.of(
                "edit",
                () -> new EditCommand(editorFactory, persistenceManager),
                "Open or switch to a file"
            ),
            
            // List command
            CommandDescriptor.of(
                "list",
                () -> new EditorListCommand(),
                "List all open editors"
            )
        );
    }
}
```

### Key Points

1. **Provider Name**: Return a unique identifier in `getProviderName()`
2. **Command Registration**: Return a list of `CommandDescriptor` objects
3. **Factory Pattern**: Use `Supplier<Command>` to create command instances
4. **Dependencies**: Commands may require dependencies (injected via constructor)
5. **Metadata**: Include descriptions to help users understand command purpose

---

## Registering Services

To make your provider discoverable by Java ServiceLoader, you must register it in `META-INF/services`:

### Step 1: Create Service File

Create a file at: `plugins/core-impl/src/main/resources/META-INF/services/com.team20.editor.extension.spi.command.CommandProvider`

### Step 2: List Provider Implementations

Add the fully-qualified class name of your provider (one per line):

```
com.team20.editor.domain.command.impl.workspace.WorkspaceCommandProvider
com.team20.editor.domain.command.impl.logging.LoggingCommandProvider
com.team20.editor.domain.command.impl.text.TextCommandProvider
```

### Example: Multiple Service Files

For different SPIs, create separate service files:

```
META-INF/services/
├── com.team20.editor.extension.spi.command.CommandProvider
├── com.team20.editor.extension.spi.editor.EditorProvider
├── com.team20.editor.extension.spi.node.NodeAdapterProvider
├── com.team20.editor.extension.spi.serialization.SerializerProvider
└── com.team20.editor.monitoring.logging.LogSink
```

### Verification at Runtime

When the application starts, you should see log messages indicating provider discovery:

```
Loading command provider: workspace-commands
Loading command provider: logging-commands
...
Registered 15 commands from 3 providers
```

Use the `list` or similar commands to verify that your commands are available.

---

## Migration Process

### Using the Migration Script

The project includes an automated migration script to move implementations from core to plugins.

#### Step 1: Review What Will Be Migrated

Edit `scripts/migrate_providers_to_plugins.sh` and review the `FILE_MAPPINGS` array:

```bash
declare -a FILE_MAPPINGS=(
    "core/src/main/java/.../CloseCommand.java:plugins/core-impl/src/main/java/.../CloseCommand.java"
    "core/src/main/java/.../EditCommand.java:plugins/core-impl/src/main/java/.../EditCommand.java"
    ...
)
```

#### Step 2: Run the Migration Script

```bash
# From repository root
./scripts/migrate_providers_to_plugins.sh
```

The script will:
- ✅ Move files using `git mv` (or `mv` if not in git)
- ✅ Merge service provider files (unique entries only)
- ✅ Remove core service files after merge
- ✅ Validate plugin pom.xml configuration
- ✅ Commit changes with a descriptive message

#### Step 3: Review Changes

```bash
# Check what was changed
git status
git diff HEAD~1

# Review moved files
git show --stat HEAD
```

### Manual Migration Steps

If you prefer to migrate files manually:

1. **Identify Implementation Class**
   ```bash
   # Example: CloseCommand
   SOURCE=core/src/main/java/com/team20/editor/domain/command/impl/workspace/CloseCommand.java
   TARGET=plugins/core-impl/src/main/java/com/team20/editor/domain/command/impl/workspace/CloseCommand.java
   ```

2. **Move File**
   ```bash
   mkdir -p $(dirname ${TARGET})
   git mv ${SOURCE} ${TARGET}
   ```

3. **Update Service Registration**
   Add the class to appropriate service file in `plugins/core-impl/src/main/resources/META-INF/services/`

4. **Remove from Core Services** (if present)
   Remove the entry from core's META-INF/services

### Plugin POM Configuration

Ensure `plugins/core-impl/pom.xml` declares dependency on core:

```xml
<dependencies>
  <dependency>
    <groupId>com.team20</groupId>
    <artifactId>text-editor</artifactId>
    <version>${project.version}</version>
    <scope>compile</scope>
  </dependency>
</dependencies>
```

---

## Testing and Validation

### Build Validation

After migration, verify that the project builds successfully:

```bash
# Build core and plugins together
mvn -am -pl core,plugins/core-impl clean package

# Or use the project build script
./build.sh compile
```

### Runtime Validation

Test the application to ensure plugins are loaded correctly:

```bash
# Run the application
./build.sh run

# Expected output should show:
# - Plugin providers being discovered
# - Commands being registered
# - Application starting without errors
```

### Interactive Testing

Once the application is running, test migrated commands:

```bash
# Test edit command
> edit test.txt
已新建文件: test.txt

# Test close command
> close
已关闭活动文件

# Test list command
> list
Open editors: (none)
```

### Verification Script

You can create a verification script to automate checks:

```bash
#!/usr/bin/env bash
# verify_plugins.sh

echo "Verifying plugin structure..."

# Check that service files exist
for spi in CommandProvider EditorProvider SerializerProvider NodeAdapterProvider; do
    SERVICE_FILE="plugins/core-impl/src/main/resources/META-INF/services/com.team20.editor.extension.spi.*.$spi"
    if ls ${SERVICE_FILE} 1> /dev/null 2>&1; then
        echo "✓ Service file exists for $spi"
    else
        echo "✗ Missing service file for $spi"
    fi
done

# Check that core doesn't contain implementations
echo "Checking core module for stray implementations..."
IMPL_FILES=$(find core/src/main/java -name "*Provider.java" -o -name "*Sink.java" -o -name "*Serializer.java" 2>/dev/null | wc -l)
if [ ${IMPL_FILES} -eq 0 ]; then
    echo "✓ Core module is clean (no implementation classes)"
else
    echo "⚠ Found ${IMPL_FILES} implementation files in core"
fi

echo "Plugin verification complete!"
```

---

## Best Practices

### 1. Design Principles

- **Interface Segregation**: Keep SPIs focused and minimal
- **Dependency Inversion**: Depend on abstractions (SPIs), not concrete classes
- **Single Responsibility**: Each provider should have one clear purpose
- **Open/Closed**: Core should be open for extension, closed for modification

### 2. Naming Conventions

- **Providers**: End with `Provider` (e.g., `WorkspaceCommandProvider`)
- **Commands**: End with `Command` (e.g., `CloseCommand`, `EditCommand`)
- **Descriptors**: Use descriptive names in `CommandDescriptor.of("commandName", ...)`

### 3. Error Handling

- **Graceful Degradation**: Handle missing dependencies gracefully
- **Clear Messages**: Provide helpful error messages to users
- **Logging**: Log provider loading success/failure at appropriate levels

### 4. Documentation

- **Javadoc**: Document all public APIs, especially SPIs
- **README**: Include plugin-specific README if needed
- **Examples**: Provide example implementations for common use cases

### 5. Testing

- **Unit Tests**: Test individual commands in isolation
- **Integration Tests**: Test provider loading and service discovery
- **Mock Providers**: Create test providers for core module testing

### 6. Dependency Management

- **Minimal Dependencies**: Keep plugin dependencies minimal
- **Version Alignment**: Use parent POM to manage common dependency versions
- **Scope Control**: Use appropriate Maven scopes (compile, test, runtime)

---

## Adding New Commands to a Plugin

To add a new command to an existing plugin:

1. **Create the Command Class**
   ```java
   // plugins/core-impl/src/main/java/.../MyNewCommand.java
   public class MyNewCommand implements Command {
       @Override
       public void execute(Workspace workspace) {
           // Implementation
       }
   }
   ```

2. **Register in Provider**
   ```java
   // Update your CommandProvider
   @Override
   public List<CommandDescriptor> getCommandDescriptors() {
       return List.of(
           // ... existing commands ...
           CommandDescriptor.of(
               "mynew",
               () -> new MyNewCommand(),
               "Description of my new command"
           )
       );
   }
   ```

3. **Build and Test**
   ```bash
   mvn -am -pl core,plugins/core-impl clean package
   ./build.sh run
   ```

4. **Verify**
   ```bash
   > mynew
   # Should execute your command
   ```

---

## Creating a New Plugin Module

To create an entirely new plugin (e.g., `plugins/advanced-features`):

1. **Create Module Structure**
   ```bash
   mkdir -p plugins/advanced-features/src/main/{java,resources}
   mkdir -p plugins/advanced-features/src/main/resources/META-INF/services
   mkdir -p plugins/advanced-features/src/test/java
   ```

2. **Create POM**
   ```xml
   <!-- plugins/advanced-features/pom.xml -->
   <project>
     <parent>
       <groupId>com.team20</groupId>
       <artifactId>text-editor-parent</artifactId>
       <version>1.0.0-SNAPSHOT</version>
       <relativePath>../../pom.xml</relativePath>
     </parent>
     
     <artifactId>plugins-advanced-features</artifactId>
     <packaging>jar</packaging>
     
     <dependencies>
       <dependency>
         <groupId>com.team20</groupId>
         <artifactId>text-editor</artifactId>
         <version>${project.version}</version>
       </dependency>
     </dependencies>
   </project>
   ```

3. **Add to Parent POM**
   ```xml
   <!-- pom.xml -->
   <modules>
     <module>core</module>
     <module>plugins/core-impl</module>
     <module>plugins/advanced-features</module>
   </modules>
   ```

4. **Implement Providers**
   Create your provider classes and register them in META-INF/services

5. **Build and Deploy**
   ```bash
   mvn clean package
   ./build.sh run
   ```

---

## Troubleshooting

### Providers Not Loading

**Symptom**: Commands or features missing at runtime

**Solutions**:
1. Check service file exists: `plugins/core-impl/src/main/resources/META-INF/services/...`
2. Verify class name is correct (fully-qualified, no typos)
3. Ensure plugin JAR is on classpath (check `build.sh` classpath assembly)
4. Check for errors during ServiceLoader initialization (logs)

### Build Failures

**Symptom**: Compilation errors after migration

**Solutions**:
1. Ensure core module is built first: `mvn -am -pl core clean compile`
2. Check import statements - may need to update if package structure changed
3. Verify plugin POM declares dependency on core
4. Clean and rebuild: `mvn clean package`

### Command Not Found

**Symptom**: Application says "unknown command" for migrated command

**Solutions**:
1. Verify provider returns the command in `getCommandDescriptors()`
2. Check command name matches (case-sensitive)
3. Ensure provider class is registered in META-INF/services
4. Rebuild and restart application

---

## Summary

This guide covered:

- ✅ **Why** plugin architecture improves modularity, extensibility, testing, and deployment
- ✅ **What** belongs in core (SPIs, domain, framework) vs. plugins (implementations)
- ✅ **How** to create command providers and register them via ServiceLoader
- ✅ **How** to use the migration script to move implementations from core to plugins
- ✅ **How** to test and validate plugin functionality
- ✅ **Best practices** for plugin development and maintenance

For more information, see:
- `core/src/main/java/com/team20/editor/extension/spi/` - SPI definitions
- `plugins/core-impl/src/main/java/` - Example implementations
- `scripts/migrate_providers_to_plugins.sh` - Automated migration tool
- `build.sh` - Build and run script with plugin support

---

**Questions or Issues?**

If you encounter problems or have questions about plugin development, please:
1. Check the troubleshooting section above
2. Review existing provider implementations in `plugins/core-impl`
3. Consult the core SPI documentation
4. Contact the Team20 development team

Happy plugin development! 🚀
