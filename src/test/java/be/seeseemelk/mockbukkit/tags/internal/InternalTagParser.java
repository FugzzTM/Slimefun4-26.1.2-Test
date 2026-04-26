package be.seeseemelk.mockbukkit.tags.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.tags.TagRegistry;

public class InternalTagParser {

	private static final Pattern MINECRAFT_MATERIAL = Pattern.compile("minecraft:[a-z0-9_]+");
	private static final Pattern MINECRAFT_TAG = Pattern.compile("#minecraft:[a-z_]+");

	public InternalTagParser() {
	}

	public void insertInternalTagValues(InternalTagRegistry internalTagRegistry) throws IOException, InternalTagMisconfigurationException {
		String registryPath = "/internal_tags/" + internalTagRegistry.name().toLowerCase(Locale.ROOT);

		for (InternalTag<?> tag : internalTagRegistry.getRelatedTags()) {
			String resourcePath = registryPath + "/" + tag.getName().toLowerCase(Locale.ROOT) + ".json";

			try (InputStream inputStream = MockBukkit.class.getResourceAsStream(resourcePath)) {
				if (inputStream == null) {
					throw new IOException("Could not find internal tag resource: " + resourcePath);
				}

				JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
				parse(jsonObject, internalTagRegistry.getTagRegistryEquivalent(), tag);
			}
		}
	}

	private <T> void parse(JsonObject jsonObject, TagRegistry tagRegistry, InternalTag<T> tag) throws InternalTagMisconfigurationException {
		JsonArray values = jsonObject.get("values").getAsJsonArray();
		Set<T> parsedValues = parseJsonArray(values, tagRegistry, tag.getRelatedClass());
		tag.addValues(parsedValues);
	}

	@SuppressWarnings("unchecked")
	private <T> Set<T> parseJsonArray(JsonArray jsonArray, TagRegistry tagRegistry, Class<T> relatedClass) throws InternalTagMisconfigurationException {
		if (relatedClass != Material.class) {
			throw new InternalTagMisconfigurationException("Unable to parse tags of type " + relatedClass.getName());
		}

		Set<Material> materials = EnumSet.noneOf(Material.class);

		for (JsonElement element : jsonArray) {
			String value = element.getAsString();

			if (MINECRAFT_TAG.matcher(value).matches()) {
				materials.addAll(parseTag(value, tagRegistry));
				continue;
			}

			if (MINECRAFT_MATERIAL.matcher(value).matches()) {
				Material material = parseMaterial(value);
				if (material != null) {
					materials.add(material);
				}
				continue;
			}

			// Unknown entries are ignored so older internal tag data can still load on newer Paper versions.
		}

		return (Set<T>) materials;
	}

	private Material parseMaterial(String value) {
		String[] parts = value.split(":", 2);
		if (parts.length != 2) {
			return null;
		}

		try {
			return Material.valueOf(parts[1].toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private Set<Material> parseTag(String value, TagRegistry tagRegistry) throws InternalTagMisconfigurationException {
		String key = value.split(":", 2)[1];
		org.bukkit.Tag<Material> tag = tagRegistry.getTags().get(NamespacedKey.minecraft(key));

		if (tag == null) {
			return Set.of();
		}

		return tag.getValues();
	}
}

