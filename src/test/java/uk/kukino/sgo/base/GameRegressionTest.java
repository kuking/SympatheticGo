package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;
import uk.kukino.sgo.sgf.SGFReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameRegressionTest
{

    Game game[];

    @Test
    public void test_1_90b0d5e45d270999902667b3cb36d66e798609fe() throws IOException
    {
        // src:  90b0d5e45d270999902667b3cb36d66e798609fe  kgs-19-2017-09-new/2017-09-01-12.sgf
        assertAllMovesAreValid("(;GM[1]FF[4]SZ[19]KM[6.50] ; B[pd];W[dp];B[qp];W[dd];B[np];W[qf];B[ph];W[of];B[nd];W[rd];B[qc];W[rh]" +
            ";B[og];W[nf];B[kc];W[qj];B[qg];W[rg];B[ep];W[eq];B[pf];W[pe];B[dq];W[cp];B[qe];W[pg];B[fq];W[qd];B[pf];W[re];B[oe];W[er]" +
            ";B[qe];W[rf];B[pe];W[fp];B[fc];W[eo];B[cf];W[fd];B[gd];W[fe];B[dc];W[ec];B[eb];W[ed];B[gb];W[cc];B[db];W[ge];B[hd];W[ci]" +
            ";B[di];W[dh];B[ch];W[cj];B[bh];W[dg];B[df];W[ei];B[bd];W[cd];B[bc];W[ef];B[jp];W[be];B[dj];W[ck];B[dk];W[cl];B[eh];W[eg]" +
            ";B[cb];W[ad];B[bb];W[ce];B[im];W[lq];B[lp];W[kq];B[kp];W[nq];B[oq];W[mq];B[op];W[iq];B[jq];W[jr];B[ir];W[hr];B[ql];W[pk]" +
            ";B[rj];W[ri];B[dl];W[dm];B[em];W[el];B[dn];W[cm];B[en];W[fl];B[gm];W[pl];B[rk];W[pi];B[gl];W[if];B[ej];W[fj];B[fi];W[ek]" +
            ";B[rc];W[gj];B[oi];W[oj];B[qh];W[qi];B[ni];W[qm];B[rm];W[qn])");
    }

    @Test
    public void test_2_1df9d7dffe80f2dc213a67d1ecb58151981dbe90() throws IOException
    {
        // src: 1df9d7dffe80f2dc213a67d1ecb58151981dbe90  kgs-19-2016-11-new/2016-11-20-13.sgf
        assertAllMovesAreValid("(;GM[1]FF[4]SZ[19]KM[6.50] ; B[pd];W[dp];B[pp];W[dc];B[pj];W[nq];B[lq];W[pn];B[oq];W[np];B[oo];W[qq]" +
            ";B[qp];W[or];B[pq];W[pr];B[qr];W[lp];B[kp];W[kq];B[lo];W[mp];B[jq];W[kr];B[jp];W[lr];B[on];W[nc];B[oe];W[kd];B[de];W[cg]" +
            ";B[cc];W[gc];B[cn];W[fq];B[dd];W[db];B[cf];W[dh];B[cb];W[cl];B[oc];W[nd];B[nb];W[mb];B[ob];W[ph];B[nj];W[qj];B[qk];W[qi]" +
            ";B[rk];W[qe];B[qd];W[re];B[rd];W[pf];B[lc];W[ld];B[mc];W[kc];B[lb];W[kb];B[cp];W[cq];B[bq];W[dq];B[bo];W[do];B[co];W[bm]" +
            ";B[cr];W[dr];B[br];W[bf];B[be];W[df];B[ce];W[ef];B[ej];W[el];B[gj];W[gl];B[hk];W[im];B[km];W[jl];B[jj];W[kl];B[mm];W[kj]" +
            ";B[jh];W[ji];B[ii];W[ki];B[hh];W[ig];B[ih];W[gf];B[jk];W[ml];B[nm];W[fi];B[hl];W[hm];B[gi];W[fj];B[il];W[gk];B[ij];W[jm]" +
            ";B[lm];W[hp];B[ps];W[ns];B[da];W[fb];B[ec];W[eb];B[fh];W[eh];B[bg];W[bh];B[af];W[jg];B[kh];W[mi];B[kg];W[kf];B[lf];W[jf]" +
            ";B[mj];W[kk];B[gm];W[fk];B[hq];W[gq];B[ho];W[go];B[ip];W[gp];B[dm];W[em];B[dl];W[dk];B[ah];W[bi];B[cm];W[bk];B[se];W[sf]" +
            ";B[sd];W[rg];B[fd];W[gd];B[rj];W[ri];B[ll];W[md];B[ma];W[an];B[ao];W[hr];B[jr];W[oi];B[oj];W[io];B[jo];W[in];B[bn];W[am]" +
            ";B[fe];W[fg];B[hg];W[hf];B[ge];W[he];B[ka];W[ja];B[la];W[fc];B[ir];W[hs];B[ks];W[ls];B[rq];W[lk];B[mk];W[nf];B[mg];W[ng]" +
            ";B[nh];W[pe];B[od];W[lh];B[ni];W[oh];B[of];W[og];B[dn];W[en];B[ds];W[es];B[cs];W[ed];B[ee];W[ea];B[jb];W[ia];B[ca];W[ai]" +
            ";B[ag];W[mo];B[gr];W[fr];B[is];W[iq];B[mr];W[mq];B[hq];W[gs];B[mn];W[jn];B[kn];W[sj];B[sk];W[si];B[le];W[ke];B[mh];W[li]" +
            ";B[qs];W[os];B[no];W[gh];B[gg];W[fh];B[js];W[iq];B[ko];W[hq];B[ec];W[ib];B[ed];W[lg];B[mf];W[ne];B[lj];W[hn];B[];W[ff]" +
            ";B[];W[])");
    }

    @Test
    public void test_3_filling_eyes() throws IOException
    {
        assertAllMovesAreValid("(;GM[1]FF[4]SZ[9]KM[0.00] ; B[cc];W[ha];B[dd];W[ib];B[ce];W[ia];B[bd];W[hb];B[cd])");
    }

    @Test
    public void test_4_filling_eyes_2() throws IOException
    {
        assertAllMovesAreValid("(;GM[1]FF[4]" + "SZ[9]KM[0.00] ; B[ia];W[ib];B[id];W[hb];B[hd];W[gb];B[gd];W[fb];B[ga];W[ea];B[ha])");
        assertTrue(game[0].play(Move.parseToVal("WHITE F9")));
        assertThat(game[0].deadStones(Color.BLACK), equalTo(3));

        assertAllMovesAreValid("(;GM[1]FF[4]" + "SZ[9]KM[0.00] ; B[ia];W[ib];B[id];W[hb];B[hd];W[gb];B[gd];W[fb];B[ga];W[ea];B[ha])");
        assertTrue(game[0].play(Move.parseToVal("WHITE E8")));
        assertFalse(game[0].play(Move.parseToVal("BLACK F9")));
    }

    private void assertAllMovesAreValid(String sgf) throws IOException
    {
        game = new Game[1];
        final Reader r = new StringReader(sgf);
        new SGFReader().parse(r, header -> {
            game[0] = new Game(header.size, header.handicap, (byte) (header.komi * 10));
        }, node ->
        {
            assertTrue(game[0].play(node.move), () -> "Invalid Move?! " + Coord.shortToString(node.move) + "\n" + game[0]);
        });

    }


}
