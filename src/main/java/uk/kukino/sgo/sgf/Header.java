package uk.kukino.sgo.sgf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class Header
{
    Charset charset; //CA
    byte fileFormat; //FF
    GameType gameType; //GM
    CharSequence application; //AP
    byte variationsFormat; //ST
    byte size; //SZ
    CharSequence name; //GN
    CharSequence comment; //C
    CharSequence blackName; //PB
    CharSequence whiteName; //WP
    LocalDateTime dateTime; //DT
    int timeLimitSecs; //TM
    Rule rules; //RU
    byte handicap; //HA
    byte komiX10; //KM

//    List<Short> blackTerritory;  //TB
//    List<Short> whiteTerritory;  //TW
//    CharSequence commentatorName; //AN
//    Rank blackRank; //BR
//    Rank whiteRank; //WR
//    CharSequence blackTeam; //BT
//    CharSequence whiteTeam; //WT
//    CharSequence copyRight; //CP
//    CharSequence event; //EV
//    CharSequence summary; //GC
//    CharSequence openning; //ON
//    CharSequence overtime; //OT
//    CharSequence place; //PC
//    Score result; //RE
//    CharSequence round; //RO
//    CharSequence source; //SO

//    CharSequence userOrProgramName; //US


    public void reset()
    {
        charset = StandardCharsets.UTF_8;
        fileFormat = -1;
        gameType = null;
        application = null;
        variationsFormat = 0;
        size = -1;
        name = null;
        comment = null;
        blackName = null;
        whiteName = null;
        dateTime = null;
        timeLimitSecs = 0;
        handicap = 0;
        rules = null;

        komiX10 = -1;
    }

    public Header clone()
    {
        final Header clone = new Header();
        clone.charset = charset;
        clone.fileFormat = fileFormat;
        clone.gameType = gameType;
        clone.application = application;
        clone.variationsFormat = variationsFormat;
        clone.size = size;
        clone.name = name;
        clone.comment = comment;
        clone.blackName = blackName;
        clone.whiteName = whiteName;
        clone.dateTime = dateTime;
        clone.timeLimitSecs = timeLimitSecs;
        clone.rules = rules;
        clone.handicap = handicap;
        clone.komiX10 = komiX10;
        return clone;
    }

    public boolean isEmpty()
    {
        return charset == StandardCharsets.UTF_8 &&
            fileFormat == -1 &&
            gameType == null &&
            application == null &&
            variationsFormat == 0 &&
            size == -1 &&
            name == null &&
            comment == comment &&
            blackName == null &&
            whiteName == null &&
            dateTime == null &&
            timeLimitSecs == 0 &&
            rules == null &&
            handicap == 0 &&
            komiX10 == -1;
    }

}
