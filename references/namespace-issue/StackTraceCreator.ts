/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
import { GWT } from '../GWT';
import { JavaScriptObject } from '../JavaScriptObject';
import { JsArray } from '../JsArray';
import { JsArrayString } from '../JsArrayString';

/**
 * Encapsulates logic to create a stack trace. This class should only be used in
 * Production Mode.
 * @class
 */
export class StackTraceCreator {
    static __static_initialized: boolean = false;
    static __static_initialize() { if (!StackTraceCreator.__static_initialized) { StackTraceCreator.__static_initialized = true; StackTraceCreator.__static_initializer_0(); } }

    /**
     * Maximum # of frames to look for {@link Throwable#fillInStackTrace()} in the generated stack
     * trace. This is just a safe guard just in case if {@code fillInStackTrace} doesn't show up in
     * the stack trace for some reason.
     */
    static DROP_FRAME_LIMIT: number = 5;

    /**
     * Line number used in a stack trace when it is unknown.
     */
    static LINE_NUMBER_UNKNOWN: number = -1;

    /**
     * Replacement for function names that cannot be extracted from a stack.
     */
    static ANONYMOUS: string = "anonymous";

    /**
     * Replacement for class or file names that cannot be extracted from a stack.
     */
    static UNKNOWN: string = "Unknown";



    public static CollectorLegacy = class CollectorLegacy extends StackTraceCreator.Collector {
        /**
         * 
         * @param {*} error
         */
        public collect(error: any) {
            var seen = {};
      var fnStack = [];
      error["fnStack"] = fnStack;

      // Ignore the collect() call
      var callee = arguments.callee.caller;
      while (callee) {
        var name = StackTraceCreator.getFunctionName(callee);
        fnStack.push(name);

        // Avoid infinite loop by associating names to function objects.  We
        // record each caller in the withThisName variable to handle functions
        // with identical names but separate identity (such as 'anonymous')
        var keyName = ':' + name;
        var withThisName = seen[keyName];
        if (withThisName) {
          var i, j;
          for (i = 0, j = withThisName.length; i < j; i++) {
            if (withThisName[i] === callee) {
              return;
            }
          }
        }

        (withThisName || (seen[keyName] = [])).push(callee);
        callee = callee.caller;
      }
        }

        /**
         * 
         * @param {*} t
         * @return {java.lang.StackTraceElement[]}
         */
        public getStackTrace(t: any): StackTraceElement[] {
            const stack: JsArrayString = StackTraceCreator.getFnStack(t);
            const length: number = stack.length();
            const stackTrace: StackTraceElement[] = (s => { let a=[]; while(s-->0) a.push(null); return a; })(length);
            for(let i: number = 0; i < length; i++) {{
                stackTrace[i] = new StackTraceElement(StackTraceCreator.UNKNOWN, stack.get(i), null, StackTraceCreator.LINE_NUMBER_UNKNOWN);
            };}
            return stackTrace;
        }

        constructor() {
            super();
        }

    }


    public static CollectorEmulated = class CollectorEmulated extends StackTraceCreator.Collector {
        /**
         * 
         * @param {*} error
         */
        public collect(error: any) {
            var seen = {};
      var fnStack = [];
      error["fnStack"] = fnStack;

      // Ignore the collect() call
      var callee = arguments.callee.caller;
      while (callee) {
        var name = StackTraceCreator.getFunctionName(callee);
        fnStack.push(name);

        // Avoid infinite loop by associating names to function objects.  We
        // record each caller in the withThisName variable to handle functions
        // with identical names but separate identity (such as 'anonymous')
        var keyName = ':' + name;
        var withThisName = seen[keyName];
        if (withThisName) {
          var i, j;
          for (i = 0, j = withThisName.length; i < j; i++) {
            if (withThisName[i] === callee) {
              return;
            }
          }
        }

        (withThisName || (seen[keyName] = [])).push(callee);
        callee = callee.caller;
      }
        }

        /**
         * 
         * @param {*} t
         * @return {java.lang.StackTraceElement[]}
         */
        public getStackTrace(t: any): StackTraceElement[] {
            const stack: JsArray<JsArrayString> = <any>(StackTraceCreator.getFnStack(t).cast<any>());
            const stackTrace: StackTraceElement[] = (s => { let a=[]; while(s-->0) a.push(null); return a; })(stack.length());
            for(let i: number = 0; i < stackTrace.length; i++) {{
                const frame: JsArrayString = stack.get(i);
                const name: string = frame.get(0);
                const location: string = frame.get(1);
                let fileName: string = null;
                let lineNumber: number = StackTraceCreator.LINE_NUMBER_UNKNOWN;
                if (location != null){
                    const idx: number = location.indexOf(':');
                    if (idx !== -1){
                        fileName = location.substring(0, idx);
                        lineNumber = StackTraceCreator.parseInt(location.substring(idx + 1));
                    } else {
                        lineNumber = StackTraceCreator.parseInt(location);
                    }
                }
                stackTrace[i] = new StackTraceElement(StackTraceCreator.UNKNOWN, name, fileName, lineNumber);
            };}
            return stackTrace;
        }

        constructor() {
            super();
        }

    }


    public static CollectorModern = class CollectorModern extends StackTraceCreator.Collector {
        /**
         * 
         * @param {*} error
         */
        public collect(error: any) {
        }

        /**
         * 
         * @param {*} t
         * @return {java.lang.StackTraceElement[]}
         */
        public getStackTrace(t: any): StackTraceElement[] {
            const stack: JsArrayString = StackTraceCreator.split(t);
            const stackTrace: StackTraceElement[] = [];
            let addIndex: number = 0;
            const length: number = stack.length();
            if (length === 0){
                return stackTrace;
            }
            const ste: StackTraceElement = this.parse(stack.get(0));
            if (!(ste.getMethodName() === StackTraceCreator.ANONYMOUS)){
                stackTrace[addIndex++] = ste;
            }
            for(let i: number = 1; i < length; i++) {{
                stackTrace[addIndex++] = this.parse(stack.get(i));
            };}
            return stackTrace;
        }

        /**
         * Parses a stack trace line from the browser and returns a new {@link StackTraceElement}
         * constructed with the extracted data.
         * @param {string} stString
         * @return {StackTraceElement}
         * @private
         */
        parse(stString: string): StackTraceElement {
            let location: string = "";
            if (/* isEmpty */(stString.length === 0)){
                return this.createSte(StackTraceCreator.UNKNOWN, StackTraceCreator.ANONYMOUS, StackTraceCreator.LINE_NUMBER_UNKNOWN, -1);
            }
            let toReturn: string = stString.trim();
            if (/* startsWith */((str, searchString, position = 0) => str.substr(position, searchString.length) === searchString)(toReturn, "at ")){
                toReturn = toReturn.substring(3);
            }
            toReturn = this.stripSquareBrackets(toReturn);
            let index: number = toReturn.indexOf("(");
            if (index === -1){
                index = toReturn.indexOf("@");
                if (index === -1){
                    location = toReturn;
                    toReturn = "";
                } else {
                    location = toReturn.substring(index + 1).trim();
                    toReturn = toReturn.substring(0, index).trim();
                }
            } else {
                const closeParen: number = toReturn.indexOf(")", index);
                location = toReturn.substring(index + 1, closeParen);
                toReturn = toReturn.substring(0, index).trim();
            }
            index = toReturn.indexOf('.');
            if (index !== -1){
                toReturn = toReturn.substring(index + 1);
            }
            const ieAnonymousFunctionName: string = "Anonymous function";
            if (/* isEmpty */(toReturn.length === 0) || (toReturn === ieAnonymousFunctionName)){
                toReturn = StackTraceCreator.ANONYMOUS;
            }
            const lastColonIndex: number = location.lastIndexOf(':');
            const endFileUrlIndex: number = location.lastIndexOf(':', lastColonIndex - 1);
            let line: number = StackTraceCreator.LINE_NUMBER_UNKNOWN;
            let col: number = -1;
            let fileName: string = StackTraceCreator.UNKNOWN;
            if (lastColonIndex !== -1 && endFileUrlIndex !== -1){
                fileName = location.substring(0, endFileUrlIndex);
                line = StackTraceCreator.parseInt(location.substring(endFileUrlIndex + 1, lastColonIndex));
                col = StackTraceCreator.parseInt(location.substring(lastColonIndex + 1));
            }
            return this.createSte(fileName, toReturn, line, col);
        }

        createSte(fileName: string, method: string, line: number, col: number): StackTraceElement {
            return new StackTraceElement(StackTraceCreator.UNKNOWN, method, fileName + "@" + col, line < 0 ? StackTraceCreator.LINE_NUMBER_UNKNOWN : line);
        }

        stripSquareBrackets(toReturn: string): string {
            return toReturn.replace(/\[.*?\]/g,"")
        }

        constructor() {
            super();
        }

    }


    public static CollectorModernNoSourceMap = class CollectorModernNoSourceMap extends StackTraceCreator.CollectorModern {
        /**
         * 
         * @param {string} fileName
         * @param {string} method
         * @param {number} line
         * @param {number} col
         * @return {StackTraceElement}
         */
        createSte(fileName: string, method: string, line: number, col: number): StackTraceElement {
            return new StackTraceElement(StackTraceCreator.UNKNOWN, method, fileName, StackTraceCreator.LINE_NUMBER_UNKNOWN);
        }

        constructor() {
            super();
        }

    }
    static parseInt(number: string): number {
        return parseInt(number) || StackTraceCreator.LINE_NUMBER_UNKNOWN;
    }



    public static CollectorNull = class CollectorNull extends StackTraceCreator.Collector {
        /**
         * 
         * @param {*} error
         */
        public collect(error: any) {
        }

        /**
         * 
         * @param {*} ignored
         * @return {java.lang.StackTraceElement[]}
         */
        public getStackTrace(ignored: any): StackTraceElement[] {
            return [];
        }

        constructor() {
            super();
        }

    }
    /**
     * Collect necessary information to construct stack trace trace later in time.
     * @param {*} error
     */
    public static captureStackTrace(error: any) {
        StackTraceCreator.collector_$LI$().collect(error);
    }

    public static constructJavaStackTrace(thrown: Error): StackTraceElement[] {
        const stackTrace: StackTraceElement[] = StackTraceCreator.collector_$LI$().getStackTrace(thrown);
        return StackTraceCreator.dropInternalFrames(stackTrace);
    }

    static dropInternalFrames(stackTrace: StackTraceElement[]): StackTraceElement[] {
        const dropFrameUntilFnName: string = Impl.getNameOf("@com.google.gwt.core.client.impl.StackTraceCreator::captureStackTrace(*)");
        const dropFrameUntilFnName2: string = Impl.getNameOf("@java.lang.Throwable::initializeBackingError(*)");
        const numberOfFramesToSearch: number = Math.min(stackTrace.length, StackTraceCreator.DROP_FRAME_LIMIT);
        for(let i: number = numberOfFramesToSearch - 1; i >= 0; i--) {{
            if ((stackTrace[i].getMethodName() === dropFrameUntilFnName) || (stackTrace[i].getMethodName() === dropFrameUntilFnName2)){
                StackTraceCreator.splice<any>(stackTrace, i + 1);
                break;
            }
        };}
        return stackTrace;
    }

    static splice<T>(arr: any[], length: number) {
        if (arr.length >= length){
            ArrayHelper.removeFrom(arr, 0, length);
        }
    }

    static collector: StackTraceCreator.Collector; public static collector_$LI$(): StackTraceCreator.Collector { StackTraceCreator.__static_initialize();  return StackTraceCreator.collector; }

    static  __static_initializer_0() {
        const enforceLegacy: boolean = !StackTraceCreator.supportsErrorStack();
        const c: StackTraceCreator.Collector = <any>(GWT.create<any>(StackTraceCreator.Collector));
        StackTraceCreator.collector = ((c != null && c instanceof <any>StackTraceCreator.CollectorModern) && enforceLegacy) ? new StackTraceCreator.CollectorLegacy() : c;
    }

    static supportsErrorStack(): boolean {
        // Error.stackTraceLimit is cheaper to check and available in both IE and Chrome
    if (Error.stackTraceLimit > 0) {
      $wnd.Error.stackTraceLimit = Error.stackTraceLimit = 64;
      return true;
    }

    return "stack" in new Error();
    }

    static getFnStack(e: any): JsArrayString {
        return (e && e["fnStack"]) ? e["fnStack"] : [];
    }

    static getFunctionName(fn: JavaScriptObject): string {
        return fn.name || (fn.name = StackTraceCreator.extractFunctionName(fn.toString()));
    }

    static extractFunctionName(fnName: string): string {
        var fnRE = /function(?:\s+([\w$]+))?\s*\(/;
    var match = fnRE.exec(fnName);
    return (match && match[1]) || StackTraceCreator.ANONYMOUS;
    }

    static split(t: any): JsArrayString {
        var e = t.backingJsObject;
    if (e && e.stack) {
      var stack = e.stack;
      // If the stack starts with toString of Error, drop it.
      var toString = e + "\n";
      if (stack.substring(0, toString.length) == toString) {
        stack = stack.substring(toString.length);
      }
      return stack.split('\n');
    }
    return [];
    }

}
StackTraceCreator["__class"] = "com.google.gwt.core.client.impl.StackTraceCreator";


export namespace StackTraceCreator {
        export abstract class Collector {
        // Members would be generated here
    }
}




StackTraceCreator.__static_initialize();

import { Impl } from './Impl';

