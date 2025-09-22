
export namespace Foo {
    export abstract class Bar {}
}

export class Foo {
    static BarInstance = class extends Foo.Bar{}
}

