package it.multicoredev.ui.renderer;

import it.multicoredev.ui.Window;
import it.multicoredev.ui.components.SpriteRenderer;
import it.multicoredev.utils.AssetPool;
import it.multicoredev.ui.registries.Shaders;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
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
public class RenderBatch implements Comparable<RenderBatch> {
    // Vertex
    // ======
    // Pos              |   Color                       |   TexCoord        |   TexId
    // float, float     |   float, float, float, float  |   float, float    |   float
    // ======
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEXCOORD_SIZE = 2;
    private final int TEXID_SIZE = 1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEXCOORD_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEXID_OFFSET = TEXCOORD_OFFSET + TEXCOORD_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 9;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    private final List<Texture> textures = new ArrayList<>();
    private int vaoId;
    private int vboId;
    private int maxBatchSize;
    private Shader shader;
    private int zIndex;

    public RenderBatch(int maxBatchSize, int zIndex) {
        this.maxBatchSize = maxBatchSize;
        this.zIndex = zIndex;

        shader = AssetPool.getShader(Shaders.DEFAULT);

        sprites = new SpriteRenderer[maxBatchSize];
        vertices = new float[maxBatchSize * VERTEX_SIZE * 4]; // 4 vertices per quad

        numSprites = 0;
        hasRoom = true;
    }

    public void start() {
        // Generate and bind vertex array object
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Allocate space for vertices
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        int eboId = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEXCOORD_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXCOORD_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEXID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEXID_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void addSprite(SpriteRenderer sprite) {
        int idx = numSprites;
        sprites[idx] = sprite;
        numSprites++;

        if (sprite.getTexture() != null) {
            if (!textures.contains(sprite.getTexture())) textures.add(sprite.getTexture());
        }

        // Add properties to local vertices array
        loadVertexProperties(idx);

        if (numSprites >= maxBatchSize) {
            hasRoom = false;
        }
    }

    public void render() {
        boolean rebufferData = false;
        for (int i = 0; i < numSprites; i++) {
            SpriteRenderer renderer = sprites[i];
            if (renderer.isDirty()) {
                loadVertexProperties(i);
                renderer.setClean();
                rebufferData = true;
            }
        }

        if (rebufferData) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        // Use shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjection());
        shader.uploadMat4f("uView", Window.getScene().camera().getView());

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }

        shader.uploadTextures("uTextures", texSlots);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElements(GL_TRIANGLES, numSprites * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);

        glBindVertexArray(0);

        for (int i = 0; i < textures.size(); i++) {
            textures.get(i).unbind();
        }

        shader.detach();
    }

    public boolean hasRoom() {
        return hasRoom;
    }

    public boolean hasTextureRoom() {
        return textures.size() < 16;
    }

    public boolean hasTexture(Texture texture) {
        return textures.contains(texture);
    }

    public int zIndex() {
        return zIndex;
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle and 2 triangles per quad)
        int[] elements = new int[maxBatchSize * 6];

        for (int i = 0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIdx = index * 6;
        int offset = index * 4;

        // Triangle 1
        elements[offsetArrayIdx] = offset + 3;
        elements[offsetArrayIdx + 1] = offset + 2;
        elements[offsetArrayIdx + 2] = offset;

        // Triangle 2
        elements[offsetArrayIdx + 3] = offset;
        elements[offsetArrayIdx + 4] = offset + 2;
        elements[offsetArrayIdx + 5] = offset + 1;
    }

    private void loadVertexProperties(int idx) {
        SpriteRenderer sprite = sprites[idx];

        int offset = idx * VERTEX_SIZE * 4;

        Vector4f color = sprite.getColor();
        Vector2f[] texCoords = sprite.getTexCoords();

        int texId = 0;
        if (sprite.getTexture() != null) {
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i) == sprite.getTexture()) {
                    texId = i + 1;
                    break;
                }
            }
        }

        // Add vertices with the appropriate properties
        float xAdd = 1.0f;
        float yAdd = 1.0f;

        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                yAdd = 0.0f;
            } else if (i == 2) {
                xAdd = 0.0f;
            } else if (i == 3) {
                yAdd = 1.0f;
            }

            // Load position
            vertices[offset] = sprite.gameObject().transform.position.x + (xAdd * sprite.gameObject().transform.scale.x);
            vertices[offset + 1] = sprite.gameObject().transform.position.y + (yAdd * sprite.gameObject().transform.scale.y);

            // Load color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // Load texture coordinates
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

            // Load texture id
            vertices[offset + 8] = texId;

            offset += VERTEX_SIZE;
        }
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(zIndex, o.zIndex);
    }
}
