/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */

export abstract class Collector {
    constructor() {
    }
    /*private*/ name: string = "name"
    public abstract collect(error: any);
}



export class StackTraceCreator {


    public static CollectorLegacy = class CollectorLegacy extends Collector {
        public foo: string;
        //
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


