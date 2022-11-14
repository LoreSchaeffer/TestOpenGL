package it.multicoredev.ui.scenes;

import com.google.gson.reflect.TypeToken;
import imgui.ImGui;
import it.multicoredev.ui.Camera;
import it.multicoredev.ui.GameObject;
import it.multicoredev.ui.components.Component;
import it.multicoredev.ui.renderer.Renderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static it.multicoredev.App.GSON;
import static it.multicoredev.App.LOGGER;

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
public abstract class Scene {
    protected Renderer renderer = new Renderer();
    protected Camera camera;
    protected final List<GameObject> gameObjects = new ArrayList<>();
    private boolean isRunning = false;
    protected transient GameObject activeGameObject = null;
    protected transient boolean loadedLevel = false;

    public void init() {

    }

    public void start() {
        gameObjects.forEach(obj -> {
            obj.start();
            renderer.add(obj);
        });

        isRunning = true;
    }

    public void addGameObject(GameObject obj) {
        gameObjects.add(obj);
        if (isRunning) {
            obj.start();
            renderer.add(obj);
        }
    }

    public abstract void update(float dt);

    public Camera camera() {
        return camera;
    }

    public GameObject getGameObject(String name) {
        for (GameObject obj : gameObjects) {
            if (obj.getName().equals(name)) return obj;
        }

        throw new IllegalArgumentException("GameObject not found");
    }

    public void sceneImgui() {
        if (activeGameObject != null) {
            ImGui.begin("Inspector");
            activeGameObject.imgui();
            ImGui.end();
        }

        imgui();
    }

    public void imgui() {

    }

    public void save(String path) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(GSON.toJson(gameObjects));
        } catch (Exception e) {
            LOGGER.error("Error while saving scene", e);
        }
    }

    public void load(String path) {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            gameObjects.clear();
            int maxGameObjectId = -1;
            int maxComponentId = -1;

            List<GameObject> objects = GSON.fromJson(reader, new TypeToken<List<GameObject>>() {}.getType());
            for (GameObject obj : objects) {
                addGameObject(obj);

                if (obj.getUid() > maxGameObjectId) maxGameObjectId = obj.getUid();

                for (Component component : obj.getComponents()) {
                    if (component.getUid() > maxComponentId) maxComponentId = component.getUid();
                }
            }

            GameObject.init(maxGameObjectId + 1);
            Component.init(maxComponentId + 1);
            loadedLevel = true;
        } catch (Exception e) {
            LOGGER.warn("Cannot load scene");
        }
    }
}
