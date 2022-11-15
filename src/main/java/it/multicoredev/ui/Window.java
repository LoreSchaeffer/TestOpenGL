package it.multicoredev.ui;

import it.multicoredev.ui.components.SpriteSheet;
import it.multicoredev.ui.listeners.KeyListener;
import it.multicoredev.ui.listeners.MouseListener;
import it.multicoredev.ui.registries.Scenes;
import it.multicoredev.ui.registries.Shaders;
import it.multicoredev.ui.registries.SpriteSheets;
import it.multicoredev.ui.renderer.DebugDraw;
import it.multicoredev.ui.renderer.FrameBuffer;
import it.multicoredev.ui.scenes.Scene;
import it.multicoredev.utils.AssetPool;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

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
public class Window {
    private int width;
    private int height;
    private String title;
    private long windowId;
    public float[] windowColor = new float[]{0.1f, 0.1f, 0.1f, 0.1f};
    private ImGuiLayer imGuiLayer;
    private FrameBuffer frameBuffer;

    private static Window window = null;

    private Scene currentScene = null;

    // Callback documentation https://www.glfw.org/docs/3.3/input_guide.html

    private Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public static Window create(int width, int height, String title) {
        if (window == null) window = new Window(width, height, title);
        return window;
    }

    public static Window get() {
        if (window == null) throw new IllegalStateException("Window not initialized");
        return window;
    }

    public static int getWidth() {
        return get().width;
    }

    public static void setWidth(int width) {
        get().width = width;
    }

    public static int getHeight() {
        return get().height;
    }

    public static void setHeight(int height) {
        get().height = height;
    }

    public static Scene getScene() {
        return get().currentScene;
    }

    public static FrameBuffer getFrameBuffer() {
        return get().frameBuffer;
    }

    public static float getTargetAspectRatio() {
        return (float) getWidth() / (float) getHeight();
    }

    public static void setScene(Scenes scene) {
        Scene newScene = scene.getInstance();
        if (newScene == null) throw new IllegalStateException("Scene not initialized");

        get().currentScene = newScene;
        newScene.load(scene.getPath());
        newScene.init();
        newScene.start();
    }

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        //Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Init GLFW
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        //Config GLFW (Window settings)
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);

        windowId = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowId == NULL) throw new RuntimeException("Failed to create the GLFW window");

        // Mouse callbacks
        glfwSetCursorPosCallback(windowId, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(windowId, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(windowId, MouseListener::mouseScrollCallback);

        // Key callbacks
        glfwSetKeyCallback(windowId, KeyListener::keyCallback);

        // Window callbacks
        glfwSetWindowSizeCallback(windowId, (w, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowId);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(windowId);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Alpha blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        loadResources();

        imGuiLayer = new ImGuiLayer(windowId);
        imGuiLayer.init();

        frameBuffer = new FrameBuffer(2560, 1440);
        glViewport(0, 0, 2560, 1440);

        Window.setScene(Scenes.LEVEL_EDITOR);
    }

    private void loop() {
        float beginTime = (float) glfwGetTime();
        float endTime;
        float dt = -1.0f;

        while (!glfwWindowShouldClose(windowId)) {
            // Poll events
            glfwPollEvents();

            DebugDraw.beginFrame();

            frameBuffer.bind();

            glClearColor(windowColor[0], windowColor[1], windowColor[2], windowColor[3]);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) {
                DebugDraw.draw();
                currentScene.update(dt);
            }

            frameBuffer.unbind();

            imGuiLayer.update(dt, currentScene);
            glfwSwapBuffers(windowId);

            endTime = (float) glfwGetTime();
            // Delta time
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }

    private void loadResources() {
        AssetPool.getShader(Shaders.DEFAULT);

        AssetPool.addSpriteSheet(SpriteSheets.SPRITESHEET, new SpriteSheet(AssetPool.getTexture(SpriteSheets.SPRITESHEET), 16, 16, 26, 0));
        AssetPool.addSpriteSheet(SpriteSheets.DECORATIONS_AND_BLOCKS, new SpriteSheet(AssetPool.getTexture(SpriteSheets.DECORATIONS_AND_BLOCKS), 16, 16, 80, 0));
        AssetPool.addSpriteSheet(SpriteSheets.ICONS, new SpriteSheet(AssetPool.getTexture(SpriteSheets.ICONS), 16, 16, 16, 0));
        AssetPool.addSpriteSheet(SpriteSheets.PIPES, new SpriteSheet(AssetPool.getTexture(SpriteSheets.PIPES), 16, 16, 6, 0));
    }
}
