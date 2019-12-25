package uk.kukino.sgo.sgf;

public enum Identity
{
    // Headers
    FileFormat("FF"), Charset("CA"), Application("AP"), Game("GM"), VariationFormat("ST"), Identifier("ID"),
    GameName("GN"), Size("SZ"), Handicap("HA"), Komi("KM"), Date("DT"), TimeLimit("TM"), Rules("RU"), Place("PC"), Result("RE"),
    OverTime("OT"), Event("EV"), Round("RO"), Username("US"), ViewOnly("VW"), Source("SO"), Copyright("CP"), GeneralComment("GC"),

    BlackName("PB"), BlackRank("BR"), BlackTeam("BT"), BlackTimeLeft("BL"), BlackSpecies("BS"),
    WhiteName("PW"), WhiteRank("WR"), WhiteTeam("WT"), WhiteTimeLeft("WL"), WhiteSpecies("WS"),

    // Nodes
    Comment("C"), Black("B"), White("W"), AddBlack("AB"), AddWhite("AW"),
    Circles("CR"), Triangles("TR"), Squares("SQ"), SelectedPoints("SL"), Marks("MA"),
    PlayerToPlay("PL"),
    BlackTerritory("TB"),WhiteTerritory("TW"),
    BlackMovesLeft("OB"),WhiteMovesLeft("OW"),

    // Unknown?
    Unknown("??");

    int idUp, idLo;

    Identity(final String value)
    {
        if (value.length() == 2)
        {
            idUp = ((value.charAt(0)) << 8) + value.charAt(1);
            idLo = ((value.toLowerCase().charAt(0)) << 8) + value.toLowerCase().charAt(1);
        }
        else
        {
            idUp = value.charAt(0);
            idLo = value.toLowerCase().charAt(0);
        }
    }

    public static Identity fromInt(final int id)
    {
        for (final Identity identity : values())
        {
            if (identity.idUp == id || identity.idLo == id)
            {
                return identity;
            }
        }
        return Unknown;
    }
    }
