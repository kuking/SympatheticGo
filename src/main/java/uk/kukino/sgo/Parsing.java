package uk.kukino.sgo;

public class Parsing {

    static public class UpperCaseCharSequence implements CharSequence {

        private CharSequence wrapped;

        public UpperCaseCharSequence() {
            this.wrapped = null;
        }

        public void assign(final CharSequence charSequence) {
            this.wrapped = charSequence;
        }

        @Override
        public int length() {
            return wrapped.length();
        }

        @Override
        public char charAt(int index) {
            return Character.toUpperCase(this.wrapped.charAt(index));
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            //XXX: ALLOCATES
            UpperCaseCharSequence newSeq = new UpperCaseCharSequence();
            newSeq.assign(this.wrapped.subSequence(start, end));
            return newSeq;
        }
    }
}
