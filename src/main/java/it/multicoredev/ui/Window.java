package it.multicoredev.ui;

import it.multicoredev.ui.listeners.KeyListener;
import it.multicoredev.ui.listeners.MouseListener;
import it.multicoredev.ui.scenes.LevelEditorScene;
import it.multicoredev.ui.scenes.LevelScene;
import it.multicoredev.ui.scenes.Scene;
import it.multicoredev.utils.Time;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static it.multicoredev.App.LOGGER;
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
    private float[] windowColor = new float[]{1f, 1f, 1f, 1f};

    private static Window window = null;

    private static Scene currentScene = null;

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

    public static void setScene(int scene) {
        switch (scene) {
            case 0 -> {
                currentScene = new LevelEditorScene();
                currentScene.init();
                LOGGER.info("Scene set to 0");
            }
            case 1 -> {
                currentScene = new LevelScene();
                currentScene.init();
                LOGGER.info("Scene set to 1");
            }
            default -> {
                LOGGER.error("Scene not found");
                System.exit(-1);
            }
        }
    }

    public void run() {
        LOGGER.info("Running window with LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        windowColor[0] = r;
        windowColor[1] = g;
        windowColor[2] = b;
        windowColor[3] = a;
    }

    public float[] getBackgroundColor() {
        return windowColor;
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

        Window.setScene(0);
    }

    private void loop() {
        float beginTime = Time.getTime();
        float endTime;
        float dt = -1.0f;

        while (!glfwWindowShouldClose(windowId)) {
            // Poll events
            glfwPollEvents();

            glClearColor(windowColor[0], windowColor[1], windowColor[2], windowColor[3]);
            glClear(GL_COLOR_BUFFER_BIT);

            if (dt >= 0) currentScene.update(dt);

            //LOGGER.info("FPS: " + (1.0f / dt));

            glfwSwapBuffers(windowId);

            endTime = Time.getTime();
            // Delta time
            dt = endTime - beginTime;
            beginTime = endTime;
        }
    }
}
