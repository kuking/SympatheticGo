package uk.kukino.sgo.mc;

public interface TimeManager
{
    /***
     * called each time a new move generation is attempted.
     */
    void newGenerateMove();

    /***
     * It should be called frequently from the engine
     * @param plys how many games have been tried
     * @param confidence level of confidence achieved for the best move so far
     * @return true if move generation should be finished and return best known move
     */
    boolean tick(int plys, float confidence);
}
