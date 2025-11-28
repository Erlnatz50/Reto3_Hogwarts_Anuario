package es.potersitos.modelos;

/**
 * Clase que representa el modelo de datos de un Wizard (Mago/Usuario).
 */
public class Usuarios {

    private String id;
    private String name;
    private String house;
    private String wand;
    private String imageUrl;

    public Usuarios() {
    }

    public Usuarios(String id, String name, String house, String wand, String imageUrl) {
        this.id = id;
        this.name = name;
        this.house = house;
        this.wand = wand;
        this.imageUrl = imageUrl;
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getWand() {
        return wand;
    }

    public void setWand(String wand) {
        this.wand = wand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Wizard{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", house='" + house + '\'' +
                ", wand='" + wand + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}