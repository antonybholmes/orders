package edu.columbia.rdf.orders.app;

import org.jebtk.core.AppVersion;
import org.jebtk.modern.AssetService;
import org.jebtk.modern.help.GuiAppInfo;

public class OrdersInfo extends GuiAppInfo {

  public OrdersInfo() {
    super("Orders", new AppVersion(7),
        "Copyright (C) 2014-${year} Antony Holmes",
        AssetService.getInstance().loadIcon(OrdersIcon.class, 32),
        AssetService.getInstance().loadIcon(OrdersIcon.class, 128));
  }

}
