package it.multicoredev.ui.renderer;

import it.multicoredev.ui.Window;
import it.multicoredev.ui.registries.Shaders;
import it.multicoredev.utils.AssetPool;
import it.multicoredev.utils.JMath;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL20.*;

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
public class DebugDraw {
    private static final int MAX_LINES = 500;

    private static final List<Line2D> lines = new ArrayList<>();
    private static final float[] vertices = new float[MAX_LINES * 6 * 2]; // 6 floats per vertex, 2 vertices per line
    private static final Shader shader = AssetPool.getShader(Shaders.DEBUG_LINE_2D);

    private static int vaoId;
    private static int vboId;

    private static boolean started = false;

    public static void start() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glLineWidth(1.0f);
    }

    public static void beginFrame() {
        if (!started) {
            start();
            started = true;
        }

        lines.removeIf(line -> line.beginFrame() < 0);
    }

    public static void draw() {
        if (lines.isEmpty()) return;

        int index = 0;
        for (Line2D line : lines) {
            for (int i = 0; i < 2; i++) {
                Vector2f position = i == 0 ? line.getStart() : line.getEnd();
                Vector3f color = line.getColor();

                vertices[index] = position.x;
                vertices[index + 1] = position.y;
                vertices[index + 2] = -10.0f;

                vertices[index + 3] = color.x;
                vertices[index + 4] = color.y;
                vertices[index + 5] = color.z;

                index += 6;
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertices, 0, lines.size() * 6 * 2));

        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjection());
        shader.uploadMat4f("uView", Window.getScene().camera().getView());

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawArrays(GL_LINES, 0, lines.size() * 6 * 2);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shader.detach();
    }

    public static void addLine(Line2D line) {
        if (lines.size() >= MAX_LINES) return;
        lines.add(line);
    }

    public static void addLine(Vector2f start, Vector2f end, Vector3f color, int lifetime) {
        addLine(new Line2D(start, end, color, lifetime));
    }

    public static void addLine(Vector2f start, Vector2f end, Vector3f color) {
        addLine(new Line2D(start, end, color, 1));
    }

    public static void addLine(Vector2f start, Vector2f end) {
        addLine(new Line2D(start, end, new Vector3f(0.0f, 1.0f, 0.0f), 1));
    }

    public static void addBox(Vector2f center, Vector2f dimensions, float rotation, Vector3f color, int lifetime) {
        Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).div(2.0f));
        Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).div(2.0f));

        Vector2f[] vertices = {
                new Vector2f(min.x, min.y),
                new Vector2f(min.x, max.y),
                new Vector2f(max.x, max.y),
                new Vector2f(max.x, min.y)
        };

        if (rotation != 0) {
            for (Vector2f vertex : vertices) {
                JMath.rotate(vertex, rotation, center);
            }
        }

        addLine(vertices[0], vertices[1], color, lifetime);
        addLine(vertices[1], vertices[2], color, lifetime);
        addLine(vertices[2], vertices[3], color, lifetime);
        addLine(vertices[3], vertices[0], color, lifetime);
    }

    public static void addBox(Vector2f center, Vector2f dimensions, float rotation, Vector3f color) {
        addBox(center, dimensions, rotation, color, 1);
    }

    public static void addBox(Vector2f center, Vector2f dimensions, float rotation) {
        addBox(center, dimensions, rotation, new Vector3f(0.0f, 1.0f, 0.0f), 1);
    }

    public static void addBox(Vector2f center, Vector2f dimensions, Vector3f color, int lifetime) {
        addBox(center, dimensions, 0.0f, color, lifetime);
    }

    public static void addBox(Vector2f center, Vector2f dimensions, Vector3f color) {
        addBox(center, dimensions, 0.0f, color, 1);
    }

    public static void addBox(Vector2f center, Vector2f dimensions) {
        addBox(center, dimensions, 0.0f, new Vector3f(0.0f, 1.0f, 0.0f), 1);
    }

    public static void addPolygon(Vector2f center, float radius, int edges, float rotation, Vector3f color, int lifetime) {
        Vector2f[] points = new Vector2f[edges];
        int increment = 360 / points.length;
        int currentAngle = 0;

        for (int i = 0; i < points.length; i++) {
            Vector2f tmp = new Vector2f(radius, 0.0f);
            JMath.rotate(tmp, currentAngle, new Vector2f());
            points[i] = new Vector2f(tmp).add(center);

            currentAngle += increment;
        }

        if (rotation != 0) {
            for (Vector2f point : points) {
                JMath.rotate(point, rotation, center);
            }
        }

        for (int i = 0; i < points.length; i++) {
            if (i > 0) addLine(points[i - 1], points[i], color, lifetime);
        }

        addLine(points[points.length - 1], points[0], color, lifetime);
    }

    public static void addPolygon(Vector2f center, float radius, int edges, float rotation, Vector3f color) {
        addPolygon(center, radius, edges, rotation, color, 1);
    }

    public static void addPolygon(Vector2f center, float radius, int edges, float rotation) {
        addPolygon(center, radius, edges, rotation, new Vector3f(0.0f, 1.0f, 0.0f), 1);
    }

    public static void addPolygon(Vector2f center, float radius, int edges, Vector3f color, int lifetime) {
        addPolygon(center, radius, edges, 0.0f, color, lifetime);
    }

    public static void addPolygon(Vector2f center, float radius, int edges, Vector3f color) {
        addPolygon(center, radius, edges, 0.0f, color, 1);
    }

    public static void addPolygon(Vector2f center, float radius, int edges) {
        addPolygon(center, radius, edges, 0.0f, new Vector3f(0.0f, 1.0f, 0.0f), 1);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color, int lifetime) {
        addPolygon(center, radius, 32, color, lifetime);
    }

    public static void addCircle(Vector2f center, float radius, Vector3f color) {
        addPolygon(center, radius, 32, color, 1);
    }

    public static void addCircle(Vector2f center, float radius) {
        addPolygon(center, radius, 32, new Vector3f(0.0f, 1.0f, 0.0f), 1);
    }

    public static void addCircle(Vector2f center, float radius, int lifetime) {
        addPolygon(center, radius, 32, new Vector3f(0.0f, 1.0f, 0.0f), lifetime);
    }
}
