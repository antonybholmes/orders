package edu.columbia.rdf.orders.app;


public class Item implements Comparable<Item> {
	private String mName;
	private String mCatalog;
	private String mVendor;

	public Item(String name,
			String vendor,
			String catalog) {
		mName = name;
		mVendor = vendor;
		mCatalog = catalog;
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
