/* 
 * JSweet - http://www.jsweet.org
 * Copyright (C) 2015 CINCHEO SAS <renaud.pawlak@cincheo.fr>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package source.structural;

import static jsweet.util.Lang.$export;

/**
 * Test for inner static class with generics transpilation.
 * This test verifies that GwtEvent.Type<H> style patterns are properly transpiled.
 */
public class InnerStaticClassWithGenerics {

    public static void main(String[] args) {
        // Create instances to test the inner class with generics
        GwtEventTest<String> stringEvent = new GwtEventTest<>();
        GwtEventTest.Type<String> stringType = new GwtEventTest.Type<>();
        
        stringEvent.setAssociatedType(stringType);
        
        // Test the inner class functionality
        $export("typeCreated", stringType != null);
        $export("eventCreated", stringEvent != null);
        $export("typeAssigned", stringEvent.getAssociatedType() != null);
    }
}

/**
 * Base event class similar to com.google.web.bindery.event.shared.Event
 */
abstract class BaseEvent<H extends EventHandler> {
    
    /**
     * Base type class for events
     */
    public static class Type<H> {
        private String name;
        
        public Type() {
            this.name = "BaseType";
        }
        
        public String getName() {
            return name;
        }
    }
    
    private Type<H> associatedType;
    
    public void setAssociatedType(Type<H> type) {
        this.associatedType = type;
    }
    
    public Type<H> getAssociatedType() {
        return associatedType;
    }
}

/**
 * Event handler interface
 */
interface EventHandler {
    void onEvent();
}

/**
 * Test class that mimics GwtEvent structure
 */
class GwtEventTest<H extends EventHandler> extends BaseEvent<H> {
    
    /**
     * Type class used to register events.
     * This mimics the GwtEvent.Type<H> pattern that should transpile to proper TypeScript.
     */
    public static class Type<H> extends BaseEvent.Type<H> {
        
        public Type() {
            super();
        }
        
        public String getEventName() {
            return "GwtEventTest";
        }
    }
    
    private boolean dead = false;
    
    public boolean isLive() {
        return !dead;
    }
    
    public void kill() {
        dead = true;
    }
}