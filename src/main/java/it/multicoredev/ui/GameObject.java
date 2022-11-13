package it.multicoredev.ui;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
@JsonAdapter(GameObject.JsonAdapter.class)
public class GameObject {
    private final String name;
    public Transform transform;
    @SerializedName("z_index")
    private final int zIndex;
    private final List<Component> components = new ArrayList<>();

    public GameObject(String name, Transform transform, int zIndex) {
        this.name = name;
        this.transform = transform;
        this.zIndex = zIndex;
    }

    public GameObject(String name, Transform transform) {
        this(name, transform, 0);
    }

    public GameObject(String name, int zIndex) {
        this(name, new Transform(), zIndex);
    }

    public GameObject(String name) {
        this(name, 0);
    }

    public <T extends Component> T getComponent(Class<T> component) {
        for (Component c : components) {
            if (component.isAssignableFrom(c.getClass())) {
                try {
                    return component.cast(c);
                } catch (ClassCastException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> component) {
        components.removeIf(c -> component.isAssignableFrom(c.getClass()));
    }

    public void addComponent(Component component) {
        components.add(component);
        component.setGameObject(this);
    }

    public void update(float dt) {
        components.forEach(c -> c.update(dt));
    }

    public void start() {
        components.forEach(Component::start);
    }

    public String getName() {
        return name;
    }

    public int zIndex() {
        return zIndex;
    }

    public void imgui() {
        components.forEach(Component::imgui);
    }

    public static class JsonAdapter implements JsonDeserializer<GameObject> {

        @Override
        public GameObject deserialize(JsonElement json, Type t, JsonDeserializationContext ctx) throws JsonParseException {
            if (!json.isJsonObject()) throw new JsonParseException("Invalid or malformed GameObject: not an object");
            JsonObject obj = json.getAsJsonObject();

            if (!obj.has("name") || !obj.has("transform") || !obj.has("z_index") || !obj.has("components"))
                throw new JsonParseException("Invalid or malformed GameObject: missing name or transform, z_index or components");

            String name = obj.get("name").getAsString();
            Transform transform = ctx.deserialize(obj.get("transform"), Transform.class);
            int zIndex = obj.get("z_index").getAsInt();
            List<Component> components = ctx.deserialize(obj.get("components"), new TypeToken<List<Component>>() {}.getType());

            GameObject go = new GameObject(name, transform, zIndex);
            components.forEach(go::addComponent);

            return go;
        }
    }
}
