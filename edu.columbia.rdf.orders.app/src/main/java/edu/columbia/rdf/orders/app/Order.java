package edu.columbia.rdf.orders.app;

import java.util.Date;

public class Order extends Item {
  private String mType;
  private double mUnitPrice;
  private double mQuantity;
  private double mTotal;
  private String mFrom;
  private double mShipping;
  private Date mDate;
  private String mVerifiedType;
  private String mUnitSize;

  public Order(String name, String vendor, String catalog, String type,
      String verifiedType, String unitSize, double unitPrice, double quantity,
      double total, double shipping, String from, Date date) {
    super(catalog, name, vendor);

    mType = type;
    mVerifiedType = verifiedType;
    mUnitSize = unitSize;
    mUnitPrice = unitPrice;
    mQuantity = quantity;
    mTotal = total;
    mShipping = shipping;
    mFrom = from;
    mDate = date;
  }

  public double getTotal() {
    return mTotal;
  }

  public double getShipping() {
    return mShipping;
  }

  public double getUnitPrice() {
    return mUnitPrice;
  }

  public double getQuantity() {
    return mQuantity;
  }

  public Date getDate() {
    return mDate;
  }

  public String getType() {
    return mType;
  }

  public String getFrom() {
    return mFrom;
  }

  public String getVerifiedType() {
    return mVerifiedType;
  }

  public String getUnitSize() {
    return mUnitSize;
  }
}
