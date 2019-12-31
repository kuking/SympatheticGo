package uk.kukino.sgo.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.kukino.sgo.sgf.SGFReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static com.google.common.truth.Truth.assertThat;

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
        assertThat(game[0].play(Move.parseToVal("WHITE F9"))).isTrue();
        assertThat(game[0].deadStones(Color.BLACK)).isEqualTo(3);

        assertAllMovesAreValid("(;GM[1]FF[4]" + "SZ[9]KM[0.00] ; B[ia];W[ib];B[id];W[hb];B[hd];W[gb];B[gd];W[fb];B[ga];W[ea];B[ha])");
        assertThat(game[0].play(Move.parseToVal("WHITE E8"))).isTrue();
        assertThat(game[0].play(Move.parseToVal("BLACK F9"))).isFalse();
    }

    @Test
    @Disabled
    public void test_5_ea922d76bfb7d7c376ed05b4af1561bf1f949221_a_real_superKO() throws IOException
    {
        // src: ea922d76bfb7d7c376ed05b4af1561bf1f949221  KGS2004/2004-09-21-24.sgf
        // move 317 is really a superKO! Looks like KGS was not implementing it property back in 2004.
        assertAllMovesAreValid("(;GM[1]FF[4]SZ[19]KM[6.50] ; B[pd];W[dc];B[pp];W[cp];B[de];W[qn];B[ec];W[eb];B[ed];W[fc];B[fb];W[gb]" +
            ";B[db];W[fa];B[cc];W[nq];B[np];W[mp];B[no];W[oq];B[pq];W[pm];B[qo];W[ro];B[pn];W[rn];B[rp];W[nm];B[mn];W[kq];B[pr];W[qi]" +
            ";B[fd];W[gd];B[ge];W[he];B[hd];W[gc];B[id];W[ie];B[jd];W[je];B[kd];W[ib];B[ke];W[ig];B[kg];W[kb];B[qg];W[gf];B[fe];W[qc]" +
            ";B[qd];W[pc];B[oc];W[ob];B[nb];W[rd];B[re];W[rb];B[sd];W[sc];B[rc];W[on];B[po];W[rd];B[pb];W[qb];B[oa];W[se];B[ql];W[qm]" +
            ";B[rj];W[qj];B[qk];W[pk];B[ri];W[oj];B[nk];W[pl];B[iq];W[ko];B[io];W[km];B[ll];W[kl];B[lk];W[gq];B[rl];W[qh];B[rh];W[pg]" +
            ";B[qf];W[ng];B[ni];W[nj];B[mj];W[oi];B[nh];W[oh];B[of];W[nl];B[gp];W[fp];B[go];W[hq];B[ip];W[hl];B[ep];W[fo];B[gm];W[fn]" +
            ";B[hm];W[il];B[gl];W[gk];B[fk];W[fj];B[el];W[ek];B[fl];W[gj];B[dk];W[ej];B[dm];W[eq];B[ci];W[mi];B[mh];W[li];B[lh];W[ki]" +
            ";B[lm];W[kh];B[lg];W[dg];B[cg];W[cf];B[dh];W[bg];B[ch];W[df];B[eg];W[dj];B[cj];W[ck];B[dl];W[bk];B[bl];W[ef];B[ff];W[eh]" +
            ";B[fg];W[bj];B[kk];W[ln];B[ii];W[jk];B[jj];W[fh];B[ik];W[jl];B[jh];W[hi];B[ok];W[bn];B[lo];W[kn];B[cq];W[dq];B[lp];W[kp]" +
            ";B[sf];W[gg];B[bp];W[bo];B[co];W[dp];B[br];W[cr];B[bq];W[cn];B[dr];W[er];B[cs];W[bd];B[cd];W[be];B[bb];W[da];B[ca];W[cb]" +
            ";B[dn];W[cl];B[db];W[ol];B[mk];W[cb];B[cm];W[bm];B[db];W[lq];B[mo];W[cb];B[do];W[ba];B[ir];W[es];B[eo];W[ds];B[as];W[cr]" +
            ";B[ij];W[hk];B[dr];W[sp];B[cr];W[rq];B[rr];W[qp];B[qq];W[sr];B[rp];W[og];B[nn];W[qp];B[sn];W[rs];B[qr];W[sm];B[rm];W[mm]" +
            ";B[rp];W[sq];B[mq];W[mr];B[lr];W[mp];B[pj];W[pi];B[mq];W[or];B[kr];W[mp];B[jf];W[hf];B[mq];W[ns];B[qs];W[mp];B[gh];W[hh]" +
            ";B[mq];W[jr];B[jq];W[mp];B[ss];W[so];B[mq];W[ls];B[js];W[mp];B[ei];W[mq];B[fi];W[bh];B[sl];W[nf];B[ne];W[oe];B[pf];W[me]" +
            ";B[nd];W[mf];B[oo];W[md];B[mc];W[gi];B[ld];W[os];B[sn];W[ps];B[sm];W[sd];B[kj];W[jg];B[ml];W[ih];B[ji];W[if];B[lf];W[op]" +
            ";B[le];W[bi];B[mg];W[qp];B[om];W[rs];B[rp];W[hr];B[hs];W[gs];B[fr];W[fq];B[gn];W[in];B[hp];W[hn];B[is];W[ms];B[en];W[qp]" +
            ";B[ss];W[on];B[rp];W[rs];B[om];W[im];B[fm];W[ao];B[ss];W[on];B[lb];W[kc];B[ks];W[la];B[ma];W[ka];B[lc];W[hc];B[jc];W[jb]" +
            ";B[om];W[qp];B[jo];W[qa];B[rp];W[rs];B[ap];W[pa];B[ss];W[on];B[jn];W[ob];B[om];W[qp];B[pb];W[rs];B[rp];W[ob];B[ss];W[on]" +
            ";B[pb];W[pj];B[ob];W[al];B[kf];W[];B[om];W[qp];B[jm];W[];B[rp];W[rs];B[ho];W[];B[ss];W[on];B[jp];W[];B[om];W[qp];B[ic];" +
            "W[];B[rp];W[rs];B[];W[])");
    }

    @Test
    @Disabled
    public void test_6_b4344ab70911bd4a3a90fa17d1bae61209298b7e_another_real_superKO() throws IOException
    {
        // it does not seems KGO implements SuperKO.
        // src: b4344ab70911bd4a3a90fa17d1bae61209298b7e  kgs-19-2019-01-new/2019-01-01-1.sgf
        assertAllMovesAreValid("(;GM[1]FF[4]SZ[19]KM[0.50] ; B[pd];W[dp];B[pp];W[dd];B[fq];W[cn];B[fc];W[ec];B[fd];W[df];B[jc];W[qn]" +
            ";B[nq];W[mp];B[np];W[mn];B[mo];W[lo];B[no];W[ln];B[dq];W[cq];B[cr];W[eq];B[dr];W[ep];B[er];W[nn];B[qi];W[bq];B[hq];W[qc]" +
            ";B[qd];W[pc];B[oc];W[ob];B[nb];W[nc];B[od];W[mb];B[pb];W[na];B[qb];W[hc];B[hd];W[ph];B[pi];W[rc];B[rb];W[rf];B[re];W[oh]" +
            ";B[oi];W[nh];B[ni];W[mh];B[lj];W[ic];B[id];W[jb];B[kb];W[kc];B[jd];W[hb];B[kh];W[me];B[mf];W[ne];B[md];W[le];B[nd];W[kd]" +
            ";B[ke];W[kf];B[je];W[ld];B[lf];W[jg];B[nf];W[oe];B[of];W[pe];B[kg];W[qe];B[rd];W[jf];B[ih];W[hg];B[jh];W[gf];B[qg];W[qf]" +
            ";B[dc];W[cc];B[db];W[ed];B[eb];W[cb];B[ef];W[eg];B[cd];W[ce];B[fe];W[ff];B[ee];W[de];B[dg];W[cg];B[fg];W[dh];B[eh];W[dg]" +
            ";B[gg];W[gh];B[hf];W[fh];B[ig];W[qh];B[rg];W[rh];B[bd];W[bc];B[qp];W[ri];B[qk];W[ho];B[go];W[gp];B[hp];W[gn];B[io];W[fo]" +
            ";B[in];W[ql];B[rj];W[lk];B[rl];W[kj];B[li];W[jk];B[hm];W[mk];B[hk];W[rm];B[pl];W[pm];B[qm];W[he];B[ie];W[ql];B[ja];W[ol]" +
            ";B[qm];W[if];B[hf];W[ql];B[lb];W[ia];B[ib];W[sl];B[qm];W[sj];B[ql];W[ro];B[pg];W[rp];B[rq];W[rr];B[sq];W[sg];B[pn];W[po]" +
            ";B[on];W[oo];B[om];W[nm];B[pm];W[qq];B[qo];W[pq];B[rn];W[sn];B[sm];W[jb];B[mc];W[rm];B[sk];W[rk];B[sm];W[lc];B[nb];W[rm]" +
            ";B[sk];W[nc];B[ib];W[rk];B[sm];W[jb];B[nb];W[rm];B[sk];W[nc];B[ib];W[rk];B[sm];W[jb])");
    }

    private void assertAllMovesAreValid(String sgf) throws IOException
    {
        game = new Game[1];
        final Reader r = new StringReader(sgf);
        new SGFReader().parse(r, header -> {
            game[0] = new Game(header.size, header.handicap, (byte) (header.komi * 10));
        }, node ->
        {
            assertThat(game[0].play(node.move)).isTrue();
        });

    }


}
