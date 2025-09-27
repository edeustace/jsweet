package test;

import com.google.gwt.core.client.GWT;

public class TestGWT {
    public void testMethod() {
        if (GWT.isClient()) {
            System.out.println("Running on client");
        } else {
            System.out.println("Running on server");
        }
    }
}