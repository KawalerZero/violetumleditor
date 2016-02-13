/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2007 Cay S. Horstmann (http://horstmann.com)
 Alexandre de Pellegrin (http://alexdp.free.fr);

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet.product.diagram.sequence.nodes;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.horstmann.violet.framework.graphics.content.*;
import com.horstmann.violet.framework.graphics.shape.ContentInsideRectangle;
import com.horstmann.violet.product.diagram.abstracts.node.AbstractNode;
import com.horstmann.violet.product.diagram.abstracts.node.ColorableNode;
import com.horstmann.violet.product.diagram.abstracts.property.ArrowHead;
import com.horstmann.violet.product.diagram.abstracts.property.string.LineText;
import com.horstmann.violet.product.diagram.abstracts.property.string.decorator.LargeSizeDecorator;
import com.horstmann.violet.product.diagram.abstracts.property.string.decorator.OneLineString;
import com.horstmann.violet.product.diagram.abstracts.property.string.decorator.UnderlineDecorator;
import com.horstmann.violet.product.diagram.abstracts.edge.IEdge;
import com.horstmann.violet.product.diagram.abstracts.node.INode;
import com.horstmann.violet.product.diagram.abstracts.property.string.SingleLineText;
import com.horstmann.violet.product.diagram.sequence.edges.CallEdge;

/**
 * An object node_old in a scenario diagram.
 */
public class LifelineNode extends ColorableNode
{
    /**
     * Construct an object node_old with a default size
     */
    public LifelineNode()
    {
        super();

        name = new SingleLineText(nameConverter);
        createContentStructure();
    }

    protected LifelineNode(LifelineNode node) throws CloneNotSupportedException
    {
        super(node);
        name = node.name.clone();
        createContentStructure();
    }

    @Override
    public void deserializeSupport()
    {
        super.deserializeSupport();
        name.deserializeSupport(nameConverter);

        for(INode child : getChildren())
        {
            if (child instanceof ActivationBarNode)
            {
                activationsGroup.add(((ActivationBarNode) child).getContent());
            }
        }
    }

    @Override
    protected INode copy() throws CloneNotSupportedException
    {
        return new LifelineNode(this);
    }

    @Override
    protected void createContentStructure()
    {
        TextContent nameContent = new TextContent(name);
        nameContent.setMinHeight(DEFAULT_TOP_HEIGHT);
        nameContent.setMinWidth(DEFAULT_WIDTH);

        ContentInsideShape contentInsideShape = new ContentInsideRectangle(nameContent);

        setBorder(new ContentBorder(contentInsideShape, getBorderColor()));
        setBackground(new ContentBackground(getBorder(), getBackgroundColor()));

        activationsGroup = new RelativeLayout();
        activationsGroup.setMinWidth(ActivationBarNode.DEFAULT_WIDTH);
        activationsGroup.setMinHeight(DEFAULT_HEIGHT - DEFAULT_TOP_HEIGHT);

        EmptyContent padding = new EmptyContent();
        padding.setMinHeight(ACTIVATIONS_PADDING);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(getBackground());
        verticalLayout.add(padding);
        verticalLayout.add(activationsGroup);
        verticalLayout.add(padding);

        setContent(verticalLayout);

        setTextColor(getTextColor());
        setName(getName());
    }

    @Override
    public void setTextColor(Color textColor)
    {
        name.setTextColor(textColor);
        super.setTextColor(textColor);
    }

    @Override
    public void removeChild(INode node)
    {
        activationsGroup.remove(((ActivationBarNode) node).getContent());
        super.removeChild(node);
    }

    @Override
    public boolean addChild(INode node, Point2D p)
    {
        List<INode> activations = getChildren();
        if (!(node instanceof ActivationBarNode))
        {
            return false;
        }
        if (activations.contains(node))
        {
            return true;
        }
        addChild(node, activations.size());

        ActivationBarNode activationBarNode = (ActivationBarNode) node;
        activationBarNode.setTextColor(getTextColor());
        activationBarNode.setBackgroundColor(getBackgroundColor());
        activationBarNode.setBorderColor(getBorderColor());

        activationsGroup.add(activationBarNode.getContent());

        activationBarNode.setLocation(p);
        activationBarNode.setGraph(getGraph());
        activationBarNode.setParent(this);

        return true;
    }

    /**
     * Ensure that child node_old respects the minimum gap with package borders
     *
     * @param child
     */
    protected void onChildChangeLocation(INode child)
    {
        activationsGroup.setPosition(((AbstractNode) child).getContent(), getChildRelativeLocation(child));
    }

    protected Point2D getChildRelativeLocation(INode node)
    {
        Point2D nodeLocation = node.getLocation();
        double relativeCenteredX = getRelativeCenteredPositionX();
        if(DEFAULT_TOP_HEIGHT + ACTIVATIONS_PADDING > nodeLocation.getY() || relativeCenteredX != nodeLocation.getX())
        {
            nodeLocation.setLocation( relativeCenteredX, Math.max(nodeLocation.getY(), DEFAULT_TOP_HEIGHT));
            node.setLocation(nodeLocation);
        }

        return new Point2D.Double(nodeLocation.getX(), nodeLocation.getY()-DEFAULT_TOP_HEIGHT - ACTIVATIONS_PADDING);
    }

    @Override
    public Point2D getLocation()
    {
        double y = 0;
        for (IEdge edge : getGraph().getAllEdges())
        {
            if (edge instanceof CallEdge)
            {
                if (this == edge.getEnd())
                {
                    y = getLocationOnGraph().getY() - DEFAULT_TOP_HEIGHT / 2 + ActivationBarNode.CALL_YGAP / 2;
                }
            }
        }
        return new Point2D.Double(super.getLocation().getX(), y);
    }

    public void draw(Graphics2D g2)
    {
        Rectangle2D bounds = getBounds();
        Point2D startPoint = new Point2D.Double(bounds.getCenterX(), bounds.getMinY());
        Point2D endPoint  = new Point2D.Double(bounds.getCenterX(), getMaxYOverAllLifeLineNodes() + ACTIVATIONS_PADDING);

        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();
        g2.setColor(getBorderColor());
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f, new float[]{5.0f,5.0f}, 0.0f));
        g2.draw(new Line2D.Double(startPoint, endPoint));
        g2.setStroke(oldStroke);
        if(endOfLife)
        {
            ArrowHead.X.draw(g2, startPoint, endPoint);
        }
        g2.setColor(oldColor);

        super.draw(g2);
    }

    public boolean contains(Point2D p)
    {
        double maxYOverAllLifeLineNodes = getMaxYOverAllLifeLineNodes();
        Rectangle2D bounds = getBounds();
        if((maxYOverAllLifeLineNodes >= p.getY() &&
                ActivationBarNode.DEFAULT_WIDTH/2 >= p.getX() - bounds.getCenterX() &&
                ActivationBarNode.DEFAULT_WIDTH/2 >= bounds.getCenterX() - p.getX()) ||
                (bounds.getX() <= p.getX() &&
                        p.getX() <= bounds.getX() + bounds.getWidth()))
        {
            return true;
        }
        return false;
    }





    public boolean addConnection(IEdge e)
    {
        return false;
    }





    @Override
    public Point2D getConnectionPoint(IEdge e)
    {
        Point2D locationOnGraph = getLocationOnGraph();
        double x = locationOnGraph.getX();
        if (0 >= e.getDirection(this).getX())
        {
            x += getContent().getWidth();
        }
        return new Point2D.Double(x, locationOnGraph.getY() + DEFAULT_TOP_HEIGHT / 2);
    }

    private double getMaxY()
    {
        return getContent().getHeight() + getLocationOnGraph().getY();
    }

    private double getMaxYOverAllLifeLineNodes()
    {
        double maxY = getMaxY();

        for (INode node : getGraph().getAllNodes())
        {
            if (node instanceof LifelineNode)
            {
                maxY = Math.max(maxY, ((LifelineNode) node).getMaxY());
            }
        }
        return maxY;
    }

    private void centeredActivationsGroup()
    {
        double relativeCenteredX = getRelativeCenteredPositionX();
        for(INode child : getChildren())
        {
            child.setLocation(new Point.Double(
                    relativeCenteredX,
                    child.getLocation().getY()
            ));
        }
    }
    private double getRelativeCenteredPositionX()
    {
        return (getContent().getWidth()- ActivationBarNode.DEFAULT_WIDTH)/2;
    }

    /**
     * Sets the name property value.
     *
     * @param newValue the name of this object
     */
    public void setName(SingleLineText newValue)
    {
        name.setText(newValue.toEdit());
        centeredActivationsGroup();
    }

    /**
     * Gets the name property value.
     *
     * @return the name of this object
     */
    public SingleLineText getName()
    {
        return name;
    }

    /**
     * Sets the  end of life property value.
     *
     * @param newValue the end of life of this object
     */
    public void setEndOfLife(boolean newValue)
    {
        endOfLife = newValue;
    }

    /**
     * Gets the end of life property value.
     *
     * @return the end of life of this object
     */
    public boolean isEndOfLife()
    {
        return endOfLife;
    }

    public Rectangle2D getTopRectangle()
    {
        return new Rectangle2D.Double(0, 0, DEFAULT_TOP_HEIGHT, getContent().getWidth());
    }

    private SingleLineText name;
    private boolean endOfLife;

    private transient RelativeLayout activationsGroup = null;

    public final static int DEFAULT_TOP_HEIGHT = 60;
    private final static int DEFAULT_WIDTH = 80;
    private final static int DEFAULT_HEIGHT = 80;
    private final static int ACTIVATIONS_PADDING = 10;

    private final static LineText.Converter nameConverter = new LineText.Converter(){
        @Override
        public OneLineString toLineString(String text)
        {
            return new LargeSizeDecorator(new UnderlineDecorator(new OneLineString(text)));
        }
    };
}
