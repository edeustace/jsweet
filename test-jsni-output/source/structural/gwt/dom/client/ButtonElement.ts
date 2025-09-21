/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
/**
 * Push button.
 * 
 * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#edef-BUTTON">W3C HTML Specification</a>
 * @extends Element
 * @class
 */
export class ButtonElement extends Element {
    public static TAG: string = "button";

    /**
     * Assert that the given {@link Element} is compatible with this class and
     * automatically typecast it.
     * @param {Element} elem
     * @return {ButtonElement}
     */
    public static as(elem: Element): ButtonElement {
        return <ButtonElement>elem;
    }

    public static is$java_lang_Object(o: any): boolean {
        if (){
            return ButtonElement.is$java_lang_Object(<Element>o);
        }
        return false;
    }

    /**
     * Determines whether the given {@link Object} can be cast to
     * this class. A <code>null</code> object will cause this method to
     * return <code>false</code>.
     * @param {*} o
     * @return {boolean}
     */
    public static is(o?: any): boolean {
        if (((o != null) || o === null)) {
            return <any>ButtonElement.is$java_lang_Object(o);
        } else if (((o != null && o instanceof <any>Element) || o === null)) {
            return <any>ButtonElement.is$Element(o);
        } else throw new Error('invalid overload');
    }

    public static is$Element(elem: Element): boolean {
        return (c => c.charCodeAt==null?<any>c:c.charCodeAt(0))(elem) != null && ;
    }

    constructor() {
    }

    /**
     * Simulate a mouse-click.
     */
    public click() {
        console.info("Button clicked");
    }

    /**
     * A single character access key to give access to the form control.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @return {string}
     */
    public getAccessKey(): string {
        return this.accessKey;
        return null as any;
    }

    /**
     * The control is unavailable in this context.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @deprecated use {@link #isDisabled()} instead.
     * @return {string}
     */
    public getDisabled(): string {
        return this.disabled;
        return null as any;
    }

    /**
     * Returns the FORM element containing this control. Returns null if this
     * control is not within the context of a form.
     * @return {FormElement}
     */
    public getForm(): FormElement {
        return this.form;
        return null as any;
    }

    /**
     * Form control or object name when submitted with a form.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-name-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getName(): string {
        return this.name;
        return null as any;
    }

    /**
     * The type of button (all lower case).
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-type-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getType(): string {
        return this.type;
        return null as any;
    }

    /**
     * The current form control value.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getValue(): string {
        return this.value;
        return null as any;
    }

    /**
     * The control is unavailable in this context.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @return {boolean}
     */
    public isDisabled(): boolean {
        return !!this.disabled;
        return null as any;
    }

    /**
     * A single character access key to give access to the form control.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @param {string} accessKey
     */
    public setAccessKey(accessKey: string) {
        this.accessKey = accessKey;
        
    }

    public setDisabled$boolean(disabled: boolean) {
        this.disabled = disabled;
        
    }

    public setDisabled$java_lang_String(disabled: string) {
        this.disabled = disabled;
        
    }

    /**
     * The control is unavailable in this context.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @deprecated use {@link #setDisabled(boolean)} instead
     * @param {string} disabled
     */
    public setDisabled(disabled?: any) {
        if (((typeof disabled === 'string') || disabled === null)) {
            return <any>this.setDisabled$java_lang_String(disabled);
        } else if (((typeof disabled === 'boolean') || disabled === null)) {
            return <any>this.setDisabled$boolean(disabled);
        } else throw new Error('invalid overload');
    }

    /**
     * Form control or object name when submitted with a form.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-name-BUTTON">W3C HTML Specification</a>
     * @param {string} name
     */
    public setName(name: string) {
        this.name = name;
        
    }

    /**
     * The current form control value.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @param {string} value
     */
    public setValue(value: string) {
        this.value = value;
        
    }

}
ButtonElement["__class"] = "source.structural.gwt.dom.client.ButtonElement";



