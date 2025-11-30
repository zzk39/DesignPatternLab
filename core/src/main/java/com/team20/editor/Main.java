package com.team20.editor;

import com.team20.editor.bootstrap.ApplicationContext;
import com.team20.editor.domain.command.Command;
import com.team20.editor.domain.command.UndoableCommand;
import com.team20.editor.domain.workspace.Workspace;
import com.team20.editor.extension.registry.DefaultCommandRegistry;

import java.time.Instant;
import java.util.Scanner;

/**
 * Program entry: CLI loop.
 * Uses DefaultCommandRegistry to create commands by name + raw args.
 *
 * NOTE: For "exit"/"quit" we require a plugin-provided "exit" command from the
 * registry.
 * If the registry returns null, we refuse to perform a fallback exit and
 * instead
 * inform the user that a plugin implementing graceful exit is required.
 */
public final class Main {

    public static void main(String[] args) {
        banner();

        ApplicationContext context = null;
        Workspace workspace = null;

        try {
            // Initialize ApplicationContext early
            context = new ApplicationContext();

            // Inject ApplicationContext into registry so plugin factories can access core
            // services
            DefaultCommandRegistry.setApplicationContext(context);

            // Create workspace (this loads state and migrates legacy markers)
            workspace = context.createWorkspace();

            // Display startup information
            System.out.println(context.dumpSummary());
            System.out.println();
            System.out.println("Team20 Text Editor ready.");
            System.out.println();
            System.out.println("Type 'help' to see available commands");
            System.out.println("Type 'exit' to quit the program");
            System.out.println();
            System.out.println("Quick Start:");
            System.out.println("  init test.txt [with-log]  - Create a new file");
            System.out.println("  load <filepath>           - Load an existing file");
            System.out.println();

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("Initialization Error: " + e.getMessage());
            System.err.println("========================================");
            System.err.println();
            System.err.println("The application will start in degraded mode.");
            System.err.println("Some features may not be available.");
            System.err.println();

            // If context wasn't created, we can't continue
            if (context == null) {
                System.err.println("FATAL: Could not initialize ApplicationContext.");
                System.err.println("Please check that all required plugins are installed.");
                return;
            }

            // If workspace wasn't created, try again with minimal setup
            if (workspace == null) {
                try {
                    workspace = new Workspace();
                    System.err.println("Warning: Using workspace without persistence support.");
                } catch (Exception ex) {
                    System.err.println("FATAL: Could not create Workspace: " + ex.getMessage());
                    return;
                }
            }
        }

        final ApplicationContext finalContext = context;
        final Workspace finalWorkspace = workspace;

        // Try to let an optional CLI plugin take over (provides up/down history).
        // If plugin class not present, fall back to built-in interactive loop.
        try {
            Class<?> pluginClazz = Class.forName("com.team20.editor.plugin.cli.JLineInteractive");
            try {
                // static method: run(ApplicationContext, Workspace)
                java.lang.reflect.Method m = pluginClazz.getMethod("run",
                        ApplicationContext.class, Workspace.class);
                m.invoke(null, finalContext, finalWorkspace);
                // plugin handled the interactive loop; exit main
                return;
            } catch (NoSuchMethodException | IllegalAccessException
                    | java.lang.reflect.InvocationTargetException ex) {
                System.err.println(
                        "Optional CLI plugin exists but cannot invoke entry point run(ApplicationContext,Workspace): "
                                + ex.getMessage());
                System.err.println(
                        "Falling back to built-in command loop. To use the enhanced CLI, check plugin compatibility.");
                // fall back to built-in loop
            }
        } catch (ClassNotFoundException ignored) {
            // plugin not present -> continue with built-in loop
        }

        runInteractiveLoop(finalContext, finalWorkspace);
    }

    private static void runInteractiveLoop(ApplicationContext context, Workspace workspace) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                System.out.print("> ");
                if (!scanner.hasNextLine())
                    break;
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;

                // Handle exit/quit by delegating to the command registry (plugin must provide
                // ExitCommand).
                if ("exit".equalsIgnoreCase(input) || "quit".equalsIgnoreCase(input)) {
                    try {
                        Command exitCommand = DefaultCommandRegistry.getInstance().create("exit", "");
                        if (exitCommand != null) {
                            context.commandInvoker().execute(exitCommand, workspace);
                            // ExitCommand 正常应该退出进程；若未退出，作为保护可 break
                            break;
                        } else {
                            System.out.println("Error: 'exit' command not found.");
                            System.out.println(
                                    "Please ensure a plugin providing the exit command is deployed (e.g., ExitCommand in plugins/core-impl).");
                            continue;
                        }
                    } catch (Throwable t) {
                        System.err.println("Error executing 'exit' command: " + t.getMessage());
                        // Do NOT fallback to immediate exit; allow user to inspect error and continue.
                        continue;
                    }
                }

                if ("help".equalsIgnoreCase(input)) {
                    System.out.print(context.showHelp());
                    continue;
                }

                // Use registry to create command
                String[] parts = input.split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String rawArgs = parts.length > 1 ? parts[1] : "";

                Command command = DefaultCommandRegistry.getInstance().create(commandName, rawArgs);
                if (command != null) {
                    // 统一入口：Invoker 内部判断是否是 UndoableCommand 并入栈
                    context.commandInvoker().execute(command, workspace);
                    continue;
                }

                System.out.println("Unknown command: " + input);
                System.out.println("Type 'help' to see available commands");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Save workspace state before exiting
        try {
            if (context != null && workspace != null) {
                context.saveWorkspaceState(workspace);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to save workspace state: " + e.getMessage());
        }

        scanner.close();
    }

    private static void banner() {
        System.out.println("========================================");
        System.out.println("  Team20 Text Editor v1.0.0");
        System.out.println("========================================");
        System.out.println("Startup time: " + Instant.now());
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("----------------------------------------");
    }
}