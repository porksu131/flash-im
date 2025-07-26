package com.szr.flashim.example.gui.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

public class ChatBubble extends JComponent {
    private String text;
    private boolean isRight;
    private final int ARC_RADIUS = 20;
    private final int POINTER_SIZE = 10;
    private final int SHADOW_SIZE = 2;
    private final int BUBBLE_PADDING = 10;
    private final int MIN_BUBBLE_HEIGHT = POINTER_SIZE + ARC_RADIUS; // 最小高度确保指针可见
    private Color BUBBLE_COLOR;
    private Color TEXT_COLOR;
    private final Font FONT = new Font("微软雅黑", Font.PLAIN, 12);
    private final int MAX_BUBBLE_WIDTH = 300;
    private final int MIN_BUBBLE_WIDTH = 25;

    public ChatBubble(String text, boolean isRight) {
        this.text = text;
        this.isRight = isRight;
        this.BUBBLE_COLOR = isRight ? new Color(14, 132, 233) : new Color(230, 230, 230);
        this.TEXT_COLOR = isRight ? Color.WHITE : Color.BLACK;
        setOpaque(false);
        setAlignmentX(isRight ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
    }

    public ChatBubble() {
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public boolean isRight() {
        return isRight;
    }

    public void setRight(boolean right) {
        isRight = right;
        this.BUBBLE_COLOR = isRight ? new Color(14, 132, 233) : new Color(230, 230, 230);
        this.TEXT_COLOR = isRight ? Color.WHITE : Color.BLACK;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        int width = getWidth() - SHADOW_SIZE;
        int height = getHeight() - SHADOW_SIZE;

        // 绘制阴影
        g2d.setColor(new Color(0, 0, 0, 25));
        g2d.fillRoundRect(SHADOW_SIZE, SHADOW_SIZE, width, height, ARC_RADIUS, ARC_RADIUS);

        // 创建气泡形状
        RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(
                0, 0,
                width, height,
                ARC_RADIUS, ARC_RADIUS
        );

        // 创建三角形指示器
        GeneralPath pointer = new GeneralPath();
        if (isRight) {
            pointer.moveTo(width - POINTER_SIZE, height - ARC_RADIUS);
            pointer.lineTo(width, height - ARC_RADIUS - POINTER_SIZE/2);
            pointer.lineTo(width - POINTER_SIZE, height - ARC_RADIUS - POINTER_SIZE);
        } else {
            pointer.moveTo(POINTER_SIZE, height - ARC_RADIUS);
            pointer.lineTo(0, height - ARC_RADIUS - POINTER_SIZE/2);
            pointer.lineTo(POINTER_SIZE, height - ARC_RADIUS - POINTER_SIZE);
        }
        pointer.closePath();

        // 组合形状
        Area area = new Area(bubble);
        area.add(new Area(pointer));

        // 绘制气泡
        g2d.setColor(BUBBLE_COLOR);
        g2d.fill(area);

        // 绘制高光效果
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.draw(new RoundRectangle2D.Double(0.5, 0.5, width-1, height-1, ARC_RADIUS, ARC_RADIUS));

        // 绘制文本（左对齐）
        drawWrappedText(g2d, width);

        g2d.dispose();
    }

    private void drawWrappedText(Graphics2D g2d, int bubbleWidth) {
        if (text == null || text.isEmpty()) return;

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(FONT);

        // 计算文本区域宽度（减去左右内边距）
        int textWidth = Math.max(20, bubbleWidth - BUBBLE_PADDING * 2);

        // 创建属性文本以支持高级布局
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FONT, FONT);
        attributes.put(TextAttribute.FOREGROUND, TEXT_COLOR);

        AttributedString attributedText = new AttributedString(text, attributes);
        AttributedCharacterIterator characterIterator = attributedText.getIterator();

        // 使用LineBreakMeasurer进行专业文本布局
        LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, g2d.getFontRenderContext());

        // 关键修改：所有消息都左对齐
        int x = BUBBLE_PADDING;
        int y = BUBBLE_PADDING;

        // 设置行距
        float lineSpacing = FONT.getSize() * 0.4f;

        while (measurer.getPosition() < characterIterator.getEndIndex()) {
            TextLayout layout = measurer.nextLayout(textWidth);
            y += layout.getAscent();

            // 所有文本左对齐
            layout.draw(g2d, x, y);

            y += layout.getDescent() + layout.getLeading() + lineSpacing;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int textWidth = MAX_BUBBLE_WIDTH - BUBBLE_PADDING * 2;

        // 处理空消息
        if (text == null || text.isEmpty()) {
            return new Dimension(MIN_BUBBLE_WIDTH, MIN_BUBBLE_HEIGHT);
        }

        // 创建虚拟图形上下文
        Graphics2D g2d = (Graphics2D) getGraphics();
        if (g2d == null) {
            return new Dimension(
                    Math.min(MAX_BUBBLE_WIDTH, 200),
                    MIN_BUBBLE_HEIGHT + BUBBLE_PADDING * 2
            );
        }

        // 创建属性文本
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FONT, FONT);
        AttributedString attributedText = new AttributedString(text, attributes);
        AttributedCharacterIterator characterIterator = attributedText.getIterator();

        // 使用LineBreakMeasurer计算高度
        LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, g2d.getFontRenderContext());
        float totalHeight = 0;
        float lineSpacing = FONT.getSize() * 0.4f;

        while (measurer.getPosition() < characterIterator.getEndIndex()) {
            TextLayout layout = measurer.nextLayout(textWidth);

            if (totalHeight > 0) {
                totalHeight += lineSpacing;
            }

            totalHeight += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }

        // 计算气泡宽度（基于文本宽度）
        int maxLineWidth = 0;
        measurer.setPosition(0);
        while (measurer.getPosition() < characterIterator.getEndIndex()) {
            TextLayout layout = measurer.nextLayout(textWidth);
            maxLineWidth = Math.max(maxLineWidth, (int) Math.ceil(layout.getAdvance()));
        }

        // 确保最小宽度
        int bubbleWidth = Math.min(
                MAX_BUBBLE_WIDTH,
                Math.max(MIN_BUBBLE_WIDTH, maxLineWidth + BUBBLE_PADDING * 2 + 5)
        );

        // 计算气泡高度
        int textHeight = (int) Math.ceil(totalHeight);
        int bubbleHeight = textHeight + BUBBLE_PADDING * 2 + ARC_RADIUS / 2 - 10;
        bubbleHeight = Math.max(bubbleHeight, MIN_BUBBLE_HEIGHT);

        return new Dimension(bubbleWidth, bubbleHeight);
    }
}
