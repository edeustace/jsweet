
export namespace FooNamespace {
    export abstract class Bar {}
}

export class Foo {
    static BarInstance = class extends FooNamespace.Bar{}
}

