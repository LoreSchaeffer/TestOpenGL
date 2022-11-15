package it.multicoredev.ui.components;

import it.multicoredev.ui.Window;
import it.multicoredev.ui.renderer.DebugDraw;
import it.multicoredev.utils.Settings;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * BSD 3-Clause License
 * <p>
 * Copyright (c) 2022, Lorenzo Magni
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class GridLines extends Component {

    @Override
    public void update(float dt) {
        Vector2f cameraPos = Window.getScene().camera().position;
        Vector2f projectionSize = Window.getScene().camera().getProjectionSize();

        int firstX = ((int) (cameraPos.x / Settings.GRID_WIDTH) - 1) * Settings.GRID_WIDTH;
        int firstY = ((int) (cameraPos.y / Settings.GRID_HEIGHT) - 1) * Settings.GRID_HEIGHT;

        int numHLines = (int) (projectionSize.y / Settings.GRID_HEIGHT) + 2;
        int numVLines = (int) (projectionSize.x / Settings.GRID_WIDTH) + 2;

        int width = (int) projectionSize.x + Settings.GRID_WIDTH;
        int height = (int) projectionSize.y + Settings.GRID_HEIGHT;

        Vector3f color = new Vector3f(0.5f, 0.5f, 0.5f);

        int maxLines = Math.max(numHLines, numVLines);
        for (int i = 0; i < maxLines; i++) {
            int x = firstX + Settings.GRID_WIDTH * i;
            int y = firstY + Settings.GRID_HEIGHT * i;

            if (i < numHLines) {
                DebugDraw.addLine(new Vector2f(firstX, y), new Vector2f(x + width, y), color);
            }

            if (i < numVLines) {
                DebugDraw.addLine(new Vector2f(x, firstY), new Vector2f(x, y + height), color);
            }
        }
    }
}
