package dev.skyos.features.general

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.skyos.SkyOsMod
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.jar.JarFile
import java.util.regex.Pattern

object AutoModUpdater {

    private val LOGGER = LoggerFactory.getLogger("skyos-updater")
    private const val GITHUB_RELEASES_API = "https://api.github.com/repos/freelocs-dev/SkyOS/releases"
    private const val GITHUB_RELEASES_PAGE = "https://github.com/freelocs-dev/SkyOS/releases"
    private val RELEASE_TAG_PATTERN: Pattern = Pattern.compile("(?i)^release[-_\\s]*(\\d+(?:\\.\\d+)*)\$")
    private val LOOSE_VERSION_PATTERN: Pattern = Pattern.compile("(\\d+(?:\\.\\d+)*)")
    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)
    private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(20)

    private val EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { r ->
        Thread(r, "SkyOS-Updater").also { it.isDaemon = true }
    }
    private val HTTP: HttpClient = HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val INITIALIZED = AtomicBoolean(false)
    private val AUTO_JOIN_CHECK_DONE = AtomicBoolean(false)
    private val CHECK_RUNNING = AtomicBoolean(false)
    private val DOWNLOAD_RUNNING = AtomicBoolean(false)
    private val INSTALL_HOOK_REGISTERED = AtomicBoolean(false)

    @Volatile private var latestRelease: ReleaseInfo? = null
    @Volatile private var latestUpdate: ReleaseInfo? = null
    @Volatile private var pendingInstall: PendingInstall? = null

    private val CURRENT_VERSION_TEXT: String by lazy { resolveCurrentVersionText() }
    private val CURRENT_VERSION: Version? by lazy { Version.parseLoose(CURRENT_VERSION_TEXT) }

    fun init() {
        if (!INITIALIZED.compareAndSet(false, true)) return

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            if (client.currentServer == null) return@register
            if (AUTO_JOIN_CHECK_DONE.compareAndSet(false, true)) {
                checkForUpdatesAsync(autoJoin = true)
            }
        }
    }

    fun isUpdateAvailable(): Boolean = latestUpdate != null

    fun checkForUpdatesAsync(autoJoin: Boolean = false) {
        if (!CHECK_RUNNING.compareAndSet(false, true)) {
            if (!autoJoin) sendInfo("Update check already running.")
            return
        }
        CompletableFuture.runAsync({
            try {
                val result = fetchLatestRelease()
                latestRelease = result.latestRelease
                latestUpdate = if (result.updateAvailable) result.latestRelease else null

                val release = result.latestRelease
                if (release == null) {
                    if (!autoJoin) sendInfo("No valid GitHub release tag found.")
                    return@runAsync
                }
                if (result.updateAvailable) {
                    sendAutoDownloadStartingMessage(release)
                    downloadLatestAsync(autoJoin = true)
                } else if (!autoJoin) {
                    sendInfo("You are up to date ($CURRENT_VERSION_TEXT).")
                }
            } catch (e: Exception) {
                LOGGER.warn("Update check failed", e)
                if (!autoJoin) sendInfo("Update check failed. Try again later.")
            } finally {
                CHECK_RUNNING.set(false)
            }
        }, EXECUTOR)
    }

    fun downloadLatestAsync(autoJoin: Boolean = false) {
        if (!DOWNLOAD_RUNNING.compareAndSet(false, true)) {
            if (!autoJoin) sendInfo("A download is already in progress.")
            return
        }
        CompletableFuture.runAsync({
            try {
                val result = fetchLatestRelease()
                latestRelease = result.latestRelease
                latestUpdate = if (result.updateAvailable) result.latestRelease else null

                val release = result.latestRelease
                if (release == null) {
                    sendInfo("No valid release found to download.")
                    return@runAsync
                }
                if (!result.updateAvailable) {
                    if (!autoJoin) sendInfo("Already on latest version ($CURRENT_VERSION_TEXT).")
                    return@runAsync
                }
                if (release.asset == null || release.asset.downloadUrl.isBlank()) {
                    sendInfo("No downloadable .jar found in the latest release.")
                    sendOpenReleaseHint(release)
                    return@runAsync
                }

                val downloaded = downloadReleaseAsset(release)
                val install = prepareInstall(release, downloaded)
                pendingInstall = install
                registerInstallHook()
                sendDownloadCompleteMessage(release)
            } catch (e: Exception) {
                LOGGER.warn("Update download failed", e)
                sendInfo("Download failed. Check logs for details.")
            } finally {
                DOWNLOAD_RUNNING.set(false)
            }
        }, EXECUTOR)
    }

    private fun registerInstallHook() {
        if (!INSTALL_HOOK_REGISTERED.compareAndSet(false, true)) return
        Runtime.getRuntime().addShutdownHook(Thread(::launchPendingInstaller, "SkyOS-Update-Installer"))
    }

    private fun launchPendingInstaller() {
        val install = pendingInstall ?: return
        try {
            Files.createDirectories(install.modsDir)
            if (isWindows()) launchWindowsInstaller(install) else launchPosixInstaller(install)
        } catch (e: Exception) {
            LOGGER.error("Failed to launch update installer", e)
        }
    }

    private fun launchWindowsInstaller(install: PendingInstall) {
        val script = install.downloadedFile.parent.resolve("skyos-install-${System.currentTimeMillis()}.cmd")
        val log = install.downloadedFile.parent.resolve("skyos-install.log")
        val current = install.currentJar?.toString() ?: install.targetJar.toString()
        val content = buildString {
            appendLine("@echo off")
            appendLine("setlocal")
            appendLine("set \"DOWNLOAD=${escapeWin(install.downloadedFile.toString())}\"")
            appendLine("set \"TARGET=${escapeWin(install.targetJar.toString())}\"")
            appendLine("set \"CURRENT=${escapeWin(current)}\"")
            appendLine("set \"MODSDIR=${escapeWin(install.modsDir.toString())}\"")
            appendLine("set \"LOG=${escapeWin(log.toString())}\"")
            append("""
echo Installing SkyOS update at %DATE% %TIME% > "%LOG%"
if not exist "%MODSDIR%" mkdir "%MODSDIR%" >> "%LOG%" 2>&1
set /a A=0
:cr
set /a A+=1
copy /Y "%DOWNLOAD%" "%TARGET%" >> "%LOG%" 2>&1
if errorlevel 1 goto cw
goto cd
:cw
if %A% GEQ 60 goto cf
timeout /t 1 /nobreak >nul
goto cr
:cd
if /I "%CURRENT%"=="%TARGET%" goto cl
if not exist "%CURRENT%" goto cl
set /a B=0
:dr
set /a B+=1
del /F /Q "%CURRENT%" >> "%LOG%" 2>&1
if not exist "%CURRENT%" goto cl
if %B% GEQ 60 goto df
timeout /t 1 /nobreak >nul
goto dr
:df
echo Old jar not removed: "%CURRENT%" >> "%LOG%"
:cl
if exist "%DOWNLOAD%" del /F /Q "%DOWNLOAD%" >> "%LOG%" 2>&1
echo SkyOS update installer finished. >> "%LOG%"
goto done
:cf
echo Copy failed after %A% attempts. >> "%LOG%"
:done
del /F /Q "%~f0" >nul 2>&1
""".trimIndent())
        }
        Files.writeString(script, content, StandardCharsets.UTF_8)
        ProcessBuilder("cmd.exe", "/c", script.toString()).start()
    }

    private fun launchPosixInstaller(install: PendingInstall) {
        val script = install.downloadedFile.parent.resolve("skyos-install-${System.currentTimeMillis()}.sh")
        val log = install.downloadedFile.parent.resolve("skyos-install.log")
        val current = install.currentJar?.toString() ?: install.targetJar.toString()
        val content = """
#!/bin/sh
DOWNLOAD=${posix(install.downloadedFile.toString())}
TARGET=${posix(install.targetJar.toString())}
CURRENT=${posix(current)}
MODSDIR=${posix(install.modsDir.toString())}
LOG=${posix(log.toString())}
echo "Installing SkyOS update at $(date)" > "${'$'}LOG"
mkdir -p "${'$'}MODSDIR" >> "${'$'}LOG" 2>&1
attempt=0; installed=0
while [ "${'$'}attempt" -lt 60 ]; do
  if cp -f "${'$'}DOWNLOAD" "${'$'}TARGET" >> "${'$'}LOG" 2>&1; then installed=1; break; fi
  attempt=${'$'}((attempt+1)); sleep 1
done
if [ "${'$'}installed" != "1" ]; then
  echo "Copy failed after ${'$'}attempt attempts." >> "${'$'}LOG"
  rm -f "${'$'}0"; exit 1
fi
if [ "${'$'}CURRENT" != "${'$'}TARGET" ] && [ -f "${'$'}CURRENT" ]; then
  attempt=0
  while [ "${'$'}attempt" -lt 60 ] && [ -f "${'$'}CURRENT" ]; do
    rm -f "${'$'}CURRENT" >> "${'$'}LOG" 2>&1 || true
    [ ! -f "${'$'}CURRENT" ] && break
    attempt=${'$'}((attempt+1)); sleep 1
  done
fi
rm -f "${'$'}DOWNLOAD" >> "${'$'}LOG" 2>&1
echo "SkyOS update installer finished." >> "${'$'}LOG"
rm -f "${'$'}0"
""".trimIndent()
        Files.writeString(script, content, StandardCharsets.UTF_8)
        script.toFile().setExecutable(true, true)
        ProcessBuilder("sh", script.toString()).start()
    }

    private fun fetchLatestRelease(): CheckResult {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(GITHUB_RELEASES_API))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "SkyOS-Updater/$CURRENT_VERSION_TEXT")
            .timeout(REQUEST_TIMEOUT)
            .GET().build()
        val response = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        if (response.statusCode() !in 200..299)
            throw IOException("GitHub API returned ${response.statusCode()}")
        val root: JsonElement = JsonParser.parseString(response.body())
        if (!root.isJsonArray) return CheckResult(null, false)
        val latest = selectLatestRelease(root.asJsonArray)
        val cv = CURRENT_VERSION
        val updateAvailable = latest != null && cv != null && latest.version > cv
        return CheckResult(latest, updateAvailable)
    }

    private fun selectLatestRelease(releases: JsonArray): ReleaseInfo? {
        val candidates = mutableListOf<ReleaseInfo>()
        for (el in releases) {
            if (el == null || !el.isJsonObject) continue
            val obj = el.asJsonObject
            if (readBool(obj, "draft") || readBool(obj, "prerelease")) continue
            val tag = readStr(obj, "tag_name") ?: continue
            val name = readStr(obj, "name")
            val url = readStr(obj, "html_url")
            val version = parseVersion(tag, name) ?: continue
            val asset = pickAsset(obj.getAsJsonArray("assets"))
            candidates.add(ReleaseInfo(tag, name, url, version, version.asText(), asset))
        }
        return candidates.maxByOrNull { it.version }
    }

    private fun pickAsset(assets: JsonArray?): ReleaseAsset? {
        if (assets == null) return null
        var best: ReleaseAsset? = null
        var bestScore = Int.MIN_VALUE
        for (el in assets) {
            if (el == null || !el.isJsonObject) continue
            val obj = el.asJsonObject
            val name = readStr(obj, "name") ?: continue
            val url = readStr(obj, "browser_download_url") ?: continue
            if (!name.lowercase(Locale.ROOT).endsWith(".jar")) continue
            val lo = name.lowercase(Locale.ROOT)
            var score = 0
            if (lo.contains("skyos")) score += 4
            if (!lo.contains("sources")) score += 2
            if (!lo.contains("dev")) score += 1
            if (score > bestScore) { bestScore = score; best = ReleaseAsset(name, url) }
        }
        return best
    }

    private fun downloadReleaseAsset(release: ReleaseInfo): Path {
        val asset = requireNotNull(release.asset)
        val dir = FabricLoader.getInstance().configDir.resolve("skyos").resolve("updates")
        Files.createDirectories(dir)
        val safeName = sanitize(asset.name, "skyos-${release.versionText}.jar")
        val target = dir.resolve(safeName)
        val temp = dir.resolve("$safeName.part")
        val request = HttpRequest.newBuilder()
            .uri(URI.create(asset.downloadUrl))
            .header("Accept", "application/octet-stream")
            .header("User-Agent", "SkyOS-Updater/$CURRENT_VERSION_TEXT")
            .timeout(REQUEST_TIMEOUT)
            .GET().build()
        val response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (response.statusCode() !in 200..299)
            throw IOException("Download failed with status ${response.statusCode()}")
        response.body().use { stream: InputStream ->
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING)
        }
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        return target.toAbsolutePath().normalize()
    }

    private fun prepareInstall(release: ReleaseInfo, downloaded: Path): PendingInstall {
        val modsDir = FabricLoader.getInstance().gameDir.resolve("mods").toAbsolutePath().normalize()
        val currentJar = resolveCurrentJar(modsDir, release.versionText)
        val targetJar = selectTargetJar(modsDir, release, downloaded)
        return PendingInstall(downloaded, targetJar, currentJar, modsDir)
    }

    private fun selectTargetJar(modsDir: Path, release: ReleaseInfo, downloaded: Path): Path {
        val fallback = release.asset?.name ?: downloaded.fileName.toString()
        return modsDir.resolve(sanitize(fallback, "skyos-${release.versionText}.jar")).toAbsolutePath().normalize()
    }

    private fun resolveCurrentJar(modsDir: Path, targetVersion: String): Path? {
        val code = resolveCodeSourceJar()
        if (isDirectChild(code, modsDir)) return code
        return findInstalledJar(modsDir, targetVersion)
    }

    private fun findInstalledJar(modsDir: Path, targetVersion: String): Path? {
        if (!Files.isDirectory(modsDir)) return null
        return try {
            Files.list(modsDir).use { stream ->
                val jars = stream
                    .filter { Files.isRegularFile(it) }
                    .filter { it.fileName?.toString()?.lowercase(Locale.ROOT)?.endsWith(".jar") == true }
                    .sorted(Comparator.comparing { p: Path -> p.fileName?.toString()?.lowercase(Locale.ROOT) ?: "" })
                    .toList()
                var fallback: Path? = null
                for (jar in jars) {
                    val v = readJarVersion(jar) ?: continue
                    val norm = jar.toAbsolutePath().normalize()
                    if (v == CURRENT_VERSION_TEXT) return norm
                    if (v != targetVersion && fallback == null) fallback = norm
                }
                fallback
            }
        } catch (e: IOException) {
            LOGGER.debug("Failed to scan mods dir", e); null
        }
    }

    private fun readJarVersion(jar: Path): String? {
        return try {
            JarFile(jar.toFile()).use { jf ->
                val entry = jf.getJarEntry("fabric.mod.json") ?: return null
                jf.getInputStream(entry).use { stream ->
                    val root = JsonParser.parseString(String(stream.readAllBytes(), StandardCharsets.UTF_8))
                    if (!root.isJsonObject) return null
                    val meta = root.asJsonObject
                    if (SkyOsMod.MOD_ID != readStr(meta, "id")) return null
                    readStr(meta, "version")
                }
            }
        } catch (_: Exception) { null }
    }

    private fun isDirectChild(path: Path?, dir: Path): Boolean =
        path != null && path.parent?.toAbsolutePath()?.normalize() == dir.toAbsolutePath().normalize()

    private fun resolveCodeSourceJar(): Path? = try {
        val loc = AutoModUpdater::class.java.protectionDomain?.codeSource?.location?.toURI() ?: return null
        val p = Path.of(loc).toAbsolutePath().normalize()
        if (Files.isRegularFile(p) && p.fileName.toString().lowercase(Locale.ROOT).endsWith(".jar")) p else null
    } catch (_: Exception) { null }

    private fun parseVersion(tag: String?, name: String?): Version? =
        Version.parseReleaseTag(tag) ?: Version.parseReleaseTag(name) ?: Version.parseLoose(tag)

    private fun resolveCurrentVersionText(): String =
        FabricLoader.getInstance()
            .getModContainer(SkyOsMod.MOD_ID)
            .map { it.metadata.version.friendlyString }
            .filter { it.isNotBlank() }
            .orElse("0.0.0")

    // ── Chat messaging ────────────────────────────────────────────────────────

    private fun sendAutoDownloadStartingMessage(release: ReleaseInfo) {
        sendComponent(
            prefix()
                .append(lit("Update available "))
                .append(lit("$CURRENT_VERSION_TEXT → ${release.versionText}").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(lit(". Downloading automatically..."))
        )
    }

    private fun sendDownloadCompleteMessage(release: ReleaseInfo) {
        sendComponent(
            prefix()
                .append(lit("Downloaded "))
                .append(lit(release.versionText).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(lit(". Restart Minecraft to install."))
        )
    }

    private fun sendUpdateAvailableMessage(release: ReleaseInfo) {
        sendComponent(
            prefix()
                .append(lit("Update available "))
                .append(lit("$CURRENT_VERSION_TEXT → ${release.versionText}").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
        )
        val url = release.htmlUrl ?: GITHUB_RELEASES_PAGE
        sendComponent(
            prefix().append(
                button("[Open Release]", ChatFormatting.GOLD,
                    ClickEvent(ClickEvent.Action.OPEN_URL, url),
                    "Open GitHub releases page")
            )
        )
    }

    private fun sendOpenReleaseHint(release: ReleaseInfo) {
        val url = release.htmlUrl ?: GITHUB_RELEASES_PAGE
        sendComponent(
            prefix().append(
                button("[Open Release]", ChatFormatting.GOLD,
                    ClickEvent(ClickEvent.Action.OPEN_URL, url),
                    "Open GitHub releases page")
            )
        )
    }

    private fun sendInfo(text: String) =
        sendComponent(prefix().append(lit(text).withStyle(ChatFormatting.GRAY)))

    private fun prefix(): MutableComponent =
        lit("[SkyOS] ").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)

    private fun lit(text: String): MutableComponent = Component.literal(text).withStyle(ChatFormatting.WHITE)

    private fun button(text: String, color: ChatFormatting, click: ClickEvent, hover: String): MutableComponent =
        Component.literal(text).withStyle { s ->
            s.withColor(color).withBold(true).withUnderlined(true).withClickEvent(click)
             .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hover)))
        }

    private fun sendComponent(comp: Component?) {
        comp ?: return
        val mc = Minecraft.getInstance()
        if (!mc.isSameThread()) {
            mc.execute { sendComponent(comp) }
            return
        }
        mc.player?.displayClientMessage(comp, false)
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun sanitize(value: String?, fallback: String): String {
        if (value.isNullOrBlank()) return fallback
        val s = value.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()
        return s.ifBlank { fallback }
    }

    private fun escapeWin(v: String?): String = (v ?: "").replace("\"", "\"\"")

    private fun posix(v: String?): String {
        val s = v ?: ""
        return "'" + s.replace("'", "'\"'\"'") + "'"
    }

    private fun readBool(obj: JsonObject?, key: String): Boolean =
        try { obj?.get(key)?.asBoolean ?: false } catch (_: Exception) { false }

    private fun readStr(obj: JsonObject?, key: String): String? =
        try { obj?.get(key)?.asString?.takeIf { it.isNotBlank() } } catch (_: Exception) { null }

    private fun isWindows(): Boolean =
        System.getProperty("os.name", "").lowercase(Locale.ROOT).contains("win")

    // ── Data types ────────────────────────────────────────────────────────────

    private data class CheckResult(val latestRelease: ReleaseInfo?, val updateAvailable: Boolean)

    private data class ReleaseInfo(
        val tagName: String,
        val releaseName: String?,
        val htmlUrl: String?,
        val version: Version,
        val versionText: String,
        val asset: ReleaseAsset?
    )

    private data class ReleaseAsset(val name: String, val downloadUrl: String)

    private data class PendingInstall(
        val downloadedFile: Path,
        val targetJar: Path,
        val currentJar: Path?,
        val modsDir: Path
    )

    private class Version(private val parts: List<Int>) : Comparable<Version> {

        companion object {
            fun parseReleaseTag(raw: String?): Version? {
                if (raw.isNullOrBlank()) return null
                val m = RELEASE_TAG_PATTERN.matcher(raw.trim())
                if (!m.matches()) return null
                return fromString(m.group(1))
            }

            fun parseLoose(raw: String?): Version? {
                if (raw.isNullOrBlank()) return null
                val m = LOOSE_VERSION_PATTERN.matcher(raw.trim())
                if (!m.find()) return null
                return fromString(m.group(1))
            }

            private fun fromString(text: String?): Version? {
                if (text.isNullOrBlank()) return null
                val nums = mutableListOf<Int>()
                for (p in text.split(".")) nums.add(p.toIntOrNull() ?: return null)
                while (nums.size > 1 && nums.last() == 0) nums.removeAt(nums.size - 1)
                return Version(nums)
            }
        }

        fun asText(): String = parts.joinToString(".")

        override fun compareTo(other: Version): Int {
            val max = maxOf(parts.size, other.parts.size)
            for (i in 0 until max) {
                val l = parts.getOrElse(i) { 0 }
                val r = other.parts.getOrElse(i) { 0 }
                if (l != r) return l.compareTo(r)
            }
            return 0
        }
    }
}
