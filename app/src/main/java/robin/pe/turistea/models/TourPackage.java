package robin.pe.turistea.models;
public class TourPackage {
    private int id;
    private String name;
    private String description;
    private String image;
    private double price;
    private String location;
    private int duration; // días
    
    public TourPackage() {
    }
    
    public TourPackage(int id, String name, String description, String image, double price, String location, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.price = price;
        this.location = location;
        this.duration = duration;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImage() {
        return image;
    }
    
    public double getPrice() {
        return price;
    }
    
    public String getLocation() {
        return location;
    }
    
    public int getDuration() {
        return duration;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
 
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
}

