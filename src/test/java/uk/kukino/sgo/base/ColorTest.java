package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ColorTest
{

    @Test
    public void parses()
    {
        assertThat(Color.parse("White")).isEqualTo(Color.WHITE);
        assertThat(Color.parse("white")).isEqualTo(Color.WHITE);
        assertThat(Color.parse("wHiTe")).isEqualTo(Color.WHITE);
        assertThat(Color.parse("Black")).isEqualTo(Color.BLACK);
        assertThat(Color.parse("black")).isEqualTo(Color.BLACK);
        assertThat(Color.parse("bLaCk")).isEqualTo(Color.BLACK);
        assertThat(Color.parse("W")).isEqualTo(Color.WHITE);
        assertThat(Color.parse("w")).isEqualTo(Color.WHITE);
        assertThat(Color.parse("B")).isEqualTo(Color.BLACK);
        assertThat(Color.parse("b")).isEqualTo(Color.BLACK);
        assertThat(Color.parse("  ")).isNull();
        assertThat(Color.parse("")).isNull();
        assertThat(Color.parse(null)).isNull();
    }

    @Test
    public void writes()
    {
        final StringBuilder sb = new StringBuilder();

        Color.WHITE.write(sb);
        assertThat(sb.toString()).isEqualTo("White");

        sb.delete(0, sb.length());
        Color.BLACK.write(sb);
        assertThat(sb.toString()).isEqualTo("Black");

        sb.delete(0, sb.length());
        Color.WHITE.write(sb, false);
        assertThat(sb.toString()).isEqualTo("WHITE");

        sb.delete(0, sb.length());
        Color.BLACK.write(sb, false);
        assertThat(sb.toString()).isEqualTo("BLACK");
    }

}
