// Test the full GWT sample app with UI components step by step
//
import { Hello } from "./com/google/gwt/sample/hello/client/Hello.js";

async function main() {
  console.log("Starting full GWT sample app test...");

  try {
    // // Test basic JavaScript functionality
    // console.log('Basic JS test passed');

    // // Test importing individual dependencies first
    // console.log('Testing EntryPoint import...');
    // const { EntryPoint } = await import('../user-ts/com/google/gwt/core/client/EntryPoint.js');
    // console.log('EntryPoint imported successfully');

    // console.log('Testing ClickEvent import...');
    // const { ClickEvent } = await import('../user-ts/com/google/gwt/event/dom/client/ClickEvent.js');
    // console.log('ClickEvent imported successfully');

    // console.log('Testing ClickHandler import...');
    // const { ClickHandler } = await import('../user-ts/com/google/gwt/event/dom/client/ClickHandler.js');
    // console.log('ClickHandler imported successfully');

    // console.log('Testing Button import...');
    // const { Button } = await import('../user-ts/com/google/gwt/user/client/ui/Button.js');
    // console.log('Button imported successfully');

    // console.log('Testing RootPanel import...');
    // const { RootPanel } = await import('../user-ts/com/google/gwt/user/client/ui/RootPanel.js');
    // console.log('RootPanel imported successfully');

    // // Now test the Hello class
    // console.log('Testing Hello class import...');
    // const { Hello } = await import('./com/google/gwt/sample/hello/client/Hello.js');
    // console.log('Hello class imported successfully');

    const helloApp = new Hello();
    console.log("Hello class instantiated");

    helloApp.onModuleLoad();
    console.log("✅ GWT sample app loaded successfully!");
  } catch (error) {
    console.error("❌ Test failed:", error);
    console.error("Error details:", error.message);
    console.error("Stack trace:", error.stack);
  }
}

// Run the main function
main();
