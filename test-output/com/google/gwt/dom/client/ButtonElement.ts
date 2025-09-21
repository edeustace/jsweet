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

    public static is$com_google_gwt_core_client_JavaScriptObject(o: JavaScriptObject): boolean {
        if (Element.is(o)){
            return ButtonElement.is$com_google_gwt_dom_client_Element(<Element>o);
        }
        return false;
    }

    public static is$com_google_gwt_dom_client_Node(node: Node): boolean {
        if (Element.is(node)){
            return ButtonElement.is$com_google_gwt_dom_client_Element(<Element>node);
        }
        return false;
    }

    public static is$com_google_gwt_dom_client_Element(elem: Element): boolean {
        return elem != null && elem.hasTagName(ButtonElement.TAG);
    }

    /**
     * Determine whether the given {@link Element} can be cast to this class.
     * A <code>null</code> node will cause this method to return
     * <code>false</code>.
     * @param {Element} elem
     * @return {boolean}
     */
    public static is(elem?: any): boolean {
        if (((elem != null && elem instanceof <any>Element) || elem === null)) {
            return <any>ButtonElement.is$com_google_gwt_dom_client_Element(elem);
        } else if (((elem != null && elem instanceof <any>Node) || elem === null)) {
            return <any>ButtonElement.is$com_google_gwt_dom_client_Node(elem);
        } else if (((elem != null && elem instanceof <any>JavaScriptObject) || elem === null)) {
            return <any>ButtonElement.is$com_google_gwt_core_client_JavaScriptObject(elem);
        } else throw new Error('invalid overload');
    }

    constructor() {
        super();
    }

    /**
     * Simulate a mouse-click.
     */
    public click() {
        DOMImpl.impl.buttonClick(this);
    }

    /**
     * A single character access key to give access to the form control.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @return {string}
     */
    public getAccessKey(): string {
        // JSNI_METHOD:getAccessKey:0
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
        // JSNI_METHOD:getDisabled:0
        return null as any;
    }

    /**
     * Returns the FORM element containing this control. Returns null if this
     * control is not within the context of a form.
     * @return {FormElement}
     */
    public getForm(): FormElement {
        // JSNI_METHOD:getForm:0
        return null as any;
    }

    /**
     * Form control or object name when submitted with a form.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-name-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getName(): string {
        // JSNI_METHOD:getName:0
        return null as any;
    }

    /**
     * The type of button (all lower case).
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-type-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getType(): string {
        // JSNI_METHOD:getType:0
        return null as any;
    }

    /**
     * The current form control value.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    public getValue(): string {
        // JSNI_METHOD:getValue:0
        return null as any;
    }

    /**
     * The control is unavailable in this context.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @return {boolean}
     */
    public isDisabled(): boolean {
        // JSNI_METHOD:isDisabled:0
        return null as any;
    }

    /**
     * A single character access key to give access to the form control.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @param {string} accessKey
     */
    public setAccessKey(accessKey: string) {
        // JSNI_METHOD:setAccessKey:-217105878
        
    }

    public setDisabled$boolean(disabled: boolean) {
        // JSNI_METHOD:setDisabled:2006063379
        
    }

    public setDisabled$java_lang_String(disabled: string) {
        // JSNI_METHOD:setDisabled:-217105878
        
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
        // JSNI_METHOD:setName:-217105878
        
    }

    /**
     * The current form control value.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @param {string} value
     */
    public setValue(value: string) {
        // JSNI_METHOD:setValue:-217105878
        
    }

}
ButtonElement["__class"] = "com.google.gwt.dom.client.ButtonElement";



