package robin.pe.turistea.models;

import java.io.Serializable;

public class RouteItemDetail implements Serializable {
    private int id;
    private int index;
    private String title;
    private String description;
    private String bgImage;
    private String bgImageKey;
    private String bgImageSize;

    public RouteItemDetail(int id, int index, String title, String description, String bgImage, String bgImageKey, String bgImageSize) {
        this.id = id;
        this.index = index;
        this.title = title;
        this.description = description;
        this.bgImage = bgImage;
        this.bgImageKey = bgImageKey;
        this.bgImageSize = bgImageSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
    }

    public String getBgImageKey() {
        return bgImageKey;
    }

    public void setBgImageKey(String bgImageKey) {
        this.bgImageKey = bgImageKey;
    }

    public String getBgImageSize() {
        return bgImageSize;
    }

    public void setBgImageSize(String bgImageSize) {
        this.bgImageSize = bgImageSize;
    }
}
