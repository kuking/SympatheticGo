package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;
import uk.kukino.sgo.sgf.SGFReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameRegressionTest
{

    @Test
    public void test_90b0d5e45d270999902667b3cb36d66e798609fe() throws IOException
    {
        // src:  90b0d5e45d270999902667b3cb36d66e798609fe  kgs-19-2017-09-new/2017-09-01-12.sgf
        final var sgf = "(;GM[1]FF[4]SZ[19]KM[6.50] ; B[pd];W[dp];B[qp];W[dd];B[np];W[qf];B[ph];W[of];B[nd];W[rd];B[qc];W[rh];B[og]" +
            ";W[nf];B[kc];W[qj];B[qg];W[rg];B[ep];W[eq];B[pf];W[pe];B[dq];W[cp];B[qe];W[pg];B[fq];W[qd];B[pf];W[re];B[oe];W[er];B[qe]" +
            ";W[rf];B[pe];W[fp];B[fc];W[eo];B[cf];W[fd];B[gd];W[fe];B[dc];W[ec];B[eb];W[ed];B[gb];W[cc];B[db];W[ge];B[hd];W[ci];B[di]" +
            ";W[dh];B[ch];W[cj];B[bh];W[dg];B[df];W[ei];B[bd];W[cd];B[bc];W[ef];B[jp];W[be];B[dj];W[ck];B[dk];W[cl];B[eh];W[eg];B[cb]" +
            ";W[ad];B[bb];W[ce];B[im];W[lq];B[lp];W[kq];B[kp];W[nq];B[oq];W[mq];B[op];W[iq];B[jq];W[jr];B[ir];W[hr];B[ql];W[pk];B[rj]" +
            ";W[ri];B[dl];W[dm];B[em];W[el];B[dn];W[cm];B[en];W[fl];B[gm];W[pl];B[rk];W[pi];B[gl];W[if];B[ej];W[fj];B[fi];W[ek];B[rc]" +
            ";W[gj];B[oi];W[oj];B[qh];W[qi];B[ni];W[qm];B[rm];W[qn])";

        assertAllMovesAreValid(sgf);
    }

    private void assertAllMovesAreValid(String sgf) throws IOException
    {
        final Game game[] = new Game[1];
        final Reader r = new StringReader(sgf);
        new SGFReader().parse(r, header -> {
            game[0] = new Game(header.size, header.handicap, (byte) (header.komi * 10));
        }, node ->
        {
            assertTrue(game[0].play(node.move), () -> "Invalid Move?! " + Coord.shortToString(node.move) + "\n" + game[0]);
        });

    }


}
