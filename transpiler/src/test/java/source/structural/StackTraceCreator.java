package source.structural;

public class StackTraceCreator {

    abstract static class Collector {

        private final String name = "name";
        public abstract void collect(Object error);
    }

    static class CollectorLegacy extends Collector {

        public String foo = "bar";
        @Override
        public void collect(Object error) {
            // implementation
        }
    }
}
