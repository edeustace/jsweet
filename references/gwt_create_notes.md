# GWT.create() Analysis and TypeScript Port Strategy

## What is GWT.create()?

`GWT.create()` is GWT's **compile-time dependency injection system**. It's not runtime reflection - the GWT compiler completely replaces these calls with direct instantiations of the correct implementation.

## How It Works in Original GWT

### Compile-Time Process
1. `GWT.create(SomeClass.class)` is analyzed by the GWT compiler
2. Compiler looks at deferred binding rules and browser properties
3. Generates the correct implementation based on:
   - Browser type (IE, WebKit, Firefox, etc.)
   - Browser version
   - Feature support (Canvas, CSS3, etc.)
   - Locale/i18n settings
4. Replaces the call with `new ConcreteImplementation()`
5. No actual "create" method exists in final JavaScript

### Example Transformations
```java
// Source code:
static final DOMImpl impl = GWT.create(DOMImpl.class);

// Gets replaced with (depending on target browser):
static final DOMImpl impl = new DOMImplWebkit();     // Chrome/Safari
static final DOMImpl impl = new DOMImplMozilla();    // Firefox  
static final DOMImpl impl = new DOMImplTrident();    // IE
```

## Current Usage in GWT Codebase

From analysis of `gwt-2.11.0/user/src`:

### Core Infrastructure (Essential for rendering)
```java
static final DOMImpl impl = GWT.create(DOMImpl.class);                    // DOM.java:65
private static final WindowImpl impl = GWT.create(WindowImpl.class);      // Window.java
private static HistoryImpl impl = GWT.create(HistoryImpl.class);          // History.java
```

### Feature Detection
```java
detector = GWT.create(CanvasElementSupportDetector.class);               // Canvas.java
```

### UI Component Implementations
```java
private static CaptionPanelImpl impl = GWT.create(CaptionPanelImpl.class);  // CaptionPanel.java
private static final HTMLTableImpl impl = GWT.create(HTMLTableImpl.class); // HTMLTable.java
private static final ClippedImageImpl impl = GWT.create(ClippedImageImpl.class); // Image.java
```

### Resource Systems
```java
private static final Resources DEFAULT_RESOURCES = GWT.create(Resources.class); // Tree.java
```

## Strategy for TypeScript Port

### Key Insight: Modern Evergreen Browsers Only
Since we're targeting modern evergreen browsers (Chrome, Firefox, Safari, Edge), we can **dramatically simplify** the GWT.create() system:

- **No IE-specific implementations needed**
- **No legacy browser workarounds** 
- **No complex feature detection** - assume modern APIs exist
- **Canvas, CSS3, HTML5 APIs** - all available

### Implementation Options

#### Option 1: Simple Factory Pattern (Recommended)
```typescript
class GWTFactory {
  static create<T>(classType: any): T {
    // Simple mapping for essential implementations
    if (classType === DOMImpl) {
      return new DOMImplStandard() as T;
    }
    if (classType === WindowImpl) {
      return new WindowImplStandard() as T;
    }
    if (classType === CanvasElementSupportDetector) {
      return new CanvasElementSupportedDetector() as T; // Always supported
    }
    
    // Default: return a basic implementation
    throw new Error(`No implementation found for ${classType.name}`);
  }
}
```

#### Option 2: Direct Replacement via JSweet Adapters
Use JSweet adapters to replace `GWT.create()` calls during transpilation:
```java
// Original:
static final DOMImpl impl = GWT.create(DOMImpl.class);

// Adapter transforms to:
static final DOMImpl impl = new DOMImplStandard();
```

#### Option 3: Static Analysis + Code Generation
Pre-analyze the codebase to find all `GWT.create()` calls and generate a mapping file.

### What Can Be Simplified/Stubbed

#### Browser-Specific Implementations → Single Modern Implementation
- `DOMImpl*` classes → Use single `DOMImplStandard`
- `WindowImpl*` classes → Use single `WindowImplStandard`  
- User agent detection → Always assume modern browser

#### Feature Detection → Always Enabled
- Canvas support → Always true
- CSS3 features → Always true
- HTML5 APIs → Always true
- Touch events → Use standard event detection

#### Complex Systems → Stub or Simplify
- I18N/Localization → Stub with English defaults
- Resource optimization → Use simple resource loading
- RPC serialization → May not be needed for UI rendering

## Implementation Plan

### Phase 1: Identify Essential GWT.create() Usage
1. Analyze all `GWT.create()` calls in core rendering path
2. Categorize: Essential vs. Optional vs. Stubbable
3. Create mapping of Interface → Single Modern Implementation

### Phase 2: Implement TypeScript Factory
1. Create `GWTFactory.create()` method
2. Map essential interfaces to modern implementations
3. Stub out optional/complex features

### Phase 3: Update JSweet Adapters
1. Add adapter to replace `GWT.create()` calls
2. Either call TypeScript factory or direct instantiation
3. Remove unused browser-specific implementations

### Phase 4: Testing & Refinement
1. Test with simple Hello World rendering
2. Identify missing implementations
3. Add implementations as needed for core functionality

## Benefits of This Approach

1. **Simplified codebase** - Remove thousands of lines of legacy browser code
2. **Modern APIs** - Use native browser capabilities instead of polyfills
3. **Faster development** - Single code path instead of multiple browser variants
4. **Easier maintenance** - No complex deferred binding rules
5. **Better performance** - No runtime feature detection overhead

## Files to Reference

- **Core GWT.create() logic**: `gwt-2.11.0/user/src/com/google/gwt/core/shared/GWT.java:60-85`
- **DOM implementation**: `gwt-2.11.0/user/src/com/google/gwt/user/client/DOM.java:65`
- **Usage examples**: Found via `grep -r "GWT\.create" gwt-to-ts-test/gwt-2.11.0/user/src/`

## Next Steps

1. Create list of all `GWT.create()` calls in core rendering path
2. Identify which browser-specific implementations can be merged
3. Implement basic TypeScript factory for essential cases
4. Test with Hello World to validate approach