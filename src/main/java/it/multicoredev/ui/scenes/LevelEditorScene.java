package it.multicoredev.ui.scenes;

import it.multicoredev.ui.Camera;
import it.multicoredev.ui.GameObject;
import it.multicoredev.ui.Transform;
import it.multicoredev.ui.components.Sprite;
import it.multicoredev.ui.components.SpriteRenderer;
import it.multicoredev.ui.components.SpriteSheet;
import it.multicoredev.utils.AssetPool;
import it.multicoredev.utils.Shaders;
import it.multicoredev.utils.SpriteSheets;
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
    private GameObject mario;
    private GameObject goomba;
    private GameObject s1;
    private GameObject s2;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        camera = new Camera();
        loadResources();

        SpriteSheet sprites = AssetPool.getSpriteSheet(SpriteSheets.SPRITESHEET);

        mario = new GameObject("Obj 1", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        mario.addComponent(new SpriteRenderer(sprites.getSprite(0)));
        addGameObject(mario);

        goomba = new GameObject("Obj 2", new Transform(new Vector2f(400, 100), new Vector2f(256, 256)));
        goomba.addComponent(new SpriteRenderer(sprites.getSprite(15)));
        addGameObject(goomba);

        s1 = new GameObject("Obj 3", new Transform(new Vector2f(100, 400), new Vector2f(256, 256)), 1);
        s1.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("assets/textures/red.png"))));
        addGameObject(s1);

        s2 = new GameObject("Obj 4", new Transform(new Vector2f(250, 400), new Vector2f(256, 256)), 2);
        s2.addComponent(new SpriteRenderer(new Sprite(AssetPool.getTexture("assets/textures/green.png"))));
        addGameObject(s2);
    }

    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeLeft = 0.0f;

    @Override
    public void update(float dt) {
        //LOGGER.info("FPS: " + (1f / dt));

        spriteFlipTimeLeft -= dt;
        if (spriteFlipTimeLeft <= 0) {
            spriteFlipTimeLeft = spriteFlipTime;
            spriteIndex++;
            if (spriteIndex > 4) {
                spriteIndex = 0;
            }

            mario.getComponent(SpriteRenderer.class).setSprite(AssetPool.getSpriteSheet(SpriteSheets.SPRITESHEET).getSprite(spriteIndex));
        }


        gameObjects.forEach(go -> go.update(dt));

        renderer.render();
    }

    private void loadResources() {
        AssetPool.getShader(Shaders.DEFAULT);
        AssetPool.addSpriteSheet(SpriteSheets.SPRITESHEET, new SpriteSheet(AssetPool.getTexture(SpriteSheets.SPRITESHEET), 16, 16, 26, 0));
    }
}
