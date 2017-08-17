package edu.columbia.rdf.apps.orders;

import org.abh.common.AppVersion;
import org.abh.common.ui.UIService;
import org.abh.common.ui.help.GuiAppInfo;


public class OrdersInfo extends GuiAppInfo {

	public OrdersInfo() {
		super("Orders",
				new AppVersion(6),
				"Copyright (C) 2014-${year} Antony Holmes",
				UIService.getInstance().loadIcon(OrdersIcon.class, 32),
				UIService.getInstance().loadIcon(OrdersIcon.class, 128));
	}

}
