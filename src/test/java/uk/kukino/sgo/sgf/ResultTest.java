package uk.kukino.sgo.sgf;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.kukino.sgo.base.Color;

import static com.google.common.truth.Truth.assertThat;

public class ResultTest
{

    Result r;

    @Test
    public void simple()
    {
        r = Result.fromCs("B+23.5");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Standard);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.BLACK);
        assertThat(r.hasPoints()).isTrue();
        assertThat(r.points()).isWithin(0.1f).of(23.5f);

        r = Result.fromCs("W+0.5");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Standard);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.WHITE);
        assertThat(r.hasPoints()).isTrue();
        assertThat(r.points()).isWithin(0.1f).of(0.5f);
    }


    @Test
    public void resign()
    {
        r = Result.fromCs("B+Resign");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Resign);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.BLACK);
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();

        r = Result.fromCs("W+R");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Resign);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.WHITE);
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();
    }


    @Test
    public void forfeit()
    {
        r = Result.fromCs("B+FORFEIT");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Forfeit);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.BLACK);
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();

        r = Result.fromCs("W+F");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Forfeit);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.WHITE);
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();
    }

    @Test
    private void voided()
    {
        r = Result.fromCs("Void");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Void);
        assertThat(r.hasWinner()).isFalse();
        assertThat(r.winner()).isNull();
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();
    }

    @Test
    private void time()
    {
        r = Result.fromCs("W+T");
        assertThat(r.outcome()).isEqualTo(Result.Outcome.Timeout);
        assertThat(r.hasWinner()).isTrue();
        assertThat(r.winner()).isEqualTo(Color.WHITE);
        assertThat(r.hasPoints()).isFalse();
        assertThat(r.points()).isNaN();
    }

    @Test
    public void miscParsing()
    {
        assertThat(Result.fromCs(" B+5.4 ").toString()).isEqualTo("B+5.4");
        assertThat(Result.fromCs(" B+5.4 ").toString()).isEqualTo("B+5.4");
        assertThat(Result.fromCs(null)).isNull();
        assertThat(Result.fromCs("   ")).isNull();
        assertThat(Result.fromCs("Black+")).isNull();
        assertThat(Result.fromCs("B+")).isNull();
        assertThat(Result.fromCs("Bla+5.5")).isNull();
        assertThat(Result.fromCs("Whit+5.5")).isNull();
        assertThat(Result.fromCs("White + 5.5")).isNull();
    }

    @Test
    public void caseInsensitiveAndToString()
    {
        assertThat(Result.fromCs("B+FORFEIT").toString()).isEqualTo("B+F");
        assertThat(Result.fromCs("W+Forfeit").toString()).isEqualTo("W+F");
        assertThat(Result.fromCs("w+f").toString()).isEqualTo("W+F");

        assertThat(Result.fromCs("B+RESIGN").toString()).isEqualTo("B+R");
        assertThat(Result.fromCs("W+Resign").toString()).isEqualTo("W+R");
        assertThat(Result.fromCs("w+r").toString()).isEqualTo("W+R");

        assertThat(Result.fromCs("B+TIME").toString()).isEqualTo("B+T");
        assertThat(Result.fromCs("W+Time").toString()).isEqualTo("W+T");
        assertThat(Result.fromCs("w+t").toString()).isEqualTo("W+T");

        assertThat(Result.fromCs("b+0.5").toString()).isEqualTo("B+0.5");
    }

    @Test
    @Disabled // overkill to cache this object that is created once per SGF file
    public void cachesResults()
    {
        Result res1 = Result.fromCs("B+1.5");
        Result res2 = Result.fromCs("B+1.5");
        assertThat(res1).isSameInstanceAs(res2);
        res1 = Result.fromCs("B+R");
        res2 = Result.fromCs("B+R");
        assertThat(res1).isSameInstanceAs(res2);
        res1 = Result.fromCs("B+F");
        res2 = Result.fromCs("B+F");
        assertThat(res1).isSameInstanceAs(res2);
        res1 = Result.fromCs("B+T");
        res2 = Result.fromCs("B+T");
        assertThat(res1).isSameInstanceAs(res2);
    }

}
