import java.awt.Color;
import java.awt.Graphics;

class Message {
    int fromPort;
    int toPort;
    Edge edge;
    String rawContent;
    double ePosition, eSpeed;
    Color gColor;
    Cube cube;

    public Message(int port, String content) {
        this.fromPort = port;
        this.rawContent = content;
        gColor = Color.red;
        processContent();
    }

    void processContent() {
        int pos = 0;
        while ((pos = rawContent.indexOf("$", pos)) != -1) {
            pos++;
            if (rawContent.charAt(pos) == 'C') {
                try {
                    gColor = new Color(Integer.parseInt(rawContent.substring(pos + 1, pos + 7), 16));
                } catch (Exception e) {
                    gColor = Color.black;
                }
            }
        }
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public void drawInfo(Graphics g, int rX, int rY, int rW, int rH) {
        // tuto to mozno nie je uplne najrychljeise

        String[] ids = Canvas.shortenWrap(g, ((Integer) edge.from.getID()).toString() + ">"
                + ((Integer) edge.to.getID()).toString(), rW - 2, ">");
        for (int i = 0; i < ids.length
                && g.getFontMetrics().getHeight() * (i + 1) < rH + g.getFontMetrics().getLeading(); ++i) {
            g.drawString(ids[i], rX + 1, rY + g.getFontMetrics().getHeight() * (i + 1)
                    - g.getFontMetrics().getLeading() - g.getFontMetrics().getDescent());
        }
        String[] contents = Canvas.multiGet(g, rawContent, rW - 2);
        for (int i = 0; i < contents.length
                && g.getFontMetrics().getHeight() * (i + ids.length + 1) < rH
                        + g.getFontMetrics().getLeading(); ++i) {
            g.drawString(contents[i], rX + 1, rY + g.getFontMetrics().getHeight()
                    * (i + ids.length + 1) - g.getFontMetrics().getLeading()
                    - g.getFontMetrics().getDescent());
        }
    }

    public void edgeDraw(Graphics g, double offsetx, double offsety, double zoom) {
        g.setColor(gColor);
        double x = edge.from.getX() * (1.0 - ePosition) + edge.to.getX() * ePosition;
        double y = edge.from.getY() * (1.0 - ePosition) + edge.to.getY() * ePosition;
        int rX = (int) (offsetx + x * zoom);
        int rY = (int) (offsety + y * zoom);
        // Tu sa da nastavovat velkost trojuholnika
        int rR = (int) (12.0 * zoom);
        int xPoints[] = new int[3], yPoints[] = new int[3];
        xPoints[0] = rX;
        yPoints[0] = rY;
        double ux = edge.from.getY() - edge.to.getY(), uy = edge.to.getX() - edge.from.getX();

        double k = rR / Math.sqrt(ux * ux + uy * uy);
        double vx = ux * k;
        double vy = uy * k;
        xPoints[1] = (int) Math.round(rX + vx + vy / 2.0);
        yPoints[1] = (int) Math.round(rY + vy - vx / 2.0);
        xPoints[2] = (int) Math.round(rX + vx - vy / 2.0);
        yPoints[2] = (int) Math.round(rY + vy + vx / 2.0);
        g.fillPolygon(xPoints, yPoints, 3);
        // g.drawString(((Integer) edge.to.getID()).toString(), x, y);
    }

    public void edgeStep(long time) {
        ePosition = 0.5;

        /*double expectedTime = (1.0 - qSize) / vspeed + qX
                / (hspeed * MessageQueue.getInstance().getRealSendSpeed());
        if (state == MessageState.dead) {
            expectedTime = 0;
        }

        double expectedSpeed;
        if (expectedTime < 1e-2) {
            expectedSpeed = 1;
        } else {
            expectedSpeed = (1.0 - ePosition) / expectedTime;
        }
        if (expectedSpeed > 1.0) {
            expectedSpeed = 1.0;
        }

        eSpeed = expectedSpeed;
        // speed += (expectedSpeed-speed)*(0.01);
        ePosition += eSpeed * time * 0.001;
        if (ePosition >= 1.0) {
            ePosition = 1.0;
        }
        if (state == MessageState.dead) {
            MessageQueue.getInstance().deadList.remove(this);
            edge.to.receive(this);
        }*/
    }

}
