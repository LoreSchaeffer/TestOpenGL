package it.multicoredev.ui;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import imgui.ImGui;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

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
@JsonAdapter(Component.JsonAdapter.class)
public abstract class Component {
    protected transient GameObject gameObject = null;

    public void start() {

    }

    public void update(float dt) {

    }

    void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public GameObject gameObject() {
        return gameObject;
    }

    public void imgui() {
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isTransient(field.getModifiers())) continue;

                boolean isPrivate = Modifier.isPrivate(field.getModifiers());
                if (isPrivate) field.setAccessible(true);

                Class<?> type = field.getType();
                Object value = field.get(this);
                String name = field.getName();

                if (type == int.class) {
                    int[] imInt = {(int) value};

                    if (ImGui.dragInt(name, imInt)) {
                        field.set(this, imInt[0]);
                    }
                } else if (type == float.class) {
                    float[] imFloat = {(float) value};

                    if (ImGui.dragFloat(name, imFloat)) {
                        field.set(this, imFloat[0]);
                    }
                } else if (type == boolean.class) {
                    boolean val = (boolean) value;

                    if (ImGui.checkbox(name, val)) {
                        field.set(this, !val);
                    }
                } else if (type == Vector3f.class) {
                    Vector3f val = (Vector3f) value;
                    float[] imVec = {val.x, val.y, val.z};

                    if (ImGui.dragFloat3(name, imVec)) {
                        val.set(imVec[0], imVec[1], imVec[2]);
                    }
                } else if (type == Vector4f.class) {
                    Vector4f val = (Vector4f) value;
                    float[] imVec = {val.x, val.y, val.z, val.w};

                    if (ImGui.dragFloat4(name, imVec)) {
                        val.set(imVec[0], imVec[1], imVec[2], imVec[3]);
                    }
                }
                if (isPrivate) field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {

        }
    }

    public static class JsonAdapter implements JsonDeserializer<Component>, JsonSerializer<Component> {

        @Override
        public Component deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException {
            if (!json.isJsonObject()) throw new JsonParseException("Invalid or malformed Component: not and object");
            JsonObject obj = json.getAsJsonObject();

            if (!obj.has("type") || !obj.has("data")) throw new JsonParseException("Invalid or malformed Component: missing type or data");

            String type = obj.get("type").getAsString();

            try {
                return ctx.deserialize(obj.get("data"), Class.forName(type));
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unknown element 'type': " + type, e);
            }
        }

        @Override
        public JsonElement serialize(Component component, Type t, JsonSerializationContext ctx) {
            JsonObject json = new JsonObject();
            json.addProperty("type", component.getClass().getCanonicalName());
            json.add("data", ctx.serialize(component, component.getClass()));

            return json;
        }
    }
}
