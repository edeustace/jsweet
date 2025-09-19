
# Good - we want to print out this typescript for this java:

```java
package source.structural;

public class GwtEventTest {
    
    public static class Type  {
        
        public Type() {
            super();
        }
        
        public String getEventName() {
            return "GwtEventTest";
        }
    }
}
```

# Good

```typescript
export class GwtEventTest {

o
    public static Type = class {
        public constructor() {
        }

        public getEventName(): string {
            return "GwtEventTest";
        }
    }
}
```
# Bad

This is what currently happens if we just call `print(def)` in  Java2TypeScriptTranslator.java.

```typescript

/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
export class GwtEventTest {


    export class Type {
    public constructor() {
    }

    public getEventName(): string {
        return "GwtEventTest";
    }
}
Type["__class"] = "source.structural.GwtEventTest.Type";
}
GwtEventTest["__class"] = "source.structural.GwtEventTest";



```