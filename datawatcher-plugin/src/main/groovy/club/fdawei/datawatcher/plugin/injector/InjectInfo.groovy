package club.fdawei.datawatcher.plugin.injector

import com.android.build.api.transform.Format

class InjectInfo {

    private File sourceDir
    private File destFile
    private Format format

    InjectInfo(File sourceDir, File destFile, Format format) {
        this.sourceDir = sourceDir
        this.destFile = destFile
        this.format = format
    }

    File getSourceDir() {
        return sourceDir
    }

    File getDestFile() {
        return destFile
    }

    Format getFormat() {
        return format
    }
}