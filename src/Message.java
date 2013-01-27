import java.awt.Color;
import java.awt.Graphics;

class Message {
    int fromPort;
    int toPort;
    Edge edge;
    String content;
    int x, y;

    public Message(int port, String content) {
        this.fromPort = port;
        this.content = content;
        dead = false;
        position = speed = 0.0;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
        x = edge.from.getX();
        y = edge.from.getY();
    }

    public void queueDraw(Graphics g, double position, double size) {
        g.setColor(new Color(200, 255, 200));
        Canvas.realFillRect(g, position+1, 5, size-2, size);
        g.setColor(new Color(0, 0, 0));
        Canvas.realDrawRect(g, position+1, 5, size-2, size);
        g.drawString(((Integer) edge.to.getID()).toString(),
                (int)(5+position), 20);
    }

    public void messageDraw(Graphics g) {
        g.setColor(new Color(255, 0, 0));
        int x = (int) Math.round(edge.from.getX() * (1.0 - position)
                + edge.to.getX() * (position));
        int y = (int) Math.round(edge.from.getY() * (1.0 - position)
                + edge.to.getY() * (position));
        int xPoints[] = new int[3], yPoints[] = new int[3];
        xPoints[0] = x; yPoints[0] = y;
        double ux = edge.from.getY() - edge.to.getY(), uy = edge.to.getX() - edge.from.getX();
        //Tu sa da nastavovat velkost trojuholnika
        double k = 12.0 / Math.sqrt(ux * ux + uy * uy);
        double vx = ux * k;
        double vy = uy * k;
        xPoints[1] = (int) Math.round(x + vx + vy/2.0); yPoints[1] = (int) Math.round(y + vy - vx/2.0);
        xPoints[2] = (int) Math.round(x + vx - vy/2.0); yPoints[2] = (int) Math.round(y + vy + vx/2.0);
        g.fillPolygon(xPoints, yPoints, 3);
        //g.drawString(((Integer) edge.to.getID()).toString(), x, y);
    }

    double position, speed;
    long expectedRecieve;
    boolean dead;

    public void setRecieveness(long time) {
        if (time < 0) {
            expectedRecieve = 0;
            dead = true;
            return;
        }
        expectedRecieve = time;
    }

    public void step(long time) {
        //System.err.println("time " + time + " expected " + expectedTime);
        double expectedTime = (expectedRecieve -  System.currentTimeMillis())*0.001;
        double expectedSpeed;
        if (expectedTime < 1e-3) {
            expectedSpeed = 1;
        } else {
            expectedSpeed = (1.0 - position) / expectedTime;
        }

        speed = expectedSpeed;
        //speed += (expectedSpeed-speed)*(0.01);
        position += speed * time * 0.001;
        if (dead && position >= 1.0) {
            MessageQueue.getInstance().deadlist.remove(this);
            position = 1.0;
            edge.to.receive(this);
        }
    }

}
