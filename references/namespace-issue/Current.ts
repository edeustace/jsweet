export class Foo {
    static BarInstance = class extends Foo.Bar{}
}

export namespace Foo {
    export abstract class Bar {}
}