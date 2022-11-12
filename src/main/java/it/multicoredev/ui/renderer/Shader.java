package it.multicoredev.ui.renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;

import static it.multicoredev.App.LOGGER;
import static org.lwjgl.opengl.GL11.GL_FALSE;
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
public class Shader {
    private final File file;

    private int shaderProgramId;
    private boolean inUse = false;
    private String vertexSrc;
    private String fragmentSrc;

    public Shader(File file) {
        this.file = file;

        try {
            String src = new String(Files.readAllBytes(this.file.toPath()));
            String[] splitSrc = src.split("(#type)( )+([a-zA-Z]+)");

            int idx = src.indexOf("#type") + 6;
            int eol = src.indexOf("\n", idx);
            String firstPattern = src.substring(idx, eol).trim();

            idx = src.indexOf("#type", eol) + 6;
            eol = src.indexOf("\n", idx);
            String secondPattern = src.substring(idx, eol).trim();

            if (firstPattern.equals("vertex")) vertexSrc = splitSrc[1];
            else if (firstPattern.equals("fragment")) fragmentSrc = splitSrc[1];
            else throw new RuntimeException("Unexpected token '" + firstPattern + "'");

            if (secondPattern.equals("vertex")) vertexSrc = splitSrc[2];
            else if (secondPattern.equals("fragment")) fragmentSrc = splitSrc[2];
            else throw new RuntimeException("Unexpected token '" + secondPattern + "'");
        } catch (IOException e) {
            LOGGER.error("Could not open file for shaders: '" + this.file.getName() + "'", e);
            System.exit(-1);
        }
    }

    public void compileAndLink() {
        // Load and compile the vertex shader
        int vertexId = glCreateShader(GL_VERTEX_SHADER);

        // Pass the shaders source code to the GPU and compile it
        glShaderSource(vertexId, vertexSrc);
        glCompileShader(vertexId);

        // Check for errors in compilation
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            LOGGER.error("ERROR: '" + file.getName() + "'\n\tVertex shader compilation failed");
            LOGGER.error(glGetShaderInfoLog(vertexId, len));
            System.exit(-1);
        }

        // Load and compile the fragment shader
        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        // Pass the shaders source code to the GPU and compile it
        glShaderSource(fragmentId, fragmentSrc);
        glCompileShader(fragmentId);

        // Check for errors in compilation
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            LOGGER.error("ERROR: '" + file.getName() + "'\n\tFragment shader compilation failed");
            LOGGER.error(glGetShaderInfoLog(fragmentId, len));
            System.exit(-1);
        }

        // Link shaders and check for errors
        shaderProgramId = glCreateProgram();
        glAttachShader(shaderProgramId, vertexId);
        glAttachShader(shaderProgramId, fragmentId);
        glLinkProgram(shaderProgramId);

        // Check for linking errors
        success = glGetProgrami(shaderProgramId, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramId, GL_INFO_LOG_LENGTH);
            LOGGER.error("ERROR: '" + file.getName() + "'\n\tLinking of shaders failed");
            LOGGER.error(glGetProgramInfoLog(shaderProgramId, len));
            System.exit(-1);
        }
    }

    public void use() {
        if (!inUse) {
            glUseProgram(shaderProgramId);
            inUse = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        inUse = false;
    }

    public void uploadMat4f(String varName, Matrix4f mat) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        mat.get(buffer);
        glUniformMatrix4fv(varLocation, false, buffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(9);
        mat.get(buffer);
        glUniformMatrix3fv(varLocation, false, buffer);
    }

    public void uploadVec4F(String varName, Vector4f vec) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
    }

    public void uploadVec3F(String varName, Vector3f vec) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform3f(varLocation, vec.x, vec.y, vec.z);
    }

    public void uploadVec2F(String varName, Vector2f vec) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform2f(varLocation, vec.x, vec.y);
    }

    public void uploadFloat(String varName, float value) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform1f(varLocation, value);
    }

    public void uploadInt(String varName, int value) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform1i(varLocation, value);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform1i(varLocation, slot);
    }

    public void uploadTextures(String varName, int[] slots) {
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();
        glUniform1iv(varLocation, slots);
    }
}
