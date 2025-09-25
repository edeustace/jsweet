package org.jsweet.cli.extension;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.lang.model.element.Element;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.jsweet.transpiler.extension.PrinterAdapter;
import org.jsweet.transpiler.model.ExtendedElement;
import org.jsweet.transpiler.model.MethodInvocationElement;

/**
 * Unit tests for GWTCreateAdapter.
 */
public class GWTCreateAdapterTest {

    @Mock
    private PrinterAdapter parentAdapter;

    @Mock
    private MethodInvocationElement mockInvocation;

    @Mock
    private ExtendedElement mockTargetExpression;

    @Mock
    private Element mockTargetType;

    @Mock
    private ExtendedElement mockArgument;

    private GWTCreateAdapter adapter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new GWTCreateAdapter(parentAdapter);
    }

    @Test
    public void testGWTCreateWithMapping() {
        // Setup: GWT.create(Foo.class) should become new FooImpl()
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.GWT");
        when(mockInvocation.getArgumentCount()).thenReturn(1);
        when(mockInvocation.getArgument(0)).thenReturn(mockArgument);
        when(mockArgument.toString()).thenReturn("source.gwt.Foo.class");

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should substitute with mapped impl
        assertTrue("Should substitute the method invocation", result);
        verify(parentAdapter, never()).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testGWTCreateWithFallback() {
        // Setup: GWT.create(UnmappedClass.class) should become new UnmappedClass()
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.GWT");
        when(mockInvocation.getArgumentCount()).thenReturn(1);
        when(mockInvocation.getArgument(0)).thenReturn(mockArgument);
        when(mockArgument.toString()).thenReturn("com.example.UnmappedClass.class");

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should substitute using fallback (simple class name)
        assertTrue("Should substitute the method invocation", result);
        verify(parentAdapter, never()).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testGWTCreateWithSimpleNameFallback() {
        // Setup: GWT.create(SimpleClass.class) should become new SimpleClass()
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.GWT");
        when(mockInvocation.getArgumentCount()).thenReturn(1);
        when(mockInvocation.getArgument(0)).thenReturn(mockArgument);
        when(mockArgument.toString()).thenReturn("SimpleClass.class");

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should substitute using fallback (simple class name)
        assertTrue("Should substitute the method invocation", result);
        verify(parentAdapter, never()).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testNonGWTMethod() {
        // Setup: SomeOther.create() should not be substituted
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.SomeOtherClass");
        when(parentAdapter.substituteMethodInvocation(mockInvocation)).thenReturn(false);

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should delegate to parent adapter
        assertFalse("Should not substitute non-GWT method", result);
        verify(parentAdapter).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testNonCreateMethod() {
        // Setup: GWT.someOtherMethod() should not be substituted
        when(mockInvocation.getMethodName()).thenReturn("someOtherMethod");
        when(parentAdapter.substituteMethodInvocation(mockInvocation)).thenReturn(false);

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should delegate to parent adapter
        assertFalse("Should not substitute non-create method", result);
        verify(parentAdapter).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testAddGWTTypeMapping() {
        // Setup a custom mapping
        adapter.addGWTTypeMapping("com.example.CustomInterface", "com.example.CustomImpl");

        // Setup: GWT.create(CustomInterface.class) should become new CustomImpl()
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.GWT");
        when(mockInvocation.getArgumentCount()).thenReturn(1);
        when(mockInvocation.getArgument(0)).thenReturn(mockArgument);
        when(mockArgument.toString()).thenReturn("com.example.CustomInterface.class");

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should substitute with custom mapping
        assertTrue("Should substitute with custom mapping", result);
        verify(parentAdapter, never()).substituteMethodInvocation(mockInvocation);
    }

    @Test
    public void testNonClassLiteralArgument() {
        // Setup: GWT.create(someVariable) should not be substituted
        when(mockInvocation.getMethodName()).thenReturn("create");
        when(mockInvocation.getTargetExpression()).thenReturn(mockTargetExpression);
        when(mockTargetExpression.getTypeAsElement()).thenReturn(mockTargetType);
        when(mockTargetType.toString()).thenReturn("com.example.GWT");
        when(mockInvocation.getArgumentCount()).thenReturn(1);
        when(mockInvocation.getArgument(0)).thenReturn(mockArgument);
        when(mockArgument.toString()).thenReturn("someVariable");
        when(parentAdapter.substituteMethodInvocation(mockInvocation)).thenReturn(false);

        // Test
        boolean result = adapter.substituteMethodInvocation(mockInvocation);

        // Verify - should delegate to parent adapter
        assertFalse("Should not substitute non-class literal", result);
        verify(parentAdapter).substituteMethodInvocation(mockInvocation);
    }
}