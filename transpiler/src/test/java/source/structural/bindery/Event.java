package source.structural.bindery;

public abstract class Event<H> {
    public static class Type<H> {
        private static int nextHashCode;
        private final int index;

        public Type() {
            index = ++nextHashCode;
        }

        @Override
        public final int hashCode() {
            return index;
        }

        @Override
        public String toString() {
            return "Event type";
        }
    }

    private Object source;

    protected Event() {
    }

    public abstract Type<H> getAssociatedType();

    public Object getSource() {
        return source;
    }

    public String toDebugString() {
        String name = this.getClass().getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        return "event: " + name + ":";
    }

    @Override
    public String toString() {
        return "An event type";
    }

    protected abstract void dispatch(H handler);

    protected void setSource(Object source) {
        this.source = source;
    }
}

