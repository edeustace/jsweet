/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
export class StackTraceCreator {


    public static CollectorLegacy = class CollectorLegacy extends StackTraceCreator.Collector {
        public foo: string;

        /**
         * 
         * @param {*} error
         */
        public collect(error: any) {
        }

        constructor() {
            super();
            this.foo = "bar";
        }

    }
}
StackTraceCreator["__class"] = "source.structural.StackTraceCreator";


export namespace StackTraceCreator {
        export abstract class Collector {
        // Members would be generated here
    }
}



