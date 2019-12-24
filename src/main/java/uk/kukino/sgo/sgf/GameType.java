package uk.kukino.sgo.sgf;

public enum GameType
{
    Go(1), Othello(2), chess(3), Gomoku_Renju(4), Nine_Mens_Morris(5), Backgammon(6), Chinese_chess(7), Shogi(8), Lines_of_Action(9),
    Ataxx(10), Hex(11), Jungle(12), Neutron(13), Philosophers_Football(14), Quadrature(15), Trax(16), Tantrix(17), Amazons(18), Octi(19),
    Gess(20), Twixt(21), Zertz(22), Plateau(23), Yinsh(24), Punct(25), Gobblet(26), hive(27), Exxit(28), Hnefatal(29), Kuba(30),
    Tripples(31), Chase(32), Tumbling_Down(33), Sahara(34), Byte(35), Focus(36), Dvonn(37), Tamsk(38), Gipf(39), Kropki(40);

    final int value;

    GameType(int value)
    {
        this.value = value;
    }

    public static GameType byId(final int value)
    {
        for (final GameType gt : values())
        {
            if (gt.value == value)
            {
                return gt;
            }
        }
        return null;
    }
}
