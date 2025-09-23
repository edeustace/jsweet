# TypeScript Namespace-Class Merging Issue

## Problem Description

When JSweet transpiles Java inner classes that reference outer class members, it generates TypeScript code that violates TypeScript's namespace-class merging restrictions.

**Example from `StackTraceCreator.ts`:**

```typescript
export class StackTraceCreator {
    // Line 40: This tries to extend StackTraceCreator.Collector
    public static CollectorLegacy = class CollectorLegacy extends StackTraceCreator.Collector {
        // ...
    }
}

// Line 394: But Collector is defined here in the namespace
export namespace StackTraceCreator {
    export abstract class Collector {
        abstract collect(error: any): void;
        abstract getStackTrace(t: any): StackTraceElement[];
    }
}
```

**Error:** `TS2729: Property 'Collector' is used before its initialization`

**Root Cause:** TypeScript doesn't allow referencing namespace members from within the class that the namespace merges with. This creates a circular reference that TypeScript cannot resolve.

## Solution Options

### Option 1: Separate Files (Cleanest)

**Approach:** Split the namespace content into separate files to avoid merging restrictions.

**Implementation:**
```typescript
// StackTraceCreator.Collector.ts
export abstract class Collector {
    abstract collect(error: any): void;
    abstract getStackTrace(t: any): StackTraceElement[];
}

// StackTraceCreator.ts
import { Collector } from './StackTraceCreator.Collector';

export class StackTraceCreator {
    public static CollectorLegacy = class extends Collector { // Now this works
        collect(error: any): void {
            // implementation
        }
        getStackTrace(t: any): StackTraceElement[] {
            // implementation
        }
    }
}

export namespace StackTraceCreator {
    export { Collector };
}
```

**Pros:**
- Clean separation of concerns
- Follows TypeScript best practices
- No semantic changes to the API

**Cons:**
- Requires significant changes to JSweet transpiler
- Changes file structure
- May affect build tooling

### Option 2: Interface Pattern (Quick Fix)

**Approach:** Replace abstract classes with interfaces to avoid the inheritance issue.

**Implementation:**
```typescript
export interface ICollector {
    collect(error: any): void;
    getStackTrace(t: any): StackTraceElement[];
}

export class StackTraceCreator {
    public static CollectorLegacy = class implements ICollector { // This works
        collect(error: any): void {
            // implementation
        }
        getStackTrace(t: any): StackTraceElement[] {
            // implementation
        }
    }
}

export namespace StackTraceCreator {
    export type Collector = ICollector;
}
```

**Pros:**
- Quick to implement
- Minimal transpiler changes needed
- Resolves the immediate compilation error

**Cons:**
- Changes semantics (interfaces vs abstract classes)
- May break existing code that expects abstract class behavior
- Loses some type safety features of abstract classes

### Option 3: Hoist Base Class (JSweet Adapter)

**Approach:** Generate a different pattern that hoists the base class outside the namespace merging.

**Implementation:**
```typescript
// Generate this pattern instead:
abstract class StackTraceCreatorCollector { // Separate top-level class
    abstract collect(error: any): void;
    abstract getStackTrace(t: any): StackTraceElement[];
}

export class StackTraceCreator {
    public static CollectorLegacy = class extends StackTraceCreatorCollector { // This works
        collect(error: any): void {
            // implementation
        }
        getStackTrace(t: any): StackTraceElement[] {
            // implementation
        }
    }
}

export namespace StackTraceCreator {
    export { StackTraceCreatorCollector as Collector };
}
```

**JSweet Adapter Code:**
```java
public class NamespaceFixAdapter extends PrinterAdapter {
    @Override
    public boolean substituteType(TypeElement type) {
        // Detect inner classes that need hoisting
        if (needsNamespaceHoisting(type)) {
            generateHoistedPattern(type);
            return true;
        }
        return super.substituteType(type);
    }

    private boolean needsNamespaceHoisting(TypeElement type) {
        // Logic to detect problematic namespace-class patterns
        return type.getEnclosingElement() instanceof TypeElement
            && hasAbstractMethods(type)
            && isExtendedByInnerClasses(type);
    }
}
```

**Pros:**
- Preserves original semantics
- Automated solution via JSweet adapter
- No manual file restructuring needed
- Maintains abstract class behavior

**Cons:**
- Requires custom JSweet adapter development
- More complex implementation
- May need debugging for edge cases

## Recommended Approach

**Core JSweet Transpiler Fix** is the best long-term solution because:

This is a **fundamental TypeScript limitation** that affects any Java-to-TypeScript transpiler, not just specific GWT use cases. The fix should be implemented in the core JSweet transpiler itself.

### Why This Belongs in Core JSweet

1. **General TypeScript Issue** - Any Java inner class that references outer class members will hit this limitation
2. **Framework-Agnostic** - Not specific to GWT, affects all JSweet users
3. **Architectural Problem** - The current JSweet pattern of namespace merging is fundamentally incompatible with TypeScript's rules
4. **Upstream Contribution** - This would benefit the entire JSweet community

### Where to Implement

**Target File:** `transpiler/src/main/java/org/jsweet/transpiler/Java2TypeScriptTranslator.java`

**Target Methods:** Those handling:
- Inner class generation
- Namespace emission
- Type declaration ordering

### Implementation Strategy

Instead of generating this problematic pattern:
```typescript
export class Outer {
    static Inner = class extends Outer.SomeType { }  // ❌ Fails - circular reference
}
export namespace Outer {
    export class SomeType { }
}
```

JSweet should detect this pattern and generate:
```typescript
abstract class OuterSomeType { }  // ✅ Hoisted to avoid circular reference

export class Outer {
    static Inner = class extends OuterSomeType { }  // ✅ Works - no circular reference
}
export namespace Outer {
    export { OuterSomeType as SomeType };  // ✅ Preserves API
}
```

### Benefits of Core Implementation

- **Fixes it for everyone** - All JSweet users benefit from the solution
- **Consistent behavior** - Same pattern generated across all transpiled code
- **Future-proof** - Automatically handles new cases as they arise
- **Community contribution** - Could be contributed back to main JSweet project
- **Maintainable** - No need for project-specific adapters or workarounds

## Implementation Steps (Core Transpiler Fix)

1. Identify the methods in `Java2TypeScriptTranslator.java` that handle inner class and namespace generation
2. Add detection logic for the problematic pattern (inner classes extending outer class namespace members)
3. Implement hoisting logic to generate the compatible TypeScript pattern
4. Test with `StackTraceCreator.ts` and other affected files
5. Consider contributing the fix back to the JSweet project

## Alternative Approaches (If Core Fix Not Feasible)

The three adapter-based solutions above remain valid alternatives if modifying the core transpiler isn't immediately feasible.

## Alternative Workarounds

If a full solution isn't immediately feasible:

- **Stub the problematic classes** - Replace with simple implementations
- **Use type assertions** - `as any` to bypass TypeScript errors temporarily
- **Skip compilation** - Exclude problematic files until fixed