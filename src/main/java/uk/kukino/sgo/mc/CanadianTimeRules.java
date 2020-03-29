package uk.kukino.sgo.mc;

import java.util.function.Supplier;

/***
 * Implements Canadian Time rules, work in progress, pending: overtime, total Game time limit.
 * Max move secs, max. plys per move, and confidence implemented.
 */
public final class CanadianTimeRules
{

    private Supplier<Long> timeSupplier = () -> System.currentTimeMillis();
    private int gameLimitSecs = Integer.MAX_VALUE >> 10; // so it does not overflows, about 24 days
    private int moveMaxSecs = Integer.MAX_VALUE >> 10;
    private int moveMaxPlys = Integer.MAX_VALUE;
    private int overtimeMoves = 0;
    private int overtimeSecs = 0;
    private float enoughConfidence = 1f;
    private int boardSize = 19;
    private boolean built = false;

    private CanadianTimeRules()
    {
    }

    public static CanadianTimeRules builder()
    {
        return new CanadianTimeRules();
    }

    private void assertNotBuilt()
    {
        if (built)
        {
            throw new IllegalStateException("The builder has already created an TimeManager, can't create two.");
        }
    }

    /***
     * Overrides default System.currentTimeMillis() watch.
     * @param timeSupplier
     * @return fluid builder
     */
    public CanadianTimeRules timeSupplier(final Supplier<Long> timeSupplier)
    {
        assertNotBuilt();
        this.timeSupplier = timeSupplier;
        return this;
    }

    public CanadianTimeRules gameLimitSecs(final int secs)
    {
        assertNotBuilt();
        this.gameLimitSecs = secs;
        return this;
    }

    public CanadianTimeRules overtime(final int moves, final int secs)
    {
        assertNotBuilt();
        this.overtimeMoves = moves;
        this.overtimeSecs = secs;
        return this;
    }

    /***
     * if generated move has this enough confidence, instruct to play it.
     * @param confidence a value between 0 and 1.
     * @return
     */
    public CanadianTimeRules enoughConfidence(final float confidence)
    {
        assertNotBuilt();
        this.enoughConfidence = confidence;
        return this;
    }

    public CanadianTimeRules maxMoveSecs(final int secs)
    {
        assertNotBuilt();
        this.moveMaxSecs = secs;
        return this;
    }

    public CanadianTimeRules maxMovePlys(final int plys)
    {
        assertNotBuilt();
        this.moveMaxPlys = plys;
        return this;
    }

    /***
     * hint the time manager with the board size, so it can estimate how many moves to expect and split time proportionally.
     * @param size the game board size
     */
    public CanadianTimeRules boardSize(final int size)
    {
        assertNotBuilt();
        this.boardSize = size;
        return this;
    }

    public TimeManager build()
    {
        this.built = true;
        return new CanadianTimeManagementImpl(this);
    }


    private final class CanadianTimeManagementImpl implements TimeManager
    {
        private CanadianTimeRules config;
        private boolean asItEverTick = false;
        private boolean expectedNewMove = true;
        private int currentPlys = 0;
        private long currentMoveMs = 0;

        private CanadianTimeManagementImpl(final CanadianTimeRules config)
        {
            this.config = config;
        }

        @Override
        public void newGenerateMove()
        {
            this.expectedNewMove = false;
            this.currentPlys = 0;
            this.currentMoveMs = config.timeSupplier.get();
        }

        @Override
        public boolean tick(final int deltaPlys, final float confidence)
        {
            if (expectedNewMove)
            {
                if (!asItEverTick)
                {
                    throw new IllegalStateException("newGenerateMove() should have been called first at the beginning of time.");
                }
                return newMove();
            }
            asItEverTick = true;
            currentPlys += deltaPlys;

            if (config.moveMaxPlys < currentPlys)
            {
                return newMove();
            }
            if (config.enoughConfidence < confidence)
            {
                return newMove();
            }
            final long moveMsSoFar = config.timeSupplier.get() - currentMoveMs;
            if (config.moveMaxSecs * 1000 <= moveMsSoFar)
            {
                return newMove();
            }

            return false;
        }

        private boolean newMove()
        {
            expectedNewMove = true;
            return true;
        }

        @Override
        public String toString()
        {
            return "CanadianTimeRules{move: " + (currentPlys / 1000) + "kp/" + (this.config.timeSupplier.get() - this.currentMoveMs) / 1000 + "s}";
        }
    }


}
