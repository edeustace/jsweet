var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
/* Generated from Java with JSweet 5.0.0-SNAPSHOT - http://www.jsweet.org */
/**
 * Push button.
 *
 * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#edef-BUTTON">W3C HTML Specification</a>
 * @extends Element
 * @class
 */
var ButtonElement = /** @class */ (function (_super) {
    __extends(ButtonElement, _super);
    function ButtonElement() {
        return _super.call(this) || this;
    }
    /**
     * Assert that the given {@link Element} is compatible with this class and
     * automatically typecast it.
     * @param {Element} elem
     * @return {ButtonElement}
     */
    ButtonElement.as = function (elem) {
        return elem;
    };
    ButtonElement.is$com_google_gwt_core_client_JavaScriptObject = function (o) {
        if (Element.is(o)) {
            return ButtonElement.is$com_google_gwt_dom_client_Element(o);
        }
        return false;
    };
    ButtonElement.is$com_google_gwt_dom_client_Node = function (node) {
        if (Element.is(node)) {
            return ButtonElement.is$com_google_gwt_dom_client_Element(node);
        }
        return false;
    };
    ButtonElement.is$com_google_gwt_dom_client_Element = function (elem) {
        return elem != null && elem.hasTagName(ButtonElement.TAG);
    };
    /**
     * Determine whether the given {@link Element} can be cast to this class.
     * A <code>null</code> node will cause this method to return
     * <code>false</code>.
     * @param {Element} elem
     * @return {boolean}
     */
    ButtonElement.is = function (elem) {
        if (((elem != null && elem instanceof Element) || elem === null)) {
            return ButtonElement.is$com_google_gwt_dom_client_Element(elem);
        }
        else if (((elem != null && elem instanceof Node) || elem === null)) {
            return ButtonElement.is$com_google_gwt_dom_client_Node(elem);
        }
        else if (((elem != null && elem instanceof JavaScriptObject) || elem === null)) {
            return ButtonElement.is$com_google_gwt_core_client_JavaScriptObject(elem);
        }
        else
            throw new Error('invalid overload');
    };
    /**
     * Simulate a mouse-click.
     */
    ButtonElement.prototype.click = function () {
        DOMImpl.impl.buttonClick(this);
    };
    /**
     * A single character access key to give access to the form control.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @return {string}
     */
    ButtonElement.prototype.getAccessKey = function () {
        // JSNI_METHOD:getAccessKey:0
        return null;
    };
    /**
     * The control is unavailable in this context.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @deprecated use {@link #isDisabled()} instead.
     * @return {string}
     */
    ButtonElement.prototype.getDisabled = function () {
        // JSNI_METHOD:getDisabled:0
        return null;
    };
    /**
     * Returns the FORM element containing this control. Returns null if this
     * control is not within the context of a form.
     * @return {FormElement}
     */
    ButtonElement.prototype.getForm = function () {
        // JSNI_METHOD:getForm:0
        return null;
    };
    /**
     * Form control or object name when submitted with a form.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-name-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    ButtonElement.prototype.getName = function () {
        // JSNI_METHOD:getName:0
        return null;
    };
    /**
     * The type of button (all lower case).
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-type-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    ButtonElement.prototype.getType = function () {
        // JSNI_METHOD:getType:0
        return null;
    };
    /**
     * The current form control value.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @return {string}
     */
    ButtonElement.prototype.getValue = function () {
        // JSNI_METHOD:getValue:0
        return null;
    };
    /**
     * The control is unavailable in this context.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @return {boolean}
     */
    ButtonElement.prototype.isDisabled = function () {
        // JSNI_METHOD:isDisabled:0
        return null;
    };
    /**
     * A single character access key to give access to the form control.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-accesskey">W3C HTML Specification</a>
     * @param {string} accessKey
     */
    ButtonElement.prototype.setAccessKey = function (accessKey) {
        // JSNI_METHOD:setAccessKey:-217105878
    };
    ButtonElement.prototype.setDisabled$boolean = function (disabled) {
        // JSNI_METHOD:setDisabled:2006063379
    };
    ButtonElement.prototype.setDisabled$java_lang_String = function (disabled) {
        // JSNI_METHOD:setDisabled:-217105878
    };
    /**
     * The control is unavailable in this context.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-disabled">W3C HTML Specification</a>
     * @deprecated use {@link #setDisabled(boolean)} instead
     * @param {string} disabled
     */
    ButtonElement.prototype.setDisabled = function (disabled) {
        if (((typeof disabled === 'string') || disabled === null)) {
            return this.setDisabled$java_lang_String(disabled);
        }
        else if (((typeof disabled === 'boolean') || disabled === null)) {
            return this.setDisabled$boolean(disabled);
        }
        else
            throw new Error('invalid overload');
    };
    /**
     * Form control or object name when submitted with a form.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-name-BUTTON">W3C HTML Specification</a>
     * @param {string} name
     */
    ButtonElement.prototype.setName = function (name) {
        // JSNI_METHOD:setName:-217105878
    };
    /**
     * The current form control value.
     *
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-BUTTON">W3C HTML Specification</a>
     * @param {string} value
     */
    ButtonElement.prototype.setValue = function (value) {
        // JSNI_METHOD:setValue:-217105878
    };
    ButtonElement.TAG = "button";
    return ButtonElement;
}(Element));
export { ButtonElement };
ButtonElement["__class"] = "com.google.gwt.dom.client.ButtonElement";
