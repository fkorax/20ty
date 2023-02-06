/*
 * Copyright © 2022, 2023  Franchesko Korako
 *
 * This file is part of 20ty.
 *
 * 20ty is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * 20ty is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with 20ty.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.fkorax.twenty.ui.util;

import io.github.fkorax.fusion.GeometryUtils;
import io.github.fkorax.fusion.InsetsUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;

/**
 * A primitive version of a Metal L&F-compatible SetButtonUI implementation.
 */
public class MetalSetButtonUI extends MetalToggleButtonUI implements SetButtonUI {
    private static MetalSetButtonUI metalSetButtonUI = null;

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ComponentUI createUI(final JComponent c) {
        // Apparently this UI is stateless,
        // so we should be able to cache it... (lazily)
        // See parent implementation in MetalToggleButtonUI
        if (metalSetButtonUI == null) {
            metalSetButtonUI = new MetalSetButtonUI();
        }

        return metalSetButtonUI;
    }

    @Override
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        // Manipulate the border
        final Border border = b.getBorder();

        if (border instanceof BorderUIResource.CompoundBorderUIResource) {
            final BorderUIResource.CompoundBorderUIResource compoundBorder = (BorderUIResource.CompoundBorderUIResource) border;

            // First, uninstall the existing border
            LookAndFeel.uninstallBorder(b);
            // Simply add the margin border (inner border)
            b.setBorder(compoundBorder.getInsideBorder());
            // Adjust the margin to account for the now missing
            // outer border ...
            final Insets newMargin = InsetsUtils.plus(b.getMargin(), compoundBorder.getOutsideBorder().getBorderInsets(b));
            // ... and adjust them further to reduce the horizontal size of the SetButton
            newMargin.left -= 8;
            newMargin.right -= 8;
            b.setMargin(newMargin);
        }
        else {
            // If this ever occurs, it will have to be addressed
            throw new RuntimeException("MetalToggleButtonUI border is not an instance of BorderUIResource.CompoundBorderUIResource");
        }
    }

    @Override
    public void update(Graphics g, @NotNull JComponent c) {
        // Imitating the original implementation of update()
        // is important somehow because we need to paint over
        // the component each time.
        // Working with Swing Graphics is still black magic.
        AbstractButton button = (AbstractButton)c;
        if (c.getBackground() instanceof UIResource && button.isContentAreaFilled() && c.isEnabled()) {
            ButtonModel model = button.getModel();
            if (!MetalUtils.isToolBarButton(c)) {
                if (!model.isArmed()
                        && !model.isPressed()
                        && MetalUtils.fillSetButtonBackground(c, g, c.getBackground(), 0, 0, c.getWidth(), c.getHeight())
                ) {
                    this.paint(g, c);
                    return;
                }
            }
            else if ((model.isRollover() || model.isSelected())
                        && MetalUtils.fillSetButtonBackground(c, g, c.getBackground(), 0, 0, c.getWidth(), c.getHeight())
            ) {
                this.paint(g, c);
                return;
            }
        }

        super.update(g, c);
    }

    @Override
    protected void paintButtonPressed(Graphics g, @NotNull AbstractButton b) {
        if (b.isContentAreaFilled()) {
            g.setColor(this.selectColor);
            // Paint a circle,
            // using either the component bounds or the Graphics bounds,
            // whichever is smaller
            final Rectangle circleBounds = GeometryUtils.getInscribedSquare(b.getWidth(), b.getHeight());
            // with Graphics2D if possible
            if (g instanceof Graphics2D) {
                ((Graphics2D) g).fill(GeometryUtils.getInscribedEllipse(circleBounds));
            }
            else {
                g.fillOval(circleBounds.x, circleBounds.y, circleBounds.width, circleBounds.height);
            }
        }
    }

}
