package edu.columbia.rdf.orders.app;

public class Item implements Comparable<Item> {
  private String mName;
  private String mCatalog;
  private String mVendor;

  public Item(String catalog, String name, String vendor) {
    mCatalog = catalog;
    mName = name;
    mVendor = vendor;
  }

  public String getVendor() {
    return mVendor;
  }

  public String getName() {
    return mName;
  }

  public String getCatalog() {
    return mCatalog;
  }

  @Override
  public int compareTo(Item o) {
    return mName.compareTo(o.mName);
  }
}
