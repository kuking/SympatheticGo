package uk.kukino.sgo.gtp;

import com.google.common.truth.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CoreTest
{

    @Mock
    Engine engine;

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
