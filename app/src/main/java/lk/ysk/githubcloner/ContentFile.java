package lk.ysk.githubcloner;

public class ContentFile {
    private final String localFile, fileName;
    private final String url;
    private final long size;

    public ContentFile(String fileName, String localFile, String url, long size) {
        this.localFile = localFile;
        this.url = url;
        this.size = size;
        this.fileName = fileName;
    }

    public String getLocalFile() {
        return localFile;
    }

    public String getUrl() {
        return url;
    }

    public long getSize() {
        return size;
    }

    public String getFileName() {
        return fileName;
    }
}
