package uk.kukino.sgo.sgf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class Header
{
    public Charset charset; //CA
    public byte fileFormat; //FF
    public GameType gameType; //GM
    public CharSequence application; //AP
    public byte variationsFormat; //ST
    public byte size; //SZ
    public CharSequence name; //GN
    public CharSequence comment; //C
    public CharSequence blackName; //PB
    public CharSequence whiteName; //WP
    public LocalDateTime dateTime; //DT
    public int timeLimitSecs; //TM
    public Rule rules; //RU
    public byte handicap; //HA
    public float komi; //KM
    public Rank blackRank; //BR
    public Rank whiteRank; //WR
    public CharSequence place; //PC
    public Result result; //RE
    public Species blackSpecies; //BS
    public Species whiteSpecies; //WS

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
        blackSpecies = null;
        whiteSpecies = null;
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
        clone.blackSpecies = blackSpecies;
        clone.whiteSpecies = whiteSpecies;
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
            result == null &&
            blackSpecies == null &&
            whiteSpecies == null;
    }

}
