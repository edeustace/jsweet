# Implementation Plan: Making Inner Class Hoisting Configurable

## Important Context: Hoisting Was Already Attempted and Removed!

Looking at `sample.patch`, there WAS a hoisting implementation that was removed. The patch shows the removal of:

### What Was Removed (True Hoisting):
1. **Hoisted abstract inner classes OUTSIDE the containing class** (lines 35-103 of patch)
   - Abstract inner classes were generated at the top level
   - They were printed as standalone classes outside their enclosing class

2. **Special handling for hoisted references** (lines 109-147, 155-161, 169-175)
   - `isHoistedAbstractClassReference()` - detected references to hoisted classes  
   - `printTypeTree()` - handled printing references to hoisted classes
   - Logic to avoid prefixing hoisted abstract class names

3. **The hoisting logic** (lines 18-28)
   - Non-static abstract inner classes were being collected for hoisting
   - They were added to `staticInnerAbstractClasses` for special handling

### What Replaced It:
- All static inner classes go back into namespaces (standard TypeScript pattern)
- No special handling for abstract classes
- The "fix" was abandoned in favor of the namespace approach

## Current State Analysis

Looking at the current code (post-patch):
1. **Static inner classes as static properties**: Still implemented (lines 1938-1954)
2. **Abstract static inner classes in namespaces**: Still implemented (lines 2350-2387)
3. **True hoisting**: REMOVED

## The Problem We Need to Solve

We need to make the REMOVED hoisting feature configurable so that:
- **Default behavior**: No hoisting (current state after patch was applied)
- **Optional behavior**: Re-enable hoisting to fix circular reference issues

## Implementation Steps

### 1. Add Configuration Option

#### 1.1 JSweetOptions.java
```java
/**
 * Constant string for the 'hoistAbstractInnerClasses' option.
 */
String hoistAbstractInnerClasses = "hoistAbstractInnerClasses";

// Add to OPTIONS array
hoistAbstractInnerClasses,

/**
 * Tells if abstract inner classes should be hoisted outside their
 * enclosing class to avoid TypeScript namespace-class merging issues.
 * This was previously attempted but removed - this flag re-enables it.
 */
boolean isHoistAbstractInnerClasses();
```

#### 1.2 JSweetTranspiler.java
```java
private boolean hoistAbstractInnerClasses = false;

// In parseOptions()
if (options.containsKey(JSweetOptions.hoistAbstractInnerClasses)) {
    setHoistAbstractInnerClasses(getMapValue(options, JSweetOptions.hoistAbstractInnerClasses));
}

@Override
public boolean isHoistAbstractInnerClasses() {
    return hoistAbstractInnerClasses;
}

public void setHoistAbstractInnerClasses(boolean hoistAbstractInnerClasses) {
    this.hoistAbstractInnerClasses = hoistAbstractInnerClasses;
}
```

### 2. Re-implement the Removed Hoisting Code (Conditionally)

#### 2.1 Restore Abstract Class Collection (lines 1938-1959)
```java
if (def instanceof ClassTree) {
    ClassTree innerClass = (ClassTree) def;
    // ... existing interface handling ...
    
    if (innerClass.getModifiers().getFlags().contains(Modifier.STATIC)) {
        // ... existing static handling ...
    } else {
        // RE-ADD the hoisting logic (that was removed in the patch)
        if (context.options.isHoistAbstractInnerClasses() && 
            innerClass.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
            // Non-static abstract inner classes are collected for hoisting
            getScope().staticInnerAbstractClasses.add(innerClass);
        } else {
            // non-hoisted inner types handled normally
        }
    }
    continue;
}
```

#### 2.2 Restore Hoisting Generation (lines 2350+)
```java
if (context.options.isHoistAbstractInnerClasses()) {
    // RESTORE the hoisting code (from before the patch)
    // Hoist abstract inner classes outside the containing class
    for (ClassTree abstractClass : getScope().staticInnerAbstractClasses) {
        println().println().printIndent();
        
        if (!isTopLevelScope() || context.useModules || context.moduleBundleMode) {
            print("export ");
        }
        
        // Add abstract modifier for abstract classes
        if (abstractClass.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
            print("abstract ");
        }
        
        print("class ").print(abstractClass.getSimpleName().toString());
        
        // Handle extends clause if any
        if (abstractClass.getExtendsClause() != null) {
            print(" extends ");
            print(abstractClass.getExtendsClause());
        }
        
        // Handle implements clause if any
        if (abstractClass.getImplementsClause() != null && !abstractClass.getImplementsClause().isEmpty()) {
            print(" implements ");
            printArgList(null, abstractClass.getImplementsClause());
        }
        
        print(" {").startIndent();
        
        // Generate the actual class members
        for (Tree member : abstractClass.getMembers()) {
            if (member instanceof MethodTree || member instanceof VariableTree) {
                println().printIndent();
                print(member);
            }
        }
        
        println().endIndent().printIndent().print("}").println();
    }
} else {
    // CURRENT behavior - put them in namespaces
    for (ClassTree staticClass : getScope().staticInnerAbstractClasses) {
        // ... existing namespace generation code ...
    }
}
```

#### 2.3 Restore Reference Handling Methods
```java
/**
 * Check if a member select tree represents a reference to a hoisted abstract inner class.
 * RESTORE this method that was removed in the patch.
 */
private boolean isHoistedAbstractClassReference(MemberSelectTree memberSelectTree) {
    if (!context.options.isHoistAbstractInnerClasses()) {
        return false;
    }
    
    Element memberElement = Util.getElement(memberSelectTree);
    Element selectedTypeElement = Util.getTypeElement(memberSelectTree.getExpression());
    
    if (memberElement instanceof TypeElement) {
        TypeElement memberClass = (TypeElement) memberElement;
        if (memberClass.getEnclosingElement() instanceof TypeElement &&
            memberClass.getModifiers().contains(Modifier.ABSTRACT)) {
            if (selectedTypeElement != null && 
                selectedTypeElement.equals(memberClass.getEnclosingElement())) {
                return true;
            }
        }
    }
    return false;
}

/**
 * Print a type tree, handling hoisted abstract class references.
 * RESTORE this method that was removed in the patch.
 */
private AbstractTreePrinter printTypeTree(Tree typeTree) {
    if (context.options.isHoistAbstractInnerClasses() && 
        typeTree instanceof MemberSelectTree) {
        MemberSelectTree memberSelectTree = (MemberSelectTree) typeTree;
        if (isHoistedAbstractClassReference(memberSelectTree)) {
            // For hoisted abstract classes, print only the class name
            return print(memberSelectTree.getIdentifier().toString());
        }
    }
    return print(typeTree);
}
```

#### 2.4 Restore Reference Handling in visitMemberSelect
Around line 4319 (after patch):
```java
if (!accessSubstituted) {
    if (context.options.isHoistAbstractInnerClasses() && 
        isHoistedAbstractClassReference(memberSelectTree)) {
        // For hoisted abstract classes, use direct reference
        // Don't print the expression part, just the class name
    } else {
        print(memberSelectTree.getExpression()).print(".");
    }
}
```

#### 2.5 Restore Prefix Handling
Around line 5000 (after patch):
```java
// Check if this is a hoisted abstract inner class
boolean isHoistedAbstract = context.options.isHoistAbstractInnerClasses() &&
    classIdentifierTypeElement.getEnclosingElement() instanceof TypeElement &&
    classIdentifierTypeElement.getModifiers().contains(Modifier.ABSTRACT);

// add parent class name if ident is an inner class of the current class
// (but not if it's a hoisted abstract class)
if (!prefixAdded && !isHoistedAbstract && 
    classIdentifierTypeElement.getEnclosingElement() instanceof TypeElement) {
    // ... existing prefix logic ...
}
```

### 3. Also Need to Handle printTypeTree Calls
Around line 1465:
```java
if (context.options.isHoistAbstractInnerClasses()) {
    return printTypeTree(typeTree);  // Use special handling
} else {
    return print(typeTree);  // Direct print (current behavior)
}
```

## Summary

The confusion arose because:
1. **Hoisting WAS implemented** - Abstract inner classes were moved outside their enclosing classes
2. **It was REMOVED** - The `sample.patch` shows this removal
3. **Current state**: No hoisting, everything uses namespaces

The task is to:
1. **Make hoisting configurable** - Add a flag to optionally re-enable it
2. **Restore the removed code** - But only when the flag is enabled
3. **Keep current behavior as default** - For backward compatibility

## Testing

When `hoistAbstractInnerClasses=true`:
- Should generate abstract inner classes at top level
- Should handle references correctly (no qualified names)
- Should fix circular reference issues

When `hoistAbstractInnerClasses=false` (default):
- Should use current namespace approach
- Maintains backward compatibility