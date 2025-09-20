/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
/**
 * Test class to examine JSNI method overloading behavior.
 * This class has overloaded JSNI methods to test if JSweet's overloading logic
 * is properly applied to JSNI methods or if they bypass it.
 * @class
 */
export class JsniDuplicates {
    public foo$java_lang_String(string: string): string {
        // JSNI_METHOD:foo:-217105878
        return null as any;
    }

    /**
     * JSNI method that takes a String parameter.
     * @param {string} string the string parameter
     * @return {string} the processed string
     */
    public foo(string?: any): string {
        if (((typeof string === 'string') || string === null)) {
            return <any>this.foo$java_lang_String(string);
        } else if (((typeof string === 'number') || string === null)) {
            return <any>this.foo$int(string);
        } else if (((typeof string === 'boolean') || string === null)) {
            return <any>this.foo$boolean(string);
        } else throw new Error('invalid overload');
    }

    public foo$int(intValue: number): string {
        // JSNI_METHOD:foo:3237420
        return null as any;
    }

    public foo$boolean(boolValue: boolean): string {
        // JSNI_METHOD:foo:2006063379
        return null as any;
    }

    public bar$double(value: number): string {
        return "double: " + value;
    }

    public bar$float(value: number): string {
        return "float: " + value;
    }

    /**
     * Another regular Java method for comparison.
     * @param {number} value the float parameter
     * @return {string} the processed float
     */
    public bar(value?: any): string {
        if (((typeof value === 'number') || value === null)) {
            return <any>this.bar$float(value);
        } else if (((typeof value === 'number') || value === null)) {
            return <any>this.bar$double(value);
        } else throw new Error('invalid overload');
    }

    public constructor() {
    }

    /**
     * Test method to verify we can access source and detect JSNI.
     */
    public testJsniDetection() {
        console.info("Testing JSNI detection");
    }

}
JsniDuplicates["__class"] = "source.structural.gwt.JsniDuplicates";




