package it.multicoredev.ui.registries;

import it.multicoredev.ui.scenes.LevelEditorScene;
import it.multicoredev.ui.scenes.LevelScene;
import it.multicoredev.ui.scenes.Scene;

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
public enum Scenes {
    LEVEL_EDITOR("level_editor", LevelEditorScene.class),
    LEVEL("level", LevelScene.class);

    private final String id;
    private final Class<? extends Scene> sceneClass;

    Scenes(String id, Class<? extends Scene> sceneClass) {
        this.id = id;
        this.sceneClass = sceneClass;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return "saves/" + id + ".json";
    }

    public Scene getInstance() {
        try {
            return sceneClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.error("Error while creating scene '" + id + "'", e);
            return null;
        }
    }
}
