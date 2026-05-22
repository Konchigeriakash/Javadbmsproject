package model;

public class OrderItem {
    private int    itemId;
    private int    orderId;
    private int    productId;
    private String productName;
    private int    quantity;
    private double unitPrice;

    public OrderItem(int itemId, int orderId, int productId,
                     String productName, int quantity, double unitPrice) {
        this.itemId      = itemId;
        this.orderId     = orderId;
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
    }

    public int    getItemId()      { return itemId; }
    public int    getOrderId()     { return orderId; }
    public int    getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public int    getQuantity()    { return quantity; }
    public double getUnitPrice()   { return unitPrice; }
    public double getLineTotal()   { return quantity * unitPrice; }
}