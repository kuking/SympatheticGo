package uk.kukino.sgo.util;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class ParsingTest
{
    @Test
    public void parseFloat()
    {
        assertThat(Parsing.parseFloat("ABC")).isNaN();
        assertThat(Parsing.parseFloat("")).isNaN();
        assertThat(Parsing.parseFloat("   ")).isNaN();
        assertThat(Parsing.parseFloat(" 12  ")).isNaN();
        assertThat(Parsing.parseFloat(" 12.34  ")).isNaN();
        assertThat(Parsing.parseFloat("-")).isNaN();
        assertThat(Parsing.parseFloat("--")).isNaN();
        assertThat(Parsing.parseFloat("--1")).isNaN();
        assertThat(Parsing.parseFloat("12..34")).isNaN();
        assertThat(Parsing.parseFloat("12.34.")).isNaN();
        assertThat(Parsing.parseFloat("12$34")).isNaN();
        assertThat(Parsing.parseFloat("1234!")).isNaN();

        assertThat(Parsing.parseFloat("0")).isWithin(0.000001f).of(0f);
        assertThat(Parsing.parseFloat("000000")).isWithin(0.000001f).of(0f);
        assertThat(Parsing.parseFloat("000000.0000000")).isWithin(0.000001f).of(0f);
        assertThat(Parsing.parseFloat("000000.0000001")).isWithin(0.000001f).of(0.0000001f);
        assertThat(Parsing.parseFloat("12")).isWithin(0.000001f).of(12f);
        assertThat(Parsing.parseFloat("-12")).isWithin(0.000001f).of(-12f);
        assertThat(Parsing.parseFloat("12.34")).isWithin(0.000001f).of(12.34f);
        assertThat(Parsing.parseFloat("-12.34")).isWithin(0.000001f).of(-12.34f);
        assertThat(Parsing.parseFloat("-0000000000012.34")).isWithin(0.000001f).of(-12.34f);
        assertThat(Parsing.parseFloat("-120000000.000034")).isWithin(0.000001f).of(-120000000.000034f);
    }

}