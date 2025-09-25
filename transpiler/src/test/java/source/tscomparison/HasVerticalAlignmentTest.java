package source.tscomparison;

public interface HasVerticalAlignmentTest {
    public static class VerticalAlignmentConstant {
        private final String verticalAlignString;

        private VerticalAlignmentConstant(String verticalAlignString) {
            this.verticalAlignString = verticalAlignString;
        }

        public String getVerticalAlignString() {
            return verticalAlignString;
        }
    }

    VerticalAlignmentConstant ALIGN_TOP = new VerticalAlignmentConstant("top");
}