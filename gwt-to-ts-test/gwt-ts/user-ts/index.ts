// Import GWT globals first - MUST be before any GWT imports
import "./gwt-globals";

// Now safe to import GWT modules
export const FOO = "bar";
export type { EntryPoint } from "./com/google/gwt/core/client/EntryPoint";
export { ClickEvent } from "./com/google/gwt/event/dom/client/ClickEvent";
export type { ClickHandler } from "./com/google/gwt/event/dom/client/ClickHandler";
export { Window } from "./com/google/gwt/user/client/Window";
export { Button } from "./com/google/gwt/user/client/ui/Button";
export { RootPanel } from "./com/google/gwt/user/client/ui/RootPanel";
