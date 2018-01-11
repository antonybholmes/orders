package edu.columbia.rdf.orders.app;

import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UnsupportedLookAndFeelException;

import org.jebtk.core.AppService;
import org.jebtk.core.io.Io;
import org.jebtk.core.io.PathUtils;
import org.jebtk.modern.UI;
import org.jebtk.modern.theme.ThemeService;

public class MainOrders {
  private static final Path EXCEL_PATHS_FILE = PathUtils
      .getPath("excel_paths.txt");

  public static final void main(String[] args) throws FontFormatException,
      IOException, ClassNotFoundException, InstantiationException,
      IllegalAccessException, UnsupportedLookAndFeelException {
    AppService.getInstance().setAppInfo("orders");

    ThemeService.getInstance().setTheme();

    // Read the excel paths

    List<String> excelPaths = Io.getLines(EXCEL_PATHS_FILE);

    OrdersInfo info = new OrdersInfo();

    JFrame window = new MainOrdersWindow(info, excelPaths);

    UI.centerWindowToScreen(window);

    window.setVisible(true);
  }
}
