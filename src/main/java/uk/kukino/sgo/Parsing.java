package uk.kukino.sgo;

public class Parsing {

    public static int scanSpaces(final CharSequence seq, int i) {
        while (i < seq.length() && seq.charAt(i) == ' ') i++;
        return i;
    }

    public static int scanAlphas(final CharSequence seq, int i) {
        while (i < seq.length() &&
                Character.toUpperCase(seq.charAt(i)) >= 'A' &&
                Character.toUpperCase(seq.charAt(i)) <= 'Z') i++;
        return i;
    }
}
