package uk.kukino.sgo.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

class IntIntAsLongTest
{

    @Test
    public void easy()
    {
        long l = IntIntAsLong.enc(123, 456);
        assertThat(IntIntAsLong.left(l)).isEqualTo(123);
        assertThat(IntIntAsLong.right(l)).isEqualTo(456);

        l = IntIntAsLong.enc(-1, 1);
        assertThat(IntIntAsLong.left(l)).isEqualTo(-1);
        assertThat(IntIntAsLong.right(l)).isEqualTo(1);
    }

    @Test
    @Disabled //FIXME
    public void extremes()
    {
        long l = IntIntAsLong.enc(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertThat(IntIntAsLong.left(l)).isEqualTo(Integer.MAX_VALUE);
        assertThat(IntIntAsLong.right(l)).isEqualTo(Integer.MIN_VALUE);

        l = IntIntAsLong.enc(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertThat(IntIntAsLong.left(l)).isEqualTo(Integer.MIN_VALUE);
        assertThat(IntIntAsLong.right(l)).isEqualTo(Integer.MAX_VALUE);
    }

}