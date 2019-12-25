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
    float komi; //KM
    Rank blackRank; //BR
    Rank whiteRank; //WR
    CharSequence place; //PC
    Result result; //RE

//    List<Short> blackTerritory;  //TB
//    List<Short> whiteTerritory;  //TW
//    CharSequence commentatorName; //AN
//    CharSequence blackTeam; //BT
//    CharSequence whiteTeam; //WT
//    CharSequence copyRight; //CP
//    CharSequence event; //EV
//    CharSequence summary; //GC
//    CharSequence openning; //ON
//    CharSequence overtime; //OT
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
        komi = Float.NaN;
        blackRank = null;
        whiteRank = null;
        place = null;
        result = null;
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
        clone.handicap = handicap;
        clone.rules = rules;
        clone.komi = komi;
        clone.blackRank = blackRank == null ? null : blackRank.clone();
        clone.whiteRank = whiteRank == null ? null : whiteRank.clone();
        clone.place = place;
        clone.result = result == null ? null : result.clone();

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
            comment == null &&
            blackName == null &&
            whiteName == null &&
            dateTime == null &&
            timeLimitSecs == 0 &&
            rules == null &&
            handicap == 0 &&
            Float.isNaN(komi) &&
            blackRank == null &&
            whiteRank == null &&
            place == null &&
            result == null;
    }

}
