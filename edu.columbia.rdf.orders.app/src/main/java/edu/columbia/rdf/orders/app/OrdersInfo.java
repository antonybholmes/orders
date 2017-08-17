package edu.columbia.rdf.orders.app;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.UIService;
import org.jebtk.modern.help.GuiAppInfo;


public class OrdersInfo extends GuiAppInfo {

	public OrdersInfo() {
		super("Orders",
				new AppVersion(6),
				"Copyright (C) 2014-${year} Antony Holmes",
				UIService.getInstance().loadIcon(OrdersIcon.class, 32),
				UIService.getInstance().loadIcon(OrdersIcon.class, 128));
	}

}
