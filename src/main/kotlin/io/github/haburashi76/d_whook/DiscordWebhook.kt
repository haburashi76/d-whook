package io.github.haburashi76.d_whook

import java.awt.Color
import java.io.IOException
import java.lang.reflect.Array
import java.net.URL
import javax.net.ssl.HttpsURLConnection


@Suppress("Deprecation")
class DiscordWebhook
/**
 * Constructs a new DiscordWebhook instance
 *
 * @param url The webhook URL obtained in Discord
 */(private val url: String) {
    private var content: String? = null
    private var username: String? = null
    private var avatarUrl: String? = null
    private var tts = false
    private val embeds: MutableList<EmbedObject> = ArrayList()

    fun setContent(content: String?) {
        this.content = content
    }

    fun setUsername(username: String?) {
        this.username = username
    }

    @Throws(IOException::class)
    fun execute() {
        if (this.content == null && embeds.isEmpty()) {
            throw IllegalArgumentException("Set content or add at least one EmbedObject")
        }

        val json = JSONObject()

        json.put("content", this.content)
        json.put("username", this.username)
        json.put("avatar_url", this.avatarUrl)
        json.put("tts", this.tts)

        if (embeds.isNotEmpty()) {
            val embedObjects: MutableList<JSONObject> = ArrayList()

            for (embed: EmbedObject in this.embeds) {
                val jsonEmbed = JSONObject()

                jsonEmbed.put("title", embed.title)
                jsonEmbed.put("description", embed.description)
                jsonEmbed.put("url", embed.url)

                if (embed.color != null) {
                    val color = embed.color
                    var rgb = color!!.red
                    rgb = (rgb shl 8) + color.green
                    rgb = (rgb shl 8) + color.blue

                    jsonEmbed.put("color", rgb)
                }

                val footer: EmbedObject.Footer? = embed.footer
                val image = embed.image
                val thumbnail: EmbedObject.Thumbnail? = embed.thumbnail
                val author: EmbedObject.Author? = embed.author
                val fields = embed.getFields()

                if (footer != null) {
                    val jsonFooter = JSONObject()

                    jsonFooter.put("text", footer.text)
                    jsonFooter.put("icon_url", footer.iconUrl)
                    jsonEmbed.put("footer", jsonFooter)
                }

                if (image != null) {
                    val jsonImage = JSONObject()

                    jsonImage.put("url", image.url)
                    jsonEmbed.put("image", jsonImage)
                }

                if (thumbnail != null) {
                    val jsonThumbnail = JSONObject()

                    jsonThumbnail.put("url", thumbnail.url)
                    jsonEmbed.put("thumbnail", jsonThumbnail)
                }

                if (author != null) {
                    val jsonAuthor = JSONObject()

                    jsonAuthor.put("name", author.name)
                    jsonAuthor.put("url", author.url)
                    jsonAuthor.put("icon_url", author.iconUrl)
                    jsonEmbed.put("author", jsonAuthor)
                }

                val jsonFields: MutableList<JSONObject> = ArrayList()
                for (field: EmbedObject.Field in fields) {
                    val jsonField = JSONObject()

                    jsonField.put("name", field.name)
                    jsonField.put("value", field.value)
                    jsonField.put("inline", field.isInline)

                    jsonFields.add(jsonField)
                }

                jsonEmbed.put("fields", jsonFields.toTypedArray())
                embedObjects.add(jsonEmbed)
            }

            json.put("embeds", embedObjects.toTypedArray())
        }

        val url = URL(this.url)
        val connection = url.openConnection() as HttpsURLConnection
        connection.addRequestProperty("Content-Type", "application/json")
        connection.addRequestProperty("User-Agent", "Java-DiscordWebhook-BY-Gelox_")
        connection.doOutput = true
        connection.requestMethod = "POST"

        val stream = connection.outputStream
        stream.write(json.toString().toByteArray())
        stream.flush()
        stream.close()

        connection.inputStream.close()
        connection.disconnect()
    }

    class EmbedObject() {
        var title: String? = null
            private set
        var description: String? = null
            private set
        var url: String? = null
            private set
        var color: Color? = null
            private set

        var footer: Footer? = null
            private set
        var thumbnail: Thumbnail? = null
            private set
        var image: Image? = null
            private set
        var author: Author? = null
            private set
        private val fields: MutableList<Field> = ArrayList()

        fun getFields(): List<Field> = fields

        inner class Footer(val text: String, val iconUrl: String)

        inner class Thumbnail(val url: String)

        inner class Image(val url: String)

        inner class Author(val name: String, val url: String, val iconUrl: String)

        inner class Field(
            val name: String,
            val value: String,
            val isInline: Boolean
        )
    }

    private inner class JSONObject {
        private val map = HashMap<String, Any>()

        fun put(key: String, value: Any?) {
            if (value != null) {
                map[key] = value
            }
        }

        override fun toString(): String {
            val builder = StringBuilder()
            val entrySet: Set<Map.Entry<String, Any>> = map.entries
            builder.append("{")

            for ((i, entry: Map.Entry<String, Any>) in entrySet.withIndex()) {
                builder.append(quote(entry.key)).append(":")
                val v = entry.value
                if (v is String) {
                    builder.append(quote(v.toString()))
                } else if (v is Int) {
                    builder.append(v.toString().toInt())
                } else if (v is Boolean) {
                    builder.append(v)
                } else if (v is JSONObject) {
                    builder.append(v.toString())
                } else if (v.javaClass.isArray) {
                    builder.append("[")
                    val len = Array.getLength(v)
                    for (j in 0 until len) {
                        builder.append(Array.get(v, j).toString()).append(if (j != len - 1) "," else "")
                    }
                    builder.append("]")
                }

                builder.append(if (i + 1 == entrySet.size) "}" else ",")
            }

            return builder.toString()
        }

        private fun quote(string: String): String {
            return "\"" + string + "\""
        }
    }
}