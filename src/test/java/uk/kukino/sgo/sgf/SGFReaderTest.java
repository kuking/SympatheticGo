package uk.kukino.sgo.sgf;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.kukino.sgo.base.Move;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

public class SGFReaderTest
{

    SGFReader underTest;

    int headers[];
    List<Node> nodes;

    @BeforeEach
    public void before()
    {
        underTest = new SGFReader();
        headers = new int[] {0};
        nodes = new ArrayList<>();
    }

    @Test
    public void canHandleNonPrintableCharacters() throws IOException
    {
        var sgfR = new StringReader("  (  ;   FF[4]   GM[1] \n \r \t  SZ[19]   ) \n");
        underTest.parse(sgfR, header ->
            {
                assertThat(header.fileFormat, equalTo((byte) 4));
                assertThat(header.gameType, equalTo(GameType.Go));
                assertThat(header.size, equalTo((byte) 19));
                headers[0]++;
            },
            node -> fail("No nodes in this SGF, this should have not been called.")
        );
        assertThat(headers[0], is(1));
    }

    @Test
    public void canHandleAMoreCompleteHeader() throws IOException
    {
        var sgfR = new StringReader("(;FF[4]GM[1]SZ[19]GN[LeName]PB[Black]HA[0]PW[White]KM[5.5]DT[1999-07-21]TM[1800]RU[Japanese])");
        underTest.parse(sgfR, header ->
            {
                assertThat(header.fileFormat, equalTo((byte) 4));
                assertThat(header.gameType, equalTo(GameType.Go));
                assertThat(header.charset, equalTo(Charsets.UTF_8)); // default
                assertThat(header.size, equalTo((byte) 19));
                assertThat(header.name, equalTo("LeName"));
                assertThat(header.blackName, equalTo("Black"));
                assertThat(header.whiteName, equalTo("White"));
                assertThat(header.handicap, equalTo((byte) 0));
                assertThat(header.komiX10, equalTo((byte) 55));
                assertThat(header.dateTime, equalTo(LocalDateTime.parse("1999-07-21T12:00")));
                assertThat(header.timeLimitSecs, equalTo(1800));
                assertThat(header.rules, equalTo(Rule.Japanese));
                headers[0]++;
            },
            node -> fail("No nodes in this SGF, this should have not been called.")
        );
        assertThat(headers[0], is(1));
    }

    @Test
    public void simplestOneMove() throws IOException
    {

        var sgfR = new StringReader(
            "(;GM[1]FF[4]CA[ASCII]AP[CGoban:2]ST[2]RU[Japanese]SZ[19]KM[5.50]PW[Player White]PB[Player Black]C[Game Comment];B[dp])");
        underTest.parse(sgfR, header ->
            {
                assertThat(header.fileFormat, equalTo((byte) 4));
                assertThat(header.gameType, equalTo(GameType.Go));
                assertThat(header.charset, equalTo(Charsets.US_ASCII));
                assertThat(header.size, equalTo((byte) 19));
                assertThat(header.application, equalTo("CGoban:2"));
                assertThat(header.blackName, equalTo("Player Black"));
                assertThat(header.whiteName, equalTo("Player White"));
                assertThat(header.handicap, equalTo((byte) 0)); // default
                assertThat(header.komiX10, equalTo((byte) 55));
                assertThat(header.dateTime, nullValue());
                assertThat(header.timeLimitSecs, equalTo(0));
                assertThat(header.rules, equalTo(Rule.Japanese));
                assertThat(header.comment, equalTo("Game Comment"));
                headers[0]++;
            },
            node ->
            {
                assertThat(node.move, equalTo(Move.parseToVal("BLACK D4")));
                nodes.add(node.clone());
            }
        );
        assertThat(headers[0], is(1));
        assertThat(nodes, hasSize(1));
    }

    @Test
    public void aSimpleGame() throws IOException
    {

        var sgfR = new StringReader(
            "(;GM[1]FF[4]CA[UTF-8]AP[CGoban:3]ST[2]RU[Japanese]SZ[9]KM[0.00]PW[White]PB[Black]" +
                ";B[cc];W[cb];B[dc];W[db];B[ec];W[eb];B[fc];W[fb];B[fd];W[gc]" +
                ";B[ed];W[gd];B[dd];W[fe];B[cd];W[ee];B[bc];W[de];B[bd];W[ce]" +
                ";B[ad];W[be];B[ac];W[bb];B[ab];W[aa];B[ae];W[af])\n");
        underTest.parse(sgfR, header ->
            {
                assertThat(header.size, equalTo((byte) 9));
                headers[0]++;
            },
            node ->
            {
                nodes.add(node.clone());
            }
        );
        assertThat(headers[0], is(1));
        assertMoves("B C7", "W C8", "B D7", "W D8", "B E7", "W E8", "B F7", "W F8", "B F6", "W G7", "B E6", "W G6", "B D6", "W F5",
            "B C6", "W E5", "B B7", "W D5", "B B6", "W C5", "B A6", "W B5", "B A7", "W B8", "B A8", "W A9", "B A5", "W A4");
    }

    @Test
    public void explicitlyVariantsNotImplemented() throws IOException
    {
        var sgfR = new StringReader(
            "(;FF[4]GM[1]SZ[19]\n" +
                "C[Black to play and live.]\n" +
                "(;B[af];W[ah]\n" +
                "(;B[ce];W[ag]C[only one eye this way])\n" +
                "(;B[ag];W[ce])))\n");
        try
        {
            underTest.parse(sgfR, header -> {
            }, node -> {
            });
            fail("This should have failed complaining about not variants implemented.");
        }
        catch (final ParseException e)
        {
            assertThat(e.getMessage(), startsWith("Variants not implemented in this SGF reader."));
        }
    }

    private void assertMoves(final String... moves)
    {
        assertThat(moves.length, equalTo(nodes.size()));
        for (int i = 0; i < moves.length; i++)
        {
            assertThat("Expected " + moves[i] + " at pos " + i, nodes.get(i).move, equalTo(Move.parseToVal(moves[i])));
        }

    }
}
