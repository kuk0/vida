import java.awt.Color;
import java.awt.Graphics2D;

public class ZoomWindow implements Drawable {

    public Canvas canvas;
    public Vertex vertex;
    public Message message;
    public int width, height;

    public ZoomWindow() {
        setCanvas(new Canvas(this));
        canvas.setBackground(new Color(0, 0, 0, 0));
        canvas.repaintColor = new Color(200, 255, 255, 200);
        height = CONST.zoomWindowHeight;
        width = CONST.zoomWindowWidth;
        vertex = null;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public void drawVertex(Vertex v) {
        vertex = v;
        message = null;
        GUI.gRepaint();
    }

    public void drawMessage(Message m) {
        vertex = null;
        message = m;
        GUI.gRepaint();
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(0, 0, 0));
        g.drawRect(0, 0, width - 1, height - 1);
        if (vertex != null)
            vertex.zoomDraw(g);
        if (message != null)
            message.zoomDraw(g);
    }
}
