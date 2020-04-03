package uk.kukino.sgo.images;

import uk.kukino.sgo.base.Board;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.sgf.Result;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Renderer
{
    private class Ctx
    {
        final Graphics2D g;
        final int w, h;
        final int x0, xe;
        final int y0, ye;
        final float xg, yg;
        final int size;

        public Ctx(final BufferedImage image, final int boardSize)
        {
            g = (Graphics2D) image.getGraphics();
            w = image.getWidth();
            h = image.getHeight();
            x0 = (int) (w * 0.05f);
            xe = (int) (w * 0.85f);
            y0 = (int) (h * 0.05f);
            ye = (int) (h * 0.85f);
            xg = (xe - x0) / (float) (boardSize - 1);
            yg = (ye - y0) / (float) (boardSize - 1);
            size = boardSize;
        }

        public boolean setStoneColor(final uk.kukino.sgo.base.Color color)
        {
            if (color == uk.kukino.sgo.base.Color.WHITE)
            {
                g.setColor(Color.WHITE);
            }
            else if (color == uk.kukino.sgo.base.Color.BLACK)
            {
                g.setColor(Color.BLACK);
            }
            else
            {
                return false;
            }
            return true;
        }
    }

    private void drawBoard(final Ctx c)
    {
        c.g.setColor(Color.YELLOW);
        c.g.fillRect(0, 0, c.h, c.w);
        c.g.setColor(Color.BLACK);
        for (int x = 0; x < c.size; x++)
        {
            c.g.drawLine(c.x0 + (int) (x * c.xg), c.y0, c.x0 + (int) (x * c.xg), c.ye);
        }
        for (int y = 0; y < c.size; y++)
        {
            c.g.drawLine(c.x0, c.y0 + (int) (y * c.yg), c.ye, c.y0 + (int) (y * c.yg));
        }
    }


    private void drawDeaths(final Ctx c, final int count, final uk.kukino.sgo.base.Color color)
    {

        final float lxg = c.xg / 1.5f;
        final float lyg = c.yg / 1.5f;

        for (int i = 0; i < count; i++)
        {
            if (!c.setStoneColor(color))
            {
                return;
            }

            final int x0;
            final int y0;
            final int wr = (int) (lxg * 0.8f);
            final int hr = (int) (lxg * 0.8f);
            if (color == uk.kukino.sgo.base.Color.BLACK)
            {
                x0 = c.x0 + (int) (i * lxg - lxg * 0.35f);
                y0 = c.ye + (int) (c.yg * 0.6f);
            }
            else
            {
                x0 = c.xe + (int) (c.xg * 0.6f);
                y0 = c.y0 + (int) (i * lyg - lyg * 0.35f);
            }
            c.g.fillRoundRect(x0, y0, wr, hr, (int) c.xg, (int) c.yg);
            c.g.setColor(Color.BLACK);
            c.g.drawRoundRect(x0, y0, wr, hr, (int) c.xg, (int) c.yg);
        }
    }

    private void drawStone(final Ctx c, final int sx, final int sy, final uk.kukino.sgo.base.Color color)
    {
        if (!c.setStoneColor(color))
        {
            return;
        }
        final int x0 = c.x0 + (int) (sx * c.xg - c.xg * 0.45f);
        final int y0 = c.y0 + (int) (sy * c.yg - c.yg * 0.45f);
        final int wr = (int) (c.xg * 0.9f);
        final int hr = (int) (c.yg * 0.9f);
        c.g.fillRoundRect(x0, y0, wr, hr, (int) c.xg, (int) c.yg);
        c.g.setColor(Color.BLACK);
        c.g.drawRoundRect(x0, y0, wr, hr, (int) c.xg, (int) c.yg);
    }

    public BufferedImage render(final Game game, final Result result, final int size) throws IOException
    {
        final Board board = game.getBoard();
        final BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        final Ctx c = new Ctx(img, board.size());
        drawBoard(c);
        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                drawStone(c, x, board.size() - y - 1, board.get(x, y));
            }
        }
        drawDeaths(c, game.deadStones(uk.kukino.sgo.base.Color.BLACK), uk.kukino.sgo.base.Color.BLACK);
        drawDeaths(c, game.deadStones(uk.kukino.sgo.base.Color.WHITE), uk.kukino.sgo.base.Color.WHITE);
        return img;
    }
}
