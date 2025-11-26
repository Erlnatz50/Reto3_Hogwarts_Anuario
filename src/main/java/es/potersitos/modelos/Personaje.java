package es.potersitos.modelos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

public class Personaje implements Serializable {

    /** Logger para esta clase */
    private static final Logger logger = LoggerFactory.getLogger(Personaje.class);

    /** Bundle del sistema de internacionalización */
    private ResourceBundle bundle;

    private String id;
    private String type;

    // Atributos
    private String slug;
    private List<String> aliasNames;
    private String animagus;
    private String bloodStatus;
    private String boggart;
    private String born;
    private String died;
    private String eyeColor;
    private List<String> familyMembers;
    private String gender;
    private String hairColor;
    private String height;
    private String house;
    private String image;
    private List<String> jobs;
    private String maritalStatus;
    private String name;
    private String nationality;
    private String patronus;
    private List<String> romances;
    private String skinColor;
    private String species;
    private List<String> titles;
    private List<String> wands;
    private String weight;
    private String wiki;

    // Para almacenar la imagen como blob, si lo deseas
    private byte[] imageBlob;

    // -------------------------
    // GETTERS Y SETTERS
    // -------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<String> getAliasNames() {
        return aliasNames;
    }

    public void setAliasNames(List<String> aliasNames) {
        this.aliasNames = aliasNames;
    }

    public String getAnimagus() {
        return animagus;
    }

    public void setAnimagus(String animagus) {
        this.animagus = animagus;
    }

    public String getBloodStatus() {
        return bloodStatus;
    }

    public void setBloodStatus(String bloodStatus) {
        this.bloodStatus = bloodStatus;
    }

    public String getBoggart() {
        return boggart;
    }

    public void setBoggart(String boggart) {
        this.boggart = boggart;
    }

    public String getBorn() {
        return born;
    }

    public void setBorn(String born) {
        this.born = born;
    }

    public String getDied() {
        return died;
    }

    public void setDied(String died) {
        this.died = died;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public List<String> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<String> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPatronus() {
        return patronus;
    }

    public void setPatronus(String patronus) {
        this.patronus = patronus;
    }

    public List<String> getRomances() {
        return romances;
    }

    public void setRomances(List<String> romances) {
        this.romances = romances;
    }

    public String getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(String skinColor) {
        this.skinColor = skinColor;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public List<String> getWands() {
        return wands;
    }

    public void setWands(List<String> wands) {
        this.wands = wands;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public byte[] getImageBlob() {
        return imageBlob;
    }

    public void setImageBlob(byte[] imageBlob) {
        this.imageBlob = imageBlob;
    }
}
