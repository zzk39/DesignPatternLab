package com.team20.editor.infrastructure.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleEventBus 单元测试
 */
public class SimpleEventBusTest {

    private SimpleEventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new SimpleEventBus();
    }

    @Test
    void testSubscribe() {
        TestEventListener listener = new TestEventListener();
        eventBus.subscribe(listener);

        assertEquals(1, eventBus.getListenerCount());
    }

    @Test
    void testSubscribeNull() {
        eventBus.subscribe(null);
        assertEquals(0, eventBus.getListenerCount());
    }

    @Test
    void testSubscribeDuplicate() {
        TestEventListener listener = new TestEventListener();
        eventBus.subscribe(listener);
        eventBus.subscribe(listener);

        assertEquals(1, eventBus.getListenerCount());
    }

    @Test
    void testUnsubscribe() {
        TestEventListener listener = new TestEventListener();
        eventBus.subscribe(listener);
        eventBus.unsubscribe(listener);

        assertEquals(0, eventBus.getListenerCount());
    }

    @Test
    void testUnsubscribeNotSubscribed() {
        TestEventListener listener = new TestEventListener();
        eventBus.unsubscribe(listener);
        assertEquals(0, eventBus.getListenerCount());
    }

    @Test
    void testPublish() {
        TestEventListener listener = new TestEventListener();
        eventBus.subscribe(listener);

        CommandEvent event = new CommandEvent("test", "args", "file.txt");
        eventBus.publish(event);

        assertEquals(1, listener.receivedEvents.size());
        assertEquals(event, listener.receivedEvents.get(0));
    }

    @Test
    void testPublishNull() {
        TestEventListener listener = new TestEventListener();
        eventBus.subscribe(listener);

        eventBus.publish(null);

        assertEquals(0, listener.receivedEvents.size());
    }

    @Test
    void testPublishToMultipleListeners() {
        TestEventListener listener1 = new TestEventListener();
        TestEventListener listener2 = new TestEventListener();
        eventBus.subscribe(listener1);
        eventBus.subscribe(listener2);

        CommandEvent event = new CommandEvent("test", "args", "file.txt");
        eventBus.publish(event);

        assertEquals(1, listener1.receivedEvents.size());
        assertEquals(1, listener2.receivedEvents.size());
    }

    @Test
    void testPublishWithFailingListener() {
        FailingEventListener failingListener = new FailingEventListener();
        TestEventListener normalListener = new TestEventListener();

        eventBus.subscribe(failingListener);
        eventBus.subscribe(normalListener);

        CommandEvent event = new CommandEvent("test", "args", "file.txt");
        eventBus.publish(event);

        assertEquals(1, normalListener.receivedEvents.size());
    }

    @Test
    void testCommandEvent() {
        CommandEvent event = new CommandEvent("append", "\"test\"", "file.txt");

        assertEquals("append", event.getCommandName());
        assertEquals("\"test\"", event.getArguments());
        assertEquals("file.txt", event.getFilepath());
    }

    @Test
    void testWorkspaceEvent() {
        WorkspaceEvent event = new WorkspaceEvent("fileOpened", "test.txt");

        assertEquals("fileOpened", event.getType());
        assertEquals("test.txt", event.getData());
    }

    private static class TestEventListener implements EventListener {
        List<Event> receivedEvents = new ArrayList<>();

        @Override
        public void onEvent(Event event) {
            receivedEvents.add(event);
        }
    }

    private static class FailingEventListener implements EventListener {
        @Override
        public void onEvent(Event event) {
            throw new RuntimeException("Test exception");
        }
    }
}
