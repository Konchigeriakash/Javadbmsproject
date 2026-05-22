package model;

public class Order {
    private int    orderId;
    private String customerName;
    private String orderDate;
    private String status;
    private double total;

    public Order(int orderId, String customerName,
                 String orderDate, String status, double total) {
        this.orderId      = orderId;
        this.customerName = customerName;
        this.orderDate    = orderDate;
        this.status       = status;
        this.total        = total;
    }

    public int    getOrderId()      { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getOrderDate()    { return orderDate; }
    public String getStatus()       { return status; }
    public double getTotal()        { return total; }

    public void setStatus(String status) { this.status = status; }
}