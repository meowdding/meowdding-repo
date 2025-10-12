import me.owdding.repo.RemoteRepo
import org.junit.jupiter.api.Test
import java.nio.file.Path

object DownloadTest {

    @Test
    fun test() {
        var hasDownloaded = false
        RemoteRepo.initialize(Path.of("repo-data")) {
            hasDownloaded = true
        }
        assert(hasDownloaded)
    }

}
