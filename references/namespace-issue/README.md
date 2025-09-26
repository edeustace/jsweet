# TypeScript Namespace-Class Merging Issue

## Problem

JSweet generates TypeScript code that violates TypeScript's namespace-class merging restrictions, resulting in compilation error **TS2729: Property 'Collector' is used before its initialization**.

## Example

### Original Java Code (simplified)
```java
public class StackTraceCreator {
    
    abstract static class Collector {
        public abstract void collect(Object error);
    }
    
    static class CollectorLegacy extends Collector {
        @Override
        public void collect(Object error) {
            // implementation
        }
    }
}
```

### Generated TypeScript (problematic)
```typescript
export class StackTraceCreator {
    
    // Line 40: Error TS2729 - Collector is used before initialization
    public static CollectorLegacy = class CollectorLegacy extends StackTraceCreator.Collector {
        public collect(error: any) {
            // implementation
        }
    }
}

// Line 394: Collector is defined here in the namespace (after usage)
export namespace StackTraceCreator {
    export abstract class Collector {
        // Members would be generated here
    }
}
```

## The Issue

TypeScript doesn't allow a class to reference its own namespace members during class definition. When `StackTraceCreator.CollectorLegacy` tries to extend `StackTraceCreator.Collector` at line 40, the `Collector` class hasn't been initialized yet because it's defined in the namespace at line 394.

This creates a circular dependency that TypeScript cannot resolve, even though the code structure is valid in Java.

## Files

- `StackTraceCreator.java` - Original Java source
- `StackTraceCreator.ts` - Generated TypeScript with the issue
- `sample.patch` - Shows a previous attempt to fix this by hoisting classes
- `hoisting-implementation-plan.md` - Plan to make hoisting configurable