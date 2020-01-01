package uk.kukino.sgo.gtp;

import com.google.common.truth.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CoreTest
{

    final short B_D4 = Move.parseToVal("Black D4");
    final short W_Q16 = Move.parseToVal("White Q16");

    @Mock
    Engine engine;

    ArgumentCaptor<Float> floatCapt = ArgumentCaptor.forClass(Float.class);
    ArgumentCaptor<Integer> intCapt = ArgumentCaptor.forClass(Integer.class);

    Core underTest;

    @BeforeEach
    public void beforeEach()
    {
        underTest = new Core(engine);
    }

    @Test
    public void nullIsNoop()
    {
        assertGTP(null).isEqualTo("");
        assertNotClosed();
    }

    @Test
    public void emptyLineAndControlsAreOK()
    {
        assertGTP("").isEqualTo("");
        assertGTP("  ").isEqualTo("");
        assertGTP(" \t\t\t ").isEqualTo("");
        assertGTP(" \r\r\r ").isEqualTo("");
        assertGTP(" \t\r ").isEqualTo("");
        assertGTP(" \n\n\r ").isEqualTo("");
        assertNotClosed();
    }

    @Test
    public void comment()
    {
        assertGTP("  # ").isEqualTo("");
        assertGTP("123 # ").isEqualTo("");
        assertGTP("# ").isEqualTo("");
        assertGTP("# Hello Comment ").isEqualTo("");
    }

    @Test
    public void idWithoutCommandIsOK()
    {
        assertGTP("123  ").isEqualTo("");
    }

    @Test
    public void miscNonTrimmedCommands()
    {
        when(engine.name()).thenReturn("EngineName");
        when(engine.version()).thenReturn("v1.2.3.4");
        assertGTP("\t\r 1 \t\t\r   name   \r\r  ").isEqualTo("=1 EngineName");
        assertGTP(" \r  2 \t\r   version  \r\r  ").isEqualTo("=2 v1.2.3.4");
        assertGTP("3 \r\r protocol_version    \r").isEqualTo("=3 2");
        assertGTP("\t\t\r   name   \r\r  ").isEqualTo("= EngineName");
        assertGTP("\t\r   version  \r\r  ").isEqualTo("= v1.2.3.4");
        assertGTP("protocol_version    \r").isEqualTo("= 2");
    }

    @Test
    public void unknownCommand()
    {
        assertGTP("wtf").isEqualTo("? unknown command");
        assertGTP("0123 wtf").isEqualTo("?123 unknown command");
        assertNotClosed();
    }

    @Test
    public void protocolVersion()
    {
        assertGTP("protocol_version").isEqualTo("= 2");
        assertGTP("34 protocol_version").isEqualTo("=34 2");
    }

    @Test
    public void name()
    {
        when(engine.name()).thenReturn("EngineName");
        assertGTP("name").isEqualTo("= EngineName");
        verify(engine, times(1)).name();
    }

    @Test
    public void version()
    {
        when(engine.version()).thenReturn("v1.2.3.4");
        assertGTP("version").isEqualTo("= v1.2.3.4");
        verify(engine, times(1)).version();
    }

    @Test
    public void listCommands()
    {
        //TODO: should be sorted alphabetically?
        String allCommands = "= ";
        for (Command cmd : Command.values())
        {
            allCommands += cmd.name().toLowerCase() + "\n";
        }
        allCommands = allCommands.substring(0, allCommands.length() - 1);
        assertGTP("list_commands").isEqualTo(allCommands);
    }

    @Test
    public void knownCommand()
    {
        assertGTP("known_command known_command").isEqualTo("= true");
        assertGTP("known_command name").isEqualTo("= true");
        assertGTP("known_command     version    ").isEqualTo("= true");
        assertGTP("known_command").isEqualTo("= false");
        assertGTP("known_command invalid").isEqualTo("= false");
        assertGTP("565 known_command invalid").isEqualTo("=565 false");
    }

    @Test
    public void quit()
    {
        assertGTP("quit").isEqualTo("= ");
        assertClosed();
    }

    @Test
    public void quitWithId()
    {
        assertGTP("938 quit").isEqualTo("=938 ");
        assertClosed();
    }

    @Test
    public void boardSize()
    {
        when(engine.setBoardSize((byte) 19)).thenReturn(true);
        when(engine.setBoardSize((byte) 35)).thenReturn(false);

        assertGTP("boardsize 19").isEqualTo("= ");
        verify(engine, times(1)).setBoardSize((byte) 19);

        assertGTP("boardsize 35").isEqualTo("? unacceptable size");
        verify(engine, times(1)).setBoardSize((byte) 35);

        assertGTP("boardsize").isEqualTo("? boardsize not an integer");
        assertGTP("boardsize         ").isEqualTo("? boardsize not an integer");
        assertGTP("boardsize  -123   ").isEqualTo("? unacceptable size");
        assertGTP("boardsize  19x19  ").isEqualTo("? boardsize not an integer");
        assertGTP("boardsize  some   ").isEqualTo("? boardsize not an integer");
        assertGTP("22 boardsize  some   ").isEqualTo("?22 boardsize not an integer");
    }

    @Test
    public void clearBoard()
    {
        assertGTP("clear_board").isEqualTo("= ");
        assertGTP("123 clear_board").isEqualTo("=123 ");
        assertGTP("clear_board wtf").isEqualTo("= ");
        verify(engine, times(3)).clearBoard();
    }

    @Test
    public void komi()
    {
        assertGTP("komi 5.5").isEqualTo("= ");

        verify(engine, times(1)).setKomi(floatCapt.capture());
        assertThat(floatCapt.getValue()).isEqualTo(5.5f);

        assertGTP("123 komi 4.5").isEqualTo("=123 ");
        assertGTP("komi").isEqualTo("? komi not a float");
        assertGTP("komi LALA").isEqualTo("? komi not a float");
    }

    @Test
    public void play()
    {
        when(engine.play(B_D4)).thenReturn(true).thenReturn(false);

        assertGTP("play B D4").isEqualTo("= ");
        assertGTP("play Black D4").isEqualTo("? illegal move");
        assertGTP("play Not a Move").isEqualTo("? invalid color or coordinate");
        assertGTP("play").isEqualTo("? invalid color or coordinate");

        verify(engine, times(2)).play(eq(B_D4)); // only calls the twice as only two valid moves are provided

        assertGTP("123 play 123").isEqualTo("?123 invalid color or coordinate");
        assertGTP("play Black Black D4").isEqualTo("? invalid color or coordinate");
    }

    @Test
    public void genmove()
    {
        when(engine.genMove(Color.BLACK)).thenReturn(B_D4).thenReturn(Move.pass(Color.BLACK));
        when(engine.genMove(Color.WHITE)).thenReturn(W_Q16);

        assertGTP("genmove black").isEqualTo("= D4");
        assertGTP("genmove b").isEqualTo("= PASS");
        assertGTP("genmove White").isEqualTo("= Q16");
        assertGTP("genmove W").isEqualTo("= Q16");

        verify(engine, times(2)).genMove(Color.BLACK);
        verify(engine, times(2)).genMove(Color.WHITE);

        assertGTP("123 genmove black").isEqualTo("=123 PASS");
        assertGTP("genmove").isEqualTo("? invalid color");
    }

    @Test
    public void fixedHandicap()
    {

        when(engine.fixedHandicap(2))
            .thenReturn(new short[] {Coord.parseToVal("D4"), Coord.parseToVal("Q16")})
            .thenReturn(null);

        assertGTP("fixed_handicap 2").isEqualTo("= D4 Q16 ");
        assertGTP("fixed_handicap 2").isEqualTo("? board not empty");
        verify(engine, times(2)).fixedHandicap(2);

        assertGTP("fixed_handicap").isEqualTo("? handicap not an integer");
        assertGTP("fixed_handicap 10").isEqualTo("? invalid handicap");
        assertGTP("123 fixed_handicap 10").isEqualTo("?123 invalid handicap");
    }

    @Test
    public void placeFreeHandicap()
    {
        fail("Implement");
    }

    @Test
    public void undo()
    {
        when(engine.undo()).thenReturn(true).thenReturn(false);

        assertGTP("undo").isEqualTo("= ");
        assertGTP("undo").isEqualTo("? cannot undo");
        verify(engine, times(2)).undo();

        assertGTP("123 undo").isEqualTo("?123 cannot undo");
    }

    @Test
    public void timeSettings()
    {
        when(engine.timeSettings(anyInt(), anyInt(), anyInt())).thenReturn(true).thenReturn(false);

        assertGTP("time_settings 1 2 3").isEqualTo("= ");
        assertGTP("time_settings 4 5 6").isEqualTo("? time not accepted");
        verify(engine, times(2)).timeSettings(intCapt.capture(), intCapt.capture(), intCapt.capture());
        assertThat(intCapt.getAllValues()).containsExactly(1, 2, 3, 4, 5, 6);

        assertGTP("time_settings").isEqualTo("? not three integers");
        assertGTP("time_settings -1 -2 -3").isEqualTo("? invalid positive integer");
        assertGTP("123 time_settings 1 2").isEqualTo("?123 not three integers");
    }

    @Test
    public void timeLeft()
    {
        fail("Implement");
    }

    @Test
    public void finalScore()
    {
        fail("Implement");
    }

    @Test
    public void finalStatusList()
    {
        fail("Implement");
    }

    @Test
    public void showboard()
    {
        when(engine.displayableBoard()).thenReturn("board-printout\nmultiline");
        assertGTP("showboard").isEqualTo("= \nboard-printout\nmultiline");
        assertGTP("123 showboard").isEqualTo("=123 \nboard-printout\nmultiline");

        verify(engine, times(2)).displayableBoard();
    }

    // ----------------------------------------------------------------------------------------------------------------------------------


    private Subject assertGTP(final CharSequence cmd)
    {
        final Object result = underTest.input(cmd);
        if (result != null)
        {
            return assertThat(result.toString());
        }
        else
        {
            return assertThat(result);
        }
    }

    private void assertNotClosed()
    {
        assertThat(underTest.isClosed()).isFalse();
    }

    private void assertClosed()
    {
        assertThat(underTest.isClosed()).isTrue();
    }

}
