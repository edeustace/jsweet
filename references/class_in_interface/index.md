
# Class in interface

[ERROR] method '<init>' cannot define a body in interface 'HasHorizontalAlignment' (try 'ab
│ stract' or 'native' modifiers)
1294   │   at /Users/ed.eustace/dev/github.com/ed-eustace/jsweet/gwt-to-ts-test/gwt-2.11.0/user/src/
│ com/google/gwt/user/client/ui/HasHorizontalAlignment.java(57,5)

```java
public interface HasHorizontalAlignment {

    /**
     * Type for values defined and used in {@link HasAutoHorizontalAlignment}.
     * Defined here so that HorizontalAlignmentConstant can be derived from it,
     * thus allowing HasAutoHorizontalAlignment methods to accept and return both
     * AutoHorizontalAlignmentConstant and HorizontalAlignmentConstant values -
     * without allowing the methods defined here to accept or return
     * AutoHorizontalAlignmentConstant values.
     */
    public static class AutoHorizontalAlignmentConstant {
        // The constructor is package-private to prevent uncontrolled inheritance
        // and instantiation of this class.
        AutoHorizontalAlignmentConstant() { // <------ this is the issue
        }
    }
}
```

The fix - classes inside of interfaces need to moved to the interface namespace instead.