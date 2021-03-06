import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

public class Vertex {

    private double x, y, radius;
    private int ID;
    private Color color;
    private Color auraColor;

    // @formatter:off
    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public void move(double x, double y) { 
        this.x = x; 
        this.y = y;
        bubble.move(this.x, this.y-this.radius);
    }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color;}
    public Color getAuraColor() { return auraColor; }
    public void setAuraColor(Color color) { this.auraColor = color;}
    public void setRadius(double radius) {
        this.radius = radius;
        GUI.graph.pushAway(this);
        bubble.move(this.x, this.y-this.radius);
    }
    // @formatter:on

    ArrayList<Edge> edges;
    Program program;
    Bubble bubble;

    public Vertex(double x, double y) {
        this(x, y, 0);
    }

    public Vertex(double x, double y, int ID) {
        edges = new ArrayList<Edge>();
        this.x = x;
        this.y = y;
        this.ID = ID;
        radius = CONST.vertexSize;
        watchVariables = new HashMap<String, String>();
        color = new Color(0, 255, 0);
        bubble = new Bubble(this.x, this.y - this.radius);
        auraColor = new Color(255, 255, 255, 0);
    }

    public void defaultSettings() {
        color = new Color(0, 255, 0);
        radius = CONST.vertexSize;
        bubble.defaultSettings();
    }

    void send(Message message) {
        edges.get(message.fromPort).send(message);
    }

    void receive(Message message) {
        program.receive(message);
        message.state = DeliverState.delivered;
        if (message.selected != 0) {
            message.selected = 0;
        }
    }

    void shout(String s, int strength) {
        long duration = strength * 4000;
        bubble.addInformation(s, BubbleSet.time + duration);
    }

    public Map<String, String> watchVariables;

    public void setVariable(String name, String value) {
        // TODO Fuj object hore nechceme, ale toto je len provizorne riesenie 
        if (name.charAt(0) == '_') {
            if (name.equals("_vertex_color")) {
                String parts[] = value.split(",", 3);
                setColor(new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1]
                        .trim()), Integer.parseInt(parts[2].trim())));
            }
            if (name.equals("_vertex_size")) {
                setRadius(CONST.vertexSize * Math.sqrt(Integer.parseInt(value.trim()) * 0.01));
            }
        } else {
            watchVariables.remove(name);
            watchVariables.put(name, value);
        }
        GUI.gRepaint();
    }

    public void draw(Graphics2D g) {
        draw(g, true);
    }

    public void draw(Graphics2D g, boolean showID) {
        drawAura(g);
        Ellipse2D ellipse = new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius);
        g.setColor(color);
        g.fill(ellipse);
        g.setColor(Canvas.contrastColor(color, Constrast.borderbw));
        g.draw(ellipse);

        if (showID) {
            g.setColor(Canvas.contrastColor(color, Constrast.textbw));
            g.setFont(new Font(Font.DIALOG, Font.PLAIN, (int) (13 * Math.sqrt(1))));
            String caption = Canvas.shorten(g, ((Integer) ID).toString(), (int) (radius * 2),
                    Preference.begin);
            if (caption.endsWith(".."))
                caption = "V";
            g.drawString(caption, (float) (x - g.getFontMetrics().stringWidth(caption) / 2),
                    (float) (y + g.getFontMetrics().getAscent() / 2));
        }

    }

    public void drawAura(Graphics2D g) {
        g.setColor(auraColor);
        Ellipse2D ellipse = new Ellipse2D.Double(x - radius - 6f, y - radius - 6f,
                2f * radius + 12f, 2f * radius + 12f);
        g.fill(ellipse);
    }

    public void zoomDraw(Graphics2D g) {
        int edgeHeight = 20;
        int indent = 25;
        int width = CONST.zoomWindowWidth - 2 * indent, height = CONST.zoomWindowHeight - 2
                * indent;
        g.setColor(new Color(0, 255, 0, 100));
        g.fillRect(indent, indent, width, height);
        g.setColor(new Color(0, 0, 0));
        g.drawRect(indent, indent, width, height);
        g.drawLine(indent, indent + edgeHeight, width + indent, indent + edgeHeight);
        g.drawLine(indent, height + indent - edgeHeight, width + indent, height + indent
                - edgeHeight);
        ArrayList<Vertex> neigh = new ArrayList<Vertex>();
        for (Edge edge : edges) {
            if (!neigh.contains(edge.to))
                neigh.add(edge.to);
        }
        int n = neigh.size();
        int up = n / 2, down = n / 2 + n % 2;
        for (int i = 0; i < up - 1; i++) {
            g.drawLine(indent + (i + 1) * (width / up), indent, indent + (i + 1) * (width / up),
                    indent + edgeHeight);
        }
        for (int i = 0; i < down - 1; i++) {
            g.drawLine(indent + (i + 1) * (width / down), CONST.zoomWindowHeight - indent
                    - edgeHeight, indent + (i + 1) * (width / down), CONST.zoomWindowHeight
                    - indent);
        }
        int fontSize = 1000;
        for (Vertex vertex : neigh) {
            fontSize = Math
                    .min(fontSize,
                            findFontSize(g, width / down, edgeHeight,
                                    ((Integer) vertex.getID()).toString()));
        }
        for (int i = 0; i < up; i++) {
            int boxWidth = width / up;
            String caption = ((Integer) neigh.get(i).getID()).toString();
            g.setFont(new Font(null, Font.PLAIN, fontSize));
            int textWidth = g.getFontMetrics().stringWidth(caption);
            g.drawString(((Integer) neigh.get(i).getID()).toString(), indent + i * boxWidth
                    + (boxWidth - textWidth) / 2, indent + edgeHeight - 1);
            g.drawLine(indent + i * boxWidth + boxWidth / 2, indent, indent + i * boxWidth
                    + boxWidth / 2, 0);
        }
        for (int i = 0; i < down; i++) {
            int boxWidth = width / down;
            String caption = ((Integer) neigh.get(up + i).getID()).toString();
            g.setFont(new Font(null, Font.PLAIN, fontSize));
            int textWidth = g.getFontMetrics().stringWidth(caption);
            g.drawString(((Integer) neigh.get(up + i).getID()).toString(), indent + i * boxWidth
                    + (boxWidth - textWidth) / 2, CONST.zoomWindowHeight - indent - 1);
            g.drawLine(indent + i * boxWidth + boxWidth / 2, CONST.zoomWindowHeight - indent,
                    indent + i * boxWidth + boxWidth / 2, CONST.zoomWindowHeight);
        }
        g.setFont(new Font(null, Font.PLAIN, 15));
        g.drawString("ID: " + ((Integer) this.getID()).toString(), indent + 5, indent + edgeHeight
                + 15);
        Iterator<Entry<String, String>> it = watchVariables.entrySet().iterator();
        int count = 2;
        while (it.hasNext()) {
            Map.Entry<String, String> me = it.next();
            g.drawString(me.getKey() + ": " + me.getValue(), indent + 5, indent + edgeHeight + 15
                    * count);
            count++;
        }
    }

    public int findFontSize(Graphics g, int boxWidth, int maxSize, String caption) {
        int z = 1, k = maxSize;
        while (k - z > 1) {
            int mid = (k + z) / 2;
            g.setFont(new Font(null, Font.PLAIN, mid));
            if (g.getFontMetrics().stringWidth(caption) > boxWidth - 2)
                k = mid;
            else
                z = mid;
        }
        return z;
    }

    public boolean isOnPoint(double x, double y) {
        return isNearPoint(x, y, 0.0);
    }

    public boolean isNearPoint(double x1, double y1, double distance) {
        return (x1 - x) * (x1 - x) + (y1 - y) * (y1 - y) < (radius + distance)
                * (radius + distance);
    }

    public void onClicked() {
        Dialog.DialogNewVertex newVertexDialog = new Dialog.DialogNewVertex(getID());
        int ok = JOptionPane.showConfirmDialog(null, newVertexDialog.getPanel(), "Edit vertex",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            setID(newVertexDialog.getID());
        }
    }

    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

}
