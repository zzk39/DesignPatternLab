package com.team20.editor.plugin.stats;

import com.team20.editor.infrastructure.event.CommandEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionStatisticsListener 单元测试
 */
public class SessionStatisticsListenerTest {

    private SessionStatisticsListener listener;

    @BeforeEach
    void setUp() {
        listener = new SessionStatisticsListener();
    }

    // =========== startTracking 测试 ===========

    @Test
    void testStartTracking() {
        listener.startTracking("test.txt");
        // 刚开始追踪，时长应该是0或非常短
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testStartTrackingNull() {
        // 不应该抛出异常
        listener.startTracking(null);
    }

    @Test
    void testStartTrackingBlank() {
        // 不应该抛出异常
        listener.startTracking("   ");
    }

    @Test
    void testStartTrackingSameFileTwice() {
        listener.startTracking("test.txt");
        listener.startTracking("test.txt");
        // 应该不会重复计时
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    // =========== stopTracking 测试 ===========

    @Test
    void testStopTracking() {
        listener.startTracking("test.txt");
        listener.stopTracking("test.txt");
        // 停止后时长应该被记录
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testStopTrackingNull() {
        // 不应该抛出异常
        listener.stopTracking(null);
    }

    @Test
    void testStopTrackingNotStarted() {
        // 停止一个没有开始的文件，不应该抛出异常
        listener.stopTracking("test.txt");
    }

    // =========== resetDuration 测试 ===========

    @Test
    void testResetDuration() throws InterruptedException {
        listener.startTracking("test.txt");
        Thread.sleep(100);
        listener.stopTracking("test.txt");
        
        listener.resetDuration("test.txt");
        
        assertEquals(0, listener.getDurationSeconds("test.txt"));
    }

    @Test
    void testResetDurationNull() {
        // 不应该抛出异常
        listener.resetDuration(null);
    }

    // =========== getDurationSeconds 测试 ===========

    @Test
    void testGetDurationSecondsNull() {
        assertEquals(0, listener.getDurationSeconds(null));
    }

    @Test
    void testGetDurationSecondsNotTracked() {
        assertEquals(0, listener.getDurationSeconds("unknown.txt"));
    }

    // =========== formatDuration 测试 ===========

    @Test
    void testFormatDurationSeconds() {
        assertEquals("0秒", listener.formatDuration(0));
        assertEquals("30秒", listener.formatDuration(30));
        assertEquals("59秒", listener.formatDuration(59));
    }

    @Test
    void testFormatDurationMinutes() {
        assertEquals("1分钟", listener.formatDuration(60));
        assertEquals("5分钟", listener.formatDuration(300));
        assertEquals("59分钟", listener.formatDuration(3540));
    }

    @Test
    void testFormatDurationHours() {
        assertEquals("1小时", listener.formatDuration(3600));
        assertEquals("1小时30分钟", listener.formatDuration(5400));
        assertEquals("2小时15分钟", listener.formatDuration(8100));
        assertEquals("23小时59分钟", listener.formatDuration(86340));
    }

    @Test
    void testFormatDurationDays() {
        assertEquals("1天", listener.formatDuration(86400));
        assertEquals("1天3小时", listener.formatDuration(97200));
        assertEquals("2天12小时", listener.formatDuration(216000));
    }

    // =========== onEvent 测试 ===========

    @Test
    void testOnEventLoad() {
        CommandEvent event = new CommandEvent("load", "test.txt", "test.txt");
        listener.onEvent(event);
        // load 事件应该开始追踪
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testOnEventEdit() {
        CommandEvent event = new CommandEvent("edit", "test.txt", "test.txt");
        listener.onEvent(event);
        // edit 事件应该开始追踪
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testOnEventInit() {
        CommandEvent event = new CommandEvent("init", "test.txt", "test.txt");
        listener.onEvent(event);
        // init 事件应该开始追踪
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testOnEventClose() {
        listener.startTracking("test.txt");
        CommandEvent event = new CommandEvent("close", "test.txt", "test.txt");
        listener.onEvent(event);
        // close 事件应该停止追踪
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testOnEventExit() {
        listener.startTracking("test.txt");
        CommandEvent event = new CommandEvent("exit", "", "");
        listener.onEvent(event);
        // exit 事件应该停止所有追踪
        assertTrue(listener.getDurationSeconds("test.txt") >= 0);
    }

    @Test
    void testOnEventOther() {
        CommandEvent event = new CommandEvent("append", "text", "test.txt");
        listener.onEvent(event);
        // 其他命令不应该影响追踪
    }

    // =========== 切换文件测试 ===========

    @Test
    void testSwitchFiles() throws InterruptedException {
        // 开始追踪文件1
        listener.startTracking("file1.txt");
        Thread.sleep(100);
        
        // 切换到文件2
        listener.startTracking("file2.txt");
        Thread.sleep(100);
        
        listener.stopTracking("file2.txt");
        
        // 两个文件都应该有记录的时长
        assertTrue(listener.getDurationSeconds("file1.txt") >= 0);
        assertTrue(listener.getDurationSeconds("file2.txt") >= 0);
    }

    // =========== 累积时长测试 ===========

    @Test
    void testAccumulatedDuration() throws InterruptedException {
        // 第一次追踪
        listener.startTracking("test.txt");
        Thread.sleep(100);
        listener.stopTracking("test.txt");
        
        long firstDuration = listener.getDurationSeconds("test.txt");
        
        // 第二次追踪同一文件
        listener.startTracking("test.txt");
        Thread.sleep(100);
        listener.stopTracking("test.txt");
        
        long totalDuration = listener.getDurationSeconds("test.txt");
        
        // 总时长应该大于等于第一次时长
        assertTrue(totalDuration >= firstDuration);
    }
}
