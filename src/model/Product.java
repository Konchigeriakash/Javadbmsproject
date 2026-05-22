package model;

public class Product {
    private int    productId;
    private String name;
    private double unitPrice;
    private int    quantity;       // joined from Inventory
    private int    reorderLevel;

    public Product(int productId, String name,
                   double unitPrice, int quantity, int reorderLevel) {
        this.productId    = productId;
        this.name         = name;
        this.unitPrice    = unitPrice;
        this.quantity     = quantity;
        this.reorderLevel = reorderLevel;
    }
    // getters + setters for each field
    public int    getProductId()    { return productId; }
    public String getName()         { return name; }
    public double getUnitPrice()    { return unitPrice; }
    public int    getQuantity()     { return quantity; }
    public int    getReorderLevel() { return reorderLevel; }
}