package de.staticred.discordbot.api;

import de.staticred.discordbot.util.Debugger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class EventManager {

    List<Listener> eventList = new ArrayList<>();

    public static EventManager instance = new EventManager();

    public void registerEvent(Listener event) {
        eventList.add(event);
    }

    public void removeEvent(Listener event) {
        eventList.remove(event);
    }

    public List<Listener> getEvents() {
        return eventList;
    }

    public void fireEvent(Event event) {
        for(Listener listener : eventList) {
            for(final Method method : listener.getClass().getMethods()) {
                 if(method.isAnnotationPresent(BotEvent.class)) {
                     if(method.getParameters().length != 1) {
                         Debugger.debugMessage("Illegal Event structure. Only use 1 argument per Event Method.");
                         return;
                     }
                     for(final Parameter parameter : method.getParameters()) {
                        if(Event.class.isAssignableFrom(parameter.getType())) {
                            if(parameter.getType().getName().equalsIgnoreCase(event.getClass().getName())) {
                                try {
                                    method.invoke(listener,event);
                                    Debugger.debugMessage("Method " + method.getName() + " fired!");
                                } catch (IllegalAccessException e) {
                                    Debugger.debugMessage("Method is inaccessible, please check the following lines.");
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    Debugger.debugMessage("Invalid method structure, please check the following lines.");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
