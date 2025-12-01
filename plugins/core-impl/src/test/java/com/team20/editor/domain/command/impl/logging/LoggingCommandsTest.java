package com.team20.editor.domain.command.impl.logging;

import com.team20.editor.domain.editor.Editor;
import com.team20.editor.domain.workspace.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 日志命令单元测试
 */
public class LoggingCommandsTest {

    private Workspace workspace;
    private Editor mockEditor;
    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        mockEditor = mock(Editor.class);
        when(mockEditor.getName()).thenReturn("test.txt");

        workspace.addEditor(mockEditor);
        workspace.setActiveEditor(mockEditor);

        // 捕获标准输出
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        // 清理日志文件
        new File(".test.txt.log").delete();
        new File(".specific.txt.log").delete();
    }

    // =========== LogOnCommand 测试 ===========

    @Test
    void testLogOnCommandExecute() {
        LogOnCommand cmd = new LogOnCommand();
        cmd.execute(workspace);

        assertTrue(workspace.isLoggingEnabled("test.txt"));
        assertTrue(outContent.toString().contains("日志已启用"));
    }

    @Test
    void testLogOnCommandWithFilepath() {
        LogOnCommand cmd = new LogOnCommand("specific.txt");
        // 这个测试会因为没有对应的editor而使用指定的filepath
        cmd.execute(workspace);
        
        // 由于 specific.txt 不在 workspace 中，但命令仍会尝试启用它
        assertTrue(outContent.toString().contains("日志已启用"));
    }

    @Test
    void testLogOnCommandNoActiveEditor() {
        Workspace emptyWorkspace = new Workspace();
        LogOnCommand cmd = new LogOnCommand();
        cmd.execute(emptyWorkspace);

        assertTrue(outContent.toString().contains("没有打开的文件"));
    }

    @Test
    void testLogOnCommandToString() {
        LogOnCommand cmd1 = new LogOnCommand();
        assertTrue(cmd1.toString().contains("log-on"));

        LogOnCommand cmd2 = new LogOnCommand("file.txt");
        assertTrue(cmd2.toString().contains("file.txt"));
    }

    // =========== LogOffCommand 测试 ===========

    @Test
    void testLogOffCommandExecute() {
        // 先启用日志
        workspace.setLoggingEnabled("test.txt", true);
        assertTrue(workspace.isLoggingEnabled("test.txt"));

        LogOffCommand cmd = new LogOffCommand();
        cmd.execute(workspace);

        assertFalse(workspace.isLoggingEnabled("test.txt"));
        assertTrue(outContent.toString().contains("日志已禁用"));
    }

    @Test
    void testLogOffCommandWithFilepath() {
        workspace.setLoggingEnabled("test.txt", true);
        
        LogOffCommand cmd = new LogOffCommand("test.txt");
        cmd.execute(workspace);

        assertFalse(workspace.isLoggingEnabled("test.txt"));
    }

    @Test
    void testLogOffCommandNoActiveEditor() {
        Workspace emptyWorkspace = new Workspace();
        LogOffCommand cmd = new LogOffCommand();
        cmd.execute(emptyWorkspace);

        assertTrue(outContent.toString().contains("没有打开的文件"));
    }

    @Test
    void testLogOffCommandToString() {
        LogOffCommand cmd1 = new LogOffCommand();
        assertTrue(cmd1.toString().contains("log-off"));

        LogOffCommand cmd2 = new LogOffCommand("file.txt");
        assertTrue(cmd2.toString().contains("file.txt"));
    }

    // =========== LogShowCommand 测试 ===========

    @Test
    void testLogShowCommandNoLogFile() {
        LogShowCommand cmd = new LogShowCommand();
        cmd.execute(workspace);

        assertTrue(outContent.toString().contains("未找到日志文件"));
    }

    @Test
    void testLogShowCommandNoActiveEditor() {
        Workspace emptyWorkspace = new Workspace();
        LogShowCommand cmd = new LogShowCommand();
        cmd.execute(emptyWorkspace);

        assertTrue(outContent.toString().contains("没有打开的文件"));
    }

    @Test
    void testLogShowCommandToString() {
        LogShowCommand cmd1 = new LogShowCommand();
        assertTrue(cmd1.toString().contains("log-show"));

        LogShowCommand cmd2 = new LogShowCommand("file.txt");
        assertTrue(cmd2.toString().contains("file.txt"));
    }

    @Test
    void testLogShowAfterLogOn() {
        // 先启用日志，创建日志文件
        LogOnCommand logOn = new LogOnCommand();
        logOn.execute(workspace);

        // 清空输出缓冲
        outContent.reset();

        // 显示日志
        LogShowCommand logShow = new LogShowCommand();
        logShow.execute(workspace);

        String output = outContent.toString();
        assertTrue(output.contains("session start at"));
    }

    // =========== 边界情况测试 ===========

    @Test
    void testLogOnTwice() {
        LogOnCommand cmd1 = new LogOnCommand();
        cmd1.execute(workspace);
        
        outContent.reset();
        
        LogOnCommand cmd2 = new LogOnCommand();
        cmd2.execute(workspace);

        assertTrue(workspace.isLoggingEnabled("test.txt"));
        assertTrue(outContent.toString().contains("日志已启用"));
    }

    @Test
    void testLogOffWhenNotEnabled() {
        assertFalse(workspace.isLoggingEnabled("test.txt"));

        LogOffCommand cmd = new LogOffCommand();
        cmd.execute(workspace);

        assertFalse(workspace.isLoggingEnabled("test.txt"));
    }

    @Test
    void testConstructorsWithNullAndBlank() {
        // LogOnCommand
        LogOnCommand logOn1 = new LogOnCommand(null);
        LogOnCommand logOn2 = new LogOnCommand("");
        LogOnCommand logOn3 = new LogOnCommand("   ");

        // LogOffCommand
        LogOffCommand logOff1 = new LogOffCommand(null);
        LogOffCommand logOff2 = new LogOffCommand("");
        LogOffCommand logOff3 = new LogOffCommand("   ");

        // LogShowCommand
        LogShowCommand logShow1 = new LogShowCommand(null);
        LogShowCommand logShow2 = new LogShowCommand("");
        LogShowCommand logShow3 = new LogShowCommand("   ");

        // 所有这些构造都不应该抛出异常
        assertNotNull(logOn1);
        assertNotNull(logOff1);
        assertNotNull(logShow1);
    }
}
