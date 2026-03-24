package carrent;

public class Motorcycle extends Vehicle {
    private String category;

    public Motorcycle(String id, String brand, String model, int year, double price, boolean rented, String category) {
        super(id, brand, model, year, price, rented);
        this.category = category;
    }

    public Motorcycle(Motorcycle other) {
        super(other);
        this.category = other.category;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toCSV() {
        return "MOTORCYCLE;"+getId()+";"+getBrand()+";"+getModel()+";"+getYear()+";"+getPrice()+";"+isRented()+";"+category;
    }

    @Override
    public Vehicle copy() {
        return new Motorcycle(this);
    }

    @Override
    public String toString() {
        return "[MOTOCYKL] "+super.toString()+" | Kat: "+category;
    }
}