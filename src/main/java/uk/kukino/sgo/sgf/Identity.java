package uk.kukino.sgo.sgf;

public enum Identity
{
    FileFormat("FF"), Charset("CA"), Application("AP"), Game("GM"), VariationFormat("ST"),
    GameName("GN"), Size("SZ"), Handicap("HA"), Komi("KM"), Date("DT"), TimeLimit("TM"), Rules("RU"), Place("PC"), Result("RE"),
    OverTime("OT"),
    BlackName("PB"), WhiteName("PW"), WhiteRank("WR"), BlackRank("BR"),
    Comment("C"), Black("B"), White("W"),
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
