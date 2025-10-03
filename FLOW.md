# Flow

This document describes the development workflow that you *MUST* follow to achieve success.

For full context on the purpose of this repo read: ./CLAUDE.md.

To achieve Success - we have to iterate through each issue. Fixing one issue will reveal another, so really it's like an onion skin approach we're taking.

## Success

Success for this development flow is:

1. Running the dev server and running the `sample-ts` application (that uses `gwt-ts`) and for there to be: 
  a. **NO** Runtime errors
  b. A button renders on the page that says 'Click me' (see gwt-to-ts-test/gwt-ts/sample-ts/com/google/gwt/sample/hello/client/Hello.ts). This button is rendered by the gwt-ts runtime.


## Steps to iterate on

To Achieve success we have to iterate through the steps below, fixing 1 error at a time until they are all gone.

```text
[JSWEET-SRC]--cli-depends-on-->[JSWEET-CLI]--mvn exec-->[GWT-TS]-->[PNPM-VITE-BUILD]-->[PNPM-VITE-DEV]-->[LOCALHOST:5173]-->[CHECK-RUNTIME]-->[CHECK-UI]
```

1. Compile the JSWEET cli - to get the latest java logic in
2. Run the JSWEET cli - to generate the typescript for gwt-ts 
3. Run `pnpm vite build sample-ts` - to build the js app from the ts files
4. Run `pnpm vite dev sample-ts` - to start the dev server
5. Open the browser and check for runtime errors


### Pipeline commands

1. Compile CLI

```bash
mvn compile -pl transpiler -DskipTests
cd cli && mvn clean compile -DskipTests -q && cd ../
```

2. Compile GWT Java -> TS
```bash
mvn exec:java -pl cli -Dexec.args="--output gwt-to-ts-test/gwt-ts/user-ts --target ES5 --excludes *webgl* --excludes *websocket* --excludes *hibernate* --excludes *javax/validation* --excludes *validation* --excludes *logging --excludes *i18n* --excludes *rpc* --excludes *requestfactory* --excludes *autobean* --excludes *editor* --excludes *safehtml* --excludes *aria* --excludes dom/builder --excludes *junit* --excludes **/server/** --excludes **/vm/** --excludes **/touch/** --excludes *bindery/autobean* --excludes *bindery/requestfactory* gwt-to-ts-test/gwt-2.11.0/user/src"
```
> Note that you can adjust the excludes here if you need to (aka if a module is not found and it's safe to pull in).

You may get an error at any of these stages. For each error you get you are going to follow the same pattern: 

> If the error is a java compile error eg just a minor mistake mid fix, you don't need to create a document for it. It's really more fundamental errors about the Java->TS conversion and getting GWT TS running that we need documented.

1. document the error
2. document thoughts
3. fix the issues
  a. If it's a java fix in the transpiler - add a test for it in our SnapshotTest.java file. link to test in document.
4. document the fix
5. commit the fix to the git repo

Once fixed. Start the dev pipeline again and see what the next error is.

### Documenting errors

For each error that you get create a markdown document using this type of path: 
`./thoughts/errors/001_module_not_found.md`. you must use an incrementing number and a brief name with underscore for the file name.

In this document: 

- describe the error
- where in the pipeline it happened
- how you fixed it
- the commit hash for the fix

1. The commands/apps you need to run to check parts of the pipeline.
2. Categorises the places where you'll see errors
3. The catogories of the errors themselves, and where to fix them.
4. Some strict rules on where to fix the errors in the codebase and how to decide on the best location.

### Error Categories and fix strategies

1. GWT Java source is using a java construct or language feature that jsweet doesn't support.
  a. We'll need to add support to JSweet for this construct 
  b. Note that jsweet will make a best effort transpilation, so you'll often see these in `vite build` or `vite dev` + browser.
2. Java compile error in cli/transpiler
  a. You've made an error in jsweet - fix in there

3. vite build error - ts error 


## Rules that must be followed 

1. **Never** fix the generated typescript in gwt-to-ts-test/gwt-ts. 
