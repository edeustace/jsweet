# GWT User Runtime Analysis: TypeScript Porting Strategy

## Executive Summary

This analysis evaluates the GWT user codebase (`gwt-2.11.0/user`) for porting to TypeScript to create a modern GWT runtime that works with vite/esbuild. The codebase contains **3,769 Java files** across multiple functional areas, but not all are required for basic runtime functionality.

## 1. Is GWT User the Complete Runtime?

**✅ YES** - The GWT user codebase contains the complete client-side runtime needed for GWT applications to run in browsers, including:

- **Core runtime infrastructure** (EntryPoint, GWT class, module loading)
- **DOM manipulation and widgets** (Element, Widget hierarchy, panels)
- **Event system** (DOM events, custom events, handlers)
- **RPC framework** (client-server communication)
- **I18N system** (internationalization support)
- **Client-side utilities** (JSON, XML, HTTP, storage APIs)

**Missing from user code:** Only the GWT compiler itself (`gwt-dev.jar`) which processes Java code at build time.

## 2. Runtime vs Compiler Intersection Points

### 2.1 Compile-Time Only Components (Can be Stubbed/Skipped)

**Rebind System (`**/rebind/**` directories - 12 packages):**
- Generator-based code generation (replaced by build tools)
- UiBinder XML template processing
- I18N message compilation
- ClientBundle resource bundling
- RPC proxy generation
- Bean validation rule generation

**Linker System:**
- Code splitting logic
- Permutation selection
- Resource optimization

**Development Mode Infrastructure:**
- `GWTBridge` development mode bridge
- DevMode-specific logging and debugging
- Module refresh mechanisms

### 2.2 Runtime Components (Must Port)

**Core Infrastructure:**
```
com/google/gwt/core/client/
├── EntryPoint.java           # Application entry point interface
├── GWT.java                  # Core runtime API (deferred binding, logging)
├── Scheduler.java            # Task scheduling and browser event loop
├── Duration.java             # Time utilities
├── JavaScriptObject.java    # JavaScript object overlay
├── JavaScriptException.java # JS error handling
└── impl/
    ├── Impl.java            # Core runtime implementation (JSNI heavy)
    └── SchedulerImpl.java   # Scheduler implementation
```

**DOM Foundation:**
```
com/google/gwt/dom/client/
├── Document.java            # Document root access
├── Element.java             # HTML element abstraction (300+ JSNI methods)
├── Node.java                # DOM node base class
├── DOMImpl.java             # Cross-browser DOM abstraction
└── Style.java               # CSS style manipulation
```

**Widget System:**
```
com/google/gwt/user/client/ui/
├── UIObject.java            # Base UI class (element, styling)
├── Widget.java              # Base widget (events, attachment)
├── Panel.java               # Container base class
├── RootPanel.java           # Document attachment point
└── [Simple widgets: Label, Button, TextBox, etc.]
```

**Event System:**
```
com/google/gwt/event/
├── dom/client/              # DOM events (click, key, mouse)
├── logical/shared/          # Logical events (value change, selection)
└── shared/                  # Event bus infrastructure
```

## 3. Simple Application Runtime Flow

### 3.1 Bootstrap Sequence
```
1. HTML page loads GWT script
2. Module initialization code runs
3. Entry point classes identified (from .gwt.xml)
4. GWT.init() called
5. EntryPoint.onModuleLoad() invoked
6. Application code creates widgets
7. Widgets attached to RootPanel/DOM
```

### 3.2 Widget Rendering Pipeline
```java
// User creates widget
Label label = new Label("Hello World");

// Widget constructor chain:
Label()
  → LabelBase(false)                    // Creates <div>
  → super(Document.get().createDivElement())
  → UIObject.setElement(element)        // Associates DOM element
  → setStyleName("gwt-Label")          // Adds CSS class

// User attaches to page
RootPanel.get().add(label);

// Attachment chain:
RootPanel.add(widget)
  → ComplexPanel.add()                 // Adds to widget list
  → DOM.appendChild(parent, child)     // JSNI: parent.appendChild(child)
  → Widget.setParent()                 // Sets widget parent
  → Widget.onAttach()                  // Lifecycle hook
  → DOM.setEventListener()             // JSNI: element.__listener = widget
  → Widget.onLoad()                    // Override point for apps
```

### 3.3 Event Handling Flow
```java
// Event registration (in Widget.onAttach())
DOM.setEventListener(element, this)    // JSNI: element.__listener = widget
sinkEvents(Event.ONCLICK)              // JSNI: element.onclick = dispatchEvent

// Event dispatch (browser → GWT)
[Browser Event]
  → Global dispatchEvent() [JSNI]
  → DOM.dispatchEvent()
  → Widget.onBrowserEvent()            // Override point
  → HandlerManager.fireEvent()         // Type-safe event firing
  → Registered handlers invoked
```

## 4. Critical JSNI Dependencies

GWT heavily uses JSNI (JavaScript Native Interface) for browser interaction. Key JSNI components that need TypeScript conversion:

### 4.1 Core DOM Operations
- **Element manipulation:** `element.appendChild()`, `element.removeChild()`
- **Property access:** `element.className`, `element.style.display`
- **Event handling:** `element.onclick = handler`, `event.preventDefault()`
- **Document access:** `document.createElement()`, `document.getElementById()`

### 4.2 Browser API Integration
- **Timer functions:** `setTimeout()`, `setInterval()`
- **Window operations:** `window.location`, `window.open()`
- **Console logging:** `console.log()`, `console.error()`
- **Storage APIs:** `localStorage`, `sessionStorage`

### 4.3 Cross-Browser Compatibility
Modern TypeScript can eliminate most browser compatibility JSNI since:
- Target modern browsers (ES6+)
- Standard DOM APIs now consistent
- TypeScript provides better type checking than JSNI

## 5. Components That Can Be Stubbed/Simplified

### 5.1 High-Level UI Components (Optional for basic runtime)
- **Complex Panels:** `DockPanel`, `SplitLayoutPanel`, `TabPanel`
- **Data Widgets:** `CellTable`, `DataGrid`, `Tree`
- **Advanced Input:** `DatePicker`, `SuggestBox`, `RichTextArea`
- **Dialogs:** `DialogBox`, `PopupPanel`

### 5.2 Framework Features (Can defer/replace)
- **UiBinder:** XML-based UI templates (can use JSX/Vue templates instead)
- **ClientBundle:** Resource bundling (can use Vite/Webpack instead)
- **Code Splitting:** `GWT.runAsync()` (can use dynamic imports instead)
- **RPC:** GWT-RPC protocol (can use REST/GraphQL instead)

### 5.3 Development/Testing Infrastructure
- **JUnit integration:** `GWTTestCase` (use Jest/Vitest instead)
- **Development Mode:** SuperDevMode (use Vite dev server instead)
- **Logging:** `java.util.logging` (use console/Winston instead)

## 6. Recommended Porting Strategy

### Phase 1: Core Runtime (MVP for Hello World)
**Target:** Get a simple `Label` widget to render in browser

**Must Port (~300 files):**
1. **Core Infrastructure:** `EntryPoint`, `GWT`, `Scheduler`
2. **DOM Foundation:** `Document`, `Element`, `Node` (with JSNI→TypeScript conversion)
3. **Basic Widget:** `UIObject`, `Widget`, `RootPanel`, `Label`
4. **Event Basics:** Core event system for attachment/detachment
5. **Style System:** Basic CSS class manipulation

### Phase 2: Essential Widgets (Basic UI)
**Target:** Common widgets working (Button, TextBox, Panel)

**Must Port (~500 additional files):**
1. **Input Widgets:** `Button`, `TextBox`, `CheckBox`
2. **Layout Panels:** `FlowPanel`, `AbsolutePanel`, `Grid`
3. **Event Handlers:** Click, change, key events
4. **Focus Management:** Tab order, focus handling

### Phase 3: Advanced Features (Full Framework)
**Target:** Feature parity with GWT

**Can Port Incrementally:**
1. **Data Widgets:** Tables, trees, lists
2. **Complex Layouts:** Dock, split, tab panels
3. **Communication:** HTTP, JSON parsing
4. **Utilities:** I18N, validation, animation

## 7. TypeScript Conversion Challenges

### 7.1 JSNI Translation
- **~2000+ JSNI methods** across codebase need conversion
- Most are simple property access/method calls
- Complex browser compatibility logic can be simplified

### 7.2 Type System Mapping
- **JavaScriptObject:** Map to TypeScript interface types
- **Overlay Types:** Use TypeScript declaration merging
- **Generics:** Direct mapping to TypeScript generics
- **Events:** Map to TypeScript event interfaces

### 7.3 Module System
- **GWT Modules:** Replace with ES6 modules
- **Entry Points:** Replace with standard main() functions
- **Dependency Injection:** Use modern DI frameworks

## 8. Success Metrics

### Minimum Viable Runtime
```typescript
// Should work after Phase 1
const label = new Label("Hello World");
RootPanel.get().add(label);
// → Renders <div class="gwt-Label">Hello World</div> in DOM
```

### Basic Interactivity
```typescript
// Should work after Phase 2
const button = new Button("Click Me");
button.addClickHandler(() => window.alert("Clicked!"));
RootPanel.get().add(button);
// → Working button with click handler
```

## Conclusion

**The GWT user codebase IS the complete runtime** needed for browser execution. The porting strategy should focus on the core DOM manipulation, widget system, and event handling (~800 files) rather than the full 3,769 files. Many components can be stubbed, replaced with modern alternatives, or deferred to later phases.

The most critical task is converting JSNI code to TypeScript, but this can be largely automated since most JSNI is simple browser API calls that map directly to standard DOM/Browser APIs in TypeScript.