package it.multicoredev.ui.scenes;

import imgui.ImGui;
import imgui.ImVec2;
import it.multicoredev.ui.Camera;
import it.multicoredev.ui.GameObject;
import it.multicoredev.ui.Prefabs;
import it.multicoredev.ui.components.*;
import it.multicoredev.ui.registries.Scenes;
import it.multicoredev.ui.registries.SpriteSheets;
import it.multicoredev.utils.AssetPool;
import org.joml.Vector2f;

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
public class LevelEditorScene extends Scene {
    private SpriteSheet decorationsAndBlocks;
    private SpriteSheet icons;
    private SpriteSheet pipes;

    GameObject levelEditor = new GameObject("LevelEditor");

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        camera = new Camera();

        initResources();

        levelEditor.addComponent(new MouseControls());
        levelEditor.addComponent(new GridLines());

        decorationsAndBlocks = AssetPool.getSpriteSheet(SpriteSheets.DECORATIONS_AND_BLOCKS);
        icons = AssetPool.getSpriteSheet(SpriteSheets.ICONS);
        pipes = AssetPool.getSpriteSheet(SpriteSheets.PIPES);

        if (loadedLevel && !gameObjects.isEmpty()) {
            //activeGameObject = getGameObject("goomba");
            return;
        }

        /*GameObject mario = new GameObject("mario", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        mario.addComponent(new SpriteRenderer(decorationsAndBlocks.getSprite(9)));
        addGameObject(mario);

        GameObject goomba = new GameObject("goomba", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)));
        goomba.addComponent(new SpriteRenderer(decorationsAndBlocks.getSprite(15)));
        addGameObject(goomba);

        GameObject s1 = new GameObject("sqr_1", new Transform(new Vector2f(100, 400), new Vector2f(256, 256)), 1);
        s1.addComponent(new SpriteRenderer(new Vector4f(1, 0, 0, 0.6f)));
        addGameObject(s1);

        GameObject s2 = new GameObject("sqr_2", new Transform(new Vector2f(250, 400), new Vector2f(256, 256)), 2);
        s2.addComponent(new SpriteRenderer(new Vector4f(0, 1, 0, 0.6f)));
        addGameObject(s2);*/
    }

    @Override
    public void update(float dt) {
        //LOGGER.info("FPS: " + (1f / dt));
        levelEditor.update(dt);

        gameObjects.forEach(gameObject -> gameObject.update(dt));
        renderer.render();

        save(Scenes.LEVEL_EDITOR.getPath());
    }

    @Override
    public void imgui() {
        ImGui.begin("Level Editor");

        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowsSize = new ImVec2();
        ImGui.getWindowSize(windowsSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowsSize.x;
        for (int i = 0; i < decorationsAndBlocks.size(); i++) {
            Sprite sprite = decorationsAndBlocks.getSprite(i);
            float spriteWidth = sprite.getWidth() * 3;
            float spriteHeight = sprite.getHeight() * 3;
            int id = sprite.getTextureId();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if (ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
                GameObject obj = Prefabs.generateSpriteObject(sprite, 32, 32, 0);

                // Attach object to mouse cursor
                levelEditor.getComponent(MouseControls.class).pickUpObject(obj);
            }
            ImGui.popID();

            ImVec2 lastBtnPos = new ImVec2();
            ImGui.getItemRectMax(lastBtnPos);
            float lastButtonX2 = lastBtnPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;

            if (i + 1 < decorationsAndBlocks.size() && nextButtonX2 < windowX2) ImGui.sameLine();
        }

        ImGui.end();
    }

    private void initResources() {
        gameObjects.forEach(go -> {
            if (go.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer sr = go.getComponent(SpriteRenderer.class);
                if (sr.getTexture() != null) sr.setTexture(AssetPool.getTexture(sr.getTexture().getPath()));
            }
        });
    }
}
