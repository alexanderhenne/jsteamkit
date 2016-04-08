package com.jsteamkit.event;

import java.util.ArrayList;
import java.util.List;

public class EventListener {

    private final List<EventHandler> eventHandlers = new ArrayList<>();

    public void registerHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.add(eventHandler);
        }
    }

    public void unregisterHandler(EventHandler eventHandler) {
        synchronized (eventHandlers) {
            eventHandlers.remove(eventHandler);
        }
    }

    public void runHandlers(byte[] data) {
        synchronized (eventHandlers) {
            for (EventHandler eventHandler : eventHandlers) {
                eventHandler.handle(data);
            }
        }
    }
}
