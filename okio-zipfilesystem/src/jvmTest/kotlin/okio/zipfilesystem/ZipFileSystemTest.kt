package okio.zipfilesystem

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random
import okio.ByteString.Companion.toByteString
import okio.ExperimentalFileSystem
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@ExperimentalFileSystem
class ZipFileSystemTest {
  private val fileSystem = FileSystem.SYSTEM
  private var base = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / randomToken()

  @Before
  fun setUp() {
    fileSystem.createDirectory(base)
  }

  @Test
  fun name() {
    val zipPath = base / "file.zip"
    writeZipFile(
      zipPath,
      "hello.txt" to "Hello World",
    )

    val zipFileSystem = open(zipPath, fileSystem)
    val content = zipFileSystem.read("hello.txt".toPath()) {
      readUtf8()
    }
    assertThat(content).isEqualTo("Hello World")
  }

  private fun writeZipFile(zipPath: Path, vararg files: Pair<String, String>) {
    fileSystem.write(zipPath) {
      ZipOutputStream(this.outputStream()).use { zip ->
        for ((entryName, entryContent) in files) {
          zip.putNextEntry(ZipEntry(entryName))
          zip.sink().buffer().use { sink ->
            sink.writeUtf8(entryContent)
          }
        }
      }
    }
  }
}

fun randomToken() = Random.nextBytes(16).toByteString(0, 16).hex()
