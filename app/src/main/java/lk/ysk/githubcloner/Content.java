package lk.ysk.githubcloner;

public class Content {

    public enum Type {
        FILE,
        DIR,
        GO_BACK
    }

    private final String name;
    private final Type type;
    private final String url;
    private final String downloadUrl;
    private final long size;
    private boolean selected;

    public Content(String name, Type type, String url, String downloadUrl, long size) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.downloadUrl = downloadUrl;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public long getSize() {
        return size;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getSizeString() {
        if (size > 1048576)
            return String.format("%.2fMB", size/1048576f);
        else if (size > 1024)
            return String.format("%.2fKB", size/1024f);
        return String.format("%d Bytes", size);
    }

}
